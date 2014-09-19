package uk.co.jaynne.gpio;

/**
 * Dummy GpioControl that does nothing 
 * @author james
 *
 */
public class GpioControlDummy implements GpioControl{
	
	private GpioControlDummy() {}
	
	private static class SingletonHolder { 
        public static final GpioControlDummy INSTANCE = new GpioControlDummy();
	}

	public static GpioControlDummy getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public GpioPin setAsOutput(GpioPin pin) {
		return pin;
	}

	@Override
	public GpioPin setAsInput(GpioPin pin) {
		return pin;
	}

	@Override
	public GpioPin setValue(GpioPin pin, boolean value) {
		return pin;
	}

	@Override
	public boolean getValue(GpioPin pin) {
		return true;
	}

	@Override
	public void close(GpioPin pin) {
	}
	
	public boolean isInPin(GpioPin pin) {
		return true;
	}
	
	public boolean isOutPin(GpioPin pin) {
		return true;
	}
}
