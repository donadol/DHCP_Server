package server;

import java.io.IOException; 
import java.net.SocketException;
import dhcp.DHCP;
import utils.Files;
import utils.Time;

public class Proyecto{
	private static String log;
	private static DHCP request;
	
	public static void main(String[] args) throws SocketException, IOException {
		Files.deleteFile();
		Server server = new Server();
		System.out.println("Servidor Configurado con:\nIP: " + server.getServer().getIp() + "\nGateway: " + server.getServer().getGateway() + "\nMascara: " + server.getServer().getMascara() + "\nDNS: " + server.getServer().getDns()+"\nTiempo de arrendamiento: " + server.getServer().getTiempo().toSecondOfDay());
		log = new String("Servidor arriba. Hora: " + Time.getHora());
		Files.write(log);
		System.out.println(log);
		while (true) {
			request = server.readPacket();
			server.sendResponse(request);
		}
	}
}
