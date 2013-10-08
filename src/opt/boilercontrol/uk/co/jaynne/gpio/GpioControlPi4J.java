package uk.co.jaynne.gpio;

import java.util.HashMap;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioListener;

/**
 * GpioControl using the Pi4J library http://www.pi4j.com/ 
 * @author james
 *
 */
public class GpioControlPi4J implements GpioControl{
	
	private HashMap<uk.co.jaynne.gpio.GpioPin, com.pi4j.io.gpio.GpioPin> inpins;
	private HashMap<uk.co.jaynne.gpio.GpioPin, com.pi4j.io.gpio.GpioPin> outpins;
	private GpioController gpio;
	private PinState outDefaultState;
	
	private GpioControlPi4J() {
		gpio = GpioFactory.getInstance();
		inpins = new HashMap<uk.co.jaynne.gpio.GpioPin, com.pi4j.io.gpio.GpioPin>();
		outpins = new HashMap<uk.co.jaynne.gpio.GpioPin, com.pi4j.io.gpio.GpioPin>();
		outDefaultState = PinState.HIGH;
	}
	
	private static class SingletonHolder { 
        public static final GpioControlPi4J INSTANCE = new GpioControlPi4J();
	}

	public static GpioControlPi4J getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public synchronized GpioPin setAsOutput(GpioPin pin) {
		if (outpins.containsKey(pin)) {
			return pin; //already setup
		}
		if (inpins.containsKey(pin)) {
			inpins.remove(pin); //remove inpin
		}
		
		Pin translatedPin = getPin(pin);
		com.pi4j.io.gpio.GpioPin outpin = gpio.provisionOuputPin(translatedPin, pin.toString() , outDefaultState);
		outpins.put(pin, outpin);
		return pin;
	}

	@Override
	public synchronized GpioPin setAsInput(GpioPin pin) {
		if (inpins.containsKey(pin)) {
			return pin; //already setup
		}
		if (outpins.containsKey(pin)) {
			outpins.remove(pin); //remove inpin
		}
		
		Pin translatedPin = getPin(pin);
		com.pi4j.io.gpio.GpioPin outpin = gpio.provisionInputPin(translatedPin, pin.toString());
		inpins.put(pin, outpin);
		return pin;
	}

	@Override
	public synchronized GpioPin setValue(GpioPin pin, boolean value) {
		//Check if this pin is assigned as an out pin
		if (isInPin(pin)) {
			setAsOutput(pin);
		}
		if (!isOutPin(pin)) {
			setAsOutput(pin);
		}
		if (value) {
			outpins.get(pin).high();
		} else {
			outpins.get(pin).low();
		}
		return pin;
	}

	@Override
	public synchronized boolean getValue(GpioPin pin) {
		//Check if this pin is assigned as an out pin
		if (isOutPin(pin)) {
			setAsInput(pin);
		}
		if (!isInPin(pin)) {
			setAsInput(pin);
		}
		try {
			PinState state = inpins.get(pin).getState();
			if (state == PinState.HIGH) {
				return true;
			} else {
				return false;
			}
		}
		catch (Exception ex) {
			System.err.println("GpioControlPi4J.java>getValue() Exception: " + ex);
			System.out.println("GpioControlPi4J.java>getValue() Exception: " + ex);
			return false;
		}
	}

	@Override
	public synchronized void close(GpioPin pin) {
		//Close if outpin
		if (isOutPin(pin)) {
			outpins.get(pin).setShutdownOptions(true, outDefaultState);
			outpins.remove(pin);
		}
		if (isInPin(pin)) {
			inpins.get(pin).setShutdownOptions(true);
			inpins.remove(pin);
		}
	}
	
	public synchronized boolean isInPin(GpioPin pin) {
		return inpins.containsKey(pin);
	}
	
	public synchronized boolean isOutPin(GpioPin pin) {
		return outpins.containsKey(pin);
	}
	
	public synchronized void addListener(GpioPin pin, GpioListener listener) {
		if (isOutPin(pin)) {
			setAsInput(pin);
		}
		if (!isInPin(pin)) {
			setAsInput(pin);
		}
		com.pi4j.io.gpio.GpioPin listenPin = inpins.get(pin);
		listenPin.addListener(listener);
	}
	
	public synchronized void removeListeners(GpioPin pin){
		if (isOutPin(pin) || !isInPin(pin)) {
			return;
		}
		inpins.get(pin).removeAllListeners();
	}
	
	/**
	 * Translates the enum to the pin numbers used by the Framboos library
	 * @param pin GpioPin
	 * @return int pin no
	 */
/**
	private Pin getPin(GpioPin pin) {
		switch(pin) {
			case PIN3_GPIO0: return Pin.GPIO_08;
			case PIN5_GPIO1: return Pin.GPIO_09;
			case PIN7_GPIO4: return Pin.GPIO_07;
			case PIN8_GPIO14: return Pin.GPIO_15;
			case PIN10_GPIO15: return Pin.GPIO_16;
			case PIN11_GPIO17: return Pin.GPIO_00;
			case PIN12_GPIO18: return Pin.GPIO_01;
			case PIN13_GPIO21: return Pin.GPIO_02;
			case PIN15_GPIO22: return Pin.GPIO_03;
			case PIN16_GPIO23: return Pin.GPIO_04;
			case PIN18_GPIO24: return Pin.GPIO_05;
			case PIN19_GPIO10: return Pin.GPIO_12;
			case PIN21_GPIO9: return Pin.GPIO_13;
			case PIN22_GPIO25: return Pin.GPIO_06;
			case PIN23_GPIO11: return Pin.GPIO_14;
			case PIN24_GPIO8: return Pin.GPIO_10;
			case PIN26_GPIO7: return Pin.GPIO_11;
			default: return null;
		}
	}
/**/

	private Pin getPin(GpioPin pin) {
		switch(pin) {
			case PIN3_GPIO2: return Pin.GPIO_08;
			case PIN5_GPIO3: return Pin.GPIO_09;
			case PIN7_GPIO4: return Pin.GPIO_07;
			case PIN8_GPIO14: return Pin.GPIO_15;
			case PIN10_GPIO15: return Pin.GPIO_16;
			case PIN11_GPIO17: return Pin.GPIO_00;
			case PIN12_GPIO18: return Pin.GPIO_01;
			case PIN13_GPIO27: return Pin.GPIO_02;
			case PIN15_GPIO22: return Pin.GPIO_03;
			case PIN16_GPIO23: return Pin.GPIO_04;
			case PIN18_GPIO24: return Pin.GPIO_05;
			case PIN19_GPIO10: return Pin.GPIO_12;
			case PIN21_GPIO9: return Pin.GPIO_13;
			case PIN22_GPIO25: return Pin.GPIO_06;
			case PIN23_GPIO11: return Pin.GPIO_14;
			case PIN24_GPIO8: return Pin.GPIO_10;
			case PIN26_GPIO7: return Pin.GPIO_11;
			default: return null;
		}
	}
}
