package scpi;

/**
 * Holds scope settings:
 * <ul>
 * <li>Vertical division {@link #vDiv}</li>
 * <li>Vertical offset {@link #vOffset}</li>
 * <li>Horizontal division {@link #tDiv}</li>
 * <li>Horizontal offset {@link #tOffset}</li>
 * <li>Scan rate {@link #sRate}</li>
 * </ul>
 * 
 * @author minja
 *
 */
public class ScopeSettings {
	/**
	 * Vertical division.
	 */
	public double vDiv;
	/**
	 * Vertical offset.
	 */
	public double vOffset;
	/**
	 * Horizontal (time) division.
	 */
	public double tDiv;
	/**
	 * Horizontal (time) offset.
	 */
	public double tOffset;
	/**
	 * Scan rate in samples per second.
	 */
	public double sRate;
	
	public ScopeSettings() {
		
	}
	
	public ScopeSettings(double vDiv, double vOffset, double tDiv, double tOffset, double sRate) {
		super();
		this.vDiv = vDiv;
		this.vOffset = vOffset;
		this.tDiv = tDiv;
		this.tOffset = tOffset;
		this.sRate = sRate;
	}
	@Override
	public String toString() {
		return "Settings [voltageDiv=" + vDiv + ", voltageOffset=" + vOffset + ", timeDiv=" + tDiv + ", timeOffset=" + tOffset
				+ ", sampleRate=" + sRate + "]";
	}
	
	public static ScopeSettings example() {
		ScopeSettings ret = new ScopeSettings(2.000000, 0.000000, 0.000100, 0.000000, 10000000);
		return ret;
	}
}
