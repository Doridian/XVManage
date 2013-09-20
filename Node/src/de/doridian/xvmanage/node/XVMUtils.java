package de.doridian.xvmanage.node;

import java.io.File;
import java.math.BigInteger;
import java.util.Random;

public class XVMUtils {
	public static final File XVMANAGE_STORAGE_ROOT = new File(".");

	private static final Random random = new Random();

	private static final char[] ALPHANUMERIC_CHARS = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	};

	public static String randomString(final int len) {
		final StringBuilder ret = new StringBuilder(len);
		for(int i=0; i<len; i++)
			ret.append(ALPHANUMERIC_CHARS[random.nextInt(ALPHANUMERIC_CHARS.length)]);
		return ret.toString();
	}

	public static byte[] decodeHex(final String encoded) {
		byte[] decoded = new BigInteger(encoded, 16).toByteArray();
		if (decoded[0] == 0) {
			final byte[] tmp = new byte[decoded.length - 1];
			System.arraycopy(decoded, 1, tmp, 0, tmp.length);
			decoded = tmp;
		}
		return decoded;
	}
}
