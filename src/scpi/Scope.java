package scpi;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;

/**
 * Base class for the scope.
 * @author minja
 *
 */
public abstract class Scope extends Scpi {
	
	/**
	 * Scope communication format.
	 * @author minja
	 *
	 */
	public enum Format {OFF, SHORT, LONG};
	
	protected Format format = Format.OFF;
	

	public ScopeSettings settings;
	
	/**
	 * Raw data from the scope. 
	 * Obtained by {@link readRawData} method. 
	 */
	public byte[] data;
	
	public Scope(String ip) throws IOException {
		this(ip, 5025);
	}

	public Scope(String ip, int port) throws IOException {
		super(new Socket(ip, port));
		settings = new ScopeSettings();
	}

	
	/**
	 * Converts voltage byte into the double value according to the current settings (voltage division, offset, etc.).
	 * @param b byte
	 * @return voltage as a real number
	 */
	public abstract double vCalc(byte b);
	
	/**
	 * Converts time value into the double value according to the current settings (time division, offset, etc.). 
	 * @param t time 
	 * @return time as a real number
	 */
	public abstract double hCalc(int t);

	/**
	 * Reads raw data from the scope.
	 * Data is stored in the {@link #data} array of bytes field.
	 * @throws IOException
	 */
	public abstract void readRawData() throws IOException;
	
	/**
	 * Reads scope settings. {@link scpi.ScopeSettings}
	 * @throws IOException
	 */
	public abstract void readScopeSettings() throws IOException;
	
	/**
	 * Loads BMP image from the scope.
	 */
	public abstract BufferedImage readBmp() throws IOException;

	/**
	 * Sets the communication format. {@link Format}
	 * @param format
	 * @throws IOException
	 */
	public abstract void setFormat(Format format) throws IOException;
	
	public abstract String cursorMeasure() throws IOException;

}
