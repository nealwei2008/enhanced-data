DROP TABLE IF EXISTS postgres_unit_test_user;

CREATE TABLE postgres_unit_test_user (
  id serial PRIMARY KEY,
  uid integer NOT NULL UNIQUE,
  display_name varchar(64) NOT NULL,
  score integer NOT NULL,
  profile jsonb NOT NULL
);

INSERT INTO postgres_unit_test_user (uid, display_name, score, profile) VALUES
  (1, 'alpha', 10, '{"channel":"amazon","tier":"gold"}'),
  (2, 'beta', 20, '{"channel":"ebay","tier":"silver"}'),
  (3, 'gamma', 30, '{"channel":"amazon","tier":"silver"}');
