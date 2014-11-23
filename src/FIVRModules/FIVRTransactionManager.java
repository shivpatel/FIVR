package FIVRModules;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import FIVRServer.Server;

public class FIVRTransactionManager {
	
	private static String last_received_filename = "";
	public static int max_tries = 100;
	
	/**
	 * Returns last filename detected from receiveAllPackets function. Erases values after being called.
	 * @return
	 */
	public static String getLastReceivedFilename() {
		String tmp = last_received_filename;
		last_received_filename = "";
		return tmp;
	}
	
	public static ArrayList<FIVRPacket> receiveAllPackets(DatagramSocket socket, FIVRPacket packet, InetAddress sendToHost, int sendToPort) {
		
		ArrayList<FIVRPacket> data = new ArrayList<FIVRPacket>();
		
		try {
						
			int segment_size = 512;
			int rtt_timeout = 200;
			int total_packets = Integer.parseInt((new String(packet.payload) + "").trim());
			DatagramPacket datagram = new DatagramPacket(new byte[segment_size], segment_size);
			
			FIVRBufferManager buffer = new FIVRBufferManager(packet.header.seqNum+1, total_packets-1, packet.header.windowSize);
						
			while (!buffer.isFull()) {
								
				datagram = new DatagramPacket(new byte[segment_size], segment_size);
				socket.setSoTimeout(rtt_timeout);
				socket.receive(datagram);
				packet = FIVRPacketManager.depacketize(datagram);
				
				int result = buffer.addToBuffer(packet);
				
				if (result == 2) {
					// System.out.println("Sending an ACK  " + buffer.sendAckFor());
					sendAckNackResponse(socket,sendToHost,sendToPort,-1,buffer.sendAckFor(),false);
				} else if (result == -1) {
					// System.out.println("Sending an NACK " + buffer.nextExpectedSeqNum());
					sendAckNackResponse(socket,sendToHost,sendToPort,-1,buffer.nextExpectedSeqNum(),true);
				} else if (result == 1) {
					// normal operations
				}
				
			}
						
			last_received_filename = buffer.extractFilename();
			data.addAll(buffer.getAllPackets());
			
		} catch (Exception e) {
			
			return null;
			
		}
		
		System.out.println("File (" + last_received_filename + ") successfully received.");
		return data;
		
	}
	
	public static int sendAllPackets(String filename, DatagramSocket socket, InetAddress sendToHost, int sendToPort, int seqNum) {

		try {
			
			int window_size = 5;
			int segment_size = 512;
			int rtt_timeout = 200;
			
			ArrayList<FIVRPacket> toSend = null;
			try {
				toSend = FIVRPacketManager.packetize(filename.trim(),socket.getLocalPort(),sendToPort,seqNum,segment_size,window_size,window_size,0);
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}

			int i = 0;
			int timeout_attempts = 0;
			DatagramPacket datagram = null;
			FIVRPacket packet = null;
			toSend.get(i).payload = (new String("" + toSend.size())).getBytes();
			toSend.get(toSend.size()-1).payload = (filename.getBytes());
			boolean sentOpenBracketPacket = false;
			
			while (!sentOpenBracketPacket) {
				
				if (timeout_attempts >= max_tries) {
					return -1;
				}
				
				try {
					packet = toSend.get(i);
					datagram = new DatagramPacket(packet.getBytes(true),packet.getBytes(true).length,sendToHost,sendToPort);
					socket.send(datagram);
					i++;
					timeout_attempts = 0;
					sentOpenBracketPacket = true;
				} catch (Exception e) {
					timeout_attempts++;
				}
			}
			
			while (i < toSend.size()) {
				
				if (timeout_attempts >= max_tries) {
					return -1;
				}
				
				for (int j = 0; j < window_size; j++) {
					try {
						packet = toSend.get(i);
						// System.out.println("Sending packet: #" + packet.header.seqNum);
						datagram = new DatagramPacket(packet.getBytes(true),packet.getBytes(true).length,sendToHost,sendToPort);
						socket.send(datagram);
						i++;
					} catch (Exception e) { }
				}
				
				try {

					socket.setSoTimeout(rtt_timeout);
					datagram = new DatagramPacket(new byte[segment_size],segment_size,sendToHost,sendToPort);
					socket.receive(datagram);
					packet = FIVRPacketManager.depacketize(datagram);
					
					while (packet.header.isNACK == 1) {
												
						try {
							
							// System.out.println("isNack: " + packet.header.isNACK + " ACK Num: " + packet.header.ack);
							
							// send desired NACK one
							// System.out.println("Sending packet: " + (packet.header.ack - seqNum));
							packet = toSend.get(packet.header.ack - seqNum);
							datagram = new DatagramPacket(packet.getBytes(true),packet.getBytes(true).length,sendToHost,sendToPort);
							socket.send(datagram);
						
							// get new NACK/ACK response
							socket.setSoTimeout(rtt_timeout);
							datagram = new DatagramPacket(new byte[segment_size],segment_size,sendToHost,sendToPort);
							socket.receive(datagram);
							packet = FIVRPacketManager.depacketize(datagram);
							
						} catch (Exception e) { }
						
					}
						
				} catch (Exception e) {
					timeout_attempts++;
				}
				
			}
			
			return seqNum + toSend.size();
			
		} catch (Exception e) {
			
			return -1;
			
		}
		
	}

	/**
	 * Return -1 if failed, else returns next sequence number that can be used.
	 * @param socket Socket to send response over.
	 * @param sendToHost Recipient host of response packet.
	 * @param sendToPort Recipient host's port.
	 * @param seqNum Next available sequence number to use.
	 * @param ackNum Acknowledgement number to attach to this message.
	 * @param isNack
	 * @return
	 */
	public static int sendAckNackResponse(DatagramSocket socket, InetAddress sendToHost, int sendToPort, int seqNum, int ackNum, boolean isNack) {
		try {
			int WindowSize = 1;
			int nackVal = 0;
			if (isNack) { nackVal = 1; }
			FIVRHeader header = new FIVRHeader(socket.getLocalPort(),sendToPort,seqNum,ackNum,-1,WindowSize,0,0,nackVal,0,0,WindowSize,0,0,0);
			FIVRPacket response = new FIVRPacket(header, new byte[0]);
			DatagramPacket datagram = new DatagramPacket(response.getBytes(true),response.getBytes(true).length,sendToHost,sendToPort);
			socket.send(datagram);
			return seqNum + 1;
		} catch (Exception e) {
			return -1;
		}
	}
	
	
	
}