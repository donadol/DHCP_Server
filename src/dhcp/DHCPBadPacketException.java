package dhcp;

public class DHCPBadPacketException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

    public DHCPBadPacketException() {}

    public DHCPBadPacketException(String message) {
        super(message);
    }

    public DHCPBadPacketException(String message, Throwable cause) {
        super(message, cause);
    }

    public DHCPBadPacketException(Throwable cause) {
        super(cause);
    }
}
