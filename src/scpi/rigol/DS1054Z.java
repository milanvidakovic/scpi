package scpi.rigol;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import scpi.Scope;

public class DS1054Z extends Scope {

	RigolSettings settings;
	
	public DS1054Z(String ip) throws IOException {
		super(ip, 5555);
	}

	public DS1054Z(String ip, int port) throws IOException {
		super(ip, port);
		this.settings = new RigolSettings();
		readScopeSettings();
		System.out.println(id());
	}

	/**
	 * Loads BMP image from the scope.
	 */
	@Override
	public BufferedImage readBmp() throws IOException {
		writeCommand("DISP:DATA?"); // ON,0,BMP8
		in.readNBytes(11);
		BufferedImage img = ImageIO.read(getInputStream());
		getInputStream().skip(1); // skip one byte for some unknown reason
		return img;
	}
	

	/**
	 * Reads raw data from the scope.
	 * Data is stored in the {@link Scope#data} array of bytes field.
	 */
	@Override
	public void readRawData() throws IOException {
		readScopeSettings();
		
		writeCommand("WAV:SOUR CHAN1");
		writeCommand("WAV:MODE NORM");
		writeCommand("WAV:FORM BYTE");
		writeCommand("WAV:DATA?");
		
		byte[] bheader = in.readNBytes(11);
		String header = new String(bheader);
//		System.out.println(header);
		int length = Integer.parseInt(header.substring(2));
//		System.out.println(length);
		data = in.readNBytes(length);
		//dumpData();
//		System.out.println("avail: " + in.available());
		// skip one byte (0x0A)
		in.readNBytes(1);
//		System.out.printf("%02X\n", in.readNBytes(1)[0]); // 2 bytes at end (0A 0A)
	}
	
	/**
	 * Reads scope settings. {@link scpi.ScopeSettings}
	 * @throws IOException
	 */
	@Override
	public void readScopeSettings() throws IOException {
		writeCommand("CHAN1:SCAL?");
		settings.vDiv = readNumber();

		writeCommand("CHAN1:OFFS?");
		settings.vOffset = readNumber();
		
		writeCommand("TIM:SCAL?");
		settings.tDiv = readNumber();
		
		writeCommand("TIM:OFFS?");
		settings.tOffset = readNumber();
		
		writeCommand("ACQ:SRAT?");
		settings.sRate = readNumber();
		
		writeCommand("WAV:YOR?");
		settings.yOrigin = readNumber();
//		System.out.println("YORIGIN: " + settings.yOrigin);
		
		writeCommand("WAV:YREF?");
		settings.yRef = readNumber();
//		System.out.println("YREF: " + settings.yRef);
		
		writeCommand("WAV:YINC?");
		settings.yInc = readNumber();
//		System.out.println("YINC: " + settings.yInc);
		
		writeCommand("WAV:XOR?");
		settings.xOrigin = readNumber();
//		System.out.println("XORIGIN: " + settings.xOrigin);
		
		writeCommand("WAV:XREF?");
		settings.xRef = readNumber();
//		System.out.println("XREF: " + settings.xRef);
		
		writeCommand("WAV:XINC?");
		settings.xInc = readNumber();
//		System.out.println("XINC: " + settings.xInc);
				
		System.out.println(settings);
	}
	

	@Override
	public void setFormat(Format format) throws IOException {

	}
	
	@Override
	public String cursorMeasure() throws IOException {
		writeCommand("CRMS?");
		return readLine();
	}
	
	@Override
	public double vCalc(byte b) {
		int bb = b;
		if (b < 0) bb = (256 + b);
		return (bb - settings.yOrigin - settings.yRef) * settings.yInc;
	}
	
	@Override
	public double hCalc(int t) {
		return (t - settings.xOrigin - settings.xRef) * settings.xInc;
	}
	
	private void dumpData() {
		int col = 0;
		int t = 0;
		for (byte b : data) {
			System.out.printf("%02X:(%f, %f), ", b, vCalc(b), hCalc(t++));
			col++;
			if (col == 80) {
				col = 0;
				System.out.println();
			}
		}
	}
	
	private void test() {

		try {
			readScopeSettings();
			readRawData();
			dumpData();
			readRawData();
			System.out.println("VPP: " + measure(1));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private double measure(int channel) throws IOException {
		writeCommand("MEAS:STAT:ITEM VPP,CHAN1");
		writeCommand("MEAS:STAT:ITEM? CURR,VPP");
		return readNumber();
	}

	public static void main(String[] args) {
		DS1054Z scope;
		try {
			scope = new DS1054Z("192.168.123.2", 5555);
			scope.test();
			scope.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
