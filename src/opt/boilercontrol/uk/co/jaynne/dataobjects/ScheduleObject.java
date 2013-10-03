package uk.co.jaynne.dataobjects;

/**
 * Represents a single schedule item
 * @author James Cooke
 */
public class ScheduleObject{
	private int id = 0;
	private int group = 0;
	private int day = 0;
	private int hourOn = 0;
	private int minuteOn = 0;
	private int hourOff = 0;
	private int minuteOff = 0;
	private boolean heatingOn = false;
	private boolean waterOn = false;
	private boolean enabled = false;
	private String sday = "";  
	
	
	public ScheduleObject(int id, int group, int day, int hourOn, int minuteOn, 
			int hourOff, int minuteOff, boolean heatingOn, boolean waterOn, 
			boolean enabled) {
		this.id = id;
		this.group = group;
		this.day = day;
		this.hourOn = hourOn;
		this.minuteOn = minuteOn;
		this.hourOff = hourOff;
		this.minuteOff = minuteOff;
		this.heatingOn = heatingOn;
		this.waterOn = waterOn;
		this.enabled = enabled;
	}


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}


	/**
	 * @return the hourOn
	 */
	public int getHourOn() {
		return hourOn;
	}


	/**
	 * @return the hourOff
	 */
	public int getHourOff() {
		return hourOff;
	}


	/**
	 * @return the minuteOn
	 */
	public int getMinuteOn() {
		return minuteOn;
	}


	/**
	 * @return the minuteOff
	 */
	public int getMinuteOff() {
		return minuteOff;
	}


	/**
	 * @return the heatingOn
	 */
	public boolean getHeatingOn() {
		return heatingOn;
	}


	/**
	 * @return the waterOn
	 */
	public boolean getWaterOn() {
		return waterOn;
	}

	/**
	 * @return the group
	 */
	public int getGroup() {
		return group;
	}


	/**
	 * @return the enabled
	 */
	public boolean getEnabled() {
		return enabled;
	}
/**
	public String toString() {
		return "Day:" + day + " Time On:" + hourOn + ":" + minuteOn + " Time Off:" + 
				hourOff + ":" + minuteOff + " Heating On:" + heatingOn + " Water On:" + 
				waterOn + " Group:" + group + " Enabled:" + enabled;
	}
**/	
	public String toString() {
		switch (day)
		{
		case 1:
			sday = "Sunday";
			break;
		case 2:
			sday = "Monday";
			break;
		case 3:
			sday = "Tuesday";
			break;
		case 4:
			sday = "Wednesday";
			break;
		case 5:
			sday = "Thursday";
			break;
		case 6:
			sday = "Friday";
			break;
		case 7:
			sday = "Saturday";
			break;	
		}
		return sday + ": Period:" + hourOn + ":" + minuteOn + " - " + 
				hourOff + ":" + minuteOff + ", H: " + heatingOn + ", W: " + 
				waterOn + ", Group:" + group + ", Enabled:" + enabled;
	}
}
