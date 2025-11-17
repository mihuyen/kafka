@echo off
REM Script to check Kafka cluster status and leader election (Windows)

setlocal enabledelayedexpansion

echo ==========================================
echo Kafka Cluster Leader Status Check
echo ==========================================
echo.

echo 1. Checking Topic 'message-topic' metadata:
docker exec kafka1 kafka-topics --bootstrap-server kafka1:29092,kafka2:29093,kafka3:29094 --topic message-topic --describe
echo.

echo 2. Checking All Brokers in Cluster:
docker exec zookeeper zookeeper-shell localhost:2181 ls /brokers/ids
echo.

echo 3. Getting Controller Information:
docker exec zookeeper zookeeper-shell localhost:2181 get /controller
echo.

echo 4. Broker Details:
docker exec kafka1 kafka-broker-api-versions --bootstrap-server kafka1:29092
echo.
docker exec kafka2 kafka-broker-api-versions --bootstrap-server kafka2:29093
echo.
docker exec kafka3 kafka-broker-api-versions --bootstrap-server kafka3:29094
echo.

echo ==========================================