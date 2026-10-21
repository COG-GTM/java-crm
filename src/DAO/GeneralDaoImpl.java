/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import static Utilities.TimeFiles.dbTimestampToLocalDateTime;
import static Utilities.TimeFiles.localDateTimeToDbTimestamp;
import static Utilities.TimeFiles.utcCalendar;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Common variables and methods for DAO classes
 * @author Austin Wong
 */
public class GeneralDaoImpl {
    
    protected static String createdBy;
    protected static String lastUpdateBy;
    protected static LocalDateTime createDateLdt;
    protected static LocalDateTime lastUpdateLdt;
    
    protected static void getMetadata(ResultSet rs) throws SQLException {
        createdBy = rs.getString("createdBy");
        lastUpdateBy = rs.getString("lastUpdateBy");
        createDateLdt = readDateTime(rs, "createDate");
        lastUpdateLdt = readDateTime(rs, "lastUpdate");
    }
    
    // Reads a UTC DATETIME column and returns it in the user's timezone
    protected static LocalDateTime readDateTime(ResultSet rs, String column) throws SQLException {
        return dbTimestampToLocalDateTime(rs.getTimestamp(column, utcCalendar()));
    }
    
    // Binds a user-timezone LocalDateTime to a DATETIME parameter, stored as UTC
    protected static void bindDateTime(PreparedStatement ps, int parameterIndex, LocalDateTime ldt) throws SQLException {
        ps.setTimestamp(parameterIndex, localDateTimeToDbTimestamp(ldt), utcCalendar());
    }
}
