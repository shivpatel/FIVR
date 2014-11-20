package FIVRModules;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FIVRPacket implements Comparable<FIVRPacket>, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public FIVRHeader header;
	public byte[] payload;
	
	public FIVRPacket(FIVRHeader header, byte[] payload)
	{
		this.header = header;
		this.payload = payload;
	} 
	
	/**
	 * Converts FIVRPacket to a byte array
	 * @param withChecksum indicate whether you want the bytes to include the checksum inside the header
	 * @return Byte array representation of this FIVRPacket
	 * @throws IOException
	 */
	public byte[] getBytes(boolean withChecksum) 
	{
		if(payload != null)
		{
			ByteBuffer buffer = ByteBuffer.allocate(FIVRHeader.HEADER_SIZE + payload.length);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			
			if(withChecksum)
			{
				buffer.put(header.getBytes());
			}
			else
			{
				buffer.put(header.getBytesWithoutChecksum());
			}	
			
			buffer.put(payload);
			return buffer.array();
		}
		else
		{
			ByteBuffer buffer = ByteBuffer.allocate(FIVRHeader.HEADER_SIZE);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			
			if(withChecksum)
			{
				buffer.put(header.getBytes());
			}
			else
			{
				buffer.put(header.getBytesWithoutChecksum());
			}
			
			return buffer.array();
		}		
	}
	
	@Override
	public int compareTo(FIVRPacket packet) 
	{
		return (this.header.seqNum - packet.header.seqNum);
	}
}
