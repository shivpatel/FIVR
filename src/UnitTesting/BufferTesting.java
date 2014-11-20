package UnitTesting;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import FIVRModules.FIVRBuffer;
import FIVRModules.FIVRHeader;
import FIVRModules.FIVRPacket;
import FIVRModules.FIVRPacketManager;

public class BufferTesting {

	@Test
	public void packetOutOfOrder() throws IOException 
	{
		FIVRBuffer buffer = new FIVRBuffer(10, 0);
		ArrayList<FIVRPacket> packets = FIVRPacketManager.packetize("C:\\Users\\timke_000\\git\\FivR - new\\FIVR\\Lenna.png", 1234, 1235, 0, 512, 10, 0, 0);
		
		buffer.addPacket(packets.get(2));
		buffer.addPacket(packets.get(0));
		buffer.addPacket(packets.get(1));
		
		assertTrue(buffer.getBuffer().get(0).header.seqNum == 0);
		assertTrue(buffer.getBuffer().get(1).header.seqNum == 1);
		assertTrue(buffer.getBuffer().get(1).header.seqNum == 1);
	}
	
	@Test
	public void packetAddedOutOfRange() throws IOException
	{
		FIVRBuffer buffer = new FIVRBuffer(10, 0);
		ArrayList<FIVRPacket> packets = FIVRPacketManager.packetize("C:\\Users\\timke_000\\git\\FivR - new\\FIVR\\Lenna.png", 1234, 1235, 0, 512, 10, 0, 0);
		
		buffer.addPacket(packets.get(0));
		buffer.addPacket(packets.get(1));
		buffer.addPacket(packets.get(50));
		
		assertTrue(buffer.getBuffer().size() == 2);
	}
	
	@Test
	public void packetCorrupt()
	{
		
	}
	
	

}
