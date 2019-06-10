package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.*;

import dhcp.Constants;
import dhcp.DHCP;
import dhcp.DHCPMessageCreator;
import dhcp.Option;
import ip.*;
import utils.*;

public class Server {
	private Map<String, String> ipsAsignadas; // ip, mac
	private Red server;
	private List<Red> subredes;
	private static DatagramSocket server_socket;
	public static DatagramSocket getServer_socket() {
		return server_socket;
	}

	public static void setServer_socket(DatagramSocket server_socket) {
		Server.server_socket = server_socket;
	}

	public Map<String, String> getIpsAsignadas() {
		return ipsAsignadas;
	}

	public void setIpsAsignadas(Map<String, String> ipsAsignadas) {
		this.ipsAsignadas = ipsAsignadas;
	}

	public Red getServer() {
		return server;
	}

	public void setServer(Red server) {
		this.server = server;
	}

	public List<Red> getSubredes() {
		return subredes;
	}

	public void setSubredes(List<Red> subredes) {
		this.subredes = subredes;
	}

	public Server() throws SocketException {
		ipsAsignadas = new LinkedHashMap<String, String>();
		server = Files.leerConfiguracionRed();
		subredes = Files.leerConfiguracionSub();
		server_socket = new DatagramSocket(67);
		server_socket.setBroadcast(true);
	}

	public DHCP readPacket() throws IOException {
		byte[] receive_data = new byte[1024];
		DatagramPacket receive_packet = new DatagramPacket(receive_data, receive_data.length);
		server_socket.receive(receive_packet);
		return DHCP.getPacket(receive_packet);
	}

	public void sendResponse(DHCP peticion) throws IOException {
		byte tipo = peticion.getDHCPMessageType();
		if (tipo == Constants.DHCPDISCOVER)
			sendOffer(peticion);
		else if (tipo == Constants.DHCPREQUEST)
			respondRequest(peticion);
		else if (tipo == Constants.DHCPRELEASE)
			releaseIp(peticion);
	}

	public void sendOffer(DHCP peticion) throws IOException {
		String gateway, ipAsignar, mac, log;
		DHCP offer;
		mac = Utils.getHWadd(peticion.getChaddr().getAddress());
		log = ("Discover recibido MAC: " + mac + " Hora: " + Time.getHora());
		Red red;
		Files.write(log);
		System.out.println(log);
		gateway = peticion.getGiaddr().getHostAddress();
		red = getRedPeticion(gateway);
		ipAsignar = buscarIpDisponible(red.getListaIps());
		if (ipAsignar != null) {
			offer = DHCPMessageCreator.createOffer(peticion, ipAsignar, generateOptions(red.getTiempo(),
					red.getMascara(), server.getIp(), red.getGateway(), red.getDns(), Constants.DHCPOFFER));
			sendMsg(offer);
			log = ("Se envia offer a MAC: " + mac + " Hora: " + Time.getHora() + " con la ip: " + ipAsignar + " tiempo: "
					+ red.getTiempo());
			Files.write(log);
			System.out.println(log);
		} else {
			log = "No hay IPs disponibles";
			Files.write(log);
			System.out.println(log);
		}
	}

