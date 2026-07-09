-- UserServiceImpl.createUser checks email uniqueness case-insensitively
-- (existsByEmailIgnoreCase), but V1's plain UNIQUE constraint on users.email is
-- case-sensitive: two concurrent inserts differing only by case (Ada@example.com
-- vs ada@example.com) could both pass the application check and still be
-- accepted by the database. Replacing the constraint with a unique index on
-- lower(email) enforces the same case-insensitive invariant at the database
-- level, closing that race window.

ALTER TABLE users DROP CONSTRAINT users_email_key;

CREATE UNIQUE INDEX idx_users_email_lower ON users (LOWER(email));
