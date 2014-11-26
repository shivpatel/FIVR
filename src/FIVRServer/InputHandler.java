package FIVRServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import FIVRModules.FIVRTransactionManager;

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
			
			if (args[0].equalsIgnoreCase("fta-server") && args.length >= 3) {
				try {
					Server.serverPort = Integer.parseInt(args[1]);
					Server.host = InetAddress.getByName(args[2]);
					Server.emulatorPort = Integer.parseInt(args[3]);
				} catch (NumberFormatException | UnknownHostException e) {
					System.out.println("Invalid arguments");
				}
				Server.initializeState = true;
				Server.started = true;
				return;
			}

			if (args[0].equalsIgnoreCase("window") && args.length >= 2) {
				try {
					Server.windowSize = Integer.parseInt(args[1]);
					FIVRTransactionManager.window_size_main = Server.windowSize;
					Server.log("Window size changed to: " + Server.windowSize,true);
				} catch (NumberFormatException e) {
					System.out.println("Invalid arguments");
				}
				return;
			}

			if (args[0].equalsIgnoreCase("log") && args.length >= 2) {
				try {
					Server.enableLog = Boolean.parseBoolean(args[1]);
					if (Server.enableLog) Server.log("Logs enabled",true);
					if (!Server.enableLog) Server.log("Logs disabled",true);
				} catch (Exception e) {
					System.out.println("Invalid arguments");
				}
				return;
			}

			if (args[0].equalsIgnoreCase("terminate")) {
				Server.started = false;
				Server.initializeState = true;
				ServiceHandler.socket.close();
				Server.log("Server terminated.",true);
				return;
			}

		}
	}

}
