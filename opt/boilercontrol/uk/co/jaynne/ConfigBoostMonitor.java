package uk.co.jaynne;

import uk.co.jaynne.datasource.ConfigSqlSource;
import uk.co.jaynne.datasource.interfaces.ConfigSource;

public class ConfigBoostMonitor extends Thread{
	private String key;
	private boolean heating;
	private boolean water;
	private int sleepTime = 20000;
	
	/**
	 * Monitors a pin for presses and activates boost
	 * @param pin the pin to monitor
	 * @param water whether this pin controls water
	 * @param heating whether this pin controls heating
	 */
	public ConfigBoostMonitor(String key, boolean water, boolean heating) {
		this.key = key;
		this.heating = heating;
		this.water = water;
	}
	public void run() { 
		ConfigSource config = new ConfigSqlSource();
		ControlBroker control = ControlBroker.getInstance();
		
		while (!Thread.interrupted()) {
			
			try {
				boolean status = config.get(key).getBoolValue();
			if (status) {
				if (water) {
					control.toggleWaterBoostStatus();
				}
				if (heating) {
					control.toggleHeatingBoostStatus();
				}
				config.set(key, false); //unset the toggle
			}
			Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				break;
			} catch (NullPointerException e) {
				System.out.println("Config Boost monitor unable to access config");
				break;
			}
		}
		System.out.println("Config Boost monitor interrupted");
	}

}