	public void respondRequest(DHCP peticion) throws IOException {
		String gateway, ipAsignar, mac, log;
		DHCP rta;
		Red red;
		boolean f=true;
		mac = Utils.getHWadd(peticion.getChaddr().getAddress());
		log = ("Request recibido MAC: " + mac + " Hora: " + Time.getHora());
		Files.write(log);
		System.out.println(log);
		gateway = peticion.getGiaddr().getHostAddress();
		red = getRedPeticion(gateway);
		if (peticion.getOptionValue(Constants.DHCP_REQUESTED_ADDRESS) == null) {
			ipAsignar = peticion.getCiaddr().getHostAddress();
			if(!ipsAsignadas.get(ipAsignar).equals(mac))
				f=false;
		}
		else {
			ipAsignar = InetAddress.getByAddress(peticion.getOptionValue(Constants.DHCP_REQUESTED_ADDRESS))
					.getHostAddress();
			if(!ipDisponible(ipAsignar, red.getListaIps()))
				f=false;
		}
		if(f) {
			rta = DHCPMessageCreator.createACK(peticion, ipAsignar, generateOptions(red.getTiempo(), red.getMascara(),
					server.getIp(), red.getGateway(), red.getDns(), Constants.DHCPACK));
			log = ("Se envia ack a: " + mac + " Hora: " + Time.getHora()+" con ip: "+ipAsignar);
			if (!ipsAsignadas.containsKey(ipAsignar)) {
				ipsAsignadas.put(ipAsignar, mac);
			}
			Files.write(log);
			System.out.println(log);
			sendMsg(rta);
			cambiar(ipAsignar, red.getListaIps());
			Timer t = new Timer();
			t.schedule(new Clock(ipAsignar, red.getListaIps()), red.getTiempo().toSecondOfDay()*1000);

		} else {
			log = ("No hay IPs disponibles");
			System.out.println(log);
			rta = DHCPMessageCreator.createNAK(peticion, ipAsignar, server.getIp());
			log = ("Se envia nack a: " + mac + " Hora: " + Time.getHora());
			Files.write(log);
			System.out.println(log);
			sendMsg(rta);
		}
	}

	public void releaseIp(DHCP peticion) throws IOException {
		String gateway, ipAsignar, mac, log;
		Red red;
		mac = Utils.getHWadd(peticion.getChaddr().getAddress());
		log = "Release recibido MAC: " + mac + " Hora: " + Time.getHora();
		Files.write(log);
		System.out.println(log);
		ipAsignar = peticion.getCiaddr().getHostAddress();
		gateway = peticion.getGiaddr().getHostAddress();
		red = getRedPeticion(gateway);
		cambiar(ipAsignar, red.getListaIps());
		log = "Se libera ip por petición: "+ipAsignar + " Hora: " + Time.getHora();
		Files.write(log);
		System.out.println(log);
	}

	public void sendMsg(DHCP packet) throws IOException {
		byte[] send_data = packet.serialize();
		DatagramPacket send_packet = new DatagramPacket(send_data, send_data.length, packet.getAddress(), packet.getPort());
		server_socket.send(send_packet);
	}

	public Red getRedPeticion(String gateway) {
		if (gateway.equalsIgnoreCase("0.0.0.0")) // red del servidor
			return server;
		else { // otra red
			for (Red r : subredes) {
				if (r.getGateway().equalsIgnoreCase(gateway)) {
					return r;
				}
			}
		}
		return null;
	}

	public String buscarIpDisponible(List<Ip> ips) {
		for (Ip ip : ips) {
			if (!ip.getOcupada())
				return ip.getDireccion();
		}
		return null;
	}

	public Option[] generateOptions(LocalTime tiempo, String mascara, String ip, String gateway, String dns, int tipo)
			throws UnknownHostException {
		Option[] opciones = new Option[6];
		opciones[0] = new Option(Constants.DHCP_MSG_TYPE, Utils.byte2Bytes((byte) tipo));
		opciones[1] = new Option(Constants.SUBNET_MASK, InetAddress.getByName(mascara).getAddress());
		opciones[2] = new Option(Constants.ROUTERS, InetAddress.getByName(gateway).getAddress());
		opciones[3] = new Option(Constants.DHCP_SERVER_ID, InetAddress.getByName(ip).getAddress());
		opciones[4] = new Option(Constants.DOMAIN_NAME_SERVERS, InetAddress.getByName(dns).getAddress());
		opciones[5] = new Option(Constants.DHCP_LEASE_TIME, Utils.intToBytes(tiempo.toSecondOfDay()));
		return opciones;
	}

	public boolean ipDisponible(String ip, List<Ip> ips) {
		for (Ip aux : ips) {
			if (aux.getDireccion().equalsIgnoreCase(ip) && !aux.getOcupada())
				return true;
		}
		return false;
	}

	public static void cambiar(String ip, List<Ip> ips) {
		for (Ip aux : ips) {
			if (aux.getDireccion().equalsIgnoreCase(ip)) {
				aux.cambiar();
			}
		}
	}
}
