package uk.co.jaynne;

import uk.co.jaynne.gpio.GpioControl;
import uk.co.jaynne.gpio.GpioControlFramboos;
import uk.co.jaynne.gpio.GpioPin;
import java.util.Date;

public class BoostMonitor extends Thread{
	private GpioPin pin;
	private boolean heating;
	private boolean water;
	private boolean plus;
	private boolean minus;
	private boolean pinsHigh;
	boolean status; 
	private int bldelay = 20;  //in seconds
	private long elapsedTime = 0L;
	
	/**
	 * Monitors a pin for presses and activates boost
	 * @param pin the pin to monitor
	 * @param water whether this pin controls water
	 * @param heating whether this pin controls heating
	 * @param pinsHigh are pins high (true) when pressed or low
	 */
	public BoostMonitor(GpioPin pin, boolean water, boolean heating, boolean plus, boolean minus, boolean pinsHigh) {
		this.pin = pin;
		this.heating = heating;
		this.water = water;
		this.plus = plus;
		this.minus = minus;
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
				control.keyPressed();
				
				if (control.isBacklightOn()) {
					if (water) {
						System.out.println("Water Boost pressed");
						control.toggleWaterBoostStatus();
					}
					if (heating) {
						System.out.println("Heating Boost pressed");
						control.toggleHeatingBoostStatus();
					}
					if (plus) {
						System.out.println("Plus pressed");
						//control.toggleWaterBoostStatus();
					}
					if (minus) {
						System.out.println("Minus pressed");
						//control.toggleHeatingBoostStatus();
					}
				}
				
				control.turnBacklightOn();
				Thread.sleep(200); //sleep to ignore multiple presses
			}
			
			if (control.getLastKeyPressTime() > 0) {
				elapsedTime = (new Date()).getTime() - control.getLastKeyPressTime();
			} else {
				elapsedTime = 0;
			}
			
			if (elapsedTime > (bldelay*1000)) {
				control.keyPressTimeReset();
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
