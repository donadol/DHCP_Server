package ip;


public class Ip {
	private String direccion;
	private Boolean ocupada;

	public Ip(String ip){
		this.direccion= ip;
		this.ocupada= false;
	}

	public Ip(String ip, boolean ocupada){
		this.direccion= ip;
		this.ocupada= ocupada;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public Boolean getOcupada() {
		return ocupada;
	}

	public void setOcupada(Boolean disponible) {
		this.ocupada = disponible;
	}

	public void cambiar ( ){
		this.ocupada = !ocupada;
	}

}
