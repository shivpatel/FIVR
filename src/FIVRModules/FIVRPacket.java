package FIVRModules;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FIVRPacket implements Serializable
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
	
	public byte[] getBytes() throws IOException
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
	}
}
