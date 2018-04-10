package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class TimeLineHelper {

	private LocalDateTime dt;		// dot-name
	private int freq;				// #docs

	public TimeLineHelper(LocalDateTime dt, int freq) {
		this.dt = dt;
		this.freq = freq;
	}

	public LocalDateTime getTime() {
		return dt;
	}

	public int getFrequency() {
		return freq;
	}
	
	
	
	public static LocalDateTime longTOLocalDateTime(long minDate) {
		LocalDateTime time = LocalDateTime.ofEpochSecond(minDate, 0, ZoneOffset.UTC);
		return time;
	}
	
	
	public static long creationDateToLong(String timestamp) {
		
		if (timestamp == null)
			return -1;
		
		String[] datetime = timestamp.split(" ");
		String date_Str = datetime[0];
		String time_Str = datetime[1];
		String[] date_arr = date_Str.trim().split("-");
		String[] time_arr = time_Str.trim().split(":");
		if (date_arr.length == 3 && time_arr.length == 3) {
			// DATE
			int year = Integer.parseInt(date_arr[0]);
			int month = Integer.parseInt(date_arr[1]);
			int day = Integer.parseInt(date_arr[2]);
			// TIME
			int hour = Integer.parseInt(time_arr[0]);
			int min = Integer.parseInt(time_arr[1]);
			int sec = Integer.parseInt(time_arr[2]);
			
			LocalDate date = LocalDate.of(year, month, day);
			LocalTime time = LocalTime.of(hour, min, sec);
			
//			boolean isbefor = date.isBefore(mindate);
//			boolean isafter = date.isAfter(maxdate);
//			
//			if (isbefor || isafter ) {
//				continue;
//			}
			
			LocalDateTime dt = LocalDateTime.of(date, time);
			return dt.toEpochSecond(ZoneOffset.UTC);
		}
		return -1;
	}
	
}
