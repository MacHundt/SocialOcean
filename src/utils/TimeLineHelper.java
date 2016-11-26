package utils;

import java.time.LocalDateTime;

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
	
	
	
}
