package FIVRServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import FIVRModules.*;

public class ServiceHandler implements Runnable {

	public static String name;
	public static int PACKETSIZE = 200;
	public static DatagramSocket socket;
	public static String logOutput;

	public ServiceHandler(String x) {
		name = x;
	}

	@Override
	public void run() {
		try {
			while (true) {

				logOutput = "";

				if (Server.started == true) {

					if (Server.initializeState == true) {
						// do initialization for server
						socket = new DatagramSocket(Server.serverPort);
						logOutput = "Server is ready...";
						Server.initializeState = false;
					} else {
						// continue normal operation
						DatagramPacket packet = new DatagramPacket(
								new byte[PACKETSIZE], PACKETSIZE);
						socket.receive(packet); // receiving packet

						// do stuff with packet here

						// log stuff here if needed
						logOutput = "Received from " + packet.getAddress()
								+ ":" + packet.getPort() + ", data: "
								+ new String(packet.getData());
						
						// save file to current directory
						FIVRFile toSave = new FIVRFile(packet.getData());

						// create response and send back to client here
						socket.send(packet); // response
					}

				}

				if (!logOutput.equals("")) {
					File outFile = new File("server-output.txt");
					FileWriter fWriter = new FileWriter(outFile, true);
					PrintWriter pWriter = new PrintWriter(fWriter);
					pWriter.println(logOutput);
					pWriter.close();
				}

			}
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}
	}

}