/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 * User roles for role-based access control (RBAC).
 *
 * Each constant maps to a {@code roleName} value in the {@code role} database
 * table (see {@code db/001_add_rbac.sql}). The capability groups declared below
 * centralize the authorization policy so controllers pass a named group to
 * {@code GeneralController.hasPermission(...)} instead of scattering string
 * comparisons across the UI layer.
 *
 * Policy summary:
 *   ADMIN     - full access (view/add/update/delete customers and appointments, reports)
 *   STANDARD  - add/update customers and appointments, view reports; no customer delete
 *   READ_ONLY - view only; no add/update/delete
 */
public enum Role {

    ADMIN("ADMIN"),
    STANDARD("STANDARD"),
    READ_ONLY("READ_ONLY");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    // Resolves a database roleName to a Role.
    // Falls back to READ_ONLY (least privilege) when the value is unknown/null.
    public static Role fromRoleName(String roleName) {
        if (roleName != null) {
            for (Role role : values()) {
                if (role.roleName.equalsIgnoreCase(roleName)) {
                    return role;
                }
            }
        }
        return READ_ONLY;
    }

    //<editor-fold defaultstate="collapsed" desc="capability groups (centralized policy)">

    // Roles allowed to add/update customers.
    public static final Role[] MANAGE_CUSTOMERS = { ADMIN, STANDARD };

    // Roles allowed to delete customers (destructive - ADMIN only).
    public static final Role[] DELETE_CUSTOMERS = { ADMIN };

    // Roles allowed to add/update/delete appointments.
    public static final Role[] MANAGE_APPOINTMENTS = { ADMIN, STANDARD };

    // Roles allowed to view reports.
    public static final Role[] VIEW_REPORTS = { ADMIN, STANDARD };

    //</editor-fold>
}
