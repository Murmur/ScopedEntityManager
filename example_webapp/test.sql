
CREATE DATABASE IF NOT EXISTS test DEFAULT CHARACTER SET utf8 COLLATE utf8_swedish_ci;
USE test;

CREATE TABLE IF NOT EXISTS orderheader (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  custid bigint(20) NOT NULL,
  comment text,
  updated_utc datetime NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS orderrow (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  headerid bigint(20) NOT NULL,
  comment text,
  qty int(11) NOT NULL,
  updated_utc datetime NOT NULL,
  PRIMARY KEY (id),
  KEY orderrow_fk (headerid),
  CONSTRAINT orderrow_fk FOREIGN KEY (headerid) REFERENCES orderheader(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
