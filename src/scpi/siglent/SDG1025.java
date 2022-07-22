package scpi.siglent;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import scpi.Scpi;

public class SDG1025 extends Scpi {
	
	public SDG1025(String ip) throws UnknownHostException, IOException {
		super(new Socket(ip, 5025));
	}
	
	public SDG1025(String ip, int port) throws UnknownHostException, IOException {
		super(new Socket(ip, port));
	}
	
	public void setFrequency(double freq) {
		writeCommand("C1:BSWV FRQ," + freq);
	}
	public void setVoltage(double volt) {
		writeCommand("C1:BSWV AMP," + volt);
	}
	
	public String getStatus() throws IOException {
		writeCommand("C1:BSWV?");
		return readLine();
	}
	
	public void output(boolean status) {
		writeCommand(status ? "C1:OUTP ON": "C1:OUTP OFF");
	}
	
	private void test() {
		try {
//			writeCommand("list instruments");
//			String s = readLine();
//			System.out.println(s);
//			s = s.substring(s.indexOf(","));
//			String guid = s.trim().substring(3, s.length()-2);
//			System.out.println(guid);
//			writeCommand(guid);
//			System.out.println(readLine());
			
			writeCommand("USB0::62701::60986::SDG10GAX3R0381::0::INSTR");
			System.out.println(readLine());
			
			System.out.println(id());
			
			setFrequency(1000);
			sleep(100);
			setVoltage(3);
			sleep(100);
			output(true);
			sleep(100);
//			System.out.println(id());
			System.out.println(getStatus());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SDG1025 sdg;
		try {
			sdg = new SDG1025("192.168.123.4");
			sdg.test();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
