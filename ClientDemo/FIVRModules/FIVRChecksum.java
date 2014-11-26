package FIVRModules;

/**
 * An implementation of the Adler-32 checksum algorithm
 */
public class FIVRChecksum 
{
	private final static int MOD_ADLER = 65521;
	
	/**
	 * @param data Byte array of data to be converted to 32 bit integer checksum
	 * @return Checksum 32 bit integer generated from input data
	 */
	public static int generateChecksum(byte[] data)
	{
		int a = 1;
		int b = 0;
		
		/* Process each byte of the data in order */
	    for (int i = 0; i < data.length; i++)
	    {
	        a = ((a + data[i]) % MOD_ADLER);
	        b = ((b + a) % MOD_ADLER);
	    }
	 
	    return (b << 16) | a;
	}
}
