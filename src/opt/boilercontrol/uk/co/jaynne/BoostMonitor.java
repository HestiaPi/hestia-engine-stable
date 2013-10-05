package uk.co.jaynne;

import uk.co.jaynne.gpio.GpioControl;
import uk.co.jaynne.gpio.GpioControlFramboos;
import uk.co.jaynne.gpio.GpioPin;
import java.util.Date;

public class BoostMonitor extends Thread{
	private GpioPin pin;
	private boolean heating;
	private boolean water;
	private boolean pinsHigh;
	boolean status; 
	private int bldelay = 20;  //in seconds
	private long startTime = System.currentTimeMillis();
	private long elapsedTime = 0L;
	
	/**
	 * Monitors a pin for presses and activates boost
	 * @param pin the pin to monitor
	 * @param water whether this pin controls water
	 * @param heating whether this pin controls heating
	 * @param pinsHigh are pins high (true) when pressed or low
	 */
	public BoostMonitor(GpioPin pin, boolean water, boolean heating, boolean pinsHigh) {
		this.pin = pin;
		this.heating = heating;
		this.water = water;
		this.pinsHigh = pinsHigh;
	}
	public void run() {
		GpioControl gpio = GpioControlFramboos.getInstance();
		ControlBroker control = ControlBroker.getInstance();
		gpio.setAsInput(pin);
		
		while (!Thread.interrupted()) {
			status = gpio.getValue(pin);
			try {
			if (status == pinsHigh) {
				if (control.isBacklightOn()) {
					if (water) {
						control.toggleWaterBoostStatus();
					}
					if (heating) {
						control.toggleHeatingBoostStatus();
					}
				}
				control.turnBacklightOn();
				startTime = System.currentTimeMillis(); // reset countdown
				Thread.sleep(200); //sleep to ignore multiple presses
			}
			if (startTime > 0) {
				elapsedTime = (new Date()).getTime() - startTime;
			} else {
				elapsedTime = 0;
			}
			
			if (elapsedTime > (bldelay*1000)) {
				startTime = 0L;
				control.turnBacklightOff();
			}
			
			Thread.sleep(20);
			} catch (InterruptedException e) {
				break;
			}
		}
		gpio.close(pin);
		System.out.println("Boost monitor interrupted");
	}

}
