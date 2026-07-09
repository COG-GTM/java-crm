/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Austin Wong
 */
public class DBConnection {
    
    //<editor-fold desc="variables">
    
    // JDBC URL parts
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String ipAddress = "//3.227.166.251/U07k1T";
    // Connection parameters required by MySQL Connector/J 8.x
    private static final String connectionParams = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000";
    
    // JDBC URL
    private static final String jdbcURL = protocol + vendorName + ipAddress + connectionParams;
    
    // Connection Interface Reference
    private static Connection conn = null;
    
    // Username and Password
    private static final String username = "U07k1T";
    private static final String password = "53689053296";
    
    //</editor-fold>
    
    public static Connection startConnection(){
        
        try{
            // MySQL Connector/J 8.x (com.mysql.cj.jdbc.Driver) auto-registers via the
            // JDBC 4 ServiceLoader, so an explicit Class.forName(...) call is not needed.
            conn = DriverManager.getConnection(jdbcURL, username, password);
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
            Logger.getLogger("errorlog.txt").log(Level.SEVERE,null,e);
        }
        
        return conn;
        
    }
    
    public static Connection getConnection(){
        return conn;
    }
    
    public static void closeConnection(){
        
        try{
            conn.close();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        
    }
}
