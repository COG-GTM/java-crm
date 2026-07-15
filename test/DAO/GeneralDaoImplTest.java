package DAO;

import static DAO.DaoTestSupport.DB_TS;
import static DAO.DaoTestSupport.stubAuditColumns;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import Utilities.TimeFiles;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link GeneralDaoImpl#getMetadata(ResultSet)} directly. Lives in the
 * {@code DAO} package so it can read the protected static audit fields the DAO
 * base class populates.
 */
class GeneralDaoImplTest {

    @Test
    void getMetadata_populatesAuditFields() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        stubAuditColumns(rs);

        GeneralDaoImpl.getMetadata(rs);

        assertEquals(DB_TS, GeneralDaoImpl.createDate);
        assertEquals(DB_TS, GeneralDaoImpl.lastUpdate);
        assertEquals("test", GeneralDaoImpl.createdBy);
        assertEquals("test", GeneralDaoImpl.lastUpdateBy);
        assertEquals(TimeFiles.dbStrToLocalDateTime(DB_TS), GeneralDaoImpl.createDateLdt);
        assertEquals(TimeFiles.dbStrToLocalDateTime(DB_TS), GeneralDaoImpl.lastUpdateLdt);
    }

    @Test
    void getMetadata_convertsDistinctCreateAndUpdateTimestamps() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("createDate")).thenReturn("2020-06-15 08:30:00.0");
        when(rs.getString("lastUpdate")).thenReturn("2022-09-20 17:45:00.0");
        when(rs.getString("createdBy")).thenReturn("creator");
        when(rs.getString("lastUpdateBy")).thenReturn("updater");

        GeneralDaoImpl.getMetadata(rs);

        assertEquals("creator", GeneralDaoImpl.createdBy);
        assertEquals("updater", GeneralDaoImpl.lastUpdateBy);
        assertEquals(TimeFiles.dbStrToLocalDateTime("2020-06-15 08:30:00.0"),
                GeneralDaoImpl.createDateLdt);
        assertEquals(TimeFiles.dbStrToLocalDateTime("2022-09-20 17:45:00.0"),
                GeneralDaoImpl.lastUpdateLdt);
    }
}
