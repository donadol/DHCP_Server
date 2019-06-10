package dhcp;

import java.io.*;
import java.net.*;
import java.util.*;

import utils.Utils;

public class DHCP {
	// estructura del paquete
	private byte op; // Message op code 1 = bootrequest, 2 = bootreply
	private byte htype; // HW address type
	private byte hlen; // HW address length
	private byte hops; // HW options
	private byte[] xid; // Transaction id
	private byte[] secs; // Seconds elapsed since client began address acquisition or renewal
	private byte[] flags; // Flags
	private byte[] ciaddr; // Client IP
	private byte[] yiaddr; // 'Your' client IP
	private byte[] siaddr; // Next server IP
	private byte[] giaddr; // Relay agent IP
	private byte[] chaddr; // Client HW address
	private byte[] sname; // Optional server host name
	private byte[] file; // Boot file name
	private List<Option> options; // Optional parameters field
	private byte[] padding; // Final del paquete
	private InetAddress address;
	private int port;
	private boolean correcto;

	public DHCP() {
		this.op = Constants.BOOTREPLY;
		this.htype = Constants.HTYPE_ETHER;
		this.hlen = 6;
		this.xid = new byte[4];
		this.secs = new byte[2];
		this.flags = new byte[2];
		this.ciaddr = new byte[4];
		this.yiaddr = new byte[4];
		this.siaddr = new byte[4];
		this.giaddr = new byte[4];
		this.chaddr = new byte[16];
		this.sname = new byte[64];
		this.file = new byte[128];
		this.options = new ArrayList<Option>();
		this.padding = new byte[0];
		this.correcto = true;
	}

	public byte getOp() {
		return op;
	}

	public void setOp(byte op) {
		this.op = op;
	}

	public byte getHtype() {
		return htype;
	}

	public void setHtype(byte htype) {
		this.htype = htype;
	}

	public byte getHlen() {
		return hlen;
	}

	public void setHlen(byte hlen) {
		this.hlen = hlen;
	}

	public byte getHops() {
		return hops;
	}

	public void setHops(byte hops) {
		this.hops = hops;
	}

	public byte[] getXid() {
		return xid;
	}

	public void setXid(byte[] xid) {
		this.xid = xid;
	}

	public byte[] getSecs() {
		return secs;
	}

	public void setSecs(byte[] secs) {
		this.secs = secs;
	}

	public byte[] getFlags() {
		return flags;
	}

	public void setFlags(byte[] flags) {
		this.flags = flags;
	}

