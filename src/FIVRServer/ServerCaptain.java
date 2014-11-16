package FIVRServer;

public class ServerCaptain {

	public static boolean started = false;
	public static boolean initializeState = false;
	public static boolean enableLog = false;
	public static int serverPort;
	public static int windowSize;

	public static void main(String[] args) {

		// Fluffy text to introduce user
		System.out.println("Welcome to FivR Server!");
		System.out.println(" ");
		System.out.println("You may use the following commands:");
		System.out.println("start [port]");
		System.out.println("stop");
		System.out.println("window [size]");
		System.out.println("debug [true/false]");
		System.out.println(" ");

		Thread inputStreamer = new Thread(new InputStreamer("STEAMER"));
		Thread serviceHandler = new Thread(new ServiceHandler("HANDLER"));

		inputStreamer.start();
		serviceHandler.start();

	}

}
