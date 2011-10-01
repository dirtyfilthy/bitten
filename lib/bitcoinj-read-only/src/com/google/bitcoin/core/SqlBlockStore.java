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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.bitcoin.bouncycastle.util.encoders.Hex;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * SQL blockstore
 */
public class SqlBlockStore implements BlockStore {
	// We use a ByteBuffer to hold hashes here because the Java array
	// equals()/hashcode() methods do not operate on
	// the contents of the array but just inherit the default Object behavior.
	// ByteBuffer provides the functionality
	// needed to act as a key in a map.
	//
	// The StoredBlocks are also stored as serialized objects to ensure we don't
	// have assumptions that would make
	// things harder for disk based implementations.
	private PreparedStatement insertBlockStatement;
	private PreparedStatement insertTransactionStatement;
	private PreparedStatement insertTransactionInputStatement;
	private PreparedStatement insertTransactionOutputStatement;
	private PreparedStatement findTransactionOutputStatement;
	private PreparedStatement findTransactionsByBlockIdStatement;
	private PreparedStatement findTransactionInputsByTransactionIdStatement;
	private PreparedStatement findTransactionOutputsByTransactionIdStatement;
	private PreparedStatement findBlockByHashStatement;
	private PreparedStatement findBlockByChainHeadStatement;
	private PreparedStatement updateChainHeadStatement;
	private PreparedStatement insertAddressStatement;
	private PreparedStatement findAddressByHashStatement;
	private Map<ByteBuffer, byte[]> blockMap;
	private SqlFullStoredBlock chainHead = null;
	private Connection sqliteDatabase;
	private NetworkParameters networkParameters;
	

	private BigInteger chainWork;
	private int height;
	private MapCache<String, Long> addressMap= new MapCache<String, Long>(1000);
	private static final String INSERT_BLOCK_SQL = "INSERT INTO blocks (hash, height, work, version, prev_block_hash, merkle_root, time, bits, nonce) VALUES (?,?,?,?,?,?,?,?,?);";
	private static final String INSERT_TRANSACTION_SQL = "INSERT INTO transactions (hash, block_id, 'index', version, tx_out_count, tx_in_count, locktime, is_coinbase) VALUES (?,?,?,?,?,?,?,?);";
	private static final String INSERT_TRANSACTION_INPUT_SQL = "INSERT INTO transaction_inputs(transaction_id, 'index', previous_output_id, previous_output_hash, previous_output_index, script_length, script, sequence, is_coinbase, from_address_id) VALUES (?,?,?,?,?,?,?,?,?,?);";
	private static final String INSERT_TRANSACTION_OUTPUT_SQL = "INSERT INTO transaction_outputs(transaction_id, 'index', value, script_length, script,to_address_id,is_coinbase) VALUES (?,?,?,?,?,?,?);";
	private static final String FIND_TRANSACTION_OUTPUT_SQL = "SELECT transaction_outputs.id,from_address_id FROM transaction_outputs JOIN transactions ON transaction_id=transactions.id WHERE transactions.hash=? AND transaction_outputs.'index'=? LIMIT 1";
	private static final String FIND_TRANSACTIONS_BY_BLOCK_ID_SQL = "SELECT transactions.* FROM transactions WHERE transactions.block_id=? ORDER BY 'index';";
	private static final String FIND_TRANSACTION_INPUTS_BY_TRANSACTION_ID_SQL = "SELECT transaction_inputs.* FROM transaction_inputs WHERE transaction_inputs.transaction_id=? ORDER BY 'index';";
	private static final String FIND_TRANSACTION_OUTPUTS_BY_TRANSACTION_ID_SQL = "SELECT transaction_outputs.* FROM transaction_outputs WHERE transaction_outputs.transaction_id=? ORDER BY 'index';";
	private static final String FIND_BLOCK_BY_HASH_SQL = "SELECT blocks.* FROM blocks WHERE blocks.hash=? LIMIT 1";
	private static final String FIND_BLOCK_BY_CHAIN_HEAD_SQL = "SELECT blocks.* FROM chain_head LEFT JOIN blocks ON chain_head.block_id=blocks.id WHERE blocks.id IS NOT NULL LIMIT 1";
	private static final String UPDATE_CHAIN_HEAD_SQL = "UPDATE chain_head SET block_id=?";
	private static final String INSERT_ADDRESS_SQL = "INSERT INTO addresses (base58hash) VALUES (?)";
	private static final String FIND_ADDRESS_BY_HASH_SQL = "SELECT * FROM addresses WHERE base58hash=? LIMIT 1";

