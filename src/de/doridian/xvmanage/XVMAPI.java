package de.doridian.xvmanage;

import de.doridian.xvmanage.models.VMNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

public class XVMAPI {
	private static final String API_KEY = Configuration.getString("apiKey");

	public static JSONObject apiCall(VMNode node, JSONObject payload) throws IOException {
		payload.put("Key", API_KEY);

		try {
			Socket socket = SSLSocketFactory.getDefault().createSocket(node.getIp(), 1532);
			socket.setTcpNoDelay(true);

			DataInputStream socketInput = new DataInputStream(socket.getInputStream());

			DataOutputStream socketOutput = new DataOutputStream(socket.getOutputStream());

			ByteArrayOutputStream outputWriting = new ByteArrayOutputStream();

			OutputStreamWriter requestWriter = new OutputStreamWriter(outputWriting);
			payload.writeJSONString(requestWriter);
			requestWriter.close();

			byte[] buf = outputWriting.toByteArray();
			socketOutput.writeInt(buf.length);
			socketOutput.write(buf);
			socketOutput.flush();

			int len = socketInput.readInt();
			buf = new byte[len];
			socketInput.readFully(buf);

			InputStreamReader responseReader = new InputStreamReader(new ByteArrayInputStream(buf));

			JSONParser jsonParser = new JSONParser();

			JSONObject responseObject = (JSONObject)jsonParser.parse(responseReader);

			socket.close();

			return responseObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}
}
