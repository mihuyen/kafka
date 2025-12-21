# Hệ Thống Kafka Cluster 3-Node với Spring Boot

Dự án này triển khai một hệ thống **Kafka Cluster** hoàn chỉnh gồm 3 broker và 2 ứng dụng Spring Boot (Producer & Consumer) trao đổi dữ liệu JSON. Hệ thống được thiết kế để chịu lỗi (fault-tolerant) và đảm bảo tính sẵn sàng cao.

## 🚀 Tính Năng

*   **Kafka Cluster**: 3 Brokers, quản lý bởi Zookeeper.
*   **Producer Service**: Gửi tin nhắn JSON, hỗ trợ gửi vào partition cụ thể.
*   **Consumer Service**: Nhận tin nhắn JSON, lưu vào H2 Database.
*   **Failover**: Hệ thống vẫn hoạt động khi 1 Kafka Broker (thậm chí là Leader) bị chết.
*   **Kafka UI**: Giao diện quản lý trực quan.

## 🛠️ Yêu Cầu

*   Docker & Docker Compose
*   Java 17+
*   Maven 3.8+

## 📦 Cài Đặt và Chạy

### 1. Khởi Động Infrastructure
Chạy Kafka Cluster, Zookeeper và Kafka UI bằng Docker Compose.

```bash
docker-compose up -d
```

Truy cập **Kafka UI** tại: [http://localhost:8080](http://localhost:8080)
<img width="1792" height="796" alt="image" src="https://github.com/user-attachments/assets/db8a57de-dcf4-4b7a-8776-46e71cefc074" />

### 2. Chạy Producer Service
Mở terminal mới:

```bash
cd producer-service
mvn spring-boot:run
```
*Port: 8081*

### 3. Chạy Consumer Service
Mở terminal mới:

```bash
cd consumer-service
mvn spring-boot:run
```
*Port: 8082*

---

## 🧪 Kiểm Tra Hoạt Động

### 1. Gửi Tin Nhắn (Producer)
Gửi một tin nhắn JSON đến Kafka.
**Lưu ý:** Trường `id` cần được gửi (mặc dù server sẽ tự generate lại) để vượt qua validation.

```bash
curl -X POST http://localhost:8081/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "id": 0,
    "content": "Hello Kafka Cluster",
    "sender": "User1",
    "category": "TEST"
  }'
```
<img width="1276" height="876" alt="image" src="https://github.com/user-attachments/assets/70bce3fc-eecd-4bc2-9a76-d6e67a3fb760" />


### 2. Kiểm Tra Nhận Tin (Consumer)
Kiểm tra số lượng tin nhắn đã nhận và lưu vào DB.

```bash
curl http://localhost:8082/api/messages/count
```

<img width="1252" height="788" alt="image" src="https://github.com/user-attachments/assets/9d87e2a6-7188-4dab-8a67-b787d7c7e5ee" />


---

## ⚠️ Test Failover (Kiểm Tra Chịu Lỗi)

Đây là quy trình kiểm tra khả năng hoạt động của hệ thống khi Leader Broker bị chết.

### Bước 1: Xác Định Leader
Chạy lệnh sau để xem broker nào đang là Leader của `message-topic` (Partition 0).

```bash
docker exec kafka1 kafka-topics --describe --topic message-topic --bootstrap-server kafka1:29092
```

<img width="747" height="412" alt="image" src="https://github.com/user-attachments/assets/e5b39ce2-9ba9-46d2-a7fb-68d534c9b28b" />


### Bước 2: "Giết" Leader
Dừng container của Leader broker (ví dụ là `kafka3`).

```bash
docker stop kafka3
```

### Bước 3: Gửi Tin Nhắn Khi Leader Chết
Hệ thống sẽ tự động bầu chọn Leader mới từ các ISR (In-Sync Replicas). Gửi tin nhắn mới để kiểm tra:

```bash
curl -X POST http://localhost:8081/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "id": 0,
    "content": "Tin nhắn khi Failover",
    "sender": "Tester",
    "category": "FAILOVER"
  }'
```

*Nếu gửi thành công, chứng tỏ Cluster đã tự phục hồi.*
<img width="1223" height="823" alt="image" src="https://github.com/user-attachments/assets/d3ed2823-1788-4f56-bb6b-015f0b63f0ee" />


### Bước 4: Kiểm Tra Consumer
Consumer vẫn phải nhận được tin nhắn này.

```bash
curl http://localhost:8082/api/messages/count
```
Số lượng tin nhắn phải tăng lên.

### Bước 5: Khôi Phục Broker
Khởi động lại broker đã dừng để nó tham gia lại cluster.

```bash
docker start kafka2
```

---

## 🔧 Troubleshooting (Khắc Phục Sự Cố)

### Lỗi Deserialization trong Consumer
Nếu Consumer báo lỗi `ClassNotFoundException` hoặc không nhận được tin:
*   Đảm bảo `Application.yml` hoặc `KafkaConsumerConfig` đã cấu hình mapping đúng.
*   **Fix:** Cấu hình `spring.json.use.type.headers: false` trong Consumer để bỏ qua header type của Producer và dùng class nội bộ.

### Lỗi Validation "must not be null" tại Producer
*   Payload JSON gửi lên bắt buộc phải có trường `"id": 0` (hoặc số bất kỳ) do validation `@NotNull` trong Model.

### Kiểm tra Logs
```bash
docker logs kafka1
docker logs kafka2
docker logs kafka3
```
