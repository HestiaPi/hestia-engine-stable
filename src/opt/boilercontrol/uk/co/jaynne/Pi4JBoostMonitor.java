package uk.co.jaynne;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioListener;
import com.pi4j.io.gpio.event.GpioPinStateChangeEvent;

public class Pi4JBoostMonitor implements GpioListener {
	private boolean heating;
	private boolean water;
	private PinState PRESSED = PinState.HIGH;
	
	/**
	 * Monitors a pin for presses and activates boost
	 * @param pin the pin to monitor
	 * @param water whether this pin controls water
	 * @param heating whether this pin controls heating
	 */
	public Pi4JBoostMonitor(boolean water, boolean heating) {
		this.heating = heating;
		this.water = water;
	}

	@Override
	public void pinStateChanged(GpioPinStateChangeEvent event) {
		ControlBroker control = ControlBroker.getInstance();
		System.out.println("Boost pressed");
		if (event.getState() == PRESSED) {
			if (water) {
				control.toggleWaterBoostStatus();
			}
			if (heating) {
				control.toggleHeatingBoostStatus();
			} 
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
		}
	}
}
