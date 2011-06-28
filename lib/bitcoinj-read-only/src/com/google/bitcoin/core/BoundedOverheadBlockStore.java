/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.bitcoin.core;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the block chain to disk.<p>
 *
 * This implementation is designed to have constant memory usage, regardless of the size of the block chain being
 * stored. It exploits operating system level buffering and the fact that get() requests are, in normal usage,
 * localized in chain space.<p>
 *
 * Blocks are stored sequentially. Most blocks are fetched out of a small in-memory cache. The slowest part is
 * traversing difficulty transition points, which requires seeking backwards over around 2000 blocks. On a Google
 * Nexus S phone this takes a couple of seconds. On a MacBook Pro it takes around 50msec.<p>
 *
 * The store has much room for optimization. Expanding the size of the cache will likely allow us to traverse
 * difficulty transitions without using too much memory and without hitting the disk at all, for the case of initial
 * block chain download. Storing the hashes on disk would allow us to avoid deserialization and hashing which is
 * expensive on Android.
 */
public class BoundedOverheadBlockStore implements BlockStore {
	private static final Logger log = LoggerFactory.getLogger(BoundedOverheadBlockStore.class);
    private static final byte FILE_FORMAT_VERSION = 1;

    private RandomAccessFile file;
    // We keep some recently found blocks in the blockCache. It can help to optimize some cases where we are
    // looking up blocks we recently stored or requested. When the cache gets too big older entries are deleted.
    private LinkedHashMap<Sha256Hash, StoredBlock> blockCache = new LinkedHashMap<Sha256Hash, StoredBlock>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredBlock> entry) {
            return size() > 100;  // This was chosen arbitrarily.
        }
    };
    // Use a separate cache to track get() misses. This is to efficiently handle the case of an unconnected block
    // during chain download. Each new block will do a get() on the unconnected block so if we haven't seen it yet we
    // must efficiently respond.
    //
    // We don't care about the value in this cache. It is always notFoundMarker. Unfortunately LinkedHashSet does not
    // provide the removeEldestEntry control.
    private StoredBlock notFoundMarker;
    private LinkedHashMap<Sha256Hash, StoredBlock> notFoundCache = new LinkedHashMap<Sha256Hash, StoredBlock>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Sha256Hash, StoredBlock> entry) {
            return size() > 100;  // This was chosen arbitrarily.
        }
    };

    private Sha256Hash chainHead;
    private NetworkParameters params;
    private FileChannel channel;

    private class Record {
        // A BigInteger representing the total amount of work done so far on this chain. As of May 2011 it takes 8
        // bytes to represent this field, so 16 bytes should be plenty for a long time.
        private static final int CHAIN_WORK_BYTES = 16;
        private final byte[] EMPTY_BYTES = new byte[CHAIN_WORK_BYTES];

        private int height;           // 4 bytes
        private byte[] chainWork;     // 16 bytes
        private byte[] blockHeader;   // 80 bytes

        public static final int SIZE = 4 + Record.CHAIN_WORK_BYTES + Block.HEADER_SIZE;

        public Record() {
            height = 0;
            chainWork = new byte[CHAIN_WORK_BYTES];
            blockHeader = new byte[Block.HEADER_SIZE];
        }

        // This should be static but the language does not allow for it.
        public void write(FileChannel channel, StoredBlock block) throws IOException {
            ByteBuffer buf = ByteBuffer.allocate(Record.SIZE);
            buf.putInt(block.getHeight());
            byte[] chainWorkBytes = block.getChainWork().toByteArray();
            assert chainWorkBytes.length <= CHAIN_WORK_BYTES : "Ran out of space to store chain work!";
            if (chainWorkBytes.length < CHAIN_WORK_BYTES) {
                // Pad to the right size.
                buf.put(EMPTY_BYTES, 0, CHAIN_WORK_BYTES - chainWorkBytes.length);
            }
            buf.put(chainWorkBytes);
            buf.put(block.getHeader().bitcoinSerialize());
            buf.position(0);
            channel.position(channel.size());
            if (channel.write(buf) < Record.SIZE)
                throw new IOException("Failed to write record!");
            channel.position(channel.size() - Record.SIZE);
        }

        public boolean read(FileChannel channel, long position, ByteBuffer buffer) throws IOException {
            buffer.position(0);
            long bytesRead = channel.read(buffer, position);
            if (bytesRead < Record.SIZE)
                return false;
            buffer.position(0);
            height = buffer.getInt();
            buffer.get(chainWork);
            buffer.get(blockHeader);
            return true;
        }

        public BigInteger getChainWork() {
            return new BigInteger(1, chainWork);
        }

        public Block getHeader(NetworkParameters params) throws ProtocolException {
            return new Block(params, blockHeader);
        }

        public int getHeight() {
            return height;
        }

        public StoredBlock toStoredBlock(NetworkParameters params) throws ProtocolException {
            return new StoredBlock(getHeader(params), getChainWork(), getHeight());
        }
    }

    public BoundedOverheadBlockStore(NetworkParameters params, File file) throws BlockStoreException {
        this.params = params;
        notFoundMarker = new StoredBlock(null, null, -1);
        try {
            load(file);
        } catch (Exception e) {
            log.error("failed to load block store from file", e);
            createNewStore(params, file);
        }
    }

    private void createNewStore(NetworkParameters params, File file) throws BlockStoreException {
        // Create a new block store if the file wasn't found or anything went wrong whilst reading.
        blockCache.clear();
        try {
            file.delete();
            this.file = new RandomAccessFile(file, "rw");  // Create fresh.
            this.channel = this.file.getChannel();
            this.file.write(FILE_FORMAT_VERSION);
        } catch (IOException e1) {
            // We could not load a block store nor could we create a new one!
            throw new BlockStoreException(e1);
        }
        try {
            // Set up the genesis block. When we start out fresh, it is by definition the top of the chain.
            Block genesis = params.genesisBlock.cloneAsHeader();
            StoredBlock storedGenesis = new StoredBlock(genesis, genesis.getWork(), 0);
            this.chainHead = new Sha256Hash(storedGenesis.getHeader().getHash());
            this.file.write(this.chainHead.hash);
            put(storedGenesis);
        } catch (VerificationException e1) {
            throw new RuntimeException(e1);  // Cannot happen.
        } catch (IOException e) {
            throw new BlockStoreException(e);
        }
    }

    private void load(File file) throws IOException, BlockStoreException {
        log.info("Reading block store from {}", file);
        this.file = new RandomAccessFile(file, "rw");
        channel = this.file.getChannel();
        // Read a version byte.
        int version = this.file.read();
        if (version == -1) {
            // No such file or the file was empty.
            throw new FileNotFoundException(file.getName() + " does not exist or is empty");
        }
        if (version != FILE_FORMAT_VERSION) {
            throw new BlockStoreException("Bad version number: " + version);
        }
        // Chain head pointer is the first thing in the file.
        byte[] chainHeadHash = new byte[32];
        this.file.read(chainHeadHash);
        this.chainHead = new Sha256Hash(chainHeadHash);
        log.info("Read chain head from disk: {}", this.chainHead);
        channel.position(channel.size() - Record.SIZE);
    }

    // TODO: This is ugly, fixinate!
    private Record dummyRecord = new Record();

    public synchronized void put(StoredBlock block) throws BlockStoreException {
        try {
            Sha256Hash hash = new Sha256Hash(block.getHeader().getHash());
            // Append to the end of the file.
            dummyRecord.write(channel, block);
            blockCache.put(hash, block);
        } catch (IOException e) {
            throw new BlockStoreException(e);
        }
    }

    public synchronized StoredBlock get(byte[] hashBytes) throws BlockStoreException {
        // Check the memory cache first.
        Sha256Hash hash = new Sha256Hash(hashBytes);
        StoredBlock fromMem = blockCache.get(hash);
        if (fromMem != null) {
            return fromMem;
        }
        if (notFoundCache.get(hash) == notFoundMarker) {
            return null;
        }

        try {
            Record fromDisk = getRecord(hash);
            StoredBlock block = null;
            if (fromDisk == null) {
                notFoundCache.put(hash, notFoundMarker);
            } else {
                block = fromDisk.toStoredBlock(params);
                blockCache.put(hash, block);
            }
            return block;
        } catch (IOException e) {
            throw new BlockStoreException(e);
        } catch (ProtocolException e) {
            throw new BlockStoreException(e);
        }
    }

    private ByteBuffer buf = ByteBuffer.allocateDirect(Record.SIZE);
    private Record getRecord(Sha256Hash hash) throws BlockStoreException, IOException, ProtocolException {
        long startPos = channel.position();
        // Use our own file pointer within the tight loop as updating channel positions is really expensive.
        long pos = startPos;
        Record record = new Record();
        do {
            if (!record.read(channel, pos, buf))
                throw new IOException("Failed to read buffer");
            if (Arrays.equals(record.getHeader(params).getHash(), hash.hash)) {
                // Found it. Update file position for next time.
                channel.position(pos);
                return record;
            }
            // Did not find it.
            if (pos == 1 + 32) {
                // At the start so wrap around to the end.
                pos = channel.size() - Record.SIZE;
            } else {
                // Move backwards.
                pos = pos - Record.SIZE;
                assert pos >= 1 + 32 : pos;
            }
        } while (pos != startPos);
        // Was never stored.
        channel.position(pos);
        return null;
    }

    public synchronized StoredBlock getChainHead() throws BlockStoreException {
        return get(chainHead.hash);
    }

    public synchronized void setChainHead(StoredBlock chainHead) throws BlockStoreException {
        try {
            byte[] hash = chainHead.getHeader().getHash();
            this.chainHead = new Sha256Hash(hash);
            // Write out new hash to the first 32 bytes of the file past one (first byte is version number).
            channel.write(ByteBuffer.wrap(hash), 1);
        } catch (IOException e) {
            throw new BlockStoreException(e);
        }
    }

}
