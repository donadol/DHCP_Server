package ip;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Red
{
	private String ip, gateway, mascara, dns;
	LocalTime tiempo;
	private List<Ip> listaIps = new ArrayList<Ip>();
	//---------------------------------------------------------------------------------------------------
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public void setGateway(String gateway){
		this.gateway = gateway;
	}
	
	public void setMascara(String mascara){
		this.mascara = mascara;
	}
	
	public void setDns(String dns){
		this.dns = dns;
	}
	
	public void setTiempo(LocalTime tiempo){
		this.tiempo = tiempo;
	}
	
	public void setListaIps(List<Ip> ipOcupadas){
		this.listaIps = ipOcupadas;
	}

	public String getIp() {
		return ip;
	}

	public String getGateway() {
		return gateway;
	}

	public String getMascara() {
		return mascara;
	}

	public String getDns() {
		return dns;
	}

	public LocalTime getTiempo() {
		return tiempo;
	}

	public List<Ip> getListaIps() {
		return listaIps;
	}
	public void addIpToList(Ip ip) {
		this.listaIps.add(ip);
	}
	
}
