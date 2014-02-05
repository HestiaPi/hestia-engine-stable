package uk.co.jaynne;

import java.util.Calendar;
//import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import uk.co.jaynne.dataobjects.ConfigObject;
import uk.co.jaynne.datasource.ConfigSqlSource;
import uk.co.jaynne.datasource.interfaces.ConfigSource;
import uk.co.jaynne.gpio.GpioControl;
import uk.co.jaynne.gpio.GpioControlFramboos;
import uk.co.jaynne.gpio.GpioPin;

import java.text.*; //DecimalFormat

public class ControlBroker {
	//public static GpioPin RELAY1 = GpioPin.PIN8_GPIO14; // water //Replaced for UART boot issue
	public static GpioPin RELAY1 = GpioPin.PIN12_GPIO18; // water
	//public static GpioPin RELAY2 = GpioPin.PIN10_GPIO15; // heating //Replaced for UART boot issue
	public static GpioPin RELAY2 = GpioPin.PIN16_GPIO23; // heating
	public static GpioPin RELAY3 = GpioPin.PIN21_GPIO9; // backlight
	
	public static GpioPin SWITCH1 =  GpioPin.PIN18_GPIO24; // boost water
	public static GpioPin SWITCH2 =  GpioPin.PIN26_GPIO7; // boost heating
	public static GpioPin SWITCH3 =  GpioPin.PIN15_GPIO22; // +
	public static GpioPin SWITCH4 =  GpioPin.PIN11_GPIO17; // -
	//public static GpioPin SWITCH4 = GpioPin.PIN21_GPIO9; // -
	//DO NOT USE PIN23_GPIO11
	
//	private static boolean RELAY_ON = true;
    private static boolean RELAY_ON = false;
//	private static boolean RELAY_OFF = false;
    private static boolean RELAY_OFF = true;
    
	private static boolean BL_RELAY_ON = true;
	private static boolean BL_RELAY_OFF = false;
	
	private ConfigSource config;
	public boolean heatingOn = false;
	public boolean heatingOnBoost = false;
	public boolean heatingOnSchedule = false;
	private long heatingBoostOffTime = 0;
	public boolean waterOn = false;
	public boolean waterOnBoost = false;
	public boolean waterOnSchedule = false;
	private long waterBoostOffTime = 0;
	private boolean backlightOn = false;
	private GpioControl gpio;
	
    public double current_temp;
    public double current_temp_reading;
    public int incorrect_temp_readings = 10; // Dont filter the first time
    public double desired_temp = 22.0; // desired temperature (in Celsius) we wish to reach/keep. Default: 22.0
    public double desired_temp_step = 0.5;
	public boolean useCelsius = true; // TODO need to read this dynamically from configuration DB
    
	public double tempSensitivity = 0; // Default:1 TODO this should be setup according to mode:Comfort/Economy/etc
	
    public long startTime =  System.currentTimeMillis();
	private ControlBroker() {
		gpio = GpioControlFramboos.getInstance();
		config = new ConfigSqlSource();
		
    	//Start with water and heating off in case they have been left in an improper state
		deactivateHeating();
		deactivateWater();
	    turnBacklightOn();
	}
	
	private static class SingletonHolder { 
        public static final ControlBroker INSTANCE = new ControlBroker();
	}

	public static ControlBroker getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public GpioControl getGpio() {
		return gpio;
	}
	
	public boolean turnHeatingOn() {
		System.out.println("turnHeatingOn() called");
		//check if the heating is already on
		if (isHeatingOn()) {
			return true;
		} // don't turn on if this is a holiday period
		if (isHolidayPeriod()) {
			return false;
		}
		return activateHeating();
	}
	
	public boolean activateHeating() {
		gpio.setValue(RELAY2, RELAY_ON);
		heatingOn = true;
		System.out.println("H:ON");
		return true;
	}
	
	public boolean turnHeatingOff() {
		System.out.println("turnHeatingOff() called");
		if (!isHeatingOn()) {
			return true;
		}
		if (isHeatingBoostOn()) { //don't turn off if boosted
			return false;
		}
		return deactivateHeating();
	}
	
	public boolean deactivateHeating() {
		gpio.setValue(RELAY2, RELAY_OFF);
		heatingOn = false;
		System.out.println("H:OFF");
		return true;
	}
	
	public boolean turnWaterOn() {
		//check if the water is already on
		if (isWaterOn()) {
			return true;
		} // don't turn on if this is a holiday period
		if (isHolidayPeriod()) {
			return false;
		}
		
		return activateWater();
	}
	
