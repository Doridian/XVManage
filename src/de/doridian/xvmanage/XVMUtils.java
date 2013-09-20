package de.doridian.xvmanage;

import java.io.File;
import java.math.BigInteger;

public class XVMUtils {
	public static final File XVMANAGE_STORAGE_ROOT = new File(new File(System.getProperty("user.home")), "xvmanage_storage");

	public static String formatProgressBar(float perc) {
		StringBuilder ret = new StringBuilder("<div class='progress'>");
		if(perc > 0) {
			ret.append("<div class='bar bar-success' style='width: "+(perc > 50 ? 50 : perc)+"%;'></div>");
			perc -= 50;
		}
		if(perc > 0) {
			ret.append("<div class='bar bar-warning' style='width: "+(perc > 25 ? 25 : perc)+"%;'></div>");
			perc -= 25;
		}
		if(perc > 0) {
			ret.append("<div class='bar bar-danger' style='width: "+(perc > 25 ? 25 : perc)+"%;'></div>");
		}
		return ret.append("</div>").toString();
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
