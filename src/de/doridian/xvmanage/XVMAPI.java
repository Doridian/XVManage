package de.doridian.xvmanage;

import de.doridian.xvmanage.models.VMNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class XVMAPI {
	private static final String API_KEY = Configuration.getString("apiKey");

	public static JSONObject apiCall(VMNode node, JSONObject payload) throws IOException {
		payload.put("key", API_KEY);

		try {
			Socket socket = new Socket(node.getIp(), 1532);
			socket.setTcpNoDelay(true);

			DataInputStream socketInput = new DataInputStream(socket.getInputStream());

			DataOutputStream socketOutput = new DataOutputStream(socket.getOutputStream());

			ByteArrayOutputStream outputWriting = new ByteArrayOutputStream();

			OutputStreamWriter requestWriter = new OutputStreamWriter(new GZIPOutputStream(new CipherOutputStream(outputWriting, getCipher(true))));
			payload.writeJSONString(requestWriter);
			requestWriter.close();

			byte[] buf = outputWriting.toByteArray();
			socketOutput.writeInt(buf.length);
			socketOutput.write(buf);
			socketOutput.flush();

			int len = socketInput.readInt();
			buf = new byte[len];
			socketInput.readFully(buf);

			InputStreamReader responseReader = new InputStreamReader(new GZIPInputStream(new CipherInputStream(new ByteArrayInputStream(buf), getCipher(false))));

			JSONParser jsonParser = new JSONParser();

			JSONObject responseObject = (JSONObject)jsonParser.parse(responseReader);

			socket.close();

			return responseObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	private static final SecretKeySpec secretKey = new SecretKeySpec(XVMUtils.decodeHex(Configuration.getString("apiPSK")), "AES");
	private static final IvParameterSpec initVector = new IvParameterSpec(XVMUtils.decodeHex(Configuration.getString("apiIV")), 0, 16);

	private static Cipher getCipher(boolean encrypt) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey, initVector);
		return cipher;
	}
}
