import java.io.*;
import java.net.*;

public class server {

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
			
			SendProtocol send = new SendProtocol(os,is,"SharedFolder"+"/"+"abc.txt","abc.txt");
			send.send();
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
				
				File fTemp = new File(this.path);
				FileInputStream fIn = new FileInputStream(fTemp);
				BufferedInputStream inputStream = new BufferedInputStream(fIn);
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


