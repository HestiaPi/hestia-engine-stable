package uk.co.jaynne;

import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.Date;

import uk.co.jaynne.dataobjects.ScheduleObject;
import uk.co.jaynne.datasource.ScheduleSqlSource;
import uk.co.jaynne.datasource.interfaces.ScheduleSource;

public class Scheduler extends Thread{
	private ControlBroker control = ControlBroker.getInstance();
	
    public void run() {
		while (!Thread.interrupted()) {
			//Heating and water default to off
			Calendar calendar = Calendar.getInstance();
			
			/**/
			TimeZone z = calendar.getTimeZone();
		    int offset = z.getRawOffset();
		    if(z.inDaylightTime(new Date())){
		        offset = offset + z.getDSTSavings();
		    }
		    int offsetHrs = offset / 1000 / 60 / 60;
		    int offsetMins = offset / 1000 / 60 % 60;
		    calendar.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
		    calendar.add(Calendar.MINUTE, (-offsetMins));
			/**/
			
			boolean heating = false;
			boolean water = false;
			
			//Get the day and minute
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			int minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
			
			ScheduleSource ss = new ScheduleSqlSource();
			//Get the schedules for today
			Set<ScheduleObject> schedules = ss.getByDay(day);
			if (schedules == null) {
				System.out.println("No schedules today");
			} else {
				
				System.out.println("Time: " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
				System.out.println("WB: " + control.isWaterBoostOn() + ", WB Finish: " + control.getWaterBoostOffTime());
				System.out.println("HB: " + control.isHeatingBoostOn() + ", HB Finish: " + control.getHeatingBoostOffTime());
				
				//Loop through all of todays schedules
				for (ScheduleObject schedule : schedules) {
					if (schedule.getEnabled()) { //if enabled
						System.out.print("*");
						System.out.print(schedule);
						//if in active period
						int timeOnMins = (schedule.getHourOn() * 60) + schedule.getMinuteOn();
						int timeOffMins = (schedule.getHourOff() * 60) + schedule.getMinuteOff();
						if (minute >= timeOnMins && minute <= timeOffMins) {
							System.out.print(" **ACTIVE**");
							//Only update the h/w status if they are false
							heating = (heating) ? heating : schedule.getHeatingOn();
							water = (water) ? water : schedule.getWaterOn();
						}
						System.out.println();
					} else {
						System.out.print("-");
						System.out.println(schedule);
					}
				}
				System.out.println("***********************");
			}
			
			//Change the heating controls based on the schedule
			if (heating) { //scheduled so turn on
				control.turnHeatingOn();
			} else if (control.isHeatingBoostOn() && 
					calendar.getTimeInMillis() > control.getHeatingBoostOffTime()) {
				//If boost is on but it is past the boost time turn off
				control.toggleHeatingBoostStatus();
			} else { //no schedule or boost so turn off
				control.turnHeatingOff();
			}
			
			//Change the water controls based on the schedule
			if (water) { //scheduled so turn on
				control.turnWaterOn();
			} else if (control.isWaterBoostOn() && 
					calendar.getTimeInMillis() > control.getWaterBoostOffTime()) {
				//If boost is on but it is past the boost time turn off
				control.toggleWaterBoostStatus();
			} else { //no schedule or boost so turn off
				control.turnWaterOff();
			}

			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				break;
			}
		}
		System.out.println("Scheduler interrupted");
    }
}
