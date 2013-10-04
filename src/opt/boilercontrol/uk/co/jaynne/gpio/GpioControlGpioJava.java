package uk.co.jaynne.gpio;

import be.doubleyouit.raspberry.gpio.Boardpin;
import be.doubleyouit.raspberry.gpio.Direction;
import be.doubleyouit.raspberry.gpio.GpioAlreadyExportedException;
import be.doubleyouit.raspberry.gpio.GpioGateway;
import be.doubleyouit.raspberry.gpio.impl.GpioGatewayImpl;

/**
 * GPIO control using the gpio-java library http://code.google.com/p/rpi-gpio-java/
 * @author James Cooke
 *
 */
public class GpioControlGpioJava implements GpioControl{	
	private GpioGateway gpio;
	
	private GpioControlGpioJava() {
		gpio = new GpioGatewayImpl();
	}
	
	private static class SingletonHolder { 
        public static final GpioControlGpioJava INSTANCE = new GpioControlGpioJava();
	}

	public static GpioControlGpioJava getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * Set a pin as an output
	 * @param bpin
	 * @return
	 */
	public GpioPin setAsOutput(GpioPin pin) {
		Boardpin bpin = getPin(pin);
		try {
			if (!gpio.isExported(bpin)) {
				gpio.export(bpin);
			}
		} catch (GpioAlreadyExportedException e) {}
		if (gpio.getDirection(bpin) != Direction.OUT) {
			gpio.setDirection(bpin, Direction.OUT);
		}
		return pin;
	}
	
	/**
	 * Set a pin as an input
	 * @param bpin
	 * @return
	 */
	public GpioPin setAsInput(GpioPin pin) {
		Boardpin bpin = getPin(pin);
		try {
			if (!gpio.isExported(bpin)) {
				gpio.export(bpin);
			}
		} catch (GpioAlreadyExportedException e) {}
		if (gpio.getDirection(bpin) != Direction.IN) {
			gpio.setDirection(bpin, Direction.IN);
		}
		return pin;
	}
	
	/**
	 * Set a pins value
	 * @param bpin
	 * @param value
	 * @return
	 */
	public GpioPin setValue(GpioPin pin, boolean value) {
		Boardpin bpin = getPin(pin);
		gpio.setValue(bpin, value);
		return pin;
	}
	
	public boolean getValue(GpioPin pin) {
		return gpio.getValue(getPin(pin));
	}
	
	public void close(GpioPin pin) {
		gpio.unexport(getPin(pin));
	}
	
	private Boardpin getPin(GpioPin pin) { //TODO map all pins
		switch(pin) {
		  case PIN3_GPIO2: return Boardpin.GPIO_08;
		  case PIN5_GPIO3: return Boardpin.GPIO_09;
		  case PIN7_GPIO4: return Boardpin.GPIO_07;
		  case PIN8_GPIO14: return Boardpin.GPIO_15;
		  case PIN10_GPIO15: return Boardpin.GPIO_16;
		  case PIN11_GPIO17: return Boardpin.GPIO_00;
		  case PIN12_GPIO18: return Boardpin.GPIO_01;
		  case PIN13_GPIO27: return Boardpin.GPIO_02;
		  case PIN15_GPIO22: return Boardpin.GPIO_03;
		  case PIN16_GPIO23: return Boardpin.GPIO_04;
		  case PIN18_GPIO24: return Boardpin.GPIO_05;
		  case PIN19_GPIO10: return Boardpin.GPIO_12;
		  case PIN21_GPIO9: return Boardpin.GPIO_13;
		  case PIN22_GPIO25: return Boardpin.GPIO_06;
		  case PIN23_GPIO11: return Boardpin.GPIO_04;
		  case PIN24_GPIO8: return Boardpin.GPIO_10;
		  case PIN26_GPIO7: return Boardpin.GPIO_11;
			default: return null;
		}
	}
}
