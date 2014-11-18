package FIVRModules;

import java.util.ArrayList;

/**
 * Maintains the buffered packets inside the window range and performs basic bookkeeping tasks and error checking.
 */
public class FIVRBuffer 
{
	private ArrayList<FIVRPacket> buffer;
	/**
	 * Instantiates FIVR Buffer used to buffer incoming packets in the window range
	 * @param bufferSize Size in bytes of the buffer in memory
	 * @param window Current packet window size (number of packets being sent at once)
	 * @param start Starting packet sequence number (inclusive)
	 * @param end Ending packet sequence number (inclusive)
	 */
	public FIVRBuffer(int bufferSize, int window, int start, int end)
	{
		buffer = new ArrayList<FIVRPacket>();
		
		
	}
	
	/**
	 * Adds a FIVRPacket to the buffer and performs error checking
	 * @param packet FIVRPacket to be added to the buffer
	 * @return True if the packet being added to the buffer is not corrupt, otherwise false. (Used to signal that a NACK should be sent for this packet)
	 */
	public boolean addPacket(FIVRPacket packet)
	{
		//TODO add packet to the inner ArrayList. return whether or not the packet was added to the list (packet in window range and not corrupt)
		return false;
	}
	
	/**
	 * Checks to see if all the packets in the window have arrived and been buffered
	 * @return True if all packets in window range have been buffered, false otherwise
	 */
	public boolean isFull()
	{
		//TODO return whether or not the buffers window range is full
		return false;
	}
	
	/**
	 * 
	 * @return ArrayList of FIVRPackets contained in the buffer
	 */
	public ArrayList<FIVRPacket> getBuffer()
	{
		return buffer;
	}
}
