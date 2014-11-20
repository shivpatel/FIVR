package FIVRModules;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Test 
{
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		ArrayList<FIVRPacket> packets = FIVRPacketManager.packetize("Lenna.png", (short)1234, (short)1235, 0, 512, 10, 0, false);
		
		FIVRPacket inputFIVRPacket = packets.get(1);
		byte[] bytes = inputFIVRPacket.getBytes();
		
		byte[] fullBytes = new byte[bytes.length + 150];
		//artificially add some zeros at the end like it would be in socket.receive()
		for(int i = 0; i < bytes.length + 150; i++)
		{
			if(i < bytes.length)
			{
				fullBytes[i] = bytes[i];
			}
			else
			{
				fullBytes[i] = 0;
			}
		} 
		
		DatagramPacket packet = new DatagramPacket(fullBytes, fullBytes.length);
		
		FIVRPacket outputFIVRPacket = FIVRPacketManager.depacketize(packet);
		
		System.out.println(packets.size());
	}
}
