package de.doridian.xvmanage.node;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Main {
	public static void main(String[] args) {
		try {
			listener();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void listener() throws IOException {
		ServerSocket serverSocket = new ServerSocket(1532);

		while(true) {
			try {
				final Socket socket = serverSocket.accept();
				socket.setTcpNoDelay(true);

				new Thread() {
					public void run() {
						try {
							DataInputStream socketInput = new DataInputStream(socket.getInputStream());

							int len = socketInput.readInt();
							byte[] buf = new byte[len];
							socketInput.readFully(buf);

							DataOutputStream socketOutput = new DataOutputStream(socket.getOutputStream());

							ByteArrayOutputStream outputWriting = new ByteArrayOutputStream();

							InputStreamReader requestReader = new InputStreamReader(new GZIPInputStream(new CipherInputStream(new ByteArrayInputStream(buf), XVMAPI.getCipher(false))));
							OutputStreamWriter responseWriter = new OutputStreamWriter(new GZIPOutputStream(new CipherOutputStream(outputWriting, XVMAPI.getCipher(true))));

							JSONParser jsonParser = new JSONParser();

							JSONObject requestObject = (JSONObject)jsonParser.parse(requestReader);

							XVMAPI.apiCall(requestObject).writeJSONString(responseWriter);
							responseWriter.close();

							buf = outputWriting.toByteArray();
							socketOutput.writeInt(buf.length);
							socketOutput.write(buf);

							socket.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
