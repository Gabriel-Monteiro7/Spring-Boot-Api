DROP TABLE IF EXISTS Product;
CREATE TABLE Product
(
  id INT
    PRIMARY KEY,
  name VARCHAR
  (250) NOT NULL,
  description VARCHAR
  (250) NOT NULL,
  category VARCHAR
  (250) NOT NULL
);
  -- image TEXT NOT NULL ,
  -- value NUMERIC DEFAULT NULL