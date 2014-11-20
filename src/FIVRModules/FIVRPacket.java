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
	 * @return Byte array representation of this FIVRPacket
	 * @throws IOException
	 */
	public byte[] getBytes()
	{
		if(payload != null)
		{
			ByteBuffer buffer = ByteBuffer.allocate(24 + payload.length);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(header.getBytes());
			buffer.put(payload);
			return buffer.array();
		}
		else
		{
			ByteBuffer buffer = ByteBuffer.allocate(24);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(header.getBytes());
			return buffer.array();
		}		
	}
	/*public byte[] getBytes() throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		
		byte[] bytes;
		
		try 
		{
		  out = new ObjectOutputStream(bos);   
		  out.writeObject(this);
		  bytes = bos.toByteArray();
		} 
		finally 
		{
		  try 
		  {
		    if (out != null) 
		    {
		      out.close();
		    }
		  } 
		  catch (IOException ex) {
		    // ignore close exception
		  }
		  try 
		  {
		    bos.close();
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
		return bytes;
	}*/

	@Override
	public int compareTo(FIVRPacket packet) 
	{
		return (this.header.seqNum - packet.header.seqNum);
	}
}
