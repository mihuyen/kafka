#!/bin/bash

# Script to check Kafka cluster status and leader election

echo "=========================================="
echo "Kafka Cluster Leader Status Check"
echo "=========================================="
echo ""

# Check broker info and leader status
echo "1. Checking Topic 'message-topic' metadata:"
docker exec kafka1 kafka-topics --bootstrap-server localhost:9092 --topic message-topic --describe

echo ""
echo "2. Checking All Brokers in Cluster:"
docker exec zookeeper zookeeper-shell localhost:2181 ls /brokers/ids

echo ""
echo "3. Getting Controller Information:"
docker exec zookeeper zookeeper-shell localhost:2181 get /controller

echo ""
echo "4. Broker Details:"
for i in 1 2 3; do
    echo "--- Broker $i Info ---"
    docker exec kafka$i kafka-broker-api-versions --bootstrap-server localhost:9092 | head -3
    docker exec kafka$i kafka-configs --bootstrap-server localhost:9092 --entity-type brokers --entity-name $i --describe 2>/dev/null || echo "Broker $i info not available"
done

echo ""
echo "=========================================="