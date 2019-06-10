package dhcp;

import java.net.InetAddress;

import utils.Utils;

public class Option {
	private byte code;
	private byte[] value;
	
	public Option(byte code, byte[] value) {
		this.code = code;
		this.value = value;
	}
	public byte getCode() {
		return code;
	}
	public void setCode(byte code) {
		this.code = code;
	}
	public byte[] getValue() {
		return value;
	}
	public void setValue(byte[] value) {
		this.value = value;
	}
	public static Option nuevaOpcion(byte code, byte value) {
        return new Option(code, Utils.byte2Bytes(value));
    }
	public static Option nuevaOpcion(byte code, InetAddress value) {
        return new Option(code, value.getAddress());
    }
}
