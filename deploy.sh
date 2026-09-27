#!/usr/bin/env bash
# Rocky Linux 9 — SecureAuth 배포 스크립트
# 실행: chmod +x deploy.sh && sudo ./deploy.sh

set -euo pipefail

TOMCAT_HOME=/opt/tomcat
WEBAPPS=$TOMCAT_HOME/webapps
WAR_NAME=secauth
DB_NAME=secauth
DB_USER=secauth
DB_PASS="SecAuth2024\!"

echo "=== [1/5] 패키지 설치 ==="
dnf install -y java-17-openjdk maven mariadb-server 2>/dev/null || true

echo "=== [2/5] MariaDB 초기화 ==="
systemctl enable --now mariadb
mysql -u root <<SQL
CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL
mysql -u root ${DB_NAME} < src/main/resources/schema.sql
echo "MariaDB 설정 완료"

echo "=== [3/5] Tomcat 10 설치 (없으면) ==="
if [ ! -d "$TOMCAT_HOME" ]; then
    TOMCAT_VER=10.1.25
    curl -fsSL "https://dlcdn.apache.org/tomcat/tomcat-10/v${TOMCAT_VER}/bin/apache-tomcat-${TOMCAT_VER}.tar.gz" \
        -o /tmp/tomcat.tar.gz
    tar -xzf /tmp/tomcat.tar.gz -C /opt
    ln -sfn "/opt/apache-tomcat-${TOMCAT_VER}" $TOMCAT_HOME
    useradd -r -m -U -d $TOMCAT_HOME -s /bin/false tomcat 2>/dev/null || true
    chown -R tomcat:tomcat $TOMCAT_HOME
fi

echo "=== [4/5] 빌드 및 배포 ==="
mvn clean package -q
cp "target/${WAR_NAME}.war" "$WEBAPPS/"
echo "WAR 배포 완료: $WEBAPPS/${WAR_NAME}.war"

echo "=== [5/5] Tomcat 서비스 등록 및 시작 ==="
cat > /etc/systemd/system/tomcat.service <<EOF
[Unit]
Description=Apache Tomcat 10
After=network.target mariadb.service

[Service]
Type=forking
User=tomcat
Group=tomcat
Environment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk
Environment=CATALINA_HOME=${TOMCAT_HOME}
ExecStart=${TOMCAT_HOME}/bin/startup.sh
ExecStop=${TOMCAT_HOME}/bin/shutdown.sh
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable --now tomcat

# 방화벽 오픈
firewall-cmd --permanent --add-port=8080/tcp 2>/dev/null || true
firewall-cmd --reload 2>/dev/null || true

echo ""
echo "=== 배포 완료 ==="
echo "접속 URL: http://$(hostname -I | awk '{print $1}'):8080/${WAR_NAME}/"
echo "기본 계정: admin / Admin@2024"
