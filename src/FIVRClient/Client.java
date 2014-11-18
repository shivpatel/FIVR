package FIVRClient;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import FIVRModules.*;

public class Client {

	public static int PACKETSIZE = 200;
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
		System.out.println("> getfile [file]");
		System.out.println("> postfile [file]");
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
				if (command[0].equalsIgnoreCase("getfile")) {
					// download file from server
					getFile(command[1]);
				} else if (command[0].equalsIgnoreCase("postfile")) {
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
			socket = new DatagramSocket(Integer.parseInt(prt) - 1);
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
			// request file from DB
			// response should be file
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
			FIVRFile file = new FIVRFile(filename);
			DatagramPacket packet = new DatagramPacket(file.getData(),
					file.getData().length, host, port);
			socket.send(packet);
			socket.setSoTimeout(2000);
			// empty out packet, for response
			packet.setData(new byte[PACKETSIZE]);
			socket.receive(packet);
			// System.out.println("Server Response: "
			// + new String(packet.getData()));
			return true;
		} catch (Exception e) {
			System.out.println("Failed to send file. Error: " + e.getMessage());
			return false;
		}
	}

}
