package UnitTesting;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EmulatorTest {

	public static void main(String[] args) throws IOException {
		
		String txt = "Hello World!";
		String emulator_ip = "143.215.129.100";
		int emulator_port = 7000;
		int client_port = 9956;
		int server_port = 9957;
		int timeout = 5000;
		
		DatagramSocket client = new DatagramSocket(client_port);
		DatagramSocket server = new DatagramSocket(server_port);
		
		DatagramPacket outgoing = new DatagramPacket(txt.getBytes(),txt.getBytes().length,InetAddress.getByName(emulator_ip),emulator_port);
		DatagramPacket incoming = new DatagramPacket(new byte[txt.getBytes().length],txt.getBytes().length,InetAddress.getByName(emulator_ip),emulator_port);
		
		// client --> server test
		client.send(outgoing);
		server.setSoTimeout(timeout);
		server.receive(incoming);
		
		String client_to_server_response = new String(incoming.getData());
		System.out.println("Client --> Server Test: " + client_to_server_response);
		
		// clear out packet for next test
		incoming = new DatagramPacket(new byte[txt.getBytes().length],txt.getBytes().length,InetAddress.getByName(emulator_ip),emulator_port);
				
		// server --> client test
		server.send(outgoing);
		client.setSoTimeout(timeout);
		client.receive(incoming);
		
		String server_to_client_response = new String(incoming.getData());
		System.out.println("Client --> Server Test: " + server_to_client_response);
		
		client.close();
		server.close();
		
	}
	
}