	public boolean activateWater() {
		gpio.setValue(RELAY1, RELAY_ON);
		waterOn = true;
		System.out.println("W:ON");
		return true;
	}
	
	public boolean turnWaterOff() {
		if (!isWaterOn()) {
			return true;
		}
		if (isWaterBoostOn()) {
			return false;
		}
		return deactivateWater();
	}
	
	public boolean deactivateWater() {
		gpio.setValue(RELAY1, RELAY_OFF);
		waterOn = false;
		System.out.println("W:OFF");
		return true;
	}
	
	public boolean isHolidayPeriod(){
		ConfigObject holidayFrom = config.get("holidayFrom");
		ConfigObject holidayUntil = config.get("holidayUntil");
		if (holidayFrom != null && holidayUntil != null) {
			return Calendar.getInstance().getTimeInMillis() > holidayFrom.getLongValue() && 
					Calendar.getInstance().getTimeInMillis() < holidayUntil.getLongValue();
		} else {
			return false;
		}
	}
	
	public boolean toggleWaterBoostStatus() {
		int boostTimeInMins = config.get("boostTime").getIntValue();
		return toggleWaterBoostStatus(boostTimeInMins);
	}
	
	public boolean keyPressed () {
		startTime = System.currentTimeMillis(); // reset countdown with each keypress
		return true;
	}
	
	public boolean keyPressTimeReset () {
		startTime = 0L;
		return true;
	}
	
	public long getLastKeyPressTime() {
		return startTime;
	}
	
	public void setLastKeyPressTimeNow() { // Fake a keypress
		startTime =  System.currentTimeMillis();
	}
	
	public void increaseDesiredTemp() {
		desired_temp = desired_temp + desired_temp_step;
	}
	
	public void decreaseDesiredTemp() {
		desired_temp = desired_temp - desired_temp_step;
	}
	
	public double getcurrent_temp() {
		return current_temp;
	}
	
	public double getdesired_temp() {
		return desired_temp;
	}
	
	public boolean getuseCelsius() {
		return useCelsius;
	}
	
	public void thermostat() {
		if (isHeatingOn())
		{
			if (current_temp > (desired_temp + tempSensitivity)) {
				System.out.println("Above range > deactivateHeating()");
				//turnHeatingOff();
				deactivateHeating();
			}
		} else if ((isheatingOnSchedule() || isHeatingBoostOn()) && !isHeatingOn()) {
			if (current_temp < (desired_temp - tempSensitivity)) {
				System.out.println("Below range > activateHeating()");
				//turnHeatingOn();
				activateHeating();
			}
		}
	}
	
	public boolean readtemperature() {
	    String s;
	    Process p;
	    int itemp;
	    boolean retVal = false;

	    try {
	    	p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/gettemperature.sh");
	        BufferedReader br = new BufferedReader(
	            new InputStreamReader(p.getInputStream()));
	        while ((s = br.readLine()) != null) {
	        	itemp = Integer.parseInt(s);
	        	current_temp_reading = ((double) itemp)/1000.0;
	        	if (((current_temp_reading < current_temp - 5) || (current_temp_reading > current_temp + 5)) && (incorrect_temp_readings < 10)) {
	        		// Incorrect reading - filter out and keep old current_temp
	        		incorrect_temp_readings++;
	        		break;
	        	} else {
	        		incorrect_temp_readings = 0;
	        		current_temp = current_temp_reading;
	        	}
	        	
	        	if (!useCelsius) {
	        		current_temp = celsiusToFahrenheit(current_temp);
	        	}
	        	//temp_f = (temp*9/5)+32;
	        	DecimalFormat dec = new DecimalFormat("###.##");
	        	/**/
	        	if (useCelsius) {
	        		System.out.println("Current Temperature: " + dec.format(current_temp) + " C, " + "Desired Temperature: " + dec.format(desired_temp));
	        	} else {
	        		System.out.println("Temperature: " + dec.format(current_temp) + " F");
	        	}
	        	/**/
    			retVal = true;
	        }
	        p.waitFor();
	        //System.out.println ("exit: " + p.exitValue());
	        p.destroy();
	    } catch (Exception e) {System.out.println(e);}
	    return retVal;
	}
	
