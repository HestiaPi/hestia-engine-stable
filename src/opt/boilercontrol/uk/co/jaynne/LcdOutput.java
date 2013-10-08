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
			boolean hBoost = control.isHeatingBoostOn();
			boolean wBoost = control.isWaterBoostOn();
			//Get heating and water status
			boolean heating = control.isHeatingOn();
			boolean water = control.isWaterOn();
			//Get holiday status
			boolean holiday = control.isHolidayPeriod();
			
			boolean iNoDec = false;
			
			//Create output strings
			line1 = time + " W:";
			line1 += (water) ? "+" : "-";
			line1 += " H:";
			line1 += (heating) ? "+" : "-";
			
			if (holiday) {
				line2 = "Holiday On";
			} else {
				//line2 = "BOOST W:";
								
				DecimalFormat dec = new DecimalFormat("###.#");
				double dTemp = readtemperature();
				//dTemp = -201.47;
				if (dTemp%1 == 0)
					iNoDec = true;
				//TODO NEED TO MANAGE DISPLAY SPACE FOR NEGATIVE VALUES TOO
				if (((dTemp > 99.999) && (dTemp < 999.999)) || ((dTemp < -99.999) && (dTemp > -999.999))) {//3 digits! - no space for degree symbol unless there are no decimals
					if (iNoDec)
						line2 = " " + dec.format(dTemp)+ "\u00DF" + "C W:";
					else
						line2 = dec.format(dTemp)+"C W:";
					line2 += (wBoost) ? "+" : "-";
					line2 += " H:";
					line2 += (hBoost) ? "+" : "-";
					line2 += "  ";
				} else if ((dTemp > 9.999) || (dTemp < -9.999)) {//2 digits
					if (iNoDec)
						line2 = dec.format(dTemp)+ ".0\u00DF" + "C W:";
					else
						line2 = dec.format(dTemp)+ "\u00DF" + "C W:";
					//Degree symbol - The real degree symbol (\u00B0) is not in the supported char list of the display, 
					//so this supported one is the closest match
					line2 += (wBoost) ? "+" : "-";
					line2 += " H:";
					line2 += (hBoost) ? "+" : "-";
					line2 += "  ";
				} else { //1 digit
					if (iNoDec)
						line2 = " " + dec.format(dTemp) + ".0\u00DF" + "C W:";
					else
						line2 = " " + dec.format(dTemp) + "\u00DF" + "C W:";
					line2 += (wBoost) ? "+" : "-";
					line2 += " H:";
					line2 += (hBoost) ? "+" : "-";
					line2 += "  ";
				}
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

			
			System.out.println("L1:" + line1);
			System.out.println("L2:" + line2);
			
			lcd.write(LcdDisplay.LCD_LINE1, line1, LcdDisplay.CENTER);
			lcd.write(LcdDisplay.LCD_LINE2, line2, LcdDisplay.CENTER);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
		lcd.write(LcdDisplay.LCD_LINE1, "|    Hestia    |", LcdDisplay.CENTER);
		lcd.write(LcdDisplay.LCD_LINE2, "| Press  Reset |", LcdDisplay.CENTER);
		lcd.close();
		System.out.println("LCD output interrupted");
	}
	
	
	 double readtemperature() {
		    String s;
		    Process p;
		    int itemp_c;
		    double temp_c;
		    double temp_f;
		    double retVal = 0;
		    
		    /*
		    String[] cmd = {
		    		"/bin/sh",
		    		"-c",
		    		"FILE=$(ls /sys/bus/w1/devices/ | sort -f | head -1)",
		    		"cat /sys/bus/w1/devices/$FILE/w1_slave | grep t= | cut -d= -f2"
		    		};
		    */

		    try {
		    	//System.out.println("eth0: ip addr show eth0 | grep inet | awk \'{print $2}\' | cut -d/ -f1");
		        //p = Runtime.getRuntime().exec("/opt/boilercontrol/getmac.sh");
		    	//p = Runtime.getRuntime().exec(cmd);
		    	p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/gettemperature.sh");
		        BufferedReader br = new BufferedReader(
		            new InputStreamReader(p.getInputStream()));
		        while ((s = br.readLine()) != null) {
		        	itemp_c = Integer.parseInt(s);
		        	temp_c = ((double) itemp_c)/1000.0;
		        	temp_f = (temp_c*9/5)+32;
		        	//DecimalFormat dec = new DecimalFormat("###.##");
		        	//System.out.println ("Temperature: " + itemp_c + " mC");
		        	//System.out.println ("Temperature: " + temp_c + " C");
		        	//System.out.println("Temperature: " + dec.format(temp_c) + " C / " + dec.format(temp_f) + " F");
					retVal = temp_c;
		        }
		        p.waitFor();
		        //System.out.println ("exit: " + p.exitValue());
		        p.destroy();
		    } catch (Exception e) {System.out.println(e);}
		    return retVal;
		}
	 
	 	String getwlan0ip() {
		    String s;
		    Process p;
		    String retVal = "Error:No IP!";
		    try {
		        p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/getwlan0ip.sh");
		        BufferedReader br = new BufferedReader(
		            new InputStreamReader(p.getInputStream()));
		        while ((s = br.readLine()) != null) {
		        	retVal = s;
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
		    String retVal = "Error:No WiFi!";
		    try {
		        p = Runtime.getRuntime().exec("/opt/boilercontrol/scripts/getssid.sh");
		        BufferedReader br = new BufferedReader(
		            new InputStreamReader(p.getInputStream()));
		        while ((s = br.readLine()) != null) {
		        	retVal = "WiFi:"+s;
		        }
		        p.waitFor();
		        //System.out.println ("exit: " + p.exitValue());
		        p.destroy();
		    } catch (Exception e) {System.out.println(e);}
		    return retVal;
		}
}
