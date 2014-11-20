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
		ArrayList<FIVRPacket> packets = FIVRPacketManager.packetize("C:\\Users\\timke_000\\git\\FivR - new\\FIVR\\Lenna.png", (short)1234, (short)1235, 0, 512, 10, 0, false);
	}
	
	@Test
	public void packetCorrupt()
	{
		
	}
	
	

}
