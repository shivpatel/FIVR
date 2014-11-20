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
		
		//Create "new file open bracket" packet
		FIVRHeader openHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, -1, 0, window, 0, 0, 0, 0, 0, packetsForNextSet, isDownload, 1, 0);
		FIVRPacket openPacket = new FIVRPacket(openHeader, null);//no payload
		openPacket.header.setChecksum(FIVRChecksum.generateChecksum(openPacket.getBytes(false)));
		seqNum += 1;
		
		packets.add(openPacket);
		
		int bufferOffset = 0;
		
		//Create data packets
		while(bufferOffset < contentBuffer.length)
		{
			FIVRHeader currentHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, -1, 0, window, 0, 0, 0, 0, 0, packetsForNextSet, isDownload, 0, 0);
			FIVRPacket currentPacket = new FIVRPacket(currentHeader, null);
			seqNum += 1;
			
			int payloadSize = segmentSize - FIVRHeader.HEADER_SIZE;
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
		FIVRHeader closeHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, -1, 0, window, 0, 0, 0, 0, 0, packetsForNextSet, isDownload, 0, 1);
		FIVRPacket closePacket = new FIVRPacket(closeHeader, null);//no payload
		closePacket.header.setChecksum(FIVRChecksum.generateChecksum(closePacket.getBytes(false)));
		seqNum += 1;
		
		packets.add(closePacket);
		
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
		
		if(datagramData.length >= 24)
		{
			ByteBuffer headerBuffer = ByteBuffer.allocate(FIVRHeader.HEADER_SIZE);
			headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
			
			//insert header bytes into the buffer (header is first 24 bytes of the datagram data)
			headerBuffer.put(datagramData, 0, FIVRHeader.HEADER_SIZE);
			headerBuffer.position(0);

			FIVRHeader header = new FIVRHeader(headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt(), headerBuffer.getInt());
			
			if(datagramData.length > 24)//packet contains payload
			{
				ByteBuffer payloadBuffer = ByteBuffer.allocate(datagramData.length - FIVRHeader.HEADER_SIZE);
				payloadBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				//strip packet payload from the datagramData
				payloadBuffer.put(datagramData, FIVRHeader.HEADER_SIZE, datagramData.length - FIVRHeader.HEADER_SIZE);
				payloadBytes = payloadBuffer.array();
				
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
}
