package uk.co.jaynne;

import uk.co.jaynne.gpio.GpioControl;
import uk.co.jaynne.gpio.GpioControlPi4J;
import uk.co.jaynne.gpio.GpioPin;
import java.util.Date;

public class BoostMonitor extends Thread{
	private GpioPin pin;
	private boolean heating;
	private boolean water;
	private boolean pinsHigh;
	boolean status; 
	private int bldelay = 30;  //  delay in seconds for backlight to dim
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
		GpioControl gpio = GpioControlPi4J.getInstance();
		ControlBroker control = ControlBroker.getInstance();
		gpio.setAsInput(pin);
		
		while (!Thread.interrupted()) {
			status = gpio.getValue(pin);
			try {
			if (status == pinsHigh) {
				if (control.isBacklightOn()) {
					if (water) {
						if (control.isWaterOn() && !control.isWaterBoostOn())
						{
							//Scheduled on so need to manual override to off
							//System.out.println("Water is scheduled on so need to manual override to off");
							control.turnWaterOff();
							control.OverrideWaterSchedule = true;
						} else {
							//Standard boost action
							//System.out.println("Standard boost action");
							control.toggleWaterBoostStatus();
						}
					}
					if (heating) {
						if (control.isHeatingOn() && !control.isHeatingBoostOn())
						{
							//Scheduled on so need to manual override to off
							//System.out.println("Heating is scheduled on so need to manual override to off");
							control.turnHeatingOff();
							control.OverrideHeatingSchedule = true;
						} else {
							//Standard boost action
							//System.out.println("Standard boost action");
							control.toggleHeatingBoostStatus();
						}
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
