package scpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Scpi {
	protected Socket s;
	protected InputStream in;
	protected OutputStream out;
	protected BufferedReader rIn;
	protected PrintWriter wOut;

	public Scpi() {
		
	}
	
	public Scpi(Socket s) throws IOException {
		this();
		this.s = s;
		in = s.getInputStream();
		out = s.getOutputStream();
		rIn = new BufferedReader(new InputStreamReader(in));
		wOut = new PrintWriter(new OutputStreamWriter(out), true);
	}

	public InputStream getInputStream() {
		return in;
	}

	/**
	 * Write command to the scope. The command is string.
	 * @param string command
	 * @return 
	 */
	public void writeCommand(String string) {
		wOut.write(string);
		wOut.write("\n");
		wOut.flush();
	}

	/**
	 * Read from the scope until \n is encountered. The bytes are translated to
	 * characters numerically (so US_ASCII).
	 * 
	 * @throws IOException
	 */
	public String readLine() throws IOException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			int c = in.read();
			switch (c) {
			case -1:
			case '\n':
				return sb.toString();
			default:
				sb.append((char) c);
			}
		}
	}

	public double readNumber() throws NumberFormatException, IOException {
		String s = readLine();
		return Double.parseDouble(s);
	}
	
	public void close() {
		try {
			wOut.close();
			rIn.close();
			out.close();
			in.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String id() throws IOException {
		writeCommand("*IDN?");
		return readLine();
	}
	
	public String opc() throws IOException {
		writeCommand("*OPC?");
		return readLine();
	}
	
	public void rst() {
		writeCommand("*RST?");
	}

}
