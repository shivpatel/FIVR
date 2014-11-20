package FIVRServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import FIVRModules.*;

/**
 * 
 * @author shiv
 *
 */
public class ServiceHandler implements Runnable {

	public static String name;
	public static DatagramSocket socket;
	public static String logOutput;

	public static int WINDOW_THRESHOLD = 25; // 25 max
	public static int WINDOW_SIZE = 5; // 5 packets per window
	public static int PACKET_SIZE = 200; // 200 bytes
	public static int BUFFER_SIZE = 1000; // 1000 bytes
	public static int RTT_TIMEOUT = 2000; // 2 sec.
	public static int CONNECTION_TIMEOUT = 30000; // 30 sec.
	public static int PACKET_SEQUENCE_NUM = 0;

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
								new byte[PACKET_SIZE], PACKET_SIZE);
						socket.receive(packet); // receiving packet

						FIVRPacket pkt = FIVRPacketManager.depacketize(packet);
						if (pkt.header.ConnectRequest) {
							System.out
									.println("Connection request packet arrived.");
							handleConnectRequset(pkt);
						} else if (pkt.header.isDownload
								&& pkt.header.fileOpenBracket) {
							System.out
									.println("Download file packet request arrived.");
							handleDownloadRequset(pkt);
						} else if (!pkt.header.isDownload
								&& pkt.header.fileOpenBracket) {
							System.out
									.println("Upload file packet request arrived.");
							handleUploadRequset(pkt);
						} else if (pkt.header.fileOpenBracket) {
							System.out
									.println("Open bracket packet arrived; unknown action next.");
						} else {
							System.out.println("Unknown packet arrived.");
						}

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
			e.printStackTrace();
		}
	}

	public void handleConnectRequset(FIVRPacket packet) {

	}

	public void handleDownloadRequset(FIVRPacket packet) {

	}

	public void handleUploadRequset(FIVRPacket packet) {

	}

}