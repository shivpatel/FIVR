package FIVRModules;

import java.io.IOException;
import java.util.ArrayList;

public class FIVRBufferManager {

	private FIVRBuffer tmpBuffer;
	private int size;
	private int start_seq_num;
	private ArrayList<FIVRPacket> all = new ArrayList<FIVRPacket>();

	/**
	 * @param start_seq_num Initial sequence number to be used (inclusive)
	 * @param size Total number of packets expected not including open bracket
	 */
	public FIVRBufferManager(int start_seq_num, int size, int window_size) {
		this.size = size;
		this.start_seq_num = start_seq_num;
		resetTempBuffer(window_size, start_seq_num);
	}
	
	private void resetTempBuffer(int window_size, int start_seq_num) {
		tmpBuffer = new FIVRBuffer(window_size, start_seq_num);
	}
	
	public int addToBuffer(FIVRPacket packet) throws IOException {
		System.out.println("Wants to add: " + packet.header.seqNum);
		if (tmpBuffer.addPacket(packet)) { 
			if (tmpBuffer.isFull() || packet.header.fileClosingBracket == 1) {
				all.addAll(tmpBuffer.getBuffer());
				int next_start_seq_num = tmpBuffer.getBuffer().get(tmpBuffer.getBuffer().size()-1).header.seqNum + 1;
				System.out.println("New packet window size: " + packet.header.windowSize);
				System.out.println("Next expected seq num: " + next_start_seq_num);
				resetTempBuffer(packet.header.windowSize, next_start_seq_num); 
				return 2;
			}
			return 1;
		} else {
			return -1;
		}
	}
	
	// 2 > send ACK
	// 1 > continue receiving packets normally
	// -1 > call getNextExpectedSeqNum() and send NACK for that #
	
	public boolean isFull() {
		return size == all.size();
	}
	
	public int sendAckFor() {
		return start_seq_num + all.size();
	}
	
	public int nextExpectedSeqNum() {
		ArrayList<Integer> seqNums = new ArrayList<Integer>();
		ArrayList<Integer> seqNums2 = new ArrayList<Integer>();
		ArrayList<FIVRPacket> existing_packets = tmpBuffer.getBuffer();
		for (int i = 0; i < tmpBuffer.window; i++) {
			seqNums.add(tmpBuffer.start + i);
		}
		for (int i = 0; i < existing_packets.size(); i++) {
			seqNums2.add(existing_packets.get(i).header.seqNum);
		}
		for (int i = 0; i < tmpBuffer.window; i++) {
			if (!seqNums2.contains(seqNums.get(i))) {
				return (int)seqNums.get(i);
			}
		}
		return -1;
	}
	
	public int packetReceivedCount() {
		return all.size();
	}
	
	public int packetsWaitingCount() {
		return size - all.size();
	}
	
	public int size() {
		return size;
	}
	
	public ArrayList<FIVRPacket> getAllPackets() {
		return all;
	}
	
	public String extractFilename() {
		String filename = new String(all.get(all.size()-1).payload) + "";
		all.remove(all.size()-1);
		return filename;
	}
	
}
