package FIVRServer;

import java.util.*;

public class InputStreamer implements Runnable {

	String name;

	public InputStreamer(String x) {
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
			if (args[0].equalsIgnoreCase("start") && args.length >= 2) {
				try {
					ServerCaptain.serverPort = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid arguments");
				}
				ServerCaptain.initializeState = true;
				ServerCaptain.started = true;
				return;
			}

			// COMMAND > window [size]
			if (args[0].equalsIgnoreCase("window") && args.length >= 2) {
				try {
					ServerCaptain.windowSize = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.out.println("Invalid arguments");
				}
				return;
			}

			// COMMAND > debug [true/false]
			if (args[0].equalsIgnoreCase("debug") && args.length >= 2) {
				try {
					ServerCaptain.enableLog = Boolean.parseBoolean(args[1]);
				} catch (Exception e) {
					System.out.println("Invalid arguments");
				}
				return;
			}

			// COMMAND > stop
			if (args[0].equalsIgnoreCase("stop")) {
				ServerCaptain.started = false;
				// stop the server
				return;
			}

		}
	}

}
