package FIVRServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

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
	public static int PACKET_SIZE = 512; // 512 bytes
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
							handleConnectRequset(pkt, packet);
						} else if (pkt.header.isDownload
								&& pkt.header.fileOpenBracket) {
							System.out
									.println("Download file packet request arrived.");
							handleDownloadRequset(pkt, packet);
						} else if (!pkt.header.isDownload
								&& pkt.header.fileOpenBracket) {
							System.out
									.println("Upload file packet request arrived.");
							handleUploadRequset(pkt, packet);
						} else if (pkt.header.fileOpenBracket) {
							System.out
									.println("Open bracket packet arrived; unknown action next.");
						} else {
							System.out.println("Unknown packet arrived.");
						}

						// // log stuff here if needed
						// logOutput = "Received from " + packet.getAddress()
						// + ":" + packet.getPort() + ", data: "
						// + new String(packet.getData());
						//
						// // save file to current directory
						// FIVRFile toSave = new FIVRFile(packet.getData());
						//
						// // create response and send back to client here
						// socket.send(packet); // response
					}

				}

//				if (!logOutput.equals("")) {
//					File outFile = new File("server-output.txt");
//					FileWriter fWriter = new FileWriter(outFile, true);
//					PrintWriter pWriter = new PrintWriter(fWriter);
//					pWriter.println(logOutput);
//					pWriter.close();
//				}

			}
		} catch (Exception e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}

	public void handleConnectRequset(FIVRPacket packet, DatagramPacket datagram) {
		try {
			System.out.println("Processing incoming connection request.");
			FIVRHeader header = new FIVRHeader(Server.serverPort,
					packet.header.sourcePort, PACKET_SEQUENCE_NUM, -1, -1,
					WINDOW_SIZE, true, false, false, false, true, WINDOW_SIZE,
					false, false, false);
			PACKET_SEQUENCE_NUM++;
			FIVRPacket response = new FIVRPacket(header, new byte[0]);
			DatagramPacket resposneDG = new DatagramPacket(response.getBytes(),
					response.getBytes().length, Server.host,
					Server.emulatorPort);
			socket.send(resposneDG);
			System.out.println("Client from " + datagram.getSocketAddress()
					+ " connected!");
		} catch (Exception e) {
			System.out.println("Client attempted to connect, but failed.");
		}
	}

	public void handleUploadRequset(FIVRPacket packet, DatagramPacket datagram) {

		System.out.println("Client sending file to server...");
		FIVRPacket current = null;
		ArrayList<FIVRPacket> filePackets = new ArrayList<FIVRPacket>();
		DatagramPacket tmpPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
		int remote_window_size = packet.header.windowSize;
		int remote_seq_start_num = packet.header.seqNum;
		boolean isLastPacket = false;

		System.out.println("Remote window size is: " + remote_window_size);
		while (!isLastPacket) {
			try {
				
				FIVRBuffer tmp = new FIVRBuffer(remote_window_size,
						remote_seq_start_num);
				
				while (!tmp.isFull()) {
					socket.receive(tmpPacket);
					current = FIVRPacketManager.depacketize(tmpPacket);
					isLastPacket = current.header.fileClosingBracket;
					boolean wasAdded = tmp.addPacket(current);
					if (!wasAdded) {
						
						// send NACK
						FIVRHeader header = new FIVRHeader(Server.serverPort,
								current.header.sourcePort, PACKET_SEQUENCE_NUM,
								remote_seq_start_num + remote_window_size - 1, -1,
								WINDOW_SIZE, false, false, true, false, false,
								WINDOW_SIZE, false, false, false);
						PACKET_SEQUENCE_NUM++;
						FIVRPacket response = new FIVRPacket(header, new byte[0]);
						DatagramPacket responseDG = new DatagramPacket(
								response.getBytes(), response.getBytes().length,
								Server.host, response.header.destPort);
						socket.send(responseDG);
						
					}
					remote_window_size = current.header.windowSize;
				}

				// SEND ACK (remote_seq_start_num + remote_window_size - 1)
				FIVRHeader header = new FIVRHeader(Server.serverPort,
						current.header.sourcePort, PACKET_SEQUENCE_NUM,
						remote_seq_start_num + remote_window_size - 1, -1,
						WINDOW_SIZE, false, false, false, false, false,
						WINDOW_SIZE, false, false, false);
				PACKET_SEQUENCE_NUM++;
				FIVRPacket response = new FIVRPacket(header, new byte[0]);
				DatagramPacket responseDG = new DatagramPacket(
						response.getBytes(), response.getBytes().length,
						Server.host, response.header.destPort);
				socket.send(responseDG);

				remote_seq_start_num = remote_seq_start_num
						+ remote_window_size - 1;
				
				filePackets.addAll(tmp.getBuffer());

			} catch (Exception e) {
				System.out
						.println("Error processing packet: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		System.out.println("File uploaded from client at"
				+ datagram.getSocketAddress());

	}

	public void handleDownloadRequset(FIVRPacket packet, DatagramPacket datagram) {

	}

}