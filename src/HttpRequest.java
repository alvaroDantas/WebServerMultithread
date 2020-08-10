import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
		Socket socket;

	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String line = "Content-type: ";

	private String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return line + "text/html";
		}

		if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
			return line + "image/jpeg";
		}

		if (fileName.endsWith(".gif")) {
			return line + "image/gif";
		}

		if (fileName.endsWith(".pdf")) {
			return line + "application/pdf";
		}

		return line + "application/octet-stream";
	}

	private int sendBytes(FileInputStream fileInputStream, OutputStream outputStream) throws Exception {

		byte[] buffer = new byte[1024];
		int bytes = 0;
		int totalBytes = 0;

		while ((bytes = fileInputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytes);
			totalBytes += bytes;
		}

		return totalBytes;
	}

	void writeLog(String line) {
		File fileLog = new File("data.log");
		try {
			FileWriter fwLog = new FileWriter(fileLog, true);
			fwLog.write(line + CRLF);
			fwLog.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	void response(String fileName, String address, String httpVersion, DataOutputStream os) throws Exception {
		FileInputStream fileInputStream = null;
		Boolean hasFile = true;
		int bytes = 0;

		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;

		File filePath = new File(fileName);

		if (!filePath.isDirectory()) {

			try {
				fileInputStream = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				hasFile = false;
			}
			if (hasFile) {
				statusLine = httpVersion + " 200" + CRLF;
				contentTypeLine = contentType(fileName) + CRLF;
			} else {
				statusLine = httpVersion + " 404" + CRLF;
				contentTypeLine = contentType(".htm") + CRLF;
				entityBody = "<html>" + "<head><title>Not Found</title></head>" + "<body>" + fileName
						+ " não encontrado</body>" + "</html>";
			}

			os.writeBytes(statusLine);
			os.writeBytes(contentTypeLine);
			os.writeBytes(CRLF);

			if (hasFile) {
				bytes = sendBytes(fileInputStream, os);
				fileInputStream.close();
			} else {
				os.writeBytes(entityBody);
			}

		}

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		String log = address + " " + dateFormat.format(cal.getTime()) + " " + fileName + " " + bytes;

		System.out.println(log);
		writeLog(log);

	}

	private void processRequest() throws Exception {
		InputStream inputStream = this.socket.getInputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

		StringTokenizer stringTokenizer = new StringTokenizer(bufferedReader.readLine());
		stringTokenizer.nextToken();
		String fileName = stringTokenizer.nextToken();
		String httpVersion = stringTokenizer.nextToken();
		fileName = "." + fileName;

		if (fileName.equals("./"))
			fileName += "index.html";

		stringTokenizer = new StringTokenizer(bufferedReader.readLine());

		stringTokenizer.nextToken();

		String address = stringTokenizer.nextToken();
		
		String headerLine = null;

		while ((headerLine = bufferedReader.readLine()).length() != 0) {
			if (headerLine.startsWith("Authorization")) {
				break;
			}
		}
		
		response(fileName, address, httpVersion, dataOutputStream);

		dataOutputStream.close();
		bufferedReader.close();
		socket.close();
	}
}