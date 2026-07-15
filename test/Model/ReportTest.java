package Model;

import org.junit.jupiter.api.Test;
import testsupport.Fixtures;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportTest {

    @Test
    void getters_returnConstructorValues() {
        Report report = new Report("Appointments by Type", 5);

        assertEquals("Appointments by Type", report.getReportName());
        assertEquals(5, report.getReportId());
    }

    @Test
    void setters_updateValues() {
        Report report = Fixtures.report();

        report.setReportName("Schedule by Consultant");
        report.setReportId(9);

        assertEquals("Schedule by Consultant", report.getReportName());
        assertEquals(9, report.getReportId());
    }

    /**
     * Documents a latent bug in {@link Report#toString()}: it builds
     * "reportName [reportId]" but actually returns only reportName. This asserts
     * the ACTUAL current behavior (not the intended "name [id]" format) so the
     * discrepancy is captured without modifying src/.
     */
    @Test
    void toString_returnsReportNameOnly_dueToLatentBug() {
        Report report = Fixtures.report("Monthly Totals", 7);
        assertEquals("Monthly Totals", report.toString());
    }
}
