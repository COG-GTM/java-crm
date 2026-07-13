-- =====================================================================
-- Customer Notes feature - schema migration
-- =====================================================================
-- Creates the `note` table used by the Customer Notes feature. Each note
-- is linked to a customer via customerId (foreign key to customer.customerId).
-- The audit columns (createDate/createdBy/lastUpdate/lastUpdateBy) mirror the
-- existing `customer` and `appointment` tables.
--
-- Apply against the MySQL database configured in
-- src/DAO/DBConnection.java, e.g.:
--     mysql -h <host> -u <user> -p <database> < sql/note.sql
-- =====================================================================

CREATE TABLE IF NOT EXISTS note (
    noteId       INT(10)     NOT NULL AUTO_INCREMENT,
    customerId   INT(10)     NOT NULL,
    noteText     TEXT        NOT NULL,
    createDate   DATETIME    NOT NULL,
    createdBy    VARCHAR(40) NOT NULL,
    lastUpdate   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    lastUpdateBy VARCHAR(40) NOT NULL,
    PRIMARY KEY (noteId),
    CONSTRAINT fk_note_customer
        FOREIGN KEY (customerId) REFERENCES customer (customerId)
);
