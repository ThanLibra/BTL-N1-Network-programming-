import java.io.*;
import java.net.*;

public class client {

	public static void main(String[] args) {

		final String serverHost = "localhost";
		final String pathFolder = "SharedFolder";
		Socket socketOfClient = null;
		OutputStream os = null;
		InputStream is = null;

		try {

			socketOfClient = new Socket(serverHost, 9999);

			os = socketOfClient.getOutputStream();

			is = socketOfClient.getInputStream();

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + serverHost);
			return;
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + serverHost);
			return;
		}

		try {

			ReceiveProtocol rec = new ReceiveProtocol(os,is);
			rec.saveFile();
			os.close();
			is.close();
			socketOfClient.close();
		} catch (UnknownHostException e) {
			System.err.println("Trying to connect to unknown host: " + e);
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}

}


class ReceiveProtocol {
	private InputStream is = null;
	private OutputStream os = null;
	private final String path = "SharedFolder";
	private String type = "";

	public ReceiveProtocol(OutputStream os, InputStream is) {
		this.is = is;
		this.os = os;
	}

	public boolean saveFile() {
		int count = 0;
		byte[] buffer = new byte[1];
		byte[] dataFile = new byte[1000];
		String length = "";
		String fileName = "";
		try {
			boolean switchName = false;
			while ((count = is.read(buffer, 0, 1)) != -1) {
				String s = new String(buffer);
				if (s.equals(":") && !switchName) {
					switchName = true;
				} else {
					if (!s.equals("@") && switchName) {
						fileName += s;
					} else if (!switchName) {
						length += s;
					} else {
						int len = Integer.parseInt(length);
						int total = 0;
						File f = new File(this.path+"/"+fileName);
						FileOutputStream fOut = new FileOutputStream(f);
						BufferedOutputStream wFile = new BufferedOutputStream(fOut);
						// get each byte
						while ((count = is.read(dataFile)) != -1) {
							total += count;
							wFile.write(dataFile,0,count);
							if (total == len) {
								return true;
							}
						}
					}

				}

			}
		} catch (IOException e) {
		}
		return true;
	}

	/*
	 * get message from stream receive message protocol form:
	 * <size_message>@<content_message>
	 * 
	 * @return content message
	 */
	public String getMessage() {
		int count = 0;
		byte[] buffer = new byte[1];
		String length = "";
		try {
			while ((count = is.read(buffer, 0, 1)) != -1) {
				String s = new String(buffer);
				if (s.equals("@")) {
					int len = Integer.parseInt(length);
					int total = 0;
					String mes = "";
					// get each byte
					while ((count = is.read(buffer, 0, 1)) != -1) {
						total += count;
						String element = new String(buffer);
						mes += element;
						if (total == len) {
							return mes;
						}
					}

				} else {
					length += s;
				}

			}
		} catch (IOException e) {
		}
		return "";
	}

}
