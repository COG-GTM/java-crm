/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.time.LocalDateTime;

/**
 *
 * @author Austin Wong
 */
public class User {
    
    //<editor-fold defaultstate="collapsed" desc="role and permission enums">
    
    // Application roles ordered by privilege level (higher level = more access)
    public enum Role {
        USER(1),
        MANAGER(2),
        ADMIN(3);
        
        private final int level;
        
        Role(int level){
            this.level = level;
        }
        
        public int getLevel(){
            return level;
        }
        
        // Parse a role from a database string, defaulting to USER when null/unknown
        public static Role fromString(String roleStr){
            if(roleStr == null)
                return USER;
            try{
                return Role.valueOf(roleStr.trim().toUpperCase());
            }
            catch(IllegalArgumentException e){
                return USER;
            }
        }
    }
    
    // Discrete permissions, each requiring a minimum role
    public enum Permission {
        VIEW_REPORTS(Role.MANAGER),
        DELETE_CUSTOMER(Role.MANAGER),
        DELETE_APPOINTMENT(Role.MANAGER);
        
        private final Role minRole;
        
        Permission(Role minRole){
            this.minRole = minRole;
        }
        
        public Role getMinRole(){
            return minRole;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="static variables and methods">
    private static User currentUser;
    
    public static User getCurrentUser(){
        return currentUser;
    }
    
    public static void setCurrentUser(User user){
        currentUser = user;
    }
    
    // Convenience null-safe check against the currently logged-in user
    public static boolean currentUserHasPermission(Permission permission){
        return currentUser != null && currentUser.hasPermission(permission);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="db variables">
    private int userId;
    private String userName;
    private String password;
    private boolean active;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime lastUpdate;
    private String lastUpdateBy;
    private Role role;
    //</editor-fold>
    
    // Constructor
    public User(int userId, String userName, String password, boolean active, LocalDateTime createDate, String createdBy, LocalDateTime lastUpdate, String lastUpdateBy, Role role){
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.active = active;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdateBy = lastUpdateBy;
        this.role = role;
    }
    
    //<editor-fold defaultstate="collapsed" desc="db variable setters and getters">
    public int getUserId(){
        return userId;
    }
    
    public void setUserId(int userId){
        this.userId = userId;
    }
    
    public String getUserName(){
        return userName;
    }
    
    public void setUserName(String userName){
        this.userName = userName;
    }
    
    public String getPassword(){
        return password;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public boolean isActive(){
        return active;
    }
    
    public void setActive(boolean active){
        this.active = active;
    }
    
    public LocalDateTime getCreateDate(){
        return createDate;
    }
    
    public void setCreateDate(LocalDateTime createDate){
        this.createDate = createDate;
    }
    
    public String getCreatedBy(){
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getLastUpdate(){
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate){
        this.lastUpdate = lastUpdate;
    }
    
    public String getLastUpdateBy(){
        return lastUpdateBy;
    }
    
    public void setLastUpdateBy(String lastUpdateBy){
        this.lastUpdateBy = lastUpdateBy;
    }
    
    public Role getRole(){
        return role;
    }
    
    public void setRole(Role role){
        this.role = role;
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="permission logic">
    
    // Returns true if this user's role is at least as privileged as the given role
    public boolean isAtLeast(Role r){
        return role != null && r != null && role.getLevel() >= r.getLevel();
    }
    
    // Returns true if this user's role grants the given permission
    public boolean hasPermission(Permission permission){
        return permission != null && isAtLeast(permission.getMinRole());
    }
    
    //</editor-fold>
    
    // toString override for ComboBox
    // Format: "userName [userId]"
    @Override
    public String toString(){
        String displayName = userName + " [" + userId + "]";
        return displayName;
    }
    
}
