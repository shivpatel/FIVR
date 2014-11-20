package FIVRServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class InputHandler implements Runnable {

	String name;

	public InputHandler(String x) {
		name = x;
	}

	@Override
	public void run() {

		try {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);

			while (true) {
				if (scanner.hasNextLine()) {
					processParams(scanner.nextLine());
				}
			}
		} catch (Exception e) {
		}

	}

	public static void processParams(String statement) {

		String[] args = statement.split(" ");

		if (args.length > 0) {
			// COMMAND > start [port]
			if (args[0].equalsIgnoreCase("fta-server") && args.length >= 3) {
				try {
					Server.serverPort = Integer.parseInt(args[1]);
					Server.host = InetAddress.getByName(args[2]);
					Server.emulatorPort = Integer.parseInt(args[3]);
					Server.emulatorPort = Server.serverPort-1; // COMMENT OUT IF TESTING WITH EMULATOR
				} catch (NumberFormatException | UnknownHostException e) {
					System.out.println("Invalid arguments");
				}
				Server.initializeState = true;
				Server.started = true;
				return;
			}

			// COMMAND > window [size]
			if (args[0].equalsIgnoreCase("window") && args.length >= 2) {
				try {
					Server.windowSize = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid arguments");
				}
				return;
			}

			// COMMAND > debug [true/false]
			if (args[0].equalsIgnoreCase("debug") && args.length >= 2) {
				try {
					Server.enableLog = Boolean.parseBoolean(args[1]);
				} catch (Exception e) {
					System.out.println("Invalid arguments");
				}
				return;
			}

			// COMMAND > stop
			if (args[0].equalsIgnoreCase("terminate")) {
				Server.started = false;
				// stop the server
				return;
			}

		}
	}

}
