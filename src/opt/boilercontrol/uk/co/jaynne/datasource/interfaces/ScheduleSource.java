package uk.co.jaynne.datasource.interfaces;

import java.util.Set;

import uk.co.jaynne.dataobjects.ScheduleObject;

/**
 * Interface to retrieve, update add and delte schedule items
 * @author James Cooke
 */
public interface ScheduleSource {
	public ScheduleObject getById(int id);
	/**
	 * Returns an ordered set of SheduleObjects containing all the schedules for the day
	 * @param day
	 * @return
	 */
	public Set<ScheduleObject> getByDay(int day);
	public int update(ScheduleObject schedule);
	public int add(ScheduleObject schedule);
	public int delete(int key);
	
}
