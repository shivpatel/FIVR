package FIVRServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServiceHandler implements Runnable {

	String name;
	int PACKETSIZE = 30;
	DatagramSocket socket;
	String logOutput;

	public ServiceHandler(String x) {
		name = x;
	}

	@Override
	public void run() {
		try {
			while (true) {

				logOutput = "";

				if (ServerCaptain.started == true) {

					if (ServerCaptain.initializeState == true) {
						// do initialization for server
						socket = new DatagramSocket(ServerCaptain.serverPort);
						logOutput = "Server is ready...\n";
						ServerCaptain.initializeState = false;
					} else {
						// continue normal operation
						DatagramPacket packet = new DatagramPacket(
								new byte[PACKETSIZE], PACKETSIZE);
						socket.receive(packet); // receiving packet

						// do stuff with packet here

						// log stuff here if needed
						logOutput = packet.getAddress() + " "
								+ packet.getPort() + ": "
								+ new String(packet.getData()) + "\n";

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