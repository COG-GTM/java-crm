-- ============================================================================
-- Migration 001: Role-Based Access Control (RBAC)
--
-- Adds a `role` table and links each `user` to a role via a new `roleId` FK
-- column. This is the first SQL script committed to the repo; there was no
-- migration tooling previously, so run this manually against the CRM schema.
--
-- Idempotency: statements are written to be safe to re-run where MySQL allows
-- it (INSERT ... ON DUPLICATE KEY UPDATE for seeds). The ALTER statements are
-- not guarded because MySQL lacks "ADD COLUMN IF NOT EXISTS" on older
-- versions; run them once.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. Role table
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role (
    roleId   INT         NOT NULL AUTO_INCREMENT,
    roleName VARCHAR(50) NOT NULL,
    PRIMARY KEY (roleId),
    UNIQUE KEY uq_role_roleName (roleName)
);

-- Seed the roles. roleName values must match the Model.Role enum constants.
INSERT INTO role (roleName) VALUES
    ('ADMIN'),
    ('STANDARD'),
    ('READ_ONLY')
ON DUPLICATE KEY UPDATE roleName = VALUES(roleName);

-- ----------------------------------------------------------------------------
-- 2. Add roleId to the user table as a FK to role.roleId
-- ----------------------------------------------------------------------------
ALTER TABLE user
    ADD COLUMN roleId INT NULL AFTER active;

-- Default existing users to STANDARD so the app keeps working after migration.
UPDATE user
SET roleId = (SELECT roleId FROM role WHERE roleName = 'STANDARD')
WHERE roleId IS NULL;

-- Enforce the relationship. (Left nullable so historical rows without a role
-- do not break; the DAO/enum treats a missing role as READ_ONLY.)
ALTER TABLE user
    ADD CONSTRAINT fk_user_role
    FOREIGN KEY (roleId) REFERENCES role (roleId);

-- ----------------------------------------------------------------------------
-- 3. (Optional) Promote a known admin account.
--    Adjust the userName to match a real administrator in your environment.
-- ----------------------------------------------------------------------------
-- UPDATE user
-- SET roleId = (SELECT roleId FROM role WHERE roleName = 'ADMIN')
-- WHERE userName = 'test';
