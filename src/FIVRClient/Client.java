package FIVRClient;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {

	public static int PACKETSIZE = 100;
	public static DatagramSocket socket = null;
	public static InetAddress host;
	public static int port;

	/**
	 * Get user commands
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("*** FIVR Client ***");
		System.out.println(" ");
		System.out.println("Available Commands:");
		System.out.println("> connect [address] [port]");
		System.out.println("> get-file [file]");
		System.out.println("> post-file [file]");
		System.out.println("> disconnect");
		System.out.println(" ");

		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("Please enter a command:");
			String[] command = scanner.nextLine().split(" ");
			if (command.length == 3 && command[0].equalsIgnoreCase("connect")) {
				// connect request
				connect(command[1], command[2]);
			} else if (command.length == 1
					&& command[0].equalsIgnoreCase("disconnect")) {
				// disconnect request
				disconnect();
			} else if (command.length == 2) {
				if (command[0].equalsIgnoreCase("get-file")) {
					// download file from server
					getFile(command[1]);
				} else if (command[0].equalsIgnoreCase("post-file")) {
					// upload file to server
					postFile(command[1]);
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
	public static void connect(String ip, String prt) {
		try {
			host = InetAddress.getByName(ip);
			port = Integer.parseInt(prt);
			socket = new DatagramSocket();
			System.out.println("Connected to server!");
		} catch (Exception e) {
			System.out.println("Error connecting: " + e);
		}
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
			DatagramPacket packet = new DatagramPacket(data, data.length, host,
					port);
			socket.send(packet);
			socket.setSoTimeout(2000);
			packet.setData(new byte[PACKETSIZE]);
			socket.receive(packet);
			System.out.println(new String(packet.getData()));
		} catch (Exception e) {
			System.out.println("Error getting file: " + e);
		}
		return false;
	}

	/**
	 * Attempt to uplaod file to remote server
	 * 
	 * @param file
	 *            name of file to upload
	 * @return
	 */
	public static boolean postFile(String file) {
		File transferFile = new File(file);
		byte[] bytes = new byte[(int) transferFile.length()];
		// send bytes array from above
		return false;
	}

}
