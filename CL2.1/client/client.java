import java.io.BufferedReader;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/*
 * TRAN KIM HIEU 
 * 17020750
 * 
 * client connect den <host> va port 8080
 * mo ta: 
 * 		cac cau lenh dieu khien:
 * 		1- @logout : client va server ngat ket noi
 * 		2- @show : client yeu cau nhan danh sach file tu server
 * 		3- download <ten_file> : client yeu cau downlaod file : vi du: download video.mp4
 * 		4- upload <ten_file> : client yeu cau upload file len server: vi du: upload video.mp4
 * 
*/
public class client {// CLIENT 2.1 -----------
	public static String path = "SharedFolder";
	public static int status = 0;
	public static String fileCurrent = "";

	static synchronized void setStatus(int i) {
		status = i;
	};

	static synchronized int getStatus() {
		return status;
	};

	static synchronized void setFile(String file) {
		fileCurrent = file;
	};

	static synchronized String getFile() {
		return fileCurrent;
	};
	
	static synchronized String[] getListFile() {
		File fileL = new File("SharedFolder");
		String[] fileList = fileL.list();
		return fileList;
	}

	public static void main(String[] args) throws IOException {
		String pathF = ".\\SharedFolder";
		String shutDown = new String("@logout");
		String downFile = new String("download");
		String showFile = new String("@show");
		String uploadFile = new String("upload");
		System.out.print("hello i'm client!");
		InputStream rBinary = null;
		String host;
		Socket client = null;
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		System.out.print("\n\nEnter IP server:");
		host = input.readLine();

		// create thread client connect to ...
		Thread fClient1 = new ThreadClient("127.0.0.1", 8181);

		// create thread client connect to ...
		Thread fClient2 = new ThreadClient("127.0.0.1", 8282);
		fClient1.start();
		fClient2.start();
		try {
			InetAddress address = InetAddress.getByName(host);
			System.out.println("IP server Address: " + address.toString());
		} catch (UnknownHostException e) {
			System.out.println("Could not find: " + host);
		}
		try {
			client = new Socket(host, 8080);
//			System.out.println(client.toString());
			// create stream in/output
			DataInputStream is = new DataInputStream(client.getInputStream());
			DataOutputStream os = new DataOutputStream(client.getOutputStream());
			String line;
			while (true) {
				line = is.readUTF();
				System.out.println("Server said: " + line + "\n\n");
				host = line;
				// ----send messages
				if (!host.contains("upload")) {
					SendProtocol send = new SendProtocol(os, is);
					send.setStringProtocol("str", host);
					send.run();
				}
				// -----end send message
//				os.writeUTF(host);
				if (host.equals(shutDown)) {
					is.close();
					os.close();
					System.out.println("\nClose connecting!");
					return;
				}
				if (host.equals(showFile)) {
//					line = is.readUTF();
					// ---receive message
					ReceiveProtocol rec = new ReceiveProtocol(is, os);
					line = rec.runStr();
					// ---end receive message
					System.out.println("server said: " + line);
				}
				if (host.contains(downFile) && host.length() > 9) {
					String valueSub = host.substring(0, 8);
					String endMes = host.substring(9, host.length());
					boolean hasFile = false;
					String lines;
					lines = is.readUTF();
					if (lines.contains(":"))
						hasFile = true;
					if (!hasFile)
						System.out.println("Dont have file " + endMes + "\n");
					else {
						System.out.println("\noh yeah! start take file " + endMes + "...");
						String fileName = endMes;
						// get size of file and file name
						String[] arrOfStr = lines.split(":", 2);
						String sizeF = arrOfStr[0];
						System.out.println("size is: " + sizeF + "\n" + endMes + "\n");
						int fileSize = Integer.parseInt(sizeF);
						if (fileSize > 0) {
							rBinary = client.getInputStream();
							os.writeUTF(sizeF);
							os.flush();
						}
						ReceiveProtocol rec = new ReceiveProtocol(is, endMes, fileSize, os);
						String status = rec.runBys();
					}
					// --set status has file enabled
					setStatus(1);
					setFile(endMes);

				}
				if (host.contains(uploadFile) && host.length() > 7) {
					File fileL = new File("SharedFolder");
					String[] fileList = fileL.list();
					String listName = "\n";
					int count = 1;
					for (String name : fileList) {
						listName = listName + String.valueOf(count) + " : " + name + "\n";
						count++;
					}
					// ----end list name

					String valueSub = null;
					valueSub = host.substring(0, 6);
					System.out.println(valueSub);
					String endMes = host.substring(7, host.length());
					boolean hasFile = false;
					for (String name : fileList) {
						if (name.equals(endMes)) {
							hasFile = true;
						}
					}
					if (!hasFile) {
						System.out.println("Dont have file " + endMes + "\n");
					} else {
						SendProtocol sendF = new SendProtocol(os, is);
						sendF.setFileProtocol("bys", pathF + "\\" + endMes, endMes, "upload");
						sendF.run();
					}
				}
			}

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
			return;
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + host);
			return;
		}

	}

}

/*
 * thread client fake server
 */
class ThreadServer extends Thread {

	private int port;

	public ThreadServer(int p) {
		this.port = p;
	}

	@Override
	public void run() {
		// server is listening on port
		try {
			ServerSocket ss = new ServerSocket(this.port);
			System.out.print("hello fake server at " + String.valueOf(this.port) + "\n");
			Socket s = null;

			try {
				// socket object to receive incoming client requests
				s = ss.accept();

				System.out.println("A new client is connected : " + s);

				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException e) {

		}

	}

}

/*
 * thread fake client
 */
class ThreadClient extends Thread {

