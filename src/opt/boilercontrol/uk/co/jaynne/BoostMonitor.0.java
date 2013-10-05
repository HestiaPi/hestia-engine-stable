package uk.co.jaynne;

import uk.co.jaynne.gpio.GpioControl;
import uk.co.jaynne.gpio.GpioControlPi4J;
import uk.co.jaynne.gpio.GpioPin;

public class BoostMonitor extends Thread{
	private GpioPin pin;
	private boolean heating;
	private boolean water;
	private boolean pinsHigh;
	boolean status;
	private int bltimeout = 0; 
	private int bldelay = 50;  //10*200ms=2sec
	
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
				bltimeout = 0;
				if (control.isBacklightOn())
				{
					if (water) {
						control.toggleWaterBoostStatus();
					}
					if (heating) {
						control.toggleHeatingBoostStatus();
					}
				}
				Thread.sleep(200); //sleep to ignore multiple presses
			}
			if (bltimeout == 0)
			{
				bltimeout++;
				control.turnBacklightOn();
			}
			else if (bltimeout < bldelay)
			{
				bltimeout++;
				Thread.sleep(200); // wait BL timeout interval
			}
			else if (bltimeout == bldelay)
			{
				bltimeout = bldelay+1;
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
