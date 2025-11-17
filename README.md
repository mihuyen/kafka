# Hệ Thống Kafka Cluster với Spring Boot

Dự án này triển khai một hệ thống Kafka cluster gồm 3 node với 2 ứng dụng Spring Boot trao đổi dữ liệu JSON qua Kafka. Hệ thống bao gồm kiểm tra leader election và test failover.

## Mục Lục

- [Giới Thiệu](#giới-thiệu)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cài Đặt và Chạy](#cài-đặt-và-chạy)
- [Các Tính Năng](#các-tính-năng)
- [Kiểm Tra Leader Election](#kiểm-tra-leader-election)
- [Test Failover](#test-failover)
- [API Documentation](#api-documentation)
- [Khắc Phục Sự Cố](#khắc-phục-sự-cố)

## Giới Thiệu

### Thành Phần Chính

1. **Kafka Cluster** - 3 broker (kafka1, kafka2, kafka3)
2. **Zookeeper** - Quản lý cluster coordination
3. **Producer Service** - Spring Boot ứng dụng gửi JSON messages
4. **Consumer Service** - Spring Boot ứng dụng nhận và lưu JSON messages
5. **Kafka UI** - Web interface quản lý Kafka cluster

### Công Nghệ Sử Dụng

- Apache Kafka 7.4.0
- Spring Boot 3.2.0
- Java 17
- Docker & Docker Compose
- H2 Database (cho Consumer)

## Yêu Cầu Hệ Thống

### Bắt Buộc

- Docker Desktop (bao gồm Docker Compose)
- Java Development Kit (JDK) 17 hoặc cao hơn
- Maven 3.8.0 hoặc cao hơn
- PowerShell hoặc Bash shell

### Khuyến Nghị

- Ít nhất 4GB RAM khả dụng cho Docker
- 2GB dung lượng ổ đĩa trống

## Cài Đặt và Chạy

### Bước 1: Khởi Động Kafka Cluster

```bash
# Điều hướng đến thư mục dự án
cd e:\Ses1Year3\Assignment\kafka

# Khởi động tất cả services (Kafka, Zookeeper, Kafka UI)
docker-compose up -d

# Kiểm tra status
docker-compose ps
```

**Output mong đợi:**
<img width="741" height="556" alt="image" src="https://github.com/user-attachments/assets/747a9cb6-1c62-475d-a265-d7607a670d1b" />


### Bước 2: Build Producer Service

```bash
cd producer-service

# Build với Maven
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

Producer sẽ chạy trên `http://localhost:8081`

### Bước 3: Build Consumer Service (Terminal mới)

```bash
cd consumer-service

# Build với Maven
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

Consumer sẽ chạy trên `http://localhost:8082`

## Các Tính Năng

### Producer Service (Port 8081)

#### 1. Gửi Single Message
```bash
curl -X POST http://localhost:8081/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello from Producer",
    "sender": "ProducerApp",
    "category": "INFO"
  }'
```

**Response:**
```json
"Message sent successfully with ID: 1"
```

#### 2. Gửi Message đến Partition Cụ Thể
```bash
curl -X POST http://localhost:8081/api/messages/send/partition/0 \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Message to partition 0",
    "sender": "ProducerApp",
    "category": "INFO"
  }'
```

#### 3. Gửi Bulk Messages
```bash
curl -X POST "http://localhost:8081/api/messages/send/bulk?count=10&sender=TestSender&category=INFO"
```

Gửi 10 messages liên tiếp

#### 4. Kiểm Tra Health
```bash
curl http://localhost:8081/api/messages/health
```

### Consumer Service (Port 8082)

#### 1. Xem Tất Cả Messages
```bash
curl http://localhost:8082/api/messages/all
```

#### 2. Xem Thống Kê
```bash
curl http://localhost:8082/api/messages/stats
```

**Response:**
```json
{
  "totalMessages": 15,
  "processedMessages": 15,
  "unprocessedMessages": 0,
  "lastMessage": {
    "dbId": 15,
    "id": 10,
    "content": "Test message",
    "sender": "ProducerApp",
    "category": "INFO",
    "timestamp": "2025-01-15 10:30:45",
    "receivedAt": "2025-01-15 10:30:46",
    "processed": true
  }
}
```

#### 3. Messages theo Sender
```bash
curl http://localhost:8082/api/messages/sender/ProducerApp
```

#### 4. Messages theo Category
```bash
curl http://localhost:8082/api/messages/category/INFO
```

#### 5. Đếm Messages
```bash
curl http://localhost:8082/api/messages/count
```

#### 6. H2 Database Console
```
http://localhost:8082/h2-console
```

## Kiểm Tra Leader Election

Leader election là quá trình Kafka tự động chọn 1 broker làm controller để quản lý cluster.

### Windows - Dùng Script

```powershell
# Chạy script kiểm tra leader
.\scripts\check-leader.bat
```

### Linux/Mac

```bash
# Chạy script kiểm tra leader
bash scripts/check-leader.sh
```

### Kiểm Tra Thủ Công

```bash
# Kiểm tra thông tin topic
docker exec kafka1 kafka-topics \
  --bootstrap-server kafka1:29092,kafka2:29093,kafka3:29094 \
  --topic message-topic \
  --describe
```

**Output ví dụ:**
```
Topic: message-topic      TopicId: XxXxXxXx      PartitionCount: 3       ReplicationFactor: 3    Configs: min.insync.replicas=2
    Topic: message-topic  Partition: 0    Leader: 2       Replicas: 2,3,1 Isr: 2,3,1
    Topic: message-topic  Partition: 1    Leader: 3       Replicas: 3,1,2 Isr: 3,1,2
    Topic: message-topic  Partition: 2    Leader: 1       Replicas: 1,2,3 Isr: 1,2,3
```

**Giải thích:**
- `Leader: 2` - Broker 2 là leader của partition này
- `Replicas: 2,3,1` - Data được replicate trên 3 brokers
- `Isr: 2,3,1` - In-Sync Replicas (tất cả replicas đều cập nhật)

### Kiểm Tra Controller

```bash
# Xem broker nào là controller
docker exec zookeeper zookeeper-shell localhost:2181 get /controller
```

**Output ví dụ:**
```
{"version":1,"id":2,"timestamp":"1705305046000"}
```

Broker 2 là controller quản lý toàn bộ cluster.

## Test Failover

Failover test kiểm tra hệ thống có tiếp tục hoạt động bình thường khi 1 broker bị dừng.

### Windows - Dùng Script

```powershell
# Test failover - dừng broker 1
.\scripts\failover-test.bat

# Hoặc chỉ định broker cụ thể
.\scripts\failover-test.bat 1
```

### Linux/Mac

```bash
# Test failover - dừng broker 1
bash scripts/failover-test.sh

# Hoặc chỉ định broker cụ thể
bash scripts/failover-test.sh 1
```

### Test Failover Thủ Công

#### Bước 1: Kiểm Tra Status Ban Đầu

```bash
# Gửi message từ Producer
curl -X POST http://localhost:8081/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Test before failover",
    "sender": "TestApp",
    "category": "TEST"
  }'

# Kiểm tra Consumer đã nhận
curl http://localhost:8082/api/messages/count
```

#### Bước 2: Dừng 1 Kafka Broker

```bash
# Dừng kafka1 (broker ID 1)
docker stop kafka1
```

#### Bước 3: Kiểm Tra Cluster Vẫn Hoạt Động

```bash
# Gửi message từ các broker còn lại
docker exec kafka2 kafka-console-producer \
  --broker-list kafka2:29093,kafka3:29094 \
  --topic message-topic << EOF
Test message while kafka1 is down
EOF

# Kiểm tra topic metadata
docker exec kafka2 kafka-topics \
  --bootstrap-server kafka2:29093,kafka3:29094 \
  --topic message-topic \
  --describe
```

**Output:** Các partition sẽ có leader mới từ kafka2 hoặc kafka3

#### Bước 4: Khôi Phục Broker

```bash
# Khởi động lại kafka1
docker start kafka1

# Kiểm tra quá trình rejoin cluster
docker logs kafka1 | tail -20
```

#### Bước 5: Kiểm Tra Cluster Trở Về Bình Thường

```bash
# Xem lại thông tin topic
docker exec kafka1 kafka-topics \
  --bootstrap-server kafka1:29092,kafka2:29093,kafka3:29094 \
  --topic message-topic \
  --describe
```

**Kết Quả Mong Đợi:**
- System vẫn hoạt động bình thường
- Consumer vẫn nhận messages
- Khi broker quay trở lại, nó tự động rejoin cluster

## API Documentation

### Producer Service (Port 8081)

| Endpoint | Method | Mô Tả |
|----------|--------|-------|
| `/api/messages/send` | POST | Gửi 1 message |
| `/api/messages/send/partition/{partition}` | POST | Gửi message đến partition cụ thể |
| `/api/messages/send/bulk` | POST | Gửi bulk messages |
| `/api/messages/health` | GET | Kiểm tra health check |

**Request Body Example:**
```json
{
  "content": "Message content here",
  "sender": "ApplicationName",
  "category": "INFO"
}
```

### Consumer Service (Port 8082)

| Endpoint | Method | Mô Tả |
|----------|--------|-------|
| `/api/messages/all` | GET | Lấy tất cả messages |
| `/api/messages/count` | GET | Đếm messages |
| `/api/messages/sender/{sender}` | GET | Lấy messages theo sender |
| `/api/messages/category/{category}` | GET | Lấy messages theo category |
| `/api/messages/processed/{processed}` | GET | Lấy messages theo status |
| `/api/messages/stats` | GET | Lấy thống kê |
| `/api/messages/health` | GET | Kiểm tra health check |
| `/h2-console` | GET | H2 Database console |

## Monitoring & Management UI

### Kafka UI

Truy cập: `http://localhost:8080`

**Tính Năng:**
- Xem tất cả brokers
- Xem topics và partitions
- Xem consumer groups
- Monitor metrics
- Quản lý messages

### H2 Database Console

Truy cập: `http://localhost:8082/h2-console`

**Credentials:**
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: `password`

**Query ví dụ:**
```sql
-- Xem tất cả messages
SELECT * FROM received_messages;

-- Đếm messages theo sender
SELECT sender, COUNT(*) FROM received_messages GROUP BY sender;

-- Xem messages theo category
SELECT * FROM received_messages WHERE category = 'INFO';
```

## Khắc Phục Sự Cố

### Vấn Đề 1: Không thể kết nối đến Kafka

**Triệu chứng:**
```
Connection refused or timeout
```

**Giải pháp:**
```bash
# Kiểm tra containers đang chạy
docker-compose ps

# Kiểm tra logs
docker-compose logs kafka1

# Khởi động lại cluster
docker-compose restart
```

### Vấn Đề 2: Producer gửi message thất bại

**Triệu chứng:**
```
Failed to send message
```

**Giải pháp:**
```bash
# Kiểm tra broker connectivity
docker exec kafka1 kafka-broker-api-versions --bootstrap-server kafka1:29092

# Kiểm tra topic tồn tại
docker exec kafka1 kafka-topics --bootstrap-server kafka1:29092 --list
```

### Vấn Đề 3: Consumer không nhận messages

**Triệu chứng:**
```
Consumer count không tăng
```

**Giải pháp:**
```bash
# Kiểm tra consumer group
docker exec kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:29092 \
  --group message-consumer-group \
  --describe

# Reset offset (nếu cần)
docker exec kafka1 kafka-consumer-groups \
  --bootstrap-server kafka1:29092 \
  --group message-consumer-group \
  --reset-offsets \
  --to-earliest \
  --execute \
  --topic message-topic
```

### Vấn Đề 4: Broker không khôi phục sau failover

**Triệu chứng:**
```
Broker offline khi khởi động lại
```

**Giải pháp:**
```bash
# Xóa volumes và khởi động lại
docker-compose down -v
docker-compose up -d
```

### Vấn Đề 5: Port đã bị sử dụng

**Triệu chứng:**
```
Bind for 0.0.0.0:9092 failed: port is already in use
```

**Giải pháp:**
```bash
# Dừng container sử dụng port
docker-compose down

# Hoặc thay đổi port trong docker-compose.yml
```

## Dừng Hệ Thống

### Dừng tất cả services

```bash
# Dừng nhưng giữ data
docker-compose down

# Dừng và xóa tất cả data
docker-compose down -v
```

## Tài Liệu Bổ Sung

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)

## Ghi Chú Quan Trọng

1. **Replication Factor** được đặt là 3 để đảm bảo fault tolerance
2. **Min In-Sync Replicas** là 2 để yêu cầu ít nhất 2 replicas phải cập nhật
3. **Idempotent Producer** được bật để đảm bảo no duplicates
4. Consumer tự động lưu trữ offset vào Kafka broker

## Liên Hệ & Hỗ Trợ

Nếu có bất kỳ vấn đề nào, vui lòng kiểm tra logs:

```bash
# Producer logs
docker logs kafka1

# Consumer logs
docker logs kafka2

# Zookeeper logs
docker logs zookeeper
```

---

**Phiên bản:** 1.0.0  
**Cập nhật lần cuối:** Tháng 1 năm 2025
