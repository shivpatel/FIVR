package FIVRClient;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import FIVRModules.*;

public class Client {

	public static DatagramSocket socket = null;
	public static InetAddress host;
	public static int port;
	public static int clientPort;
	public static boolean connected = false;

	public static int WINDOW_THRESHOLD = 25; // 25 max
	public static int WINDOW_SIZE = 5; // 5 packets per window
	public static int PACKET_SIZE = 512; // 512 bytes
	public static int BUFFER_SIZE = 1000; // 1000 bytes
	public static int RTT_TIMEOUT = 600; // 2 sec.
	public static int CONNECTION_TIMEOUT = 30000; // 30 sec.
	public static int PACKET_SEQUENCE_NUM = 0;

	/**
	 * Get user commands
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("*** FIVR Client ***");
		System.out.println(" ");
		System.out.println("Available Commands:");
		System.out
				.println("> fta-client [client port] [emulator address] [emulator port]");
		System.out.println("> connect");
		System.out.println("> get [file]");
		System.out.println("> post [file]");
		System.out.println("> window [size]");
		System.out.println("> disconnect");
		System.out.println(" ");

		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("Please enter a command:");
			String[] command = scanner.nextLine().split(" ");
			if (command.length == 4
					&& command[0].equalsIgnoreCase("fta-client")) {
				// connect request
				ftaClient(command[1], command[2], command[3]);
			} else if (command.length == 1
					&& command[0].equalsIgnoreCase("disconnect")) {
				// disconnect request
				disconnect();
			} else if (command.length == 1
					&& command[0].equalsIgnoreCase("connect")) {
				// disconnect request
				connect();
			} else if (command.length == 2) {
				if (command[0].equalsIgnoreCase("get")) {
					// download file from server
					getFile(command[1]);
				} else if (command[0].equalsIgnoreCase("post")) {
					// upload file to server
					postFile(command[1]);
				} else if (command[0].equalsIgnoreCase("window")) {
					// upload file to server
					changeWindow(command[1]);
				}
			}
		}

	}

	/**
	 * Attempt connection to server
	 * 
	 * @param ip
	 *            address of remote server
	 * @param prt
	 *            port of remote server
	 */
	public static void ftaClient(String clntPrt, String ip, String prt) {
		try {
			host = InetAddress.getByName(ip);
			port = Integer.parseInt(prt);
			// port = Integer.parseInt(clntPrt) + 1; // comment out if using emulator
			clientPort = Integer.parseInt(clntPrt);
			System.out.println("Settings add. Type connect to establish a connection.");
		} catch (Exception e) {
			System.out.println("Error connecting: " + e);
		}
	}

