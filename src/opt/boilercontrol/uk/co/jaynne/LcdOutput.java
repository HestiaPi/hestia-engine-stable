package uk.co.jaynne;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;

import uk.co.jaynne.lcd.LcdDisplay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.*; //DecimalFormat

public class LcdOutput extends Thread{
	public void run() {
		LcdDisplay lcd = LcdDisplay.getInstance();
		ControlBroker control = ControlBroker.getInstance();
		String line1 = "";
		String line2 = "";
		int GibberishReset=0;
		boolean RnD = true ; //TODO Set to true for extra logging and faster testing, disable upon release
		boolean hBoost = false;
		boolean wBoost = false;
		boolean heating = false;
		boolean water = false;
		boolean holiday = false;
		double dTemp = 0;
		double dDesiredTemp = 0;
		int LCDRefreshDelay = 1000; //TODO: This controls how responsive is the LCD. Need to adjust for production
		int readTempDelay = 0;
		
		line1 = getssid();
		line2 = getwlan0ip();
		lcd.write(LcdDisplay.LCD_LINE1, line1, LcdDisplay.RIGHT);
		lcd.write(LcdDisplay.LCD_LINE2, line2, LcdDisplay.RIGHT);
		line1 = "";
		line2 = "";
		try {
			if (RnD) //TODO 
				Thread.sleep(1000);
			else
				Thread.sleep(5000);
		} catch (InterruptedException e) {
			//break;
		}
		
		while (!Thread.interrupted()) {
			//Get time
			DateFormat timeFormat = new SimpleDateFormat("HH:mm");
			//timeFormat.setTimeZone(TimeZone.getDefault());
			//TimeZone tz = TimeZone.getDefault();
			Calendar cal = Calendar.getInstance();
/**
//System.out.println("Current Time: "+cal.getTime());
TimeZone z = cal.getTimeZone();
int offset = z.getRawOffset();
if(z.inDaylightTime(new Date())){
    offset = offset + z.getDSTSavings();
}
int offsetHrs = offset / 1000 / 60 / 60;
int offsetMins = offset / 1000 / 60 % 60;
//System.out.println("offset: " + offsetHrs);
//System.out.println("offset: " + offsetMins);
cal.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
cal.add(Calendar.MINUTE, (-offsetMins));
//System.out.println("Local Time: "+cal.getTime());
/**/
			String time = timeFormat.format(cal.getTime());
			//Get boost status
			hBoost = control.isHeatingBoostOn();
			wBoost = control.isWaterBoostOn();
			//Get heating and water status
			heating = control.isHeatingOn();
			water = control.isWaterOn();
			//Get holiday status
			holiday = control.isHolidayPeriod();
			
//			iNoDec = false;
			
			//Create output strings
			//Current W/H status (not Boost)
			/**
			line1 = time + " W:";
			line1 += (water) ? "+" : "-";
			line1 += " H:";
			line1 += (heating) ? "+" : "-";
			/**/
			
			/**
			line1 = time +"    W:";
			line1 += (wBoost) ? "+" : "-";
			line1 += " H:";
			line1 += (hBoost) ? "+" : "-";
			line1 += " ";
			//"1234567890ABCDEF"
			/**/
			
			if (!wBoost && !hBoost) {
				line1 = time +"   No Boost";
			} else if (wBoost && !hBoost) {
				line1 = time +" WaterBoost";
			} else if (!wBoost && hBoost) {
				line1 = time +" Heat Boost";
			} else if (wBoost && hBoost) {
				line1 = time +" Both Boost";
			}
			
			if (holiday) {
				line2 = "Holiday On";
			} else {
				//line2 = "BOOST W:";
								
				DecimalFormat dec = new DecimalFormat("###.#");
				if (readTempDelay == 0) {
					control.readtemperature(); //This should be the only place where readtemperature() will be called from
					readTempDelay++;
				} else if (readTempDelay < (5000/LCDRefreshDelay)) { // Re-read current temperature every 5 seconds only
					readTempDelay++;
				} else {
					readTempDelay = 0;
				}
				dTemp = control.getcurrent_temp();
				dDesiredTemp = control.getdesired_temp();
				//dTemp = -201.47;
				//System.out.printf("dTemp now is %f\n", dTemp);
				
				line2 = dec.format(dTemp) + "\u00DF";
				if (control.getuseCelsius())
					line2 += "C";
				else
					line2 += "F";
				if (dTemp%1 == 0) // Make the freed space from the missing .0
					line2 += "  ";
				line2 += " >> "; 
				if (dDesiredTemp%1 == 0) // Make the freed space from the missing .0
					line2 += "  ";
				line2 += dec.format(dDesiredTemp) + "\u00DF";
				if (control.getuseCelsius())
					line2 += "C";
				else
					line2 += "F";
			}

			/*****************************************************/
			/**                   GibberishReset                **/
			/**  This is a bug fix that was causing the LCD     **/
			/** to print gibberish due to mains interference.   **/
			/** calling lcd.lcd_init(); too often though causes **/
			/** the LCD to flicker so its only done every say   **/
			/** x writes to minimise flickering.                **/
			/*****************************************************/
			/** Possible solution found with a capacitor        **/
			/** but this is now only run every 1 hour           **/
			/** just to be on the safe                          **/
			/*****************************************************/
			//System.out.println("GibberishReset:"+GibberishReset);
			// 1440 writes are around 1 hour
			if(GibberishReset<1440)
			{
				GibberishReset++;
			}
			else
			{	
				//System.out.println("Calling local lcd_init()");
				lcd.lcd_init();
				GibberishReset = 0;
			}

			
			//System.out.println("L1:" + line1);
			//System.out.println("L2:" + line2);
			
			lcd.write(LcdDisplay.LCD_LINE1, line1, LcdDisplay.CENTER);
			lcd.write(LcdDisplay.LCD_LINE2, line2, LcdDisplay.CENTER);
			
			try {
				Thread.sleep(LCDRefreshDelay);
			} catch (InterruptedException e) {
				break;
			}
		}
		lcd.write(LcdDisplay.LCD_LINE1, "|    Hestia    |", LcdDisplay.CENTER);
		lcd.write(LcdDisplay.LCD_LINE2, "| Press  Reset |", LcdDisplay.CENTER);
		lcd.close();
		System.out.println("LCD output interrupted");
	}

/**	
	 double readtemperature() {
		    String s;
		    Process p;
		    int itemp_c;
		    double temp_c;
		    double temp_f;
		    double retVal = 0;
		    
		    try {
		    	p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/gettemperature.sh");
		        BufferedReader br = new BufferedReader(
		            new InputStreamReader(p.getInputStream()));
		        while ((s = br.readLine()) != null) {
		        	itemp_c = Integer.parseInt(s);
		        	temp_c = ((double) itemp_c)/1000.0;
		        	temp_f = (temp_c*9/5)+32;
					retVal = temp_c;
		        }
		        p.waitFor();
		        //System.out.println ("exit: " + p.exitValue());
		        p.destroy();
		    } catch (Exception e) {System.out.println(e);}
		    return retVal;
		}
/**/
	 
