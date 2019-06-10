package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import ip.Ip;
import ip.Red;

public class Files {
	public static void deleteFile() {
		File file = new File("Log.txt");
		file.delete();
	}

	public static Red leerConfiguracionRed() {
		BufferedReader br = null;
		FileReader fr = null;
		Red subNet = new Red();
		int l, h;
		try {
			fr = new FileReader("CONFIG.txt");
			br = new BufferedReader(fr);

			String linea = br.readLine();
			while (!linea.equalsIgnoreCase("SUBRED")) {
				if (linea.equalsIgnoreCase("IP")) {
					linea = br.readLine();
					subNet.setIp(linea);
					linea = br.readLine();
				} else if (linea.equalsIgnoreCase("GATEWAY")) {
					linea = br.readLine();
					subNet.setGateway(linea);
					linea = br.readLine();
				} else if (linea.equalsIgnoreCase("MASK")) {
					linea = br.readLine();
					subNet.setMascara(linea);
					linea = br.readLine();
				} else if (linea.equalsIgnoreCase("RANGO")) {
					linea = br.readLine();
					l = Integer.parseInt(linea);
					linea = br.readLine();
					h = Integer.parseInt(linea);
					System.out.println(Utils.getBeginingRed(subNet.getIp()));
					for (int i = l; i <= h; i++) {
						subNet.addIpToList(new Ip(Utils.getBeginingRed(subNet.getIp()) + String.valueOf(i), false));
					}
					linea = br.readLine();

				} else if (linea.equalsIgnoreCase("EXCLUIDAS")) {
					linea = br.readLine();
					while (!linea.equalsIgnoreCase("TIME")) {
						for (Ip d : subNet.getListaIps()) {
							if (d.getDireccion().equalsIgnoreCase(linea))
								d.setOcupada(true);
						}
						linea = br.readLine();
					}
				} else if (linea.equalsIgnoreCase("TIME")) {
					linea = br.readLine();
					subNet.setTiempo(LocalTime.ofSecondOfDay(Long.parseLong(linea)));
					linea = br.readLine();
				} else if (linea.equalsIgnoreCase("DNS")) {
					linea = br.readLine();
					subNet.setDns(linea);
					linea = br.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return subNet;
	}

	public static List<Red> leerConfiguracionSub() {
		BufferedReader br = null;
		FileReader fr = null;
		List<Red> subn = new ArrayList<Red>();
		Red subNet = new Red();
		int l, h;
		boolean band = false;
		try {
			fr = new FileReader("CONFIG.txt");
			br = new BufferedReader(fr);

			String linea = br.readLine();
			while (!linea.equalsIgnoreCase("FIN")) {
				if (band == false) {
					linea = br.readLine();
				}
				if (linea.equalsIgnoreCase("SUBRED")) {
					band = true;
				}
				if (band) {
					linea = br.readLine();
					if (linea.equalsIgnoreCase("IP")) {
						subNet.setIp(br.readLine());
						linea = br.readLine();
					}
					if (linea.equalsIgnoreCase("GATEWAY")) {
						subNet.setGateway(br.readLine());
						linea = br.readLine();
					}
					if (linea.equalsIgnoreCase("MASK")) {
						subNet.setMascara(br.readLine());
						linea = br.readLine();
					}
					if (linea.equalsIgnoreCase("RANGO")) {
						l = Integer.parseInt(br.readLine());
						h = Integer.parseInt(br.readLine());
						for (int i = l; i <= h; i++) {
							subNet.addIpToList(new Ip(Utils.getBeginingRed(subNet.getIp()) + String.valueOf(i), false));
						}
						linea = br.readLine();
					}
					if (linea.equalsIgnoreCase("EXCLUIDAS")) {
						linea = br.readLine();
						while (!linea.equalsIgnoreCase("TIME")) {
							for (Ip d : subNet.getListaIps()) {
								if (d.getDireccion().equalsIgnoreCase(linea))
									d.setOcupada(true);
							}
							linea = br.readLine();
						}
					}
					if (linea.equalsIgnoreCase("TIME")) {
						subNet.setTiempo(LocalTime.ofSecondOfDay(Long.parseLong(br.readLine())));
						linea = br.readLine();
					}
					if (linea.equalsIgnoreCase("DNS")) {
						subNet.setDns(br.readLine());
						linea = br.readLine();
					}
					subn.add(subNet);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return subn;
	}

	public static void write(String log) throws IOException {
		FileWriter escribir = new FileWriter("Log.txt", true);
		PrintWriter evento = new PrintWriter(escribir);
		evento.println(log);
		evento.close();
	}
}
