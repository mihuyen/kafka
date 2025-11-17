#!/bin/bash

# Script to test failover: stop leader and verify system continues working

echo "=========================================="
echo "Kafka Failover Test"
echo "=========================================="
echo ""

BROKER_TO_STOP=${1:-1}

echo "Step 1: Check current cluster status before stopping broker..."
docker exec kafka1 kafka-topics --bootstrap-server localhost:9092 --topic message-topic --describe

echo ""
echo "Step 2: Stopping Kafka Broker $BROKER_TO_STOP..."
docker stop kafka$BROKER_TO_STOP

sleep 5

echo "Step 3: Check cluster status after stopping broker $BROKER_TO_STOP..."
docker exec kafka$(( ($BROKER_TO_STOP % 3) + 1 )) kafka-topics --bootstrap-server localhost:9092 --topic message-topic --describe

echo ""
echo "Step 4: Verify connection to remaining brokers..."
docker exec kafka$(( (($BROKER_TO_STOP + 1) % 3) + 1 )) kafka-consumer-groups --bootstrap-server localhost:9092 --list

echo ""
echo "Step 5: Test with remaining brokers..."
docker exec kafka$(( ($BROKER_TO_STOP % 3) + 1 )) kafka-console-producer --broker-list localhost:9092 --topic message-topic --sync << EOF
Test message while broker $BROKER_TO_STOP is down
EOF

echo ""
echo "=========================================="
echo "Failover test completed. System should be operational with remaining brokers."
echo "=========================================="