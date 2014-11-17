package FIVRServer;

public class Server {

	public static volatile boolean started = false;
	public static volatile boolean initializeState = false;
	public static volatile boolean enableLog = false;
	public static volatile int serverPort;
	public static volatile int windowSize;

	public static void main(String[] args) {

		// Fluffy text to introduce user
		System.out.println("*** FIVR Server ***");
		System.out.println(" ");
		System.out.println("Available Commands:");
		System.out.println("> start [port]");
		System.out.println("> stop");
		System.out.println("> window [size]");
		System.out.println("> debug [true/false]");
		System.out.println(" ");

		Thread inputHandler = new Thread(new InputHandler("USER_INPUT_HANDLER"));
		Thread serviceHandler = new Thread(new ServiceHandler(
				"SERVER_SERVICE_HANDLER"));

		inputHandler.start();
		serviceHandler.start();

	}

}