package FIVRModules;

public class FIVRHeader {

	public int sourcePort;
	public int destPort;
	public int packetsForNextSet;
	public String seq;
	public String ack;
	public String checksum;
	public int windowSize;
	public boolean ConnectRequest;
	public boolean TerminateRequest;
	public boolean isNACK;
	public boolean SendToRecvAck;
	public boolean RecvToSendAck;

	public FIVRHeader(int sourcePort, int destPort, String seq, String ack,
			String checksum, int windowSize, boolean ConnectRequest,
			boolean TerminateRequest, boolean isNACK, boolean SendToRecvAck,
			boolean RecvToSendAck, int packetsForNextSet) {

		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.packetsForNextSet = packetsForNextSet;
		this.seq = seq;
		this.ack = ack;
		this.checksum = checksum;
		this.windowSize = windowSize;
		this.ConnectRequest = ConnectRequest;
		this.TerminateRequest = TerminateRequest;
		this.isNACK = isNACK;
		this.SendToRecvAck = SendToRecvAck;
		this.RecvToSendAck = RecvToSendAck;

	}

	// generates array of bytes from this FIVRHeader object
	public byte[] generateHeaderBytes() {
		return null;
	}

	// returns FIVRHeader object from given array of bytes
	public static FIVRHeader decodeHeaderBytes(byte[] stream) {
		return null;
	}

}
