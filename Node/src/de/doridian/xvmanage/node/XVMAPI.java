package de.doridian.xvmanage.node;

import org.json.simple.JSONObject;
import org.libvirt.LibvirtException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;

public class XVMAPI {
	private static final String API_KEY = Configuration.getString("apiKey");

	public static synchronized JSONObject apiCall(JSONObject payload) throws LibvirtException, IOException {
		if(!payload.get("key").equals(API_KEY))
			throw new IOException("Wrong API key");

		String target = (String)payload.get("target");
		String action = (String)payload.get("action");
		if(target.equals("vm")) {
			if(action.equals("list")) {
				JSONObject result = new JSONObject();
				result.put("result", LibvirtVM.getVMList());
				return result;
			} else {
				LibvirtVM targetVM = LibvirtVM.getByName((String) payload.get("vm"));
				if(action.equals("vnc")) {
					return targetVM.doVNC((Boolean)payload.get("ssl"));
				} else {
					JSONObject result = new JSONObject();
					result.put("result", targetVM.processCommand(action));
					return result;
				}
			}
		}

		throw new IOException("Invalid method");
	}

	private static final SecretKeySpec secretKey = new SecretKeySpec(XVMUtils.decodeHex(Configuration.getString("apiPSK")), "AES");
	private static final IvParameterSpec initVector = new IvParameterSpec(XVMUtils.decodeHex(Configuration.getString("apiIV")), 0, 16);

	static Cipher getCipher(boolean encrypt) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey, initVector);
		return cipher;
	}
}