	private int port;
	private String host;

	public ThreadClient(String host, int port) {
		this.port = port;
		this.host = host;
	}

	@Override
	public void run() {
		Socket cl = null;
		// server is listening on port
		try {
			cl = new Socket(this.host, this.port);
//			System.out.println(client.toString());
			// create stream in/output
			DataInputStream is = new DataInputStream(cl.getInputStream());
			DataOutputStream os = new DataOutputStream(cl.getOutputStream());
			System.out.println("create connect to server fake at " + String.valueOf(this.port + "\n"));
			while (true) {
				if (client.getStatus() != 0) {
					System.out.println("start send file for other client\n");
					String host = "upload " + client.getFile();
					//--------get file list
//					File fileL = new File("SharedFolder");
//					String[] fileList = fileL.list();
					String[] fileList =cl client.getListFile();
					String listName = "\n";
					int count = 1;
					for (String name : fileList) {
						listName = listName + String.valueOf(count) + " : " + name + "\n";
						count++;
					}
					// ----end list name

					String valueSub = null;
					valueSub = host.substring(0, 6);
					System.out.println(valueSub);
					String endMes = host.substring(7, host.length());
					boolean hasFile = false;
					for (String name : fileList) {
						if (name.equals(endMes)) {
							hasFile = true;
						}
					}
					if (!hasFile) {
						System.out.println("Dont have file " + endMes + "\n");
					} else {
						SendProtocol sendF = new SendProtocol(os, is);
						sendF.setFileProtocol("bys", "SharedFolder" + "\\" + endMes, endMes, "upload");
						sendF.run();
						client.setStatus(0);
					}
				}
			}

		} catch (IOException e) {

		}

	}

}

/*
 * protocol send - receive
 */

class SendProtocol {

	final String _beforeStr = "s";
	final String _beforeBys = "b";
	final String typeStr = "str";
	final String typeBys = "bys";
	private DataOutputStream os = null;
	private DataInputStream is = null;
	private String protocol = "";
	private String dataStr = "";
	private String path = "";
	private String name = "";
	private String statement = "";

	public SendProtocol(DataOutputStream os, DataInputStream is) {
		this.os = os;
		this.is = is;
	}

	public void setStringProtocol(String protocol, String dataStr) {
		this.protocol = protocol;
		this.dataStr = dataStr;

	}

	public void setFileProtocol(String protocol, String path, String name, String statement) {
		this.protocol = protocol;
		this.path = path;
		this.name = name;
		this.statement = statement;
	}

	public synchronized void run() {
		if (this.protocol.equals(this.typeStr)) {
			try {
				this.os.writeChars(this._beforeStr);
				this.os.writeUTF(this.dataStr);
				this.os.flush();
			} catch (IOException e) {
				return;
			}
		}
		if (this.protocol.equals(this.typeBys)) {
			try {
				byte[] dataBys = new byte[1000];
				FileInputStream inputStream = new FileInputStream(this.path);
				File ff = new File(this.path);
				if (ff.exists()) {
					long sF = ff.length();
					if (this.statement.isEmpty()) {
						this.os.writeUTF(String.valueOf(sF) + ":" + this.name);
						System.out.println(String.valueOf(sF) + ":" + this.name);
						this.os.flush();
					} else {
						SendProtocol send = new SendProtocol(os, is);
						send.setStringProtocol("str", "upload" + " " + String.valueOf(sF) + ":" + this.name);
						send.run();
						System.out.println("upload" + " " + String.valueOf(sF) + ":" + this.name);
					}

				}

				int total = 0;
				int nRead = 0;
				String line = this.is.readUTF();
				if (line.equals(String.valueOf(ff.length()))) {
					while ((nRead = inputStream.read(dataBys)) != -1) {
						total += nRead;
						this.os.write(dataBys, 0, nRead);

					}
				}
				System.out.println("Read " + total + " bytes");
				line = this.is.readUTF();
			} catch (IOException e) {
				return;
			}
		}
	}
}

class ReceiveProtocol {

	final String _beforeStr = "s";
	final String _beforeBys = "b";
	private DataInputStream is = null;
	private DataOutputStream os = null;
	private String protocol = "";
	private String dataStr = "";
	private byte[] dataBys = new byte[1000];
	private String path = "";
	private int fileSize = 0;

	public ReceiveProtocol(DataInputStream is, String path, int fileSize, DataOutputStream os) {
		this.is = is;
		this.os = os;
		this.path = path;
		this.fileSize = fileSize;
	}

	public ReceiveProtocol(DataInputStream is, DataOutputStream os) {
		this.is = is;
		this.os = os;

	}

	public String runStr() {
		try {
			String protocol = "";
			char s = this.is.readChar();
			protocol = Character.toString(s);

			if (protocol.equals(this._beforeStr)) {
				String value = this.is.readUTF();
				return value;
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	public String runBys() {
		try {
			File f = new File(client.path + "\\" + this.path);
			f.createNewFile();
			FileOutputStream wFile = new FileOutputStream(f);
			byte[] buffer = new byte[1000];
			int size;
			int total = 0;
			while ((size = this.is.read(buffer)) != -1) {
				total += size;
				if (total >= fileSize) {
					wFile.write(buffer, 0, size);
					wFile.close();
					break;
				} else {
					wFile.write(buffer, 0, size);
					wFile.flush();
				}

			}
			System.out.println("download success!\n");
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			System.out.println(dtf.format(now));
			os.writeUTF("THANK YOU I HAVE FILE");
			os.flush();
			return "success";
		} catch (IOException e) {
			System.out.println("ex io file");
			return null;
		}
	}
}
