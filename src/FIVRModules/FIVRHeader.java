package FIVRModules;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FIVRHeader implements Serializable
{
	/**
	 * Header of the FIVR Protocol. (24 bytes)
	 */
	private static final long serialVersionUID = 1L;
	public static final byte HEADER_SIZE = 24;
	public int sourcePort;
	public int destPort;
	public int packetsForNextSet;
	public int seqNum;
	public int ack;
	private int checksum;
	public int windowSize;
	public int connectRequest;
	public int terminateRequest;
	public int isNACK;
	public int sendToRecvAck;
	public int recvToSendAck;
	public int isDownload; 
	public int fileOpenBracket;
	public int fileClosingBracket;
	
	/**
	 * Header data lines format
	 * 
	 * 		-------------------------------------------------------------
	 *line1	|	Source Port (16 bits)	|	Destination Port (16 bits)	|
	 * 		-------------------------------------------------------------
	 *		-------------------------------------------------------------
	 *line2	|					Sequence Number	(32 bits)				|
	 *		-------------------------------------------------------------
	 *		-------------------------------------------------------------
	 *line3	|						ACK Number	(32 bits)				|
	 *		-------------------------------------------------------------
	 *		-------------------------------------------------------------
	 *line4	|						Checksum	(32 bits)				|
	 *		-------------------------------------------------------------
	 *		-------------------------------------------------------------
	 *		|upload/	|open	|close	|optional	|	window size		|
	 *line5 |download	|bracket|bracket|params		|	(16 bits)		|
	 *		|(1 bit)	|(1 bit)|(1 bit)|(13 bits)	|					|
	 *		-------------------------------------------------------------
	 *		-------------------------------------------------------------
	 *		|connect|terminate	|isNACK	|SendTo	|RcvTo	|# of packets in|
	 *line6	|request|request	|(1 bit)|RcvACK	|SendACK|next set		|
	 *		|(1 bit)|(1 bit)	|		|(1 bit)|(1 bit)|(27 bits)		|
	 *		-------------------------------------------------------------
	 */
	
	private int line1 = 0;
	private int line2 = 0;
	private int line3 = 0;
	private int line4 = 0;
	private int line5 = 0;
	private int line6 = 0;

	/**
	 * 
	 * @param sourcePort
	 * @param destPort
	 * @param seqNum
	 * @param ack
	 * @param checksum
	 * @param windowSize
	 * @param ConnectRequest
	 * @param TerminateRequest
	 * @param isNACK
	 * @param SendToRecvAck
	 * @param RecvToSendAck
	 * @param packetsForNextSet
	 * @param isDownload
	 * @param fileOpenBracket
	 * @param fileClosingBracket
	 */
	public FIVRHeader(int sourcePort, int destPort, int seqNum, int ack, int checksum, int windowSize, int connectRequest,
			int terminateRequest, int isNACK, int sendToRecvAck, int recvToSendAck, int packetsForNextSet, 
			int isDownload, int fileOpenBracket, int fileClosingBracket) 
	{
		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.packetsForNextSet = packetsForNextSet;
		this.seqNum = seqNum;
		this.ack = ack;
		this.checksum = checksum;
		this.windowSize = windowSize;
		this.connectRequest = connectRequest;
		this.terminateRequest = terminateRequest;
		this.isNACK = isNACK;
		this.sendToRecvAck = sendToRecvAck;
		this.recvToSendAck = recvToSendAck;
		this.isDownload = isDownload;
		this.fileOpenBracket = fileOpenBracket;
		this.fileClosingBracket = fileClosingBracket;
		
		line1 = (sourcePort << 16) | destPort;
		line2 = seqNum;
		line3 = ack;
		line4 = checksum;
		line5 = (isDownload << 31) | (fileOpenBracket << 30) | (fileClosingBracket << 29) | windowSize;
		line6 = (connectRequest << 31) | (terminateRequest << 30) | (isNACK << 29) | 
				(sendToRecvAck << 28) | (recvToSendAck << 27) | packetsForNextSet;
	}
	
	public FIVRHeader(int line1, int line2, int line3, int line4, int line5, int line6)
	{
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.line4 = line4;
		this.line5 = line5;
		this.line6 = line6;
		
		this.sourcePort = line1 >>> 16;
		this.destPort = line1 & 0x0000FFFF;
		this.seqNum = line2;
		this.ack = line3;
		this.checksum = line4;
		this.isDownload = line5 >>> 31;
		this.fileOpenBracket = line5 >>> 30;
		this.fileClosingBracket = line5 >>> 29;
		this.windowSize = line5 & 0x0000FFFF;
		this.connectRequest = line6 >>> 31;
		this.terminateRequest = line6 >>> 30;
		this.isNACK = line6 >>> 29;
		this.sendToRecvAck = line6 >>> 28;
		this.recvToSendAck = line6 >>> 27;
		this.packetsForNextSet = line6 & 0x07FFFFFF;
	}
	
	public FIVRHeader()
	{
		
	}
	
	public void setChecksum(int checksum)
	{
		this.checksum = checksum;
		this.line4 = checksum;
	}
	
	public int getChecksum()
	{
		return this.checksum;
	}
	
	/** Generates array of bytes from this FIVRHeader object
	 * 
	 * @return
	 */
	public byte[] getBytes() 
	{
		ByteBuffer buffer = ByteBuffer.allocate(FIVRHeader.HEADER_SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(line1);
		buffer.putInt(line2);
		buffer.putInt(line3);
		buffer.putInt(line4);
		buffer.putInt(line5);
		buffer.putInt(line6);

		return buffer.array();
	}
}