	public double celsiusToFahrenheit(double tempInitial) {
			return (tempInitial*9/5)+32;
	}

	
	public boolean validatemac() {
		/**************************************************************/
		/** This is a fct to lock the java executable file to the hw **/
		/** currently not being used.                                **/
		/**************************************************************/
		
	    String s;
	    Process p;
	    boolean retVal = false;
	    String[] cmd = {
	    		"/bin/sh",
	    		"-c",
	    		"ip addr show eth0 | grep link | awk '{print $2}'"
	    		};
	    try {
	    	//System.out.println("eth0: ip addr show eth0 | grep inet | awk \'{print $2}\' | cut -d/ -f1");
	        //p = Runtime.getRuntime().exec("/opt/boilercontrol/getmac.sh");
	    	p = Runtime.getRuntime().exec(cmd);
	        BufferedReader br = new BufferedReader(
	            new InputStreamReader(p.getInputStream()));
	        while ((s = br.readLine()) != null) {
	        	//"b8:27:eb:94:dd:bf"
	        	//if (s == "b8:27:eb:94:dd:bf")
	        	
	        	if (
	        			(s.charAt(0) == 'b') &&
	        			(s.charAt(1) == '8') &&
	        			(s.charAt(3) == '2') &&
	        			(s.charAt(4) == '7') &&
	        			(s.charAt(6) == 'e') &&
	        			(s.charAt(7) == 'b') &&
	        			(s.charAt(9) == '9') &&
	        			(s.charAt(10) == '4') &&
	        			(s.charAt(12) == 'd') &&
	        			(s.charAt(13) == 'd') &&
	        			(s.charAt(15) == 'b') &&
	        			(s.charAt(16) == 'f')
	        			) 
        		{
        			//System.out.println ("MAC confirmed:" + s);
        			retVal = true;
        		}
	        	else
	        	{
	        		//System.out.println ("MAC altered!!!");
	        	}
	        }
	        p.waitFor();
	        //System.out.println ("exit: " + p.exitValue());
	        p.destroy();
	    } catch (Exception e) {System.out.println(e);}
	    return retVal;
	}
	
	public void geteth0() {
	    String s;
	    Process p;
	    String[] cmd = {
	    		"/bin/sh",
	    		"-c",
	    		"ip addr show eth0 | grep inet | awk '{print $2}' | cut -d/ -f1"
	    		};
	    try {
	    	//System.out.println("eth0: ip addr show eth0 | grep inet | awk \'{print $2}\' | cut -d/ -f1");
	        p = Runtime.getRuntime().exec(cmd);
	        BufferedReader br = new BufferedReader(
	            new InputStreamReader(p.getInputStream()));
	        while ((s = br.readLine()) != null)
	            System.out.println("cmd eth0: " + s);
	        p.waitFor();
	        //System.out.println ("exit: " + p.exitValue());
	        p.destroy();
	    } catch (Exception e) {System.out.println(e);}
	}
	
	public void getwlan0() {
	    String s;
	    Process p;
	    String[] cmd = {
	    		"/bin/sh",
	    		"-c",
	    		"ip addr show wlan0 | grep inet | awk '{print $2}' | cut -d/ -f1"
	    		};
	    try {
	    	//System.out.println("eth0: ip addr show eth0 | grep inet | awk \'{print $2}\' | cut -d/ -f1");
	        p = Runtime.getRuntime().exec(cmd);
	        BufferedReader br = new BufferedReader(
	            new InputStreamReader(p.getInputStream()));
	        while ((s = br.readLine()) != null)
	            System.out.println("cmd wlan0: " + s);
	        p.waitFor();
	        //System.out.println ("exit: " + p.exitValue());
	        p.destroy();
	    } catch (Exception e) {System.out.println(e);}
	}
	
	public boolean toggleWaterBoostStatus(int minutes) {
		if (isWaterBoostOn()) { //ON so turn off
			System.out.println("WB:OFF");
			waterOnBoost = false;
			waterBoostOffTime = 0;
			setLastKeyPressTimeNow(); // In case boost was selected from web GUI
			turnBacklightOn();
			turnWaterOff();
			return isWaterOn();
		} else if (isHolidayPeriod()) {
			System.out.println("WB:HOLIDAY");
			waterBoostOffTime = 0;
			turnWaterOff();
			return isWaterOn();
		} else {
			System.out.println("WB:ON");
			long thistime = Calendar.getInstance().getTimeInMillis();
			long boostTimeInMillis = minutes * 60 * 1000;
			waterBoostOffTime = thistime + boostTimeInMillis;
			waterOnBoost = true;
			setLastKeyPressTimeNow(); // In case boost was selected from web GUI
			turnBacklightOn();
			turnWaterOn();
			return isWaterOn();
		}
	}
	
