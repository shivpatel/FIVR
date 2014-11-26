package FIVRModules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 *Takes input file and creates an array of packets from it to be sent via FIVR Protocol
 */
public class FIVRPacketManager 
{

	/**
	 * Returns an ArrayList of FIVRPackets for a specified file
	 * @param filepath
	 * @param sourcePort
	 * @param destinationPort
	 * @param startingSequenceNumber
	 * @param segmentSize
	 * @param window
	 * @param packetsForNextSet
	 * @param isDownload
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<FIVRPacket> packetize(String filepath, int sourcePort, int destinationPort, int startingSequenceNumber, int segmentSize, int window, int packetsForNextSet, int isDownload) throws IOException
	{
		ArrayList<FIVRPacket> packets = new ArrayList<FIVRPacket>();
		
		File file = new File(filepath);
		FileInputStream inputStream = new FileInputStream(file);
		
		byte[] contentBuffer = new byte[inputStream.available()];//create buffer large enough to house all the bytes in the file
		
		inputStream.read(contentBuffer);
		inputStream.close();
		
		int seqNum = startingSequenceNumber;
		
		//Create "new file open bracket" packet. Put file name in this packet payload
		FIVRHeader openHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, 0, 0, window, 0, 0, 0, 0, 0, packetsForNextSet, isDownload, 1, 0);
		FIVRPacket openPacket = new FIVRPacket(openHeader, null);
		seqNum += 1;
		
		packets.add(openPacket); 
		
		int bufferOffset = 0;
		
		//Create data packets
		while(bufferOffset < contentBuffer.length)
		{
			FIVRHeader currentHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, 0, 0, window, 0, 0, 0, 0, 0, packetsForNextSet, isDownload, 0, 0);
			FIVRPacket currentPacket = new FIVRPacket(currentHeader, null);
			seqNum += 1;
			
			int payloadSize = 0;//segmentSize - FIVRHeader.HEADER_SIZE;
			
			if(segmentSize - FIVRHeader.HEADER_SIZE + bufferOffset > contentBuffer.length)
			{
				payloadSize = contentBuffer.length - bufferOffset;
			}
			else
			{
				payloadSize = segmentSize - FIVRHeader.HEADER_SIZE;
			}
			
			byte[] payload = new byte[payloadSize];
			
			for(int i = 0; i < payloadSize; i++)
			{
				if(bufferOffset + i < contentBuffer.length)
				{
					payload[i] = contentBuffer[bufferOffset + i];
				}
				else//reached the end of the contentBuffer
				{
					break;
				}
			}
			bufferOffset += payloadSize;
			
			currentPacket.payload = payload;
			currentPacket.header.setChecksum(FIVRChecksum.generateChecksum(currentPacket.getBytes(false)));
			
			packets.add(currentPacket);
		}
		
		//Create "file closing bracket" packet
		FIVRHeader closeHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, 0, 0, window, 0, 0, 0, 0, 0, packetsForNextSet, isDownload, 0, 1);
		
		byte[] filenameBytes = file.getName().getBytes("UTF-8");
		
		FIVRPacket closePacket = new FIVRPacket(closeHeader, filenameBytes);//add filename to the payload
		closePacket.header.setChecksum(FIVRChecksum.generateChecksum(closePacket.getBytes(false)));
		seqNum += 1;
		
		packets.add(closePacket);
		
		//go back and add the total number of packets (including open and close bracket packets) used to make this file into the open bracket packet's payload
		ByteBuffer buff = ByteBuffer.allocate(4);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.putInt(packets.size());
		
		packets.get(0).payload = buff.array();
		packets.get(0).header.setChecksum(FIVRChecksum.generateChecksum(openPacket.getBytes(false)));
		
		return packets;
	}
	
	/**
	 * Extracts data from UDP DatagramPacket into a FIVRPacket
	 * @param datagram UDP datagram containing the FIVRPacket in its payload
	 * @return FIVRPacket contained in UDP the DatagramPacket, null if the datagram packet is too small to be valid (<24 bytes b/c the FIVRHeader alone is 24 bytes)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static FIVRPacket depacketize(DatagramPacket datagram) throws IOException, ClassNotFoundException
	{
		byte[] datagramData = datagram.getData();
		byte[] payloadBytes;
		
		int dataLength = datagram.getLength();
		
		if(datagram.getLength() >= 24)
		{
			ByteBuffer headerBuffer = ByteBuffer.allocate(FIVRHeader.HEADER_SIZE);
			headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
			
			//insert header bytes into the buffer (header is first 24 bytes of the datagram data)
			headerBuffer.put(datagramData, 0, FIVRHeader.HEADER_SIZE);
			headerBuffer.position(0);

			FIVRHeader header = new FIVRHeader(headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt());
			
			if(datagram.getLength() > FIVRHeader.HEADER_SIZE)//packet contains payload
			{
				ByteBuffer payloadBuffer = ByteBuffer.allocate(datagram.getLength() - FIVRHeader.HEADER_SIZE);
				payloadBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				//strip packet payload from the datagramData
				payloadBuffer.put(datagramData, FIVRHeader.HEADER_SIZE, datagram.getLength() - FIVRHeader.HEADER_SIZE);
				payloadBytes = payloadBuffer.array();		
				
				//byte[] trimmedPayload = trimPayload(payloadBytes);//get rid of trailing zeros (trailing zeros mess up checksum)
				
				FIVRPacket packet = new FIVRPacket(header, payloadBytes);
				
				return packet; 
			}
			else//packet contains no payload
			{
				FIVRPacket packet = new FIVRPacket(header, null);
				
				return packet;
			}
		}

        return null;
	}
	
	public static boolean isPacketCorrupt(FIVRPacket packet)
	{
		int checksum = FIVRChecksum.generateChecksum(packet.getBytes(false));
		
		return checksum != packet.header.getChecksum();
	}
	
	/**
	 * Trims input byte array of trailing zeros integers
	 * @param byte array to remove trailing zeros from
	 * @return input byte array without trailing zeros
	 */
	private static byte[] trimPayload(byte[] payload)
	{
		int index = -1;
		
		//calculate the index at which the trailing zeros begin
		for(int i = payload.length-1; i >= 0; i--)
		{
			byte value = payload[i];
			
			if(value != 0)
			{
				index = i;
				break;
			}
		}
		
		if(index == -1)
		{
			return new byte[0];
		}
		else
		{
			int multiplier = ((index) / 4) + 1;
			
			byte[] bytes = new byte[4 * multiplier];//resulting byte buffer must be a multiple of 4 (size of a single int)
			
			for(int i = 0; i <= index; i++)
			{
				bytes[i] = payload[i];
			}
			
			return bytes;
		}
	}
}
