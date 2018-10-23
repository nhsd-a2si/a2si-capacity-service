-- Service
CREATE TABLE service (
  service_id SERIAL PRIMARY KEY,
  service_name VARCHAR(100),
  created TIMESTAMP DEFAULT NOW(),
  modified TIMESTAMP
);

-- Wait times
CREATE TABLE wait_times (
  wait_time_id BIGSERIAL PRIMARY KEY,
  last_updated TIMESTAMP,
  service_id int REFERENCES service(service_id),
  wait_time_in_minutes INT,
  region VARCHAR(100),
  provider VARCHAR(100),
  created TIMESTAMP DEFAULT NOW(),
  modified TIMESTAMP
);

-- Last modified function
CREATE OR REPLACE FUNCTION update_modified_column()
  RETURNS TRIGGER AS $$
BEGIN
  NEW.modified = now();
  RETURN NEW;
END;
$$ language 'plpgsql';

-- Gard the date created from being modified.
CREATE OR REPLACE FUNCTION gard_created_column()
  RETURNS TRIGGER AS $$
BEGIN
  IF tg_op = 'UPDATE' THEN
    NEW.created = OLD.created;
  END IF;
  RETURN NEW;
END;
$$ language 'plpgsql';

-- Adds the constraints to wait_times
CREATE TRIGGER update_modified_column BEFORE INSERT OR UPDATE ON wait_times
  FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
CREATE TRIGGER gard_created_column BEFORE INSERT OR UPDATE ON wait_times
  FOR EACH ROW EXECUTE PROCEDURE gard_created_column();

-- Adds teh constrains to service
CREATE TRIGGER update_modified_column BEFORE INSERT OR UPDATE ON service
  FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
CREATE TRIGGER gard_created_column BEFORE INSERT OR UPDATE ON service
  FOR EACH ROW EXECUTE PROCEDURE gard_created_column();