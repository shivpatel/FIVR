package FIVRModules;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class Test 
{
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		ArrayList<FIVRPacket> packets = FIVRPacketManager.packetize("Lenna.png", 1234, 1235, 0, 512, 10, 0, 0);
		
		
		
		/*byte[] fullBytes = new byte[bytes.length + 150];
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
		} */
		
		ArrayList<FIVRPacket> outputPackets = new ArrayList<FIVRPacket>();
		
		for(int i = 0; i < packets.size(); i++)
		{
			FIVRPacket inputFIVRPacket = packets.get(i);
			byte[] bytes = inputFIVRPacket.getBytes(true);
			 
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length);		
			FIVRPacket outputFIVRPacket = FIVRPacketManager.depacketize(packet);
			outputPackets.add(outputFIVRPacket);
		}
		
		ArrayList<Byte> lennaBytes = new ArrayList<Byte>();
		for(int i = 1; i < outputPackets.size() -1; i++)
		{
			for(int j = 0; j < outputPackets.get(i).payload.length; j++)
			{
				lennaBytes.add(outputPackets.get(i).payload[j]);
			}
		}
		
		byte[] lenna = new byte[lennaBytes.size()];
		
		for(int i = 0; i < lennaBytes.size(); i++)
		{
			lenna[i] = lennaBytes.get(i);
		}
		
		FIVRFile.writeBytesToFile("lenna depacketized.png", lenna);
		
		FIVRBuffer buffer = new FIVRBuffer(10, 1);
		
		buffer.addPacket(outputPackets.get(2));
		buffer.addPacket(outputPackets.get(2));
		buffer.addPacket(outputPackets.get(0));
		buffer.addPacket(outputPackets.get(1));
		buffer.addPacket(outputPackets.get(10));
		buffer.addPacket(outputPackets.get(15));
		
		
		System.out.println(packets.size());
	}
}