	public static void connect() {
		if (connected) {
			System.out.println("Already connected to server " + host + ":" + port);
			return;
		}
		try {
			socket = new DatagramSocket(clientPort);
			FIVRHeader header = new FIVRHeader(clientPort, port,
					PACKET_SEQUENCE_NUM, -1, 0, 1, 1, 0,
					0, 0, 0, 1, 0, 0, 0);
			PACKET_SEQUENCE_NUM++;
			FIVRPacket requestPacket = new FIVRPacket(header, new byte[0]);
			requestPacket.header.setChecksum(FIVRChecksum.generateChecksum(requestPacket.getBytes(false)));//generate checksum for connection request packet
			DatagramPacket packet2 = new DatagramPacket(
					requestPacket.getBytes(true), requestPacket.getBytes(true).length,
					host, port);

			boolean gotResponse = false;
			int tries = 0;

			while (!gotResponse) {
				try {
					if (tries > FIVRTransactionManager.max_tries) {
						System.out.println("Tried to connect to server, but no luck.");
						return;
					}
					socket.setSoTimeout(RTT_TIMEOUT);
					socket.send(packet2);
					DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE, host, port);
					socket.receive(packet);
					
					FIVRPacket receivedPacket = FIVRPacketManager.depacketize(packet);
					
					if(!FIVRPacketManager.isPacketCorrupt(receivedPacket))//check for corruption
					{
						if (FIVRPacketManager.depacketize(packet).header.recvToSendAck == 1) {
							gotResponse = true;
						}
					}
					
					
				} catch (Exception e) {}
				tries++;
				RTT_TIMEOUT += 50;
			}
			
			connected = true;
			System.out.println("Connected to server!");
			return;

		} catch (Exception e) {
			System.out.println("Could not connect to server. Error: " + e.getMessage());
			return;
		}
	}

	public static void changeWindow(String size) {
		WINDOW_SIZE = Integer.parseInt(size);
		System.out.println("Window size changed to: " + WINDOW_SIZE);
	}

	/**
	 * Disconnect socket connection
	 */
	public static void disconnect() {
		if (!connected) {  
			System.out.println("Needs to be connected before disconnecting.");
			return;
		}
		connected = false;
		socket.close();
		System.out.println("Connection closed.");
	}

	/**
	 * Attempt to retrieve file from remote server
	 * 
	 * @param file
	 *            name of file to retrieve
	 * @return
	 */
	public static boolean getFile(String file) {
		if (!connected) {
			System.out.println("Connect to server first.");
			return false;
		}
		
		DatagramPacket downloadReqDatagram = null;
		try {
			// send download request packet
			byte[] data = file.getBytes("UTF-8");
			FIVRHeader header = new FIVRHeader(clientPort, port,
					PACKET_SEQUENCE_NUM, -1, -1, WINDOW_SIZE, 0, 0,
					0, 0, 0, WINDOW_SIZE, 1, 0, 0);
			PACKET_SEQUENCE_NUM++;
			FIVRPacket requestPacket = new FIVRPacket(header, data);
			requestPacket.header.setChecksum(FIVRChecksum.generateChecksum(requestPacket.getBytes(false)));
			
			downloadReqDatagram = new DatagramPacket(
					requestPacket.getBytes(true), requestPacket.getBytes(true).length,
					host, port);
			
		} catch (Exception e) {
			System.out.println("Could not get that file. Error: " + e);
			return false;
		}
		
		int too_many_tries = 0;
		boolean gotOpenBracketPacket = false;
		FIVRPacket fPacket = null;
		while (!gotOpenBracketPacket) {
			try {
				if (too_many_tries > FIVRTransactionManager.max_tries) {
					System.out.println("Could not get that file. Error: Request response never received.");
					return false;
				}
				
				socket.send(downloadReqDatagram);//resends request for download since we haven't gotten the open bracket yet
				
				//socket.setSoTimeout(RTT_TIMEOUT);
				DatagramPacket datagram = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
				socket.receive(datagram);
				fPacket = FIVRPacketManager.depacketize(datagram);
				
				if(!FIVRPacketManager.isPacketCorrupt(fPacket))
				{
					if(fPacket.header.fileOpenBracket == 1)
					{
						gotOpenBracketPacket = true;
						RTT_TIMEOUT -= 50;
					}	
				}
				
			} catch (Exception e) {}
			too_many_tries++;
			RTT_TIMEOUT += 50;
		}
		
		ArrayList<FIVRPacket> data = FIVRTransactionManager.receiveAllPackets(socket, fPacket, host, port);
		if (data == null) {
			System.out.println("Did not receive file");
			return false;
		}
		
		int bytesNeeded = 0;
		for (int i = 0; i < data.size()-1; i++) {
			bytesNeeded += data.get(i).payload.length;
		}
		
		byte[] fileData = new byte[bytesNeeded];
		int index = 0;
		for (int i = 0; i < data.size()-1; i++) {
			for (int j = 0; j < data.get(i).payload.length; j++) {
				fileData[index] = data.get(i).payload[j];
				index++;
			}
		}
		
		try {
			String name = "client-" + FIVRTransactionManager.getLastReceivedFilename().trim();
			System.out.println("The file will be stored as " + name);
			Files.write(Paths.get(name), fileData);
		} catch (Exception e) {
			System.out.println("Could not save file locally on client; local error.");
			return false;
		}
		
		return true;
	}

	/**
	 * Attempt to upload file to remote server
	 * 
	 * @param file
	 *            name of file to upload
	 * @return
	 */
	public static boolean postFile(String filename) {
		if (!connected) {
			System.out.println("Connect to server first.");
			return false;
		}
		int result = FIVRTransactionManager.sendAllPackets(filename, socket, host, port, PACKET_SEQUENCE_NUM);
		if (result == -1) {
			System.out.println("Failed to send file.");
			return false;
		} else {
			System.out.println("File successfully uploaded!");
			PACKET_SEQUENCE_NUM = result;
			return true;
		}
	}

}