	public SqlBlockStore(NetworkParameters params, File file) {
		networkParameters = params;

		// Insert the genesis block.
		try {
			boolean newDb = !file.exists();

			sqliteDatabase = connect(file);
			if (newDb) {
				create_database();
			}
			insertBlockStatement = sqliteDatabase
					.prepareStatement(INSERT_BLOCK_SQL);
			insertTransactionStatement = sqliteDatabase
					.prepareStatement(INSERT_TRANSACTION_SQL);
			insertTransactionInputStatement = sqliteDatabase
					.prepareStatement(INSERT_TRANSACTION_INPUT_SQL);
			insertTransactionOutputStatement = sqliteDatabase
					.prepareStatement(INSERT_TRANSACTION_OUTPUT_SQL);
			findTransactionOutputStatement = sqliteDatabase
					.prepareStatement(FIND_TRANSACTION_OUTPUT_SQL);
			findTransactionsByBlockIdStatement = sqliteDatabase
					.prepareStatement(FIND_TRANSACTIONS_BY_BLOCK_ID_SQL);
			findTransactionInputsByTransactionIdStatement = sqliteDatabase
					.prepareStatement(FIND_TRANSACTION_INPUTS_BY_TRANSACTION_ID_SQL);
			findTransactionOutputsByTransactionIdStatement = sqliteDatabase
					.prepareStatement(FIND_TRANSACTION_OUTPUTS_BY_TRANSACTION_ID_SQL);
			findBlockByHashStatement = sqliteDatabase
					.prepareStatement(FIND_BLOCK_BY_HASH_SQL);
			findBlockByChainHeadStatement = sqliteDatabase
					.prepareStatement(FIND_BLOCK_BY_CHAIN_HEAD_SQL);
			updateChainHeadStatement = sqliteDatabase
					.prepareStatement(UPDATE_CHAIN_HEAD_SQL);
			findAddressByHashStatement = sqliteDatabase
					.prepareStatement(FIND_ADDRESS_BY_HASH_SQL);
			insertAddressStatement = sqliteDatabase
					.prepareStatement(INSERT_ADDRESS_SQL);
			if (newDb) {

				Block genesisHeader = params.genesisBlock;
				SqlFullStoredBlock storedGenesis = new SqlFullStoredBlock(
						genesisHeader, genesisHeader.getWork(), 0);
				put(storedGenesis);
				setChainHead(storedGenesis);
			}
			sqliteDatabase.setAutoCommit(true);

		} catch (BlockStoreException e) {
			throw new RuntimeException(e); // Cannot happen.
		} catch (VerificationException e) {
			throw new RuntimeException(e); // Cannot happen.
		} catch (SQLException e) {
			throw new RuntimeException(e); // Should not happen. :)
		}
	}

	public SqlTransactionInput loadTransactionInputFromResultSet(ResultSet rs)
			throws SQLException {
		byte[] script = HexBin.decode(rs.getString(8));
		SqlTransactionInput t = new SqlTransactionInput(networkParameters,
				null, script);
		t.id = rs.getLong(1);
		t.transactionId = rs.getLong(2);
		t.index = rs.getLong(3);
		t.previousOutputId = rs.getLong(4);
		TransactionOutPoint op = new TransactionOutPoint(networkParameters,
				rs.getLong(6), null);
		System.out.println("1 " + rs.getString(1));
		System.out.println("2 " + rs.getString(2));
		System.out.println("3 " + rs.getString(3));
		System.out.println("4 " + rs.getString(4));

		System.out.println("outpoint raw hash " + rs.getString(5));
		op.hash = HexBin.decode(rs.getString(5));
		System.out.println("outpoint hash " + HexBin.encode(op.hash));
		t.outpoint = op;
		t.sequence = rs.getLong(9);
		return t;
	}
	
