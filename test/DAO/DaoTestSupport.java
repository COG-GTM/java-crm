package DAO;

import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Group B (DAO layer) test helpers.
 *
 * <p>Small conveniences shared by the DAO unit tests. Lives in the {@code DAO}
 * package so it can reach the package-private / protected members that the DAOs
 * expose, and so it does not collide with other groups' test packages.</p>
 */
final class DaoTestSupport {

    /**
     * A valid DB-format timestamp string (pattern {@code yyyy-MM-dd HH:mm:ss.S})
     * that {@link Utilities.TimeFiles#dbStrToLocalDateTime(String)} can parse.
     */
    static final String DB_TS = "2021-01-01 12:00:00.0";

    static final String ACTOR = "test";

    private DaoTestSupport() {
    }

    /**
     * Stubs the four audit columns that {@link GeneralDaoImpl#getMetadata} reads
     * so that a mocked {@link ResultSet} row survives the (real) date parsing.
     */
    static void stubAuditColumns(ResultSet rs) throws SQLException {
        when(rs.getString("createDate")).thenReturn(DB_TS);
        when(rs.getString("lastUpdate")).thenReturn(DB_TS);
        when(rs.getString("createdBy")).thenReturn(ACTOR);
        when(rs.getString("lastUpdateBy")).thenReturn(ACTOR);
    }
}
