package FIVRModules;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Test 
{
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		ArrayList<FIVRPacket> packets = FIVRPacketManager.packetize("C:\\Users\\timke_000\\git\\FivR - new\\FIVR\\Lenna.png", (short)1234, (short)1235, 0, 512, 10, 0, false);
		
		FIVRPacket inputFIVRPacket = packets.get(1);
		byte[] bytes = inputFIVRPacket.getBytes();
		
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
		
		FIVRPacket outputFIVRPacket = FIVRPacketManager.depacketize(packet);
		
		System.out.println(packets.size());
	}
}
