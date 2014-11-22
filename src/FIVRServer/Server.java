package FIVRServer;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;

public class Server {

	public static volatile boolean started = false;
	public static volatile boolean initializeState = false;
	public static volatile boolean enableLog = true;
	public static volatile int serverPort;
	public static volatile int emulatorPort;
	public static volatile InetAddress host;
	public static volatile int windowSize;

	public static void main(String[] args) {

		// Fluffy text to introduce user
		System.out.println("*** FIVR Server ***");
		System.out.println(" ");
		System.out.println("Available Commands:");
		System.out.println("> fta-server [FTA Port] [Emulator Address] [Emulator Port]");
		System.out.println("> terminate");
		System.out.println("> window [size]");
		System.out.println("> log [true/false]");
		System.out.println("Note: Log enabled by default.");
		System.out.println(" ");

		Thread inputHandler = new Thread(new InputHandler("USER_INPUT_HANDLER"));
		Thread serviceHandler = new Thread(new ServiceHandler(
				"SERVER_SERVICE_HANDLER"));

		inputHandler.start();
		serviceHandler.start();

	}
	
	public static boolean log(String message) {
		if (!enableLog) return false;
		try {
			File outFile = new File("server-output.txt");
			FileWriter fWriter = new FileWriter(outFile, true);
			PrintWriter pWriter = new PrintWriter(fWriter);
			pWriter.println(message);
			pWriter.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean log(String message, boolean printInConsole) {
		if (printInConsole) System.out.println(message);
		return log(message);
	}

}