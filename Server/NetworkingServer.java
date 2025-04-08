import java.io.IOException;
import tage.networking.IGameConnection.ProtocolType;
import java.util.Scanner;

public class NetworkingServer {
	private GameServerUDP thisUDPServer;
	private GameServerTCP thisTCPServer;

	private static int serverPort;
	private static String serverProtocol;
	
	private static Scanner scan = new Scanner(System.in);
	private static final int DEFAULT_PORT_NUMBER = 8888;
	private static final String DEFAULT_PROTOCOL = "UDP";

	public NetworkingServer(int serverPort, String protocol) {	
		try {	
			if(protocol.toUpperCase().compareTo("TCP") == 0){	
				thisTCPServer = new GameServerTCP(serverPort);
			}
			else {	
				thisUDPServer = new GameServerUDP(serverPort);
			}
		} 
		catch (IOException e) {	
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {	
		if(args.length > 1){	
			NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
		}
		else {
			try {
				System.out.println("Port Number (Enter for default - " + DEFAULT_PORT_NUMBER + "): ");
				String port = scan.nextLine();
	
				if (port.equals("")) serverPort = DEFAULT_PORT_NUMBER;
				else serverPort = Integer.parseInt(port);
	
				System.out.println("Protocol [UDP/TCP] (Enter for default - " + DEFAULT_PROTOCOL + "): ");
				String protocol = scan.nextLine();
	
				if (protocol.equals("")) serverProtocol = DEFAULT_PROTOCOL;
				else serverProtocol = protocol;
	
				NetworkingServer app = new NetworkingServer(serverPort, serverProtocol);
			}
			catch (Exception e) {
				System.out.println("Error: " + e);
			}
			
		}
	}

}
