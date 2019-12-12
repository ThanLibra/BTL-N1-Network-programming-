import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class server1 {

	public static void main(String args[]) {

		ServerSocket listener = null;
		String line;
		OutputStream os = null;
		InputStream is = null;
		Socket socketOfServer = null;

		try {
			listener = new ServerSocket(9999);
		} catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}

		try {
			System.out.println("Server is waiting to accept user...");
			socketOfServer = listener.accept();
			System.out.println("Accept a client!");
			is = socketOfServer.getInputStream();
			os = socketOfServer.getOutputStream();
			ReceiveProtocol rec = new ReceiveProtocol(os, is);
			boolean message = rec.saveFile();
			if(message) {
				System.out.println("save success!!!");
			}
//			System.out.println("message from client: " + message + "\n");

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		System.out.println("Sever stopped!");
	}
}

class SendProtocol {
	private OutputStream os = null;
	private InputStream is = null;
	private String dataStr = "";
	private String path = "";
	private String fileName = "";
	private final String key = "@";
	private final String beforeStr = "s";
	private final String beforeFile = "f";
	private final String pathFolder = "SharedFolder";

	public SendProtocol(OutputStream os, InputStream is, String data) {
		this.is = is;
		this.os = os;
		this.dataStr = data;
	}

	public SendProtocol(OutputStream os, InputStream is, String path, String fileName) {
		this.is = is;
		this.os = os;
		this.path = path;
		this.fileName = fileName;
	}

	public void send() {

		if (this.path.isEmpty() && !this.dataStr.isEmpty()) {
			/*
			 * send message protocol form: <size_message>@<content_message> type: byte[]
			 */
			try {
				int len = this.dataStr.length();
				String l = String.valueOf(len) + "@";
				byte[] bb = this.dataStr.getBytes();
				this.os.write(l.getBytes());
				this.os.write(this.dataStr.getBytes());
				this.os.flush();
			} catch (IOException e) {
				return;
			}
		} else {
			/*
			 * send file protocol form: <size_file>:<file_name>@<data_file> type: byte[]
			 */
			try {
				FileInputStream inputStream = new FileInputStream(this.path);
				File fTemp = new File(this.path);
				long sizeFL = fTemp.length();
				String sizeFS = String.valueOf(sizeFL);
				String messFile = sizeFS + ":" + this.fileName + "@";
				this.os.write(messFile.getBytes());
				int total = 0;
				int nRead = 0;
				byte[] buffer = new byte[1000];
				while ((nRead = inputStream.read(buffer)) != -1) {
					total += nRead;
					this.os.write(buffer, 0, nRead);
				}
				System.out.println("send file < "+this.fileName+" > success!! :<>: "+"Read " + total + " bytes");
			} catch (IOException e) {

			}
			return;
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
						File f = new File(this.path+"\\"+fileName);
						f.createNewFile();
						FileOutputStream wFile = new FileOutputStream(f);
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
