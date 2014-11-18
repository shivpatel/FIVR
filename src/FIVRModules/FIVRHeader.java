package FIVRModules;

import java.io.Serializable;

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
	public int checksum;
	public int windowSize;
	public boolean ConnectRequest;
	public boolean TerminateRequest;
	public boolean isNACK;
	public boolean SendToRecvAck;
	public boolean RecvToSendAck;
	public boolean isDownload;
	public boolean fileOpenBracket;
	public boolean fileClosingBracket;

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
	public FIVRHeader(int sourcePort, int destPort, int seqNum, int ack, int checksum, int windowSize, boolean ConnectRequest,
			boolean TerminateRequest, boolean isNACK, boolean SendToRecvAck, boolean RecvToSendAck, int packetsForNextSet, 
			boolean isDownload, boolean fileOpenBracket, boolean fileClosingBracket) 
	{
		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.packetsForNextSet = packetsForNextSet;
		this.seqNum = seqNum;
		this.ack = ack;
		this.checksum = checksum;
		this.windowSize = windowSize;
		this.ConnectRequest = ConnectRequest;
		this.TerminateRequest = TerminateRequest;
		this.isNACK = isNACK;
		this.SendToRecvAck = SendToRecvAck;
		this.RecvToSendAck = RecvToSendAck;
		this.isDownload = isDownload;
		this.fileOpenBracket = fileOpenBracket;
		this.fileClosingBracket = fileClosingBracket;
	}
	
	/** Generates array of bytes from this FIVRHeader object
	 * 
	 * @return
	 */
	public byte[] generateHeaderBytes() 
	{
		return null;
	}

	/** returns FIVRHeader object from given array of bytes
	 * 
	 * @param stream
	 * @return
	 */
	public static FIVRHeader decodeHeaderBytes(byte[] stream) 
	{
		return null;
	}

}
