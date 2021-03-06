package FIVRModules;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Test 
{
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		ArrayList<FIVRPacket> packets = FIVRPacketManager.packetize("Lenna.png", 1234, 1235, 0, 512, 10, 0, 0);
		
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(packets.get(0).payload);
		buff.position(0);
		int numOfPackets = buff.getInt();
		
		
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
		
		FIVRPacket closingPacket = outputPackets.get(outputPackets.size()-1);
		
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
		
		String filename = new String(outputPackets.get(outputPackets.size()-1).payload, "UTF-8");//get filename out of open bracket packet
		filename = "automatic " + filename;
		
		FIVRFile.writeBytesToFile(filename, lenna);
		
		FIVRBuffer buffer = new FIVRBuffer(11, 0);
		
		buffer.addPacket(outputPackets.get(2));
		buffer.addPacket(outputPackets.get(2));
		buffer.addPacket(outputPackets.get(0));
		buffer.addPacket(outputPackets.get(1));
		buffer.addPacket(outputPackets.get(10));
		buffer.addPacket(outputPackets.get(15));
		
		
		System.out.println(packets.size());
	}
}
