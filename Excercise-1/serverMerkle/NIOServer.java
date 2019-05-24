package serverMerkle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
	public static final String END_OF_SESSION = "close";
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, InterruptedException {
		Selector selector = Selector.open();
		ServerSocketChannel serverSocket = ServerSocketChannel.open();
		InetSocketAddress localAddr = new InetSocketAddress("localhost", 2323);
		serverSocket.bind(localAddr);
		serverSocket.configureBlocking(false);
		int ops = serverSocket.validOps();
		SelectionKey selectKy = serverSocket.register(selector, ops, null);
		while (true) {
 
			log("i'm a server and i'm waiting for new connection and buffer select...", "out");
			selector.select();
			Set<SelectionKey> activeKeys = selector.selectedKeys();
			Iterator<SelectionKey> keys = activeKeys.iterator();
			while (keys.hasNext()) {
				SelectionKey myKey = keys.next();
				if (myKey.isAcceptable()) {
					SocketChannel clientSocket = serverSocket.accept();
					clientSocket.configureBlocking(false);
					clientSocket.register(selector, SelectionKey.OP_READ);
					log("Connection Accepted: " + clientSocket.getLocalAddress() + "\n", "err");
				} else if (myKey.isReadable()) {
					SocketChannel clientSocket = (SocketChannel) myKey.channel();
					ByteBuffer buffer = ByteBuffer.allocate(256);
					clientSocket.read(buffer);
					String result = new String(buffer.array()).trim();
					log("--- Message received: " + result, "err" );
					if(result.equals("0ff89de99d4a8f4b04cb162bcb5740cf")) {
						byte[] message = new String("hash1#").getBytes();
						ByteBuffer bufferWrap = ByteBuffer.wrap(message);
						clientSocket.write(bufferWrap);
						message = new String("hash2#").getBytes();
						bufferWrap = ByteBuffer.wrap(message);
						clientSocket.write(bufferWrap);
						message = new String("next").getBytes();
						bufferWrap = ByteBuffer.wrap(message);
						clientSocket.write(bufferWrap);
						bufferWrap.clear();
						Thread.sleep(2000);
					}else if(result.equals("8ca10608a248910c25083c3dab4371c3")){
						byte[] message = new String("hash3#").getBytes();
						ByteBuffer bufferWrap = ByteBuffer.wrap(message);
						clientSocket.write(bufferWrap);
						message = new String("hash4#").getBytes();
						bufferWrap = ByteBuffer.wrap(message);
						clientSocket.write(bufferWrap);
						message = new String("next").getBytes();
						bufferWrap = ByteBuffer.wrap(message);
						clientSocket.write(bufferWrap);
						bufferWrap.clear();
						Thread.sleep(2000);
					}
					buffer.clear();
					clientSocket.read(buffer);
					result = new String(buffer.array()).trim();
					if (result.equals("close")) {
						clientSocket.close();
						log("\nIt's time to close this connection as we got a close packet", "out");
					}
				}
				keys.remove();
			}
		}
	}
 
	private static void log(String str, String mode) {
		switch(mode) {
			case "out": {System.out.println(str); break;}
			case "err": {System.err.println(str); break;}
			default: {}
		}
	}
}
