package scpi.siglent;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import scpi.Scope;

public class SDS1202 extends Scope {

	public SDS1202(String ip) throws IOException {
		super(ip, 5025);
		setFormat(Format.SHORT);
		readScopeSettings();
		System.out.println(id());
	}

	public SDS1202(String ip, int port) throws IOException {
		super(ip, port);
		setFormat(Format.SHORT);
		readScopeSettings();
		System.out.println(id());
	}

	/**
	 * Loads BMP image from the scope.
	 */
	@Override
	public BufferedImage readBmp() throws IOException {
		writeCommand("SCDP");
		BufferedImage img = ImageIO.read(getInputStream());
		getInputStream().skip(1); // skip one byte 
		return img;
	}
	
	/** 
	 * Read the number from the scope response.
	 * @param skipFromEnd Number of bytes to skip from the end of the string.
	 * @param trimFromEnd if <code>true</code> then skip the given number of bytes. 
	 * @return the expected number
	 */
	public double readNumber(int skipFromEnd, boolean trimFromEnd) throws NumberFormatException, IOException {
		if (!trimFromEnd)
			skipFromEnd = 0;
		String s = readLine();
//System.out.println(s);
		String retVal="";
		String[] tokens; 
		switch(format) {
		case SHORT: 
			tokens = s.split(" ");
			retVal = tokens[1];
			break;
		case LONG:
			tokens = s.split(" ");
			retVal = tokens[1];
			break;
		case OFF:
			retVal = s;
			break;
		}
//System.out.println(s);
		retVal = retVal.substring(0, retVal.length() - skipFromEnd);
//System.out.println(s);
		double factor = 1;
		switch (retVal.charAt(retVal.length() - 1)) {
		case 'G':
		case 'g':
			factor = 1E9;
			retVal = retVal.substring(0, retVal.length() - 1);
			break;
		case 'M':
		case 'm':
			factor = 1E6;
			retVal = retVal.substring(0, retVal.length() - 1);
			break;
		case 'k':
		case 'K':
			factor = 1E3;
			retVal = retVal.substring(0, retVal.length() - 1);
			break;
		}
//System.out.println(retVal);
		return Double.parseDouble(retVal) * factor;
	}

	/**
	 * Reads raw data from the scope.
	 * Data is stored in the {@link Scope#data} array of bytes field.
	 */
	@Override
	public void readRawData() throws IOException {
//		// request all points for the waveform
		writeCommand("WFSU SP,0,NP,0,F,0");

		writeCommand("C1:WF? DAT2");

		// parse waveform response
		byte[] bheader = null;
		int lOffset = 0;
		switch(this.format) {
		case OFF:
			bheader = in.readNBytes(15);
			lOffset = 6; // 15 - 9
			break;
		case SHORT:
			bheader = in.readNBytes(21);
			lOffset = 12; // 21 - 9
			break;
		case LONG:
			bheader = in.readNBytes(27);
			lOffset = 18; // 27 - 9
			break;
		}
		String header = new String(bheader);
//System.out.printf("header is: %s\n", header);
		int length = Integer.parseInt(header.substring(lOffset, lOffset + 9));
//System.out.printf("length is: %d\n", length);
		data = in.readNBytes(length);
		in.readNBytes(2); // 2 bytes at end (0A 0A)
	}
	
	/**
	 * Reads scope settings. {@link scpi.ScopeSettings}
	 * @throws IOException
	 */
	@Override
	public void readScopeSettings() throws IOException {
		//writeCommand(format == Format.FORMAT_OFF ? "CHDR OFF" : "CHDR SHORT");

		writeCommand("C1:VDIV?");
		settings.vDiv = readNumber(1, format != Format.OFF);

		writeCommand("C1:OFST?");
		settings.vOffset = readNumber(1, format != Format.OFF);

		writeCommand("TDIV?");
		settings.tDiv = readNumber(1, format != Format.OFF);

		writeCommand("TRDL?");
		settings.tOffset = readNumber(1, format != Format.OFF);

		writeCommand("SARA?");
		settings.sRate = readNumber(4, true);
	}
	
	public String chdr() throws IOException {
		writeCommand("CHDR?");
		return readLine();
	}

	@Override
	public void setFormat(Format format) throws IOException {
		writeCommand("CHDR " + format.name());
		this.format = format; 
	}
	
	@Override
	public String cursorMeasure() throws IOException {
		writeCommand("CRMS?");
		return readLine();
	}
	
	@Override
	public double vCalc(byte b) {
		return b * settings.vDiv / 25.0 - settings.vOffset;
	}
	
	@Override
	public double hCalc(int t) {
		return - (settings.tDiv * 14 / 2) + t * (1/settings.sRate); 
	}
	
	private void dumpData() {
		// voltage value (V) = code value *(voltageDivision / 25) - voffset.
		// if the data is > 127, data = data - 256
		// time value(S) = -(timeDivision * grid / 2) + (time interval) * S.
		// time interval = 1 / sampling rate
		int col = 0;
		int t = 0;
		for (byte b : data) {
			System.out.printf("(%f, %f), ", vCalc(b), hCalc(t++));
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
			
			setFormat(Format.LONG);
System.out.println("Comm header format: " + chdr());
System.out.println("Cursor measure: " + cursorMeasure());
			readScopeSettings();

			System.out.println(settings.toString());

			long t1, t2;

			t1 = System.currentTimeMillis();
			readRawData();
			
			System.out.println("Operation complete: " + opc());
			
			System.out.printf("Buffer length: %d\n", data.length);
			t2 = System.currentTimeMillis();
			System.out.printf("Time: %d\n", (t2 - t1));
			t1 = System.currentTimeMillis();
			readRawData();
			t2 = System.currentTimeMillis();
			System.out.printf("Buffer length: %d\n", data.length);
			System.out.printf("Time: %d\n", (t2 - t1));
			
			dumpData();
			
			t1 = System.currentTimeMillis();
			BufferedImage img = readBmp();
			t2 = System.currentTimeMillis();
			System.out.printf("BMP size %d x %d\n ", img.getWidth(), img.getHeight());
			System.out.printf("Time: %d\n", (t2 - t1));

			t1 = System.currentTimeMillis();
			img = readBmp();
			t2 = System.currentTimeMillis();
			System.out.printf("BMP size %d x %d\n ", img.getWidth(), img.getHeight());
			System.out.printf("Time: %d\n", (t2 - t1));

			System.out.println("Operation complete: " + opc());
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SDS1202 scope;
		try {
			scope = new SDS1202("192.168.1.250");
			scope.test();
			scope.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
