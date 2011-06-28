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

import org.junit.Test;
import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SqlBlockStoreTest {
    @Test
    public void testStorage() throws Exception {
        File temp = File.createTempFile("bitcoinj-sql-test",".tmp");
        
        // we don't actually want the file created
        
        temp.delete(); 
        
        
        System.out.println(temp.getAbsolutePath());
        //temp.deleteOnExit();

        NetworkParameters params = NetworkParameters.unitTests();
        Address to = new ECKey().toAddress(params);
        SqlBlockStore store = new SqlBlockStore(params, temp);
        // Check the first block in a new store is the genesis block.
        SqlFullStoredBlock genesis = store.getChainHead();
        assertNotNull(genesis);
        assertEquals(params.genesisBlock.transactions.get(0), genesis.getBlock().transactions.get(0));
        assertEquals(params.genesisBlock, genesis.getHeader());

        // Build a new block.
        SqlFullStoredBlock b1 = genesis.build(genesis.getBlock().createNextBlock(to));
        store.put(b1);
        store.setChainHead(b1);
        // Check we can get it back out again if we rebuild the store object.
        store = new SqlBlockStore(params, temp);
        StoredBlock b2 = store.get(b1.getHeader().getHash());
        assertEquals(b1, b2);
        // Check the chain head was stored correctly also.
        assertEquals(b1, store.getChainHead());
        temp.delete();
    }
}
