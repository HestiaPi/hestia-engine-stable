package uk.co.jaynne.datasource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import uk.co.jaynne.dataobjects.ScheduleObject;
import uk.co.jaynne.datasource.interfaces.ScheduleSource;

/**
 * Class to retrieve, update add and delte schedule items
 * @author James Cooke
 */
public class ScheduleSqlSource implements ScheduleSource{
	
	private static String TABLE = "schedule"; 
	private static String ID = "id"; 
	private static String GROUP = "group";
	private static String DAY = "day";
	private static String HOURON = "hourOn";
	private static String MINUTEON = "minuteOn";
	private static String HOUROFF = "hourOff";
	private static String MINUTEOFF = "minuteOff";
	private static String TEMPERATURE = "temperature";
	private static String HEATINGON = "heatingOn";
	private static String WATERON = "waterOn";
	private static String ENABLED = "enabled";
	private static int MINUTES_IN_HOUR = 60;

	
	public ScheduleObject getById(int id) {
		String sql = "SELECT * FROM `" + TABLE + "` WHERE `" + ID + "` = '" + id + "'";
		SqlStatement smt = new SqlStatement();
		
		ScheduleObject schedule = null;
		try {
			ResultSet rs = smt.query(sql);
		
			while (rs.next()) {
				schedule = getScheduleFromResultSet(rs);
			}
			
			rs.close(); //tidy up
		} catch (SQLException e) {
			return null;
		}
		return schedule;
	}
	
	/**
	 * Returns an ordered set of SheduleObjects containing all the schedules for the day
	 * @param day
	 * @return
	 */
	public Set<ScheduleObject> getByDay(int day) {
		String sql = "SELECT * FROM `" + TABLE + "` WHERE `" + DAY + "` = '" + day + "'";
		SqlStatement smt = new SqlStatement();
		
		Set<ScheduleObject> schedules = new HashSet<ScheduleObject>();
		try {
			ResultSet rs = smt.query(sql);
		
			while (rs.next()) {
				schedules.add(getScheduleFromResultSet(rs));
			}
			
			rs.close(); //tidy up
		} catch (SQLException e) {
			return null;
		}
		return schedules;
	}
	
	private ScheduleObject getScheduleFromResultSet(ResultSet rs) throws SQLException {
		try {
			return new ScheduleObject(
					rs.getInt(ID), 
					rs.getInt(GROUP), 
					rs.getInt(DAY),
					rs.getInt(HOURON),
					rs.getInt(MINUTEON),
					rs.getInt(HOUROFF),
					rs.getInt(MINUTEOFF),
					rs.getBoolean(HEATINGON),
					rs.getFloat(TEMPERATURE),
					rs.getBoolean(WATERON),
					rs.getBoolean(ENABLED));
		} catch (SQLException e) {
			throw e;
		}
	}
	
	private int save(ScheduleObject schedule) {
		String sql = "";
		if (schedule.getId() == 0) {
			sql = "INSERT INTO `" + TABLE + "` ";
		} else {
			sql = "UPDATE `" + TABLE + "` WHERE `" + ID + "` = '" + schedule.getId() + "' ";
		}
		 sql += "SET `" + DAY + "` = '" + schedule.getDay() + "', " +
				 "`" + GROUP + "` = '" + schedule.getGroup() + "', " +
				 "`" + HOURON + "` = '" + schedule.getHourOn() + "', " +
				 "`" + MINUTEON + "` = '" + schedule.getMinuteOn() + "', " +
				 "`" + HOUROFF + "` = '" + schedule.getHourOff() + "', " +
				 "`" + MINUTEOFF + "` = '" + schedule.getMinuteOff() + "', " +
				 "`" + HEATINGON + "` = '" + (schedule.getHeatingOn() ? 1 : 0) + "', " +
				 "`" + TEMPERATURE + "` = '" + schedule.getTempSet() + "', " +
				 "`" + WATERON + "` = '" + (schedule.getWaterOn() ? 1 : 0) + "', " +
		 		 "`" + ENABLED + "` = '" + (schedule.getEnabled() ? 1 : 0) + "'";
				
		 SqlStatement smt = new SqlStatement();
		
		try {
			return smt.update(sql);
		} catch (SQLException e) {
			return 0;
		}
	}
	
	public int update(ScheduleObject schedule) {
		if (schedule.getId() <= 0 || !validateSchedule(schedule)) {
			return 0;
		} else {
			return save(schedule);
			
		}
	}
	
	public int add(ScheduleObject schedule) {
		if (schedule.getId() != 0 || !validateSchedule(schedule)) {
			return 0;
		} else {
			return save(schedule);
		}
	}
	
	public int delete(int id) {

		String sql = "DELETE * FROM `" + TABLE + "` WHERE `" + ID + "` = '" + id + "' ";
				
		SqlStatement smt = new SqlStatement();
		
		try {
			return smt.update(sql);
		} catch (SQLException e) {
			return 0;
		}
	}
	
	/**
	 * Quick check if minute on and off and day are correct
	 * @param schedule
	 * @return if schedule is valid
	 */
	private boolean validateSchedule(ScheduleObject schedule) {
		if (schedule.getHourOff() < schedule.getHourOn() || 
				schedule.getDay() < 1 || schedule.getDay() > 7 ||
				schedule.getMinuteOn() < 0 || schedule.getMinuteOn() >= MINUTES_IN_HOUR || 
				schedule.getMinuteOff() < 0 || schedule.getMinuteOff() >= MINUTES_IN_HOUR ) {
			return false;
		} else {
			return true;
		}
	}
}
