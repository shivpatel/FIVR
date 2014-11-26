package FIVRModules;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import FIVRClient.Client;
import FIVRServer.Server;

public class FIVRTransactionManager {
	
	private static String last_received_filename = "";
	public static int max_tries = 2000;
	public static int rttTime = 200;
	public static int window_size_main = 5;
	public static int last_file_size = 0;
	
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
	 * Return FIVRPacket array list for transaction; returns null if error. Method assumes you've already got the open bracket packet.
	 * @param socket Socket to receive all packets on
	 * @param packet Initial open bracket packet
	 * @return
	 */
	public static ArrayList<FIVRPacket> receiveAllPackets(DatagramSocket socket, FIVRPacket packet, InetAddress sendToHost, int sendToPort) {
		ArrayList<FIVRPacket> data = new ArrayList<FIVRPacket>();
		try {
			
			// FIVR Rules Initialization
			int segment_size = 512;
			
			// Receive Session Initialization
			int timeout_attempts = 0;
			int packets_to_go = 0;
			int prev_seq_num = 0;
			int openPacketSeqNum = packet.header.seqNum;
			//int rtt_timeout = 200;
			DatagramPacket datagram = new DatagramPacket(new byte[segment_size], segment_size);

			timeout_attempts = 0;
			
			//String tmp = new String(packet.payload) + "";
			
			ByteBuffer byteBuffer = ByteBuffer.allocate(packet.payload.length);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.put(packet.payload);
			byteBuffer.position(0);
			
			//packets_to_go = Integer.parseInt(tmp.trim());
			int total_packets_count = byteBuffer.getInt();
			packets_to_go = total_packets_count;
			last_file_size = (total_packets_count * 512) / 1000;
			int first_packet_seq_num = packet.header.seqNum;
			// System.out.println("Total Packets: " + total_packets_count);
			packets_to_go--;
			prev_seq_num = packet.header.seqNum;
			
			
			//in the beginning, keeping sending an ack for the open bracket packet until you get a data packet back.
			//this indicates that the other side has seen that you have gotten the open bracket packet and you're now
			//ready to receive datapackets.
			
			boolean sendAckForSet = true;//keep from slamming the emu with packets, cause it queues them. Slow it down, champ.
			
			int antiEmulatorPummel = 0;
			
			// run until all packets received  
			while (packets_to_go > 0) {
				
				int current_window_size = packet.header.windowSize;
				int buffer_size = current_window_size;
				if (packets_to_go < current_window_size) buffer_size = packets_to_go;
				FIVRBuffer buffer = new FIVRBuffer(buffer_size,prev_seq_num+1);
				
				// while buffer isn't full
				while (!buffer.isFull()) {
					if (timeout_attempts > max_tries) throw new Exception("Could not receive anymore packets.");
					
					try {
						
						if(sendAckForSet && antiEmulatorPummel % window_size_main == 0)//keep sending ack for the set
						{
							sendAckNackResponse(socket, sendToHost, sendToPort, -1, prev_seq_num+1, false);
						}
						
						datagram = new DatagramPacket(new byte[segment_size], segment_size);
						
						//socket.setSoTimeout(rtt_timeout);
						socket.receive(datagram);
									
						packet = FIVRPacketManager.depacketize(datagram);
						
						buffer.addPacket(packet);
						
						if(!FIVRPacketManager.isPacketCorrupt(packet))
						{
							if(packet.payload != null && packet.payload.length != 0 && packet.header.seqNum > prev_seq_num)//received valid datapacket
							{
								sendAckForSet = false;
							}
							
							// determine if closed bracket packet received
							//if((total_packets_count+first_packet_seq_num-1) == packet.header.seqNum) {
							if(buffer.isFileTransferComplete())
							{
								packets_to_go = 0;
								ArrayList<FIVRPacket> tempBuff = buffer.getBuffer();
								last_received_filename = new String(tempBuff.get(tempBuff.size()-1).payload);//closing packet will ALWAYS be the last packet in the buffer if it does exist in the buffer      //new String(packet.payload);
								break;
							}
						}
						
						/*if(!buffer.addPacket(packet)) {
							// send NACK
							sendAckNackResponse(socket,sendToHost,sendToPort,-1,-1,true);
							// System.out.println("Sending a NACK.");
							// System.out.println("Packets to go is: " + packets_to_go + " and buffer size is: " + buffer_size);
							// System.out.println("Looking for packets in range of: " + (prev_seq_num+1) + " to " + (prev_seq_num+1+buffer_size));
						}*/
						
					} catch (Exception e) {
						
						timeout_attempts++;
						
					}
					antiEmulatorPummel++;
				}
				
				if(packets_to_go == 0)
				{
					System.out.println("Sending an ACK for " + (prev_seq_num+current_window_size+1));
					data.addAll(buffer.getBuffer());
					sendAckNackResponse(socket,sendToHost,sendToPort,-1,prev_seq_num+current_window_size+1,false);
					break;
				}
				else
				{
					// send ACK for set
					System.out.println("Sending an ACK for " + (prev_seq_num+current_window_size+1));
					sendAckForSet = true;
					//sendAckNackResponse(socket,sendToHost,sendToPort,-1,prev_seq_num+current_window_size+1,false);
					
					timeout_attempts = 0;
					prev_seq_num = prev_seq_num + current_window_size;
					packets_to_go = packets_to_go - buffer_size;
					
					// add to return array
					data.addAll(buffer.getBuffer());
				}
				
			}
			
		} catch (Exception e) {
			e.getMessage();
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
			int window_size = window_size_main;
			int segment_size = 512;
			int threshold = 25;
			//int rtt_timeout = 200;
			
			// Send Session Initialization
			ArrayList<FIVRPacket> toSend = null;
			try {
				toSend = FIVRPacketManager.packetize(filename.trim(),socket.getLocalPort(),sendToPort,seqNum,segment_size,window_size,window_size,0);
			} catch (Exception e) {
				return -1;
			}
			last_file_size = (toSend.size() * 512) / 1000;
			int i = 0;
			int timeout_attempts = 0;
			DatagramPacket datagram = null;
			FIVRPacket packet = null;
			
			// Initial open bracket packet should have # packets in entire file batch as String in body
			//toSend.get(i).payload = (new String("" + toSend.size())).getBytes();
			//toSend.get(toSend.size()-1).payload = (filename.getBytes());
			
			// Send open bracket packet by itself at first
			boolean sentOpenBracketPacket = false;
			while (!sentOpenBracketPacket) {
				if (timeout_attempts >= max_tries) return -1;
				try {
					packet = toSend.get(i);
					datagram = new DatagramPacket(packet.getBytes(true),packet.getBytes(true).length,sendToHost,sendToPort);
					socket.send(datagram);
					// System.out.println("Packet #" + (seqNum + i) + " sent.");
				}
				catch(Exception e){
					e.getMessage();
				}
				
				try
				{
					//listen for an ack for next seqnum indicating that the other end got the open bracket intact
					//socket.setSoTimeout(rtt_timeout);
					datagram = new DatagramPacket(new byte[segment_size],segment_size,sendToHost,sendToPort);
					socket.receive(datagram);
					FIVRPacket responsePacket = FIVRPacketManager.depacketize(datagram);
					
					if(!FIVRPacketManager.isPacketCorrupt(responsePacket))//make sure the packet is not corrupt
					{
						//make sure the packet is an ack for the open bracket packet we just sent
						if(responsePacket.header.isNACK == 0 && responsePacket.header.ack == packet.header.seqNum + 1)
						{
							i++;
							timeout_attempts = 0;
							sentOpenBracketPacket = true;
							
							Client.RTT_TIMEOUT -= 50;
						}
					}
					
				} 		
				catch (Exception e) {
					// could not send open bracket
					timeout_attempts++;
				}
			}
			
			// Packet Sender
			while (i < toSend.size()) {
				
				// check timeout attempts
				if (timeout_attempts >= max_tries) return -1;
				
				// send set of packets
				for (int j = 0; j < window_size; j++) {
					try {
						packet = toSend.get(i);
						datagram = new DatagramPacket(packet.getBytes(true),packet.getBytes(true).length,sendToHost,sendToPort);
						socket.send(datagram);
						i++;
						// System.out.println("Packet #" + (seqNum + i) + " sent.");	 
					} catch (Exception e) {
						// failed to send packet
					}
				}
				
				try {
					// check for response
					socket.setSoTimeout(Client.RTT_TIMEOUT);
					datagram = new DatagramPacket(new byte[segment_size],segment_size,sendToHost,sendToPort);
					socket.receive(datagram);
					packet = FIVRPacketManager.depacketize(datagram);
					
					// analyze response 
					//if (packet.header.isNACK == 1 || packet.header.ack != (seqNum+i)) {
					if (packet.header.isNACK == 1) {
						i = i - window_size;
						threshold = (threshold / 2) + 1;
						//window_size = threshold;
					} else {
						i = packet.header.ack - seqNum;
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
			
			FIVRHeader header = new FIVRHeader(socket.getLocalPort(),sendToPort,seqNum,ackNum,0,WindowSize,0,0,nackVal,0,0,WindowSize,0,0,0);
			FIVRPacket response = new FIVRPacket(header, new byte[0]);
			response.header.setChecksum(FIVRChecksum.generateChecksum(response.getBytes(false)));
			
			DatagramPacket datagram = new DatagramPacket(response.getBytes(true),response.getBytes(true).length,sendToHost,sendToPort);
			socket.send(datagram);
			return seqNum + 1;
		} catch (Exception e) {
			return -1;
		}
	}
	
	
	
}