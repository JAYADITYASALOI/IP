CREATE DATABASE IF NOT EXISTS jn_electro_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE jn_electro_db;
CREATE TABLE IF NOT EXISTS accounts (
  account_id INT AUTO_INCREMENT PRIMARY KEY,
  user_name VARCHAR(80) NOT NULL UNIQUE,
  email_addr VARCHAR(150) NOT NULL UNIQUE,
  pwd_hash VARCHAR(512) NOT NULL,
  display_name VARCHAR(150),
  created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS articles (
  article_id INT AUTO_INCREMENT PRIMARY KEY,
  headline VARCHAR(512) NOT NULL,
  teaser VARCHAR(1024),
  body TEXT,
  provider_name VARCHAR(255),
  provider_link VARCHAR(1024),
  pub_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  trending_flag TINYINT(1) DEFAULT 0,
  created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY ux_articles_providerlink (provider_link(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_articles_pub_time ON articles(pub_time);
CREATE INDEX idx_articles_trending ON articles(trending_flag);
CREATE TABLE IF NOT EXISTS activity_log (
  log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  action_type VARCHAR(100) NOT NULL,
  action_payload JSON NULL,
  created_ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_activity_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO articles (headline, teaser, body, provider_name, provider_link, trending_flag, pub_time)
VALUES
('Sample Headline Alpha', 'Short teaser for Alpha', 'Full body text or excerpt for article Alpha', 'SampleSource', 'https://sample.example/alpha', 1, NOW() - INTERVAL 1 HOUR),
('Sample Headline Beta', 'Short teaser for Beta', 'Full body text or excerpt for article Beta', 'SampleSource', 'https://sample.example/beta', 0, NOW() - INTERVAL 3 HOUR);