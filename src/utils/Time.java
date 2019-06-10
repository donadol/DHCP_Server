package utils;

import java.time.Duration;
import java.time.LocalTime;

public class Time {
	public static LocalTime getHora() {
		return LocalTime.now();
	}
	public static long difference(LocalTime time) {
		LocalTime now = LocalTime.now();
		Duration duration = Duration.between(now, time);
		System.out.println("Duration: "+duration.getSeconds());
	    return Math.abs(duration.getSeconds());
	}
}