	public InetAddress getCiaddr() {
		try {
			return InetAddress.getByAddress(this.ciaddr.clone());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setCiaddr(byte[] ciaddr) {
		this.ciaddr = ciaddr;
	}

	public InetAddress getYiaddr() {
		try {
			return InetAddress.getByAddress(this.yiaddr.clone());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setYiaddr(byte[] yiaddr) {
		this.yiaddr = yiaddr;
	}

	public InetAddress getSiaddr() {
		try {
			return InetAddress.getByAddress(this.siaddr.clone());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setSiaddr(byte[] siaddr) {
		this.siaddr = siaddr;
	}

	public InetAddress getGiaddr() {
		try {
			return InetAddress.getByAddress(this.giaddr.clone());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setGiaddr(byte[] giaddr) {
		this.giaddr = giaddr;
	}

	public InetAddress getChaddr() {
		try {
			return InetAddress.getByAddress(this.chaddr.clone());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setChaddr(byte[] chaddr) {
		this.chaddr = chaddr;
	}

	public byte[] getSname() {
		return sname;
	}

	public void setSname(byte[] sname) {
		this.sname = sname;
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<Option> options) {
		this.options = options;
	}

	public void setOption(Option opt) {
		this.options.add(opt);
	}

	public byte[] getPadding() {
		return padding;
	}

	public void setPadding(byte[] padding) {
		this.padding = padding;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAddressAndPort(InetSocketAddress addrPort) {
		if (addrPort == null) {
			setAddress(null);
			setPort(0);
		} else {
			setAddress(addrPort.getAddress());
			setPort(addrPort.getPort());
		}
	}

	public static DHCP getPacket(DatagramPacket datagram) throws DHCPBadPacketException, IOException {
		if (datagram == null) {
			throw new IllegalArgumentException("Datagrama es nulo");
		}
		if (datagram.getData() == null) {
			throw new IllegalArgumentException("No hay datos");
		}
		if (datagram.getOffset() < 0) {
			throw new IndexOutOfBoundsException("Offset negativo");
		}
		if (datagram.getLength() < 0) {
			throw new IllegalArgumentException("Longitud negativa");
		}
		if (datagram.getData().length < datagram.getOffset() + datagram.getLength()) {
			throw new IndexOutOfBoundsException("offset+longitud superan el tamaño de los datt");
		}
		if (datagram.getLength() < Constants.BOOTP_ABSOLUTE_MIN_LEN) {
			throw new DHCPBadPacketException("Paquete muy pequeño");
		}
		if (datagram.getLength() > Constants.DHCP_MAX_MTU) {
			throw new DHCPBadPacketException("Paquete supera el tamaño maximo");
		}
		return new DHCP().constructPacket(datagram);
	}

	public DHCP constructPacket(DatagramPacket datagram) throws IOException {
		this.address = datagram.getAddress();
		this.port = datagram.getPort();
		ByteArrayInputStream inBStream = new ByteArrayInputStream(datagram.getData(), datagram.getOffset(),
				datagram.getLength());
		DataInputStream inStream = new DataInputStream(inBStream);

		this.op = inStream.readByte();
		this.htype = inStream.readByte();
		this.hlen = inStream.readByte();
		this.hops = inStream.readByte();
		this.xid = Utils.intToBytes(inStream.readInt());
		this.secs = Utils.shortToBytes(inStream.readShort());
		this.flags = Utils.shortToBytes(inStream.readShort());
		inStream.readFully(this.ciaddr, 0, 4);
		inStream.readFully(this.yiaddr, 0, 4);
		inStream.readFully(this.siaddr, 0, 4);
		inStream.readFully(this.giaddr, 0, 4);
		inStream.readFully(this.chaddr, 0, 16);
		inStream.readFully(this.sname, 0, 64);
		inStream.readFully(this.file, 0, 128);

		inBStream.mark(4);
		if (inStream.readInt() != Constants.MAGIC_COOKIE) {
			correcto = false;
			inBStream.reset();
		}
		if (correcto) {
			int type = 0;
			while (true) {
				int read = inBStream.read();
				if (read < 0)
					break;
				type = (byte) read;
				if (type == Constants.PAD)
					continue;
				if (type == Constants.END)
					break;
				read = inBStream.read();
				if (read < 0)
					break;
				int lenght = Math.min(read, inBStream.available());
				byte[] opts = new byte[lenght];
				inBStream.read(opts);
				this.setOption(new Option((byte) type, opts));
			}
			if (type != Constants.END) {
				throw new DHCPBadPacketException("Paquete dañado");
			}
		}

		// bytes restantes a padding
		this.padding = new byte[inBStream.available()];
		inBStream.read(this.padding);
		return this;
	}

	public Byte getDHCPMessageType() {
		for (Option o : this.options) {
			if (o.getCode() == Constants.DHCP_MSG_TYPE)
				return o.getValue()[0];
		}
		return null;
	}

	public byte[] getOptionValue(byte code) {
		for (Option o : this.options) {
			if (o.getCode() == code)
				return o.getValue();
		}
		return null;
	}

	public void setDHCPMessageType(byte type) {
		this.setOption(Option.nuevaOpcion(Constants.DHCP_MSG_TYPE, type));
	}

	public byte[] serialize() {
		int minLen = Constants.BOOTP_ABSOLUTE_MIN_LEN;
		if (this.correcto) {
			minLen += Constants.BOOTP_VEND_SIZE;
		}
		return serialize(minLen, Constants.DHCP_DEFAULT_MAX_LEN);
	}

	public byte[] serialize(int minSize, int maxSize) {
		ByteArrayOutputStream outBStream = new ByteArrayOutputStream(Constants.DHCP_MAX_MTU / 2);
		DataOutputStream outStream = new DataOutputStream(outBStream);
		try {
			outStream.writeByte(this.op);
			outStream.writeByte(this.htype);
			outStream.writeByte(this.hlen);
			outStream.writeByte(this.hops);
            outStream.writeInt  (Utils.bytesToInt(this.xid));
            outStream.writeShort(Utils.bytesToShort(this.secs));
            outStream.writeShort(Utils.bytesToShort(this.flags));
			outStream.write(this.ciaddr, 0, 4);
			outStream.write(this.yiaddr, 0, 4);
			outStream.write(this.siaddr, 0, 4);
			outStream.write(this.giaddr, 0, 4);
			outStream.write(this.chaddr, 0, 16);
			outStream.write(this.sname, 0, 64);
			outStream.write(this.file, 0, 128);

			if (this.correcto) {
				outStream.writeInt(Constants.MAGIC_COOKIE);
				for (Option opt : this.options) {
					if (opt.getCode() != Constants.PAD && opt.getCode() != Constants.END && opt.getValue() != null) {
						if (opt.getValue().length >= 0 || opt.getValue().length>=255) {
							outStream.writeByte(opt.getCode());
							outStream.writeByte(opt.getValue().length);
							outStream.write(opt.getValue());
						}
					}
				}
				outStream.writeByte(Constants.END);
			}

			outStream.write(this.padding);
			int min_padding = minSize - outBStream.size();
			if (min_padding > 0) {
				byte[] add_padding = new byte[min_padding];
				outStream.write(add_padding);
			}

			byte[] data = outBStream.toByteArray();

			if (data.length > Constants.DHCP_MAX_MTU) {
				throw new DHCPBadPacketException("Paquete muy grande");
			}
			return data;
		} catch (IOException e) {
			throw new DHCPBadPacketException("IOException raised: " + e.toString());
		}
	}

	public String toString() {
		String buf = "";
		try {
			buf = "op=" + this.op + "\nhtype=" + this.htype + "\nhlen=" + this.hlen + "\nhops" + this.hops + "\nxid"
					+ this.xid + "\nsecs" + this.secs + "\nflags" + this.flags + "\nciadrr"
					+ InetAddress.getByAddress(this.ciaddr).getHostAddress() + "\nyiaddr"
					+ InetAddress.getByAddress(this.yiaddr).getHostAddress() + "\nsiaddr"
					+ InetAddress.getByAddress(this.siaddr).getHostAddress() + "\ngiaddr"
					+ InetAddress.getByAddress(this.giaddr).getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buf += "\noptions ";
		for (Option o : this.options) {
			buf += "\n " + o.getCode() + " " + o.getValue();
		}
		return buf;
	}
}
