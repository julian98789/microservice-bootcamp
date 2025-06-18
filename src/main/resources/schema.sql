
CREATE TABLE IF NOT EXISTS bootcamp (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(90),
  release_date DATE,
  duration INT
);
