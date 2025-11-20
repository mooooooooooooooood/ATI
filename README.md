📝 Eo Eo – IELTS Grading Web

Ứng dụng chấm điểm IELTS tự động được xây dựng bằng Spring Boot 3, Thymeleaf, Spring Security, và MySQL.
Dự án hỗ trợ upload audio, gọi API AI bên ngoài, quản lý người dùng, và xử lý bất đồng bộ.

🚀 1. Công nghệ sử dụng

Java 21

Spring Boot 3.5.7

Spring Web (MVC)

Spring Data JPA

Spring Security

Spring WebFlux (gọi API async)

Validation

Thymeleaf

MySQL + Hibernate

Maven

Spring Boot

External AI API (Gemini / Custom API)

📦 2. Yêu cầu hệ thống
Công cụ	Version
Java	21
Maven	3.9+
MySQL	5.7 / 8.x
IDE	IntelliJ / VSCode / Eclipse
⚙️ 3. Cách cài đặt & cấu hình
3.1. Clone project
git clone https://github.com/mooooooooooooooood/ATI.git
3.2 Import database
MySQL WorkBench: Server => Data Import (Import from Disk) => Import from Self-Contained File => Change Path => Default target schema
                  => Import Progress tab => Start import

🔧 4. Cấu hình trong application.properties

Dự án yêu cầu cấu hình các phần sau:

✅ 4.1. Database MySQL

Sửa lại username/password theo máy bạn:

spring.datasource.url=jdbc:mysql://localhost:3306/ielts_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=maychetvoitao
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver


Database ielts_db sẽ được tạo tự động nếu chưa tồn tại.

✅ 4.2. JPA / Hibernate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true

✅ 4.3. Upload file (audio / task images)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=2KB

app.upload.dir=uploads/audio/


Thư mục sẽ được tự tạo tại runtime.

✅ 4.4. Server config
server.port=8082
server.servlet.context-path=/
server.servlet.session.timeout=60m

✅ 4.5. Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.enabled=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML

✅ 4.6. Logging
logging.level.root=INFO
logging.level.com.ieltsgrading.ielts_evaluator=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=INFO
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

✅ 4.7. Encoding
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true

✅ 4.8. External API (dành cho AI chấm điểm)

BẮT BUỘC: Nếu API thay đổi bạn phải sửa ở đây.

app.external-api.base-url=https://zoogleal-parsonish-almeda.ngrok-free.dev
app.external-api.timeout-connect=30000
app.external-api.timeout-read=60000

gemini.api.key=AIzaSyBrXpaS0vYNzanlU_H1RSuNVbfGpbueLqo
grading.api.base-url=https://zoogleal-parsonish-almeda.ngrok-free.dev

✅ 4.9. Async Executor
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=100
spring.task.execution.thread-name-prefix=speaking-async-

📦 5. Maven Dependencies

Các dependency chính từ pom.xml:

Spring Boot Starter
spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-thymeleaf
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-webflux

Database
mysql-connector-j

Dev & Test
spring-boot-devtools
spring-boot-starter-test
spring-security-test
reactor-test

Extra libraries
google-cloud-vertexai
mp3agic
spring-boot-admin-starter-server

▶️ 6. Chạy ứng dụng
Cách 1 — chạy bằng Maven
mvn spring-boot:run

Cách 2 — build file jar
mvn clean package
java -jar target/ielts-evaluator-0.0.1-SNAPSHOT.jar


Mặc định chạy tại:
👉 http://localhost:8082/

🔐 7. Tài khoản security mặc định

Nếu chưa cấu hình Spring Security tùy chỉnh:

spring.security.user.name=admin@gmail.com
spring.security.user.password=123456

📁 8. Cấu trúc thư mục
└──src/
│  └── main/
│     ├── java/com/ieltsgrading/ielts_evaluator/
│     │     ├── config/
│     │     ├── controller/
│     │     ├── dto/
│     │     ├── service/
│     │     ├── repository/
│     │     ├── model/
│     │     ├── util/
│     │     └── EoEoIeltsGradingAppApplication.java
│     └── resources/
│           ├── templates/
│           ├── static/
│           └── application.properties
│          
└── temp-audio
└── uploads/audio

✨ 9. Chức năng nổi bật của dự án

Upload audio Speaking + phân tích tự động

Chấm điểm Writing Task 1 + 2 bằng AI

Xem kết quả chấm điểm 

Quản lý user + Security

Gọi API AI external bằng WebClient

Dashboard theo dõi requests