	 	String getwlan0ip() {
		    String s;
		    Process p;
		    String retVal = "";
		    try {
		        p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/getwlan0ip.sh");
		        BufferedReader br = new BufferedReader(
		            new InputStreamReader(p.getInputStream()));
		        while ((s = br.readLine()) != null) {
		        	retVal += s;
		        }
		        if (retVal.length() < 7) { // Try eth0 if no wlan0 IP found. 7 is the smallest IP length (1.1.1.1)
		        	p.waitFor();
			        p.destroy();
			        try {
				        p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/geteth0ip.sh");
				        br = new BufferedReader(
				            new InputStreamReader(p.getInputStream()));
				        while ((s = br.readLine()) != null) {
				        	retVal += s;
				        }
				        if (retVal.length() < 7) {
				        	retVal = "Error: No IP!"; 
				        } else if (retVal.length() < 14) { //If there is space, add label "IP:" in front
				        	retVal = "IP:" + retVal; 
				        }
				        p.waitFor();
				        p.destroy();
				    } catch (Exception e) {System.out.println(e);}
		        } else if (retVal.length() < 14) { //If there is space, add label "IP:" in front
		        	retVal = "IP:" + retVal; 
		        }
		        p.waitFor();
		        //System.out.println ("exit: " + p.exitValue());
		        p.destroy();
		    } catch (Exception e) {System.out.println(e);}
		    return retVal;
		}
	 	
	 	String getssid() {
		    String s;
		    Process p;
		    String retVal = "";
		    try {
		        p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/getssid.sh");
		        BufferedReader br = new BufferedReader(
		            new InputStreamReader(p.getInputStream()));
		        while ((s = br.readLine()) != null) {
		        	retVal += s;
		        }
		        if (retVal.length() < 1) {
		        	retVal = "Error: No WiFi!"; 
		        } else if (retVal.length() < 12) { //If there is space, add label "WiFi:" in front
		        	retVal = "WiFi:" + retVal; 
		        }
		        p.waitFor();
		        //System.out.println ("exit: " + p.exitValue());
		        p.destroy();
		    } catch (Exception e) {System.out.println(e);}
		    return retVal;
		}
}
