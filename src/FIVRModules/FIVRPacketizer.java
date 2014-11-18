package FIVRModules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *Takes input file and creates an array of packets from it to be sent via FIVR Protocol
 */
public class FIVRPacketizer 
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
	public static ArrayList<FIVRPacket> packetize(String filepath, short sourcePort, short destinationPort, int startingSequenceNumber, int segmentSize, int window, int packetsForNextSet, boolean isDownload) throws IOException
	{
		ArrayList<FIVRPacket> packets = new ArrayList<FIVRPacket>();
		
		File file = new File(filepath);
		FileInputStream inputStream = new FileInputStream(file);
		
		byte[] contentBuffer = new byte[inputStream.available()];//create buffer large enough to house all the bytes in the file
		
		inputStream.read(contentBuffer);
		inputStream.close();
		
		int seqNum = startingSequenceNumber;
		
		//Create "new file open bracket" packet
		FIVRHeader openHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, -1, -1, window, false, false, false, false, false, packetsForNextSet, isDownload, true, false);
		FIVRPacket openPacket = new FIVRPacket(openHeader, null);//no payload
		openPacket.header.checksum = FIVRChecksum.generateChecksum(openPacket.getBytes());
		seqNum += 1;
		
		packets.add(openPacket);
		
		int bufferOffset = 0;
		
		//Create data packets
		while(bufferOffset < contentBuffer.length)
		{
			FIVRHeader currentHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, -1, -1, window, false, false, false, false, false, packetsForNextSet, isDownload, true, false);
			FIVRPacket currentPacket = new FIVRPacket(currentHeader, null);
			currentPacket.header.checksum = FIVRChecksum.generateChecksum(currentPacket.getBytes());
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
			
			packets.add(currentPacket);
		}
		
		//Create "file closing bracket" packet
		FIVRHeader closeHeader = new FIVRHeader(sourcePort, destinationPort, seqNum, -1, -1, window, false, false, false, false, false, packetsForNextSet, isDownload, false, true);
		FIVRPacket closePacket = new FIVRPacket(closeHeader, null);//no payload
		closePacket.header.checksum = FIVRChecksum.generateChecksum(closePacket.getBytes());
		seqNum += 1;
		
		packets.add(closePacket);
		
		return packets;
	}
	
	public static FIVRPacket generatePacket()
	{
		return null;
	}
}