	public ArrayList<SqlTransactionOutput> loadTransactionOutputsFromSql(String sql) throws SQLException{
		ArrayList list=new ArrayList<SqlTransactionOutput>();
		ResultSet rs=sqliteDatabase.createStatement().executeQuery(sql);
		while(rs.next()){
			list.add(loadTransactionOutputFromResultSet(rs));
		}
		rs.close();
		return list;
	}

	public SqlTransactionOutput loadTransactionOutputFromResultSet(ResultSet rs)
			throws SQLException {
		byte[] script = HexBin.decode(rs.getString(6));
		BigInteger value = BigInteger.valueOf(rs.getLong(4));
		SqlTransactionOutput t = new SqlTransactionOutput(networkParameters,
				script);
		t.setValue(value);
		t.id = rs.getLong(1);
		t.transactionId = rs.getLong(2);
		t.index = rs.getLong(3);
		return t;
	}
	
	public ResultSet rawSqlQuery(String sql) throws SQLException{
		return sqliteDatabase.createStatement().executeQuery(sql);
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException{
		return sqliteDatabase.prepareStatement(sql);
	}

	public SqlTransaction loadTransactionFromResultSet(ResultSet rs)
			throws SQLException {
		SqlTransaction t = new SqlTransaction(networkParameters);
		ResultSet rs2;
		t.id = rs.getLong(1);
		t.block_id = rs.getLong(3);
		t.index = rs.getLong(4);
		t.version = rs.getLong(5);
		t.lockTime = rs.getLong(8);
		try {
			t.createdAt = rs.getLong(10);
		}
		catch(SQLException e){
			// do nothing
		}
		findTransactionInputsByTransactionIdStatement.setLong(1, t.id);
		rs2 = findTransactionInputsByTransactionIdStatement.executeQuery();
		while (rs2.next()) {
			SqlTransactionInput in = loadTransactionInputFromResultSet(rs2);
			in.setParentTransaction(t);
			t.inputs.add(in);
		}
		rs2.close();
		findTransactionOutputsByTransactionIdStatement.setLong(1, t.id);
		rs2 = findTransactionOutputsByTransactionIdStatement.executeQuery();
		while (rs2.next()) {
			SqlTransactionOutput out = loadTransactionOutputFromResultSet(rs2);
			out.setParentTransaction(t);
			t.outputs.add(out);
		}
		rs2.close();
		return t;
	}

	public synchronized long findOrCreateAddressId(Address a)
			throws SQLException {
		ResultSet rs;
		long id;
		Long mapid;
		String address=a.toString();
		mapid=addressMap.get(address);
		if(mapid!=null){
			return mapid;
		}
		findAddressByHashStatement.setString(1, address);
		rs = findAddressByHashStatement.executeQuery();
		if (rs.next()) {
			id = rs.getLong(1);
			rs.close();
			addressMap.put(address, id);
			return id;
		}
		rs.close();
		insertAddressStatement.setString(1, address);
		insertAddressStatement.execute();
		rs = insertAddressStatement.getGeneratedKeys();
		id = rs.getLong(1);
		rs.close();
		addressMap.put(address,id);
		return id;
	}

	public SqlFullStoredBlock loadBlockFromResultSet(ResultSet rs) {
		try {
			Block realBlock = new Block(networkParameters);

			SqlFullStoredBlock storedBlock;
			long blockId = rs.getLong(1);
			long height = rs.getLong(3);
			BigInteger chainOfWork = new BigInteger(HexBin.decode(rs
					.getString(4)));
			realBlock.setVersion(rs.getLong(5));
			realBlock.setPrevBlockHash(HexBin.decode(rs.getString(6)));
			realBlock.setMerkleRoot(HexBin.decode(rs.getString(7)));
			realBlock.setTime(rs.getLong(8));
			realBlock.setDifficultyTarget(rs.getLong(9));
			realBlock.setNonce(rs.getLong(10));
			storedBlock = new SqlFullStoredBlock(realBlock, chainOfWork,
					(int) height);
			storedBlock.id = blockId;
			findTransactionsByBlockIdStatement.setLong(1, blockId);
			ResultSet transactionsResultSet = findTransactionsByBlockIdStatement
					.executeQuery();
			Transaction t;
			while (transactionsResultSet.next()) {
				t = loadTransactionFromResultSet(transactionsResultSet);
				realBlock.addTransaction(t);
			}
			return storedBlock;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public synchronized void put(StoredBlock storedBlock)
			throws BlockStoreException {
		ResultSet generatedKeys;
		SqlFullStoredBlock block = (SqlFullStoredBlock) storedBlock;
		long blockId;
		long transactionId;
		
		try {
			sqliteDatabase.setAutoCommit(false);
			insertBlockStatement.setString(1,
					HexBin.encode(block.getBlock().getHash()));
			insertBlockStatement.setInt(2, block.getHeight());
			insertBlockStatement.setString(3,
					HexBin.encode(block.getChainWork().toByteArray()));
			insertBlockStatement.setLong(4, block.getHeader().getVersion());
			insertBlockStatement.setString(5,
					HexBin.encode(block.getBlock().getPrevBlockHash()));
			insertBlockStatement.setString(6,
					HexBin.encode(block.getBlock().getMerkleRoot()));

			insertBlockStatement.setLong(7, block.getBlock().getTime());
			insertBlockStatement.setLong(8, block.getBlock()
					.getDifficultyTarget());
			insertBlockStatement.setLong(9, block.getBlock().getNonce());
			insertBlockStatement.execute();
			generatedKeys = insertBlockStatement.getGeneratedKeys();
			generatedKeys.next();
			blockId = generatedKeys.getLong(1);
			block.id = blockId;
			generatedKeys.close();
		} catch (SQLException e) {
			throw new BlockStoreException(e);
		}
		List<Transaction> transactions = block.getBlock().transactions;
		try {
			int index = 0;
			for (Transaction t : transactions) {
				int isCoinbase = 0;
				if (t.inputs.size() > 0 && t.inputs.get(0).isCoinBase()) {
					isCoinbase = 1;
				}
				insertTransactionStatement.setString(1,
						HexBin.encode(t.getHash().hash));
				insertTransactionStatement.setLong(2, blockId);
				insertTransactionStatement.setInt(3, index);
				insertTransactionStatement.setLong(4, t.version);
				insertTransactionStatement.setLong(5, t.outputs.size());
				insertTransactionStatement.setLong(6, t.inputs.size());
				insertTransactionStatement.setLong(7, t.lockTime);
				insertTransactionStatement.setLong(8, isCoinbase);
				insertTransactionStatement.execute();
				generatedKeys = insertBlockStatement.getGeneratedKeys();
				generatedKeys.next();
				transactionId = generatedKeys.getLong(1);
				generatedKeys.close();
			
				linkAddressesToWallet(storeTransactionInputs(transactionId, t.inputs, isCoinbase));
				storeTransactionOutputs(transactionId, t.outputs, isCoinbase);
				index++;
			}
			if((block.getHeight() % 10)==0){
				sqliteDatabase.commit();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}
	
	public void linkAddressesToWallet(ArrayList<Long> addressIds) throws SQLException{
		if(addressIds.size()<2){
			return;
		}
		ResultSet rs;
		StringBuilder builder = new StringBuilder();
		ArrayList<Long> walletIds=new ArrayList<Long>();
		long walletId;
		 for (int i = 0; i < addressIds.size();) {
		        builder.append(addressIds.get(i));
		        if (++i < addressIds.size()) {
		            builder.append(",");
		        }
		    }
		String addressList=builder.toString();
		
		
		rs=sqliteDatabase.createStatement().executeQuery("SELECT MIN(COALESCE(wallet_id,id)) FROM addresses WHERE addresses.id IN ("+addressList+")");
		rs.next();
		walletId=rs.getLong(1);
		rs.close();
		rs=sqliteDatabase.createStatement().executeQuery("SELECT DISTINCT wallet_id FROM addresses WHERE addresses.id IN ("+addressList+") AND wallet_id IS NOT NULL");
		builder = new StringBuilder();
		while(rs.next()){
			walletIds.add(rs.getLong(1));
		}
		rs.close();
		for (int i = 0; i < walletIds.size();) {
	        builder.append(walletIds.get(i));
	        if (++i < walletIds.size()) {
	            builder.append(",");
	        }
	    }
		String walletList=builder.toString();
		String sql="UPDATE addresses SET wallet_id = "+walletId+" WHERE addresses.id IN (" +addressList+")";
		System.out.println(sql);
		sqliteDatabase.createStatement().execute(sql);
		if(walletIds.size()>0){
			sql="UPDATE addresses SET wallet_id = "+walletId+" WHERE addresses.wallet_id IN (" +walletList+")";
			System.out.println(sql);
			sqliteDatabase.createStatement().execute(sql);
		}
		
		
	}
	
	private void storeTransactionOutputs(long transactionId, ArrayList<TransactionOutput> outputs, int isCoinbase){
		int outputindex=0;
		for (TransactionOutput o : outputs) {
			
			try {
				insertTransactionOutputStatement.setLong(1, transactionId);
			
			insertTransactionOutputStatement.setInt(2, outputindex);
			insertTransactionOutputStatement.setLong(3, o.getValue()
					.longValue());
			insertTransactionOutputStatement.setLong(4,
					o.getScriptBytes().length);
			insertTransactionOutputStatement.setString(5,
					HexBin.encode(o.getScriptBytes()));
			try {
				System.out.println("to address: " + o.getToAddress());
				insertTransactionOutputStatement.setLong(6,
						findOrCreateAddressId(o.getToAddress()));
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				insertTransactionOutputStatement.setNull(6,
						java.sql.Types.INTEGER);
			}
			insertTransactionOutputStatement.setInt(7, isCoinbase);
			insertTransactionOutputStatement.execute();
				
			outputindex++;
			}
			catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	// returns list of address idz
	
	private ArrayList<Long> storeTransactionInputs(long transactionId, ArrayList<TransactionInput> inputs, int isCoinbase){
		int inputindex = 0;
		long previousOutputId;
		String prevOutputHash;
		ResultSet findOutputResults;
		ResultSet generatedKey;
		Long addressId;
		ArrayList<Long> addressIdz=new ArrayList<Long>();
		try {
		for (TransactionInput i : inputs) {
			
			prevOutputHash = HexBin.encode(i.outpoint.hash);
			findTransactionOutputStatement.setString(1, prevOutputHash);
			findTransactionOutputStatement.setLong(2, i.outpoint.index);
			findOutputResults = findTransactionOutputStatement
					.executeQuery();
			if (findOutputResults.next()) {
				previousOutputId = findOutputResults.getLong(1);
				addressId=findOutputResults.getLong(2);
			} else {
				previousOutputId = 0;
				addressId=null;
			}
			findOutputResults.close();
			insertTransactionInputStatement.setLong(1, transactionId);
			insertTransactionInputStatement.setInt(2, inputindex);
			insertTransactionInputStatement
					.setLong(3, previousOutputId);
			insertTransactionInputStatement
					.setString(4, prevOutputHash);
			insertTransactionInputStatement
					.setLong(5, i.outpoint.index);
			insertTransactionInputStatement.setLong(6,
					i.scriptBytes.length);
			insertTransactionInputStatement.setString(7,
					HexBin.encode(i.scriptBytes));
			insertTransactionInputStatement.setLong(8, i.sequence);
			insertTransactionInputStatement.setBoolean(9,
					i.isCoinBase());
			if(addressId!=null){
				insertTransactionInputStatement.setLong(10,addressId);
				addressIdz.add(addressId);
			} else {
				// TODO Auto-generated catch block
				insertTransactionInputStatement.setNull(10,
						java.sql.Types.INTEGER);
			}
			insertTransactionInputStatement.execute();
			
		}
		
		
	} catch (SQLException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
		return addressIdz;
	}

	public Connection getConnection() {
		return sqliteDatabase;
	}

	public synchronized SqlFullStoredBlock get(byte[] hash)
			throws BlockStoreException {
		System.out.println("Getting hash " + HexBin.encode(hash));
		try {
			findBlockByHashStatement.setString(1, HexBin.encode(hash));
			ResultSet rs = findBlockByHashStatement.executeQuery();
			if (!rs.next()) {
				rs.close();
				return null;
			}
			SqlFullStoredBlock b = loadBlockFromResultSet(rs);
			rs.close();
			return b;
		} catch (SQLException e) {
			throw new BlockStoreException(e);
		}
	}

	public synchronized SqlFullStoredBlock getChainHead() {
		if (chainHead != null) {
			return chainHead;
		}
		try {
			ResultSet rs = findBlockByChainHeadStatement.executeQuery();
			if (rs.next()) {
				SqlFullStoredBlock b = loadBlockFromResultSet(rs);
				rs.close();
				return b;
			}
			rs.close();
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	public synchronized void setChainHead(StoredBlock chainHead)
			throws BlockStoreException {
		try {
			long id = ((SqlFullStoredBlock) chainHead).id;
			// findBlockByHashStatement.setString(1,HexBin.encode(chainHead.getHeader().getHash()));
			// ResultSet rs=findBlockByHashStatement.executeQuery();
			// if(!rs.next()){
			// throw new
			// BlockStoreException("Block not stored, can't set chain head");
			// }
			// SqlFullStoredBlock s=loadBlockFromResultSet(rs);
			this.chainHead = (SqlFullStoredBlock) chainHead;
			System.out.println("setting block id " + id);
			updateChainHeadStatement.setLong(1, id);
			updateChainHeadStatement.execute();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized Connection connect(File file) {

		try {
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void create_database() throws SQLException {
		Statement s = sqliteDatabase.createStatement();
		s.execute("CREATE TABLE blocks(id INTEGER NOT NULL PRIMARY KEY, hash VARCHAR, height INTEGER, work VARCHAR, version INTEGER, prev_block_hash VARCHAR, merkle_root VARCHAR, time INTEGER, bits INTEGER, nonce INTEGER);");
		s.execute("CREATE INDEX blocks_hash_idx ON blocks (hash);");
		s.execute("CREATE TABLE transactions(id INTEGER NOT NULL PRIMARY KEY, hash VARCHAR, block_id INTEGER, 'index' INTEGER, version INTEGER, tx_out_count INTEGER, tx_in_count INTEGER, locktime INTEGER, is_coinbase INTEGER);");
		s.execute("CREATE INDEX transactions_block_id_idx ON transactions (block_id);");
		s.execute("CREATE INDEX transactions_hash_idx ON transactions (hash);");
		s.execute("CREATE TABLE transaction_inputs(id INTEGER NOT NULL PRIMARY KEY, transaction_id INTEGER, 'index' INTEGER, previous_output_id INTEGER, previous_output_hash VARCHAR, previous_output_index INTEGER, script_length INTEGER, script VARCHAR, sequence INTEGER, is_coinbase INTEGER, from_address_id INTEGER);");
		s.execute("CREATE INDEX transaction_inputs_transaction_id_idx ON transaction_inputs (transaction_id);");
		s.execute("CREATE TABLE transaction_outputs(id INTEGER NOT NULL PRIMARY KEY, transaction_id INTEGER, 'index' INTEGER, value INTEGER, script_length INTEGER, script VARCHAR, to_address_id INTEGER, is_coinbase INTEGER);");
		s.execute("CREATE INDEX transaction_outputs_transaction_id_idx ON transaction_outputs (transaction_id);");
		s.execute("CREATE TABLE chain_head(block_id INTEGER);");
		s.execute("CREATE TABLE addresses (id INTEGER NOT NULL PRIMARY KEY, base58hash VARCHAR UNIQUE, wallet_id INTEGER);");
		s.execute("CREATE INDEX wallet_id_idx ON addresses (wallet_id);");
		s.execute("CREATE INDEX tran_out_to_idx ON transaction_outputs (to_address_id);");
		s.execute("CREATE INDEX tran_in_from_idx ON transaction_inputs (from_address_id);");
		
		s.execute("CREATE INDEX addresses_base58hash_idx ON addresses (base58hash);");
		s.execute("INSERT INTO chain_head VALUES(0);");
	}

}
