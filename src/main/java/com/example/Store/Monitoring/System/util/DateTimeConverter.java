package com.example.Store.Monitoring.System.util;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

//Utility class
//Checking if a specific time falls within a given time range.
public class DateTimeConverter {

//    Converts a given UTC time to the time in the specified time zone.
    public static ZonedDateTime toZonedTime(ZonedDateTime utcTime, String zoneStr){
        ZoneId zoneId = ZoneId.of(zoneStr);
        return utcTime.withZoneSameInstant(zoneId);
    }

//    Determines whether a specific time falls within a given time interval.
//    This supports both regular and overnight ranges (e.g., 10 PM to 6 AM).
    public static boolean isWithin(LocalTime time, LocalTime start, LocalTime end){
        if(start.equals(end)){
//            If start and end are the same, assume it's a full-day range
            return true;
        }else if(start.isBefore(end)){
//            Normal case: time window doesn't roll over midnight
            return !time.isBefore(start) && !time.isAfter(end);
        }else{
//            Overnight window: time range rolls over to the next day
            return !time.isBefore(start) || !time.isAfter(end);
        }
    }

}
