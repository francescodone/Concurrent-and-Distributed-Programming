package merkleClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerkleValidityRequest {

	/**
	 * IP address of the authority
	 * */
	private final String authIPAddr;
	/**
	 * Port number of the authority
	 * */
	private final int  authPort;
	/**
	 * Hash value of the merkle tree root. 
	 * Known before-hand.
	 * */
	private final String mRoot;
	/**
	 * List of transactions this client wants to verify 
	 * the existence of.
	 * */
	private List<String> mRequests;
	
	/**
	 * Sole constructor of this class - marked private.
	 * */
	private MerkleValidityRequest(Builder b){
		this.authIPAddr = b.authIPAddr;
		this.authPort = b.authPort;
		this.mRoot = b.mRoot;
		this.mRequests = b.mRequest;
	}
	
	/**
	 * <p>Method implementing the communication protocol between the client and the authority.</p>
	 * <p>The steps involved are as follows:</p>
	 * 		<p>0. Opens a connection with the authority</p>
	 * 	<p>For each transaction the client does the following:</p>
	 * 		<p>1.: asks for a validityProof for the current transaction</p>
	 * 		<p>2.: listens for a list of hashes which constitute the merkle nodes contents</p>
	 * 	<p>Uses the utility method {@link #isTransactionValid(String, List<String>) isTransactionValid} </p>
	 * 	<p>method to check whether the current transaction is valid or not.</p>
	 * @throws InterruptedException 
	 * */
	public Map<Boolean, List<String>> checkWhichTransactionValid() throws IOException, InterruptedException {
		//throw new UnsupportedOperationException();
		Map<Boolean, List<String>> rtn = new HashMap<Boolean, List<String>>();
		List<String> valid = new ArrayList<String>();  //list of valid transactions
		List<String> invalid = new ArrayList<String>();//list of invalid transactions
		//STEP 0: Client connection
		InetSocketAddress remoteAddr = new InetSocketAddress(authIPAddr, authPort);
		try {
			SocketChannel client = SocketChannel.open(remoteAddr);
			ByteBuffer bufferNodes = ByteBuffer.allocate(574);
			/* 
			 * [for more details see comments in lines 103-105]
			 * hash1#hash2#hash3#hash4#next
			 * 
			 * "next" = 4  Byte
			 * hash   = 37 Byte
			 * "#"	  = 1  Byte
			 * 
			 * Assuming that MerkleTree has maximum 15 levels: (2^16)-1 elements should be enough
			 * 15 hash = 15*38 = 570 Byte
			 * 570+4 = 574 total Byte 
			 * 
			 */
			for (String transaction : mRequests) {
			//STEP 1: for each transaction ask for validityProof
				byte[] message = new String(transaction).getBytes();
				ByteBuffer buffer = ByteBuffer.wrap(message);
				try { //try buffer write, else try reconnect
					client.write(buffer);
				}catch(java.io.IOException e){
					 System.err.println(e);	 
					 System.out.println("Trying reconnection in 10s...");
					 if(client.isOpen()==true) {
						 client.close();
					 }
					 Thread.sleep(10000);
					 try {
						 client = SocketChannel.open(remoteAddr);
					 }catch(java.net.ConnectException f){
						 System.err.println(f);	//can't reconnect
					 }
				}
				List<String> mNodes = new ArrayList<String>();
				bufferNodes.clear();
				Thread.sleep(2000);
				try { //try buffer read, else try reconnect
					client.read(bufferNodes);
					String readNode = new String(bufferNodes.array()).trim();
					/*
					 * readNode received from the authority is like hash1#hash2#hash3#next
					 * 
					 * each hash is put on mNodes
					 */
					while(readNode.equals("next")==false) {
						mNodes.add(readNode.substring(0, readNode.indexOf("#")));
						readNode=readNode.substring(readNode.indexOf("#")+1, readNode.length());
					}
					//System.out.println(mNodes);
				}catch(java.io.IOException e){
					 System.err.println(e);	 
					 System.out.println("Trying reconnection in 10s...");
					 if(client.isOpen()==true) {
						 client.close();
					 }
					 Thread.sleep(10000);
					 try {
						 client = SocketChannel.open(remoteAddr);
					 }catch(java.net.ConnectException f){
						 System.err.println(f);	//can't reconnect
					 }
				}
			//STEP 2: listen for a list of hashes
				Boolean validTransaction = isTransactionValid(transaction, mNodes);
				if(validTransaction==true) {
					valid.add(transaction);
				}else {
					invalid.add(transaction);
				}
				buffer.clear();
				Thread.sleep(2000);
			}
			rtn.put(true, valid);
			rtn.put(false, invalid);
			byte[] message = new String("close").getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			try {
				client.write(buffer);
				Thread.sleep(2000);
				client.close();
			}catch(java.io.IOException e) {
				System.err.println("Connection is already closed.");
			}
		}catch(java.net.ConnectException e){
			System.err.println(e);
		}
		return rtn;
	}
	
	/**
	 * 	Checks whether a transaction 'merkleTx' is part of the merkle tree.
	 * 
	 *  @param merkleTx String: the transaction we want to validate
	 *  @param merkleNodes String: the hash codes of the merkle nodes required to compute 
	 *  the merkle root
	 *  
	 *  @return: boolean value indicating whether this transaction was validated or not.
	 * */
	private boolean isTransactionValid(String merkleTx, List<String> merkleNodes) {
		if(merkleTx == null) {
			throw new UnsupportedOperationException();
		}else {
			for(String hash: merkleNodes) {
				merkleTx=HashUtil.md5Java(merkleTx+hash);
			}
			return mRoot.equals(merkleTx);
		}
	}

	/**
	 * Builder for the MerkleValidityRequest class. 
	 * */
	public static class Builder {
		private String authIPAddr;
		private int authPort;
		private String mRoot;
		private List<String> mRequest;	
		
		public Builder(String authorityIPAddr, int authorityPort, String merkleRoot) {
			this.authIPAddr = authorityIPAddr;
			this.authPort = authorityPort;
			this.mRoot = merkleRoot;
			mRequest = new ArrayList<>();
		}
				
		public Builder addMerkleValidityCheck(String merkleHash) {
			mRequest.add(merkleHash);
			return this;
		}
		
		public MerkleValidityRequest build() {
			return new MerkleValidityRequest(this);
		}
	}
}