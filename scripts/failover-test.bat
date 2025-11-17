@echo off
REM Script to test failover: stop leader and verify system continues working (Windows)

setlocal enabledelayedexpansion

echo ==========================================
echo Kafka Failover Test
echo ==========================================
echo.

set BROKER_TO_STOP=%1
if "!BROKER_TO_STOP!"=="" set BROKER_TO_STOP=1

echo Step 1: Check current cluster status before stopping broker...
docker exec kafka1 kafka-topics --bootstrap-server kafka1:29092,kafka2:29093,kafka3:29094 --topic message-topic --describe

echo.
echo Step 2: Stopping Kafka Broker !BROKER_TO_STOP!...
docker stop kafka!BROKER_TO_STOP!

timeout /t 5

echo.
echo Step 3: Check cluster status after stopping broker !BROKER_TO_STOP!...
if !BROKER_TO_STOP! equ 1 (
    docker exec kafka2 kafka-topics --bootstrap-server kafka2:29093,kafka3:29094 --topic message-topic --describe
) else if !BROKER_TO_STOP! equ 2 (
    docker exec kafka1 kafka-topics --bootstrap-server kafka1:29092,kafka3:29094 --topic message-topic --describe
) else (
    docker exec kafka1 kafka-topics --bootstrap-server kafka1:29092,kafka2:29093 --topic message-topic --describe
)

echo.
echo Step 4: Check remaining brokers...
if !BROKER_TO_STOP! equ 1 (
    docker exec kafka2 kafka-consumer-groups --bootstrap-server kafka2:29093,kafka3:29094 --list
) else if !BROKER_TO_STOP! equ 2 (
    docker exec kafka1 kafka-consumer-groups --bootstrap-server kafka1:29092,kafka3:29094 --list
) else (
    docker exec kafka1 kafka-consumer-groups --bootstrap-server kafka1:29092,kafka2:29093 --list
)

echo.
echo ==========================================
echo Failover test completed. System should be operational with remaining brokers.
echo ==========================================