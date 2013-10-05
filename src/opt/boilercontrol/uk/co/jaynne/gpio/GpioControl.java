package uk.co.jaynne.gpio;

/**
 * Interface describing how to access GPIO
 * @author James Cooke
 *
 */
public interface GpioControl {
	
	/** GPIO low */
	public static boolean OFF = false;
	/** GPIO high */
	public static boolean ON = true;
	
//	public GpioControl getInstance();
	
	/**
	 * Set a pin as an output
	 * @param pin
	 * @return
	 */
	public GpioPin setAsOutput(GpioPin pin);
	
	/**
	 * Set a pin as an input
	 * @param pin
	 * @return
	 */
	public GpioPin setAsInput(GpioPin pin);
	
	/**
	 * Set a pins value
	 * @param pin
	 * @param value
	 * @return
	 */
	public GpioPin setValue(GpioPin pin, boolean value);
	
	public boolean getValue(GpioPin pin);
	
	public void close(GpioPin pin);
}
