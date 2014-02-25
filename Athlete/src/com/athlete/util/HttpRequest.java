package com.athlete.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class HttpRequest {
	/**
	 * @author edBaev
	 */

	private static final String boundary = "-----------------------******";
	private static final String newLine = "\r\n";
	private static final int maxBufferSize = 4096;

	private static final String header = "POST / HTTP/1.1\n"
			+ "Host: %s\n"
			+ "Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5\n"
			+ "Accept-Language: en-us,en;q=0.5\n"
			+ "Accept-Encoding: gzip,deflate\n"
			+ "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\n"
			+ "Keep-Alive: 300\n" + "Connection: keep-alive\n"
			+ "Content-Type: multipart/form-data; boundary=" + boundary
			+ "\n\n";

	public static void postSocket(String sUrl,
			@SuppressWarnings("rawtypes") Map params, InputStream stream,
			String fileName, String contentType) {
		OutputStream writer = null;
		BufferedReader reader = null;
		Socket socket = null;
		try {
			int bytesAvailable;
			int bufferSize;
			int bytesRead;

			String openingPart = writeContent(params, fileName, contentType);
			String closingPart = newLine + "--" + boundary + "--" + newLine;
			long totalLength = openingPart.length() + closingPart.length();

			// strip off the leading http:// otherwise the Socket will not work
			String socketUrl = sUrl;
			if (socketUrl.startsWith("http://")) {
				socketUrl = socketUrl.substring("http://".length());
			}

			socket = new Socket(socketUrl, 80);
			socket.setKeepAlive(true);
			writer = socket.getOutputStream();
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			writer.write(String.format(header, socketUrl,
					Long.toString(totalLength)).getBytes());
			writer.write(openingPart.getBytes());

			bytesAvailable = stream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];
			bytesRead = stream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				writer.write(buffer, 0, bufferSize);
				bytesAvailable = stream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = stream.read(buffer, 0, bufferSize);

			}
			stream.close();
			writer.write(closingPart.getBytes());

			writer.flush();
			// read the response
			// do something with response s
		} catch (Exception e) {

		} finally {
			if (writer != null) {
				try {
					writer.close();
					writer = null;
				} catch (Exception ignore) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (Exception ignore) {
				}
			}
			if (socket != null) {
				try {
					socket.close();
					socket = null;
				} catch (Exception ignore) {
				}
			}
		}

	}

	/**
	 * Populate the multipart request parameters into one large stringbuffer
	 * which will later allow us to calculate the content-length header which is
	 * mandatotry when putting objects in an S3 bucket
	 * 
	 * @param params
	 * @param fileName
	 *            the name of the file to be uploaded
	 * @param contentType
	 *            the content type of the file to be uploaded
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String writeContent(Map params, String fileName,
			String contentType) {

		StringBuffer buf = new StringBuffer();

		Set<String> keys = params.keySet();
		for (String key : keys) {
			String val = (String) params.get(key);
			buf.append("--").append(boundary).append(newLine);
			buf.append("Content-Disposition: form-data; name=\"").append(key)
					.append("\"").append(newLine).append(newLine).append(val)
					.append(newLine);
		}

		buf.append("--").append(boundary).append(newLine);
		buf.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
				.append(fileName).append("\"").append(newLine);
		buf.append("Content-Type: ").append(contentType).append(newLine)
				.append(newLine);

		return buf.toString();
	}
}
