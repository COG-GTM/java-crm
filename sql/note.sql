-- Customer Notes feature -- schema for the `note` table.
--
-- This project's MySQL database is provisioned externally (see DAO/DBConnection.java),
-- so this script documents / provisions the schema change required by the Customer
-- Notes feature. Apply it against the CRM database once:
--
--     mysql -h <host> -u <user> -p <database> < sql/note.sql
--
-- Column types and audit fields mirror the existing `customer` and `appointment`
-- tables (audit timestamps + createdBy/lastUpdateBy VARCHAR).
-- The foreign key cascades on delete so that removing a customer removes their notes,
-- matching the customer -> appointment cascade behaviour used elsewhere in the app.

-- Note on the audit columns: the application formats/parses timestamps with a
-- single fractional-second digit ("yyyy-MM-dd HH:mm:ss.S"), so the DATETIME
-- columns use fractional precision (1) so the value round-trips correctly.
CREATE TABLE IF NOT EXISTS note (
    noteId        INT(10)      NOT NULL AUTO_INCREMENT,
    customerId    INT(10)      NOT NULL,
    noteText      TEXT         NOT NULL,
    createDate    DATETIME(1)  NOT NULL,
    createdBy     VARCHAR(40)  NOT NULL,
    lastUpdate    DATETIME(1)  NOT NULL,
    lastUpdateBy  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (noteId),
    KEY customerId (customerId),
    CONSTRAINT note_ibfk_1 FOREIGN KEY (customerId)
        REFERENCES customer (customerId)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
