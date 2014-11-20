package FIVRClient;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

import FIVRModules.*;

public class Client {

	public static DatagramSocket socket = null;
	public static InetAddress host;
	public static int port;
	public static int clientPort;

	public static int WINDOW_THRESHOLD = 25; // 25 max
	public static int WINDOW_SIZE = 5; // 5 packets per window
	public static int PACKET_SIZE = 200; // 200 bytes
	public static int BUFFER_SIZE = 1000; // 1000 bytes
	public static int RTT_TIMEOUT = 2000; // 2 sec.
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

			port = Integer.parseInt(clntPrt); // comment out if using emulator

			clientPort = Integer.parseInt(clntPrt);
			socket = new DatagramSocket(clientPort);
			System.out.println("Connected to server!");
		} catch (Exception e) {
			System.out.println("Error connecting: " + e);
		}
	}

	public static void connect() {
		// do handshake here
	}

	public static void changeWindow(String size) {
		return;
	}

	/**
	 * Disconnect socket connection
	 */
	public static void disconnect() {
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
		try {
			byte[] data = file.getBytes();
			FIVRHeader header = new FIVRHeader();
		} catch (Exception e) {
			System.out.println("Error getting file: " + e);
		}
		return false;
	}

	/**
	 * Attempt to upload file to remote server
	 * 
	 * @param file
	 *            name of file to upload
	 * @return
	 */
	public static boolean postFile(String filename) {
		try {
			System.out.println("Sending file to at " + host + ":" + port);

			ArrayList<FIVRPacket> packetsToSend = FIVRPacketManager.packetize(
					filename, (short) clientPort, (short) port,
					PACKET_SEQUENCE_NUM, PACKET_SIZE, WINDOW_SIZE, WINDOW_SIZE,
					false);

			DatagramPacket packet = null;
			int i = 0;
			int tmpSetSeqStartNum = PACKET_SEQUENCE_NUM;
			int attempts = 0;
			PACKET_SEQUENCE_NUM += packetsToSend.size();

			while (i < packetsToSend.size()) {

				if (attempts > 10) {
					System.out.println("Failed to send file too many times.");
					return false;
				}

				for (int j = 0; j < WINDOW_SIZE; j++) {
					try {
						FIVRPacket cur = packetsToSend.get(i);
						packet = new DatagramPacket(cur.getBytes(),
								cur.getBytes().length, host, port);
						socket.send(packet);
						System.out.println("Sent packet #" + i);
						i++;
					} catch (Exception e) {
						System.out.println("Error sending package: "
								+ e.getMessage());
						// Window Size > Packets to Send
						// Server should detect "end bracket" packet
					}
				}

				socket.setSoTimeout(RTT_TIMEOUT);
				packet = new DatagramPacket(new byte[100], 100, host, port);

				try {
					socket.receive(packet);
					FIVRPacket responseFIVRPacket = FIVRPacketManager
							.depacketize(packet);
					// if got NACK or ACK is not for desired sequence number
					if (responseFIVRPacket.header.isNACK
							|| responseFIVRPacket.header.ack != (i + tmpSetSeqStartNum)) {
						i = i - WINDOW_SIZE;
						WINDOW_THRESHOLD = (WINDOW_THRESHOLD / 2) + 1;
						WINDOW_SIZE = WINDOW_THRESHOLD;
					} else {
						// i stays the same
						if (WINDOW_SIZE < WINDOW_THRESHOLD) {
							WINDOW_SIZE = (WINDOW_SIZE * WINDOW_SIZE) / 2;
						} else {
							WINDOW_SIZE++;
						}
					}
				} catch (SocketTimeoutException e) { // ACK/NACK Response Timed
														// Out
					attempts++;
					i = i - WINDOW_SIZE; // reset i to start of set
					WINDOW_THRESHOLD = 25;
					WINDOW_SIZE = 5;
				} catch (EOFException e) { // Null Received in Response
					attempts++;
					i = i - WINDOW_SIZE; // reset i to start of set
					WINDOW_THRESHOLD = 25;
					WINDOW_SIZE = 5;
				}

			}

			return true;
		} catch (Exception e) {
			System.out.println("Failed to send file. Error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

}
