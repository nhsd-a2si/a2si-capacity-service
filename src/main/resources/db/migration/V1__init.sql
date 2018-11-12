CREATE TABLE log_header (
  id BIGSERIAL PRIMARY KEY,
  action VARCHAR(4),
  component VARCHAR(25),
  user_id VARCHAR(25),
  endpoint VARCHAR(255),
  req_res_hashcode VARCHAR(10),
  timestamp TIMESTAMP DEFAULT NOW()
);

CREATE TABLE log_detail (
  id BIGSERIAL PRIMARY KEY,
  header_id BIGINT REFERENCES log_header(id),
  service_id VARCHAR(30),
  timestamp TIMESTAMP DEFAULT NOW(),
  wait_time_in_minutes INT,
  age_in_minutes INT
);