	/**
	 * Gets the water boost off time in millis since the 'epoch'
	 * @return long
	 */
	public long getWaterBoostOffTime() {
		return waterBoostOffTime;
	}
	
	public int getWaterBoostOffMinutes() {
		int getWaterBoostOffMinutes = 0;
		if (waterBoostOffTime > 0) {
			getWaterBoostOffMinutes = (int)(waterBoostOffTime-(Calendar.getInstance().getTimeInMillis()))/(60*1000);
		}
		return getWaterBoostOffMinutes;
	}
	
	public boolean toggleHeatingBoostStatus() {
		int boostTimeInMins = config.get("boostTime").getIntValue();
		return toggleHeatingBoostStatus(boostTimeInMins);
	}
	
	public boolean toggleHeatingBoostStatus(int minutes) {
		System.out.println("toggleHeatingBoostStatus() called");
		if (isHeatingBoostOn()) { //ON so turn off
			System.out.println("HB:OFF");
			heatingOnBoost = false;
			heatingBoostOffTime = 0;
			setLastKeyPressTimeNow(); // In case boost was selected from web GUI
			turnBacklightOn();
			turnHeatingOff();
			return isHeatingOn();
		} else if (isHolidayPeriod()) {
			System.out.println("HB:HOLIDAY");
			heatingBoostOffTime = 0;
			turnHeatingOff();
			return isHeatingOn();
		} else {
			System.out.println("HB:ON");
			long thistime = Calendar.getInstance().getTimeInMillis();
			long boostTimeInMillis = minutes * 60 * 1000;
			heatingBoostOffTime = thistime + boostTimeInMillis;
			heatingOnBoost = true;
			setLastKeyPressTimeNow(); // In case boost was selected from web GUI
			turnBacklightOn();
			turnHeatingOn();
			return isHeatingOn();
		}
	}
	
	/**
	 * Gets the water boost off time in millis since the 'epoch'
	 * @return long
	 */
	public long getHeatingBoostOffTime() {
		return heatingBoostOffTime;
	}
	
	public int getHeatingBoostOffMinutes() {
		int getHeatingBoostOffMinutes = 0;
		if (heatingBoostOffTime > 0) {
			getHeatingBoostOffMinutes = (int)(heatingBoostOffTime-(Calendar.getInstance().getTimeInMillis()))/(60*1000);
		}
		return getHeatingBoostOffMinutes;
	}
	
	public boolean isHeatingOn() {
		return heatingOn;
	}
	
	public boolean isHeatingBoostOn() {
		return heatingOnBoost;
	}
	
	public boolean isheatingOnSchedule() {
		return heatingOnSchedule;
	}
	
	public boolean isWaterOn() {
		return waterOn;
	}
	
	public boolean isWaterBoostOn() {
		return waterOnBoost;
	}

	public boolean iswaterOnSchedule() {
		return waterOnSchedule;
	}
	
	public boolean isBacklightOn() {
		return backlightOn;
	}
	
	public boolean toggleBacklight() {
		if (isBacklightOn()) { //ON so turn off
			//System.out.println("BL was ON");
			turnBacklightOff();
			return isBacklightOn();
		} else {
			//System.out.println("BL was OFF");
			turnBacklightOn();
			return isBacklightOn();
		}
	}
	
	public boolean turnBacklightOff() {
		//System.out.println("turnBacklightOff()");
		if (!isBacklightOn()) {
			return true;
		}
		gpio.setValue(RELAY3, BL_RELAY_OFF);
		backlightOn = false;
		//System.out.println("BL is OFF");
		return true;
	}
	
	public boolean turnBacklightOn() {
		//System.out.println("turnBacklightOn()");
		if (isBacklightOn()) {
			return true;
		}
		gpio.setValue(RELAY3, BL_RELAY_ON);
		backlightOn = true;
		//System.out.println("BL is ON");
		return true;
	}
	
	/**
	 * Open GPIO connections
	 */
	public void open() {
		gpio.setAsOutput(RELAY1);
		gpio.setAsOutput(RELAY2);
		gpio.setAsOutput(RELAY3);
	}
	
	/**
	 * Close GPIO connections
	 */
	public void close() {
		gpio.close(RELAY1);
		gpio.close(RELAY2);
		gpio.close(RELAY3);
	}
}