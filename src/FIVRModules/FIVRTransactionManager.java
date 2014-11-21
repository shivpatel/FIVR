package FIVRModules;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import FIVRServer.Server;

public class FIVRTransactionManager {
	
	private static String last_received_filename = "";
	
	/**
	 * Returns last filename detected from receiveAllPackets function. Erases values after being called.
	 * @return
	 */
	public static String getLastReceivedFilename() {
		String tmp = last_received_filename;
		last_received_filename = "";
		return tmp;
	}

	/**
	 * Return byte array of all data received for transaction; returns null if error. Method assumes you've already viewed the open bracket packet.
	 * @param socket Socket to receive all packets on
	 * @param packet Initial open bracket packet
	 * @return
	 */
	public static ArrayList<FIVRPacket> receiveAllPackets(DatagramSocket socket, FIVRPacket packet) {
		ArrayList<FIVRPacket> data = new ArrayList<FIVRPacket>();
		try {
			
			// FIVR Rules Initialization
			int segment_size = 512;
			
			// Receive Session Initialization
			int timeout_attempts = 0;
			int packets_to_go = 0;
			int prev_seq_num = 0;
			int rtt_timeout = 2000;
			DatagramPacket datagram = new DatagramPacket(new byte[segment_size], segment_size);

			timeout_attempts = 0;
			
			String tmp = new String(packet.payload) + "";
			packets_to_go = Integer.parseInt(tmp.trim());
			int total_packets_count = packets_to_go;
			// System.out.println("Total Packets: " + total_packets_count);
			packets_to_go--;
			prev_seq_num = packet.header.seqNum;
			
			while (packets_to_go > 0) {
				int current_window_size = packet.header.windowSize;
				int buffer_size = current_window_size;
				if (packets_to_go < current_window_size) buffer_size = packets_to_go;
				FIVRBuffer buffer = new FIVRBuffer(buffer_size,prev_seq_num+1);
				while (!buffer.isFull()) {
					if (timeout_attempts > 10) throw new Exception("Could not receive anymore packets.");
					try {
						datagram = new DatagramPacket(new byte[segment_size], segment_size);
						socket.receive(datagram);
						socket.setSoTimeout(rtt_timeout);
						packet = FIVRPacketManager.depacketize(datagram);
						// System.out.println("Packet's # is "+ packet.header.seqNum);
						if(total_packets_count == packet.header.seqNum) {
							packets_to_go--;
							last_received_filename = new String(packet.payload);
							break;
						}
						if(!buffer.addPacket(packet)) {
							// send NACK
							sendAckNackResponse(socket,datagram.getAddress(),packet.header.sourcePort,-1,-1,true);
							// System.out.println("Sending a NACK.");
							// System.out.println("Packets to go is: " + packets_to_go + " and buffer size is: " + buffer_size);
							// System.out.println("Looking for packets in range of: " + (prev_seq_num+1) + " to " + (prev_seq_num+1+buffer_size));
						}
					} catch (Exception e) {
						e.printStackTrace();
						timeout_attempts++;
					}
				}
				// System.out.println("Sending an ACK for " + (prev_seq_num+current_window_size+1));
				sendAckNackResponse(socket,datagram.getAddress(),packet.header.sourcePort,-1,prev_seq_num+current_window_size+1,false);
				timeout_attempts = 0;
				prev_seq_num = prev_seq_num + current_window_size;
				packets_to_go = packets_to_go - buffer_size;
				data.addAll(buffer.getBuffer());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("File (" + last_received_filename + ") successfully received.");
		return data;
	}
	
	/**
	 * Returns -1 if failed, else returns next sequence number that can be used.
	 * @param filename File to be sent
	 * @param socket Socket to send packets over
	 * @param sendToHost Receiving host
	 * @param sendToPort Receiving host's port
	 * @param seqNum Start sequence number to use
	 * @return
	 */
	public static int sendAllPackets(String filename, DatagramSocket socket, InetAddress sendToHost, int sendToPort, int seqNum) {
		try {
			// FIVR Rules Initialization
			int window_size = 5;
			int segment_size = 512;
			int threshold = 25;
			int rtt_timeout = 1000;
			// Send Session Initialization
			ArrayList<FIVRPacket> toSend = null;
			try {
				toSend = FIVRPacketManager.packetize(filename.trim(),socket.getLocalPort(),sendToPort,seqNum,segment_size,window_size,window_size,0);
			} catch (Exception e) {
				return -1;
			}
			int i = 0;
			int timeout_attempts = 0;
			DatagramPacket datagram = null;
			FIVRPacket packet = null;
			// Initial open bracket packet should have # packets in entire file batch as String in body
			toSend.get(i).payload = (new String("" + toSend.size())).getBytes();
			toSend.get(toSend.size()-1).payload = (filename.getBytes());
			System.out.println("Got here 4.");
			// Send open bracket packet by itself at first
			boolean sentOpenBracketPacket = false;
			while (!sentOpenBracketPacket) {
				if (timeout_attempts >= 10) return -1;
				try {
					packet = toSend.get(i);
					datagram = new DatagramPacket(packet.getBytes(true),packet.getBytes(true).length,sendToHost,sendToPort);
					socket.send(datagram);
					System.out.println("Packet #" + (seqNum + i) + " sent.");	
					i++;
					timeout_attempts = 0;
					sentOpenBracketPacket = true;
				} catch (Exception e) {
					// could not send open bracket
					timeout_attempts++;
				}
			}
			
			// Packet Sender
			while (i < toSend.size()) {
				
				// check timeout attempts
				if (timeout_attempts >= 10) return -1;
				
				// send set of packets
				for (int j = 0; j < window_size; j++) {
					try {
						packet = toSend.get(i);
						datagram = new DatagramPacket(packet.getBytes(true),packet.getBytes(true).length,sendToHost,sendToPort);
						socket.send(datagram);
						// System.out.println("Packet #" + (seqNum + i) + " sent.");	 
					} catch (Exception e) {
						// failed to send packet
					}
					i++;
				}
				
				try {
					// check for response
					socket.setSoTimeout(rtt_timeout);
					datagram = new DatagramPacket(new byte[segment_size],segment_size,sendToHost,sendToPort);
					socket.receive(datagram);
					packet = FIVRPacketManager.depacketize(datagram);
					
					// analyze response 
					if (packet.header.isNACK == 1 || packet.header.ack != (seqNum+i)) {
						i = i - window_size;
						threshold = (threshold / 2) + 1;
						//window_size = threshold;
					} else {
						timeout_attempts = 0;
						if (window_size < threshold) {
							//window_size = (window_size * window_size) / 2;
						} else {
							//window_size++;
						}
					}	
				} catch (Exception e) {
					timeout_attempts++;
					i = i - window_size;
					threshold = 25;
					//window_size = 5;
				}
				
			}
			
			// Return
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
