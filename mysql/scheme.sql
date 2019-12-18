-- docker run --name mymysql \
--   -v ./mysql:/docker-entrypoint-initdb.d \
--   -p 3306:3306 \
--   -e MYSQL_ROOT_PASSWORD=root \
--   -e MYSQL_DATABASE=mymysql \
--   -e MYSQL_USER=mymysql \
--   -e MYSQL_PASSWORD=mymysql \
--   -d mysql:8.0.18

CREATE TABLE access_history(
  id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  size BIGINT,
  created_at_epoch_millis BIGINT
)
