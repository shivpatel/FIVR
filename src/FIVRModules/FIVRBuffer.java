package FIVRModules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Maintains the buffered packets inside the window range and performs basic
 * bookkeeping tasks and error checking.
 */
public class FIVRBuffer {
	private ArrayList<FIVRPacket> buffer;
	public int window;
	public int start;

	/**
	 * Instantiates FIVR Buffer used to buffer incoming packets in the window
	 * range
	 * 
	 * @param window
	 *            Current packet window size (number of packets being sent at
	 *            once)
	 * @param start
	 *            Sequence number of the first packet in the range (inclusive)
	 */
	public FIVRBuffer(int window, int start) {
		buffer = new ArrayList<FIVRPacket>();
		this.window = window;
		this.start = start;
	}

	/**
	 * Adds a FIVRPacket to the buffer and performs error checking. If function
	 * returns false, then a NACK should be send for the packet.
	 * 
	 * @param packet
	 *            FIVRPacket to be added to the buffer
	 * @return True if the packet being added to the buffer is not corrupt,
	 *         otherwise false. (Used to signal whether or not a NACK should be
	 *         sent for this packet)
	 * @throws IOException
	 */
	public boolean addPacket(FIVRPacket packet) throws IOException {
		int checksum = FIVRChecksum.generateChecksum(packet.getBytes(false));

		if (checksum != packet.header.getChecksum())// packet is corrupt
		{
			// checksum is not working on last packet so I am putting this in
			// temporarily
			if (packet.header.fileClosingBracket != 1)
				return false;
		}

		if (packet.header.seqNum >= start
				&& packet.header.seqNum < start + window)// packet in window
															// range
		{

			// check to see if packet already exists in the buffer (duplicate
			// packet)
			for (int i = 0; i < buffer.size(); i++) {
				if (buffer.get(i).header.seqNum == packet.header.seqNum) {
					return false;
				}
			}

			buffer.add(packet);
			Collections.sort(buffer);// sorts packets in the buffer on sequence
										// number
		}

		return true;
	}

	/**
	 * Checks to see if all the packets in the window have arrived and been
	 * buffered
	 * 
	 * @return True if all packets in window range have been buffered, false
	 *         otherwise
	 */
	public boolean isFull() {
		return (buffer.size() == window);
	}

	/**
	 * ArrayList of FIVRPackets contained in the buffer
	 * 
	 * @return ArrayList of FIVRPackets contained in the buffer
	 */
	public ArrayList<FIVRPacket> getBuffer() {
		return buffer;
	}
}
