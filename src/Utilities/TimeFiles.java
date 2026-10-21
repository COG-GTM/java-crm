/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Commonly used functions for creating, adjusting, and formatting LocalDateTimes for both application and database use
 * @author Austin Wong
 */
public class TimeFiles {
    
    private static final DateTimeFormatter UITIMEFORMAT = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter UIDATEFORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");
    
    // Takes a LocalDate and a String representing time in "h:mm a" format
    // Returns a LocalDateTime
    public static LocalDateTime createLocalDateTime(LocalDate date, String time12){
        LocalDateTime ldt = LocalDateTime.of(date, LocalTime.parse(time12, UITIMEFORMAT));
        return ldt;
    }
    
    // Takes a LocalDateTime in the user's timezone
    // Returns the instant it represents as a java.sql.Timestamp, ready for PreparedStatement.setTimestamp
    public static Timestamp localDateTimeToDbTimestamp(LocalDateTime ldtUser){
        return Timestamp.from(ldtUser.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    // Takes a java.sql.Timestamp read from the DB (null-safe)
    // Returns a LocalDateTime in the user's timezone, automatically taking into account Daylight vs Standard time
    public static LocalDateTime dbTimestampToLocalDateTime(Timestamp ts){
        if(ts == null)
            return null;
        return LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault());
    }
    
    // Returns a fresh UTC Calendar for JDBC getTimestamp/setTimestamp
    // so the DATETIME columns are stored and read as UTC regardless of JVM or driver timezone settings
    public static Calendar utcCalendar(){
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }
    
    // Takes a LocalDateTime
    // Returns a String representing time in "h:mm a" format
    public static String localDateTimeToUITime(LocalDateTime ldt){
        String uiTime = ldt.format(UITIMEFORMAT);
        return uiTime;
    }
    
    // Takes a LocalDateTime
    // Returns a String representing date in "M/d/yyyy" format
    public static String localDateTimeToUIDate(LocalDateTime ldt){
        String uiDate = ldt.format(UIDATEFORMAT);
        return uiDate;
    }
    
    // Returns a list of appointment times from 8:00am - 5:00pm in half-hour increments
    public static ObservableList<String> getAvailableAppointmentTimes(){
        ObservableList<String> ltList = FXCollections.observableArrayList();
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 1); // .isBefore on next line is exclusive (< not <=) so we add a minute to include 17:00
        
        for(LocalTime lt = startTime; lt.isBefore(endTime); lt = lt.plusMinutes(30)){
            ltList.add(lt.format(UITIMEFORMAT));
        }
        return ltList;
    }
    
    // Takes a LocalDateTime representing start date of search period
    // Returns a list of start and end LocalDateTimes with either a rolling year or a calendar year
    // 
    // Rolling year: FROM 1 year before start TO start
    // Calendar year: FROM 1st day of start year TO last day of start year
    //
    // Note: rolling year calculates up to today, not through the end of the month
    public static ObservableList<LocalDateTime> getSearchTimes(LocalDateTime start, Boolean rolling){
        
        ObservableList<LocalDateTime> searchTimes = FXCollections.observableArrayList();
        LocalDateTime startSearch;
        LocalDateTime endSearch;

        // Rolling Year
        if(rolling == true){
            startSearch = start.minusYears(1);
            endSearch = start;
        }
        // Calendar Year
        else{
            LocalDate ld = start.toLocalDate();
            startSearch = LocalDateTime.of(ld.withDayOfYear(1), LocalTime.MIN);
            endSearch = LocalDateTime.of(ld.withDayOfYear(ld.lengthOfYear()), LocalTime.MAX);
        }  
        
        searchTimes.addAll(startSearch, endSearch);
        
        return searchTimes;
    }
    
}
