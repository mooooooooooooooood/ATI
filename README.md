IELTS Grader & Evaluator Application

This project simulates the IELTS Listening, Reading, and Speaking modules and provides automated grading and AI analysis via the Google Gemini API.

🛠️ Setup and Initialization Guide

This application requires a running MySQL instance, Java 21+, and external API keys for functionality.

Prerequisites

Java: JDK 21 or higher.

MySQL Server: Version 8.0 or higher (required for data persistence).

2. Load Test Data (CRITICAL STEP)

The application tables must be populated with test data (questions, transcripts, URLs) before launching.

Run the Script: Execute the entire file using the MySQL Command Line Client. This will create tables, drop old data, and insert all six full tests (including audio/image links).

# Open Command Prompt / Terminal and Execute the  4 command (replace [USERNAME])
mysql -u [USERNAME] -p ielts_db < [path the the project]\ATI\ATI\src\main\java\com\ieltsgrading\ielts_evaluator\hook\listening.sql

mysql -u [USERNAME] -p ielts_db < [path the the project]\ATI\ATI\src\main\java\com\ieltsgrading\ielts_evaluator\hook\reading.sql

mysql -u [USERNAME] -p ielts_db < [path the the project]\ATI\ATI\src\main\java\com\ieltsgrading\ielts_evaluator\hook\speaking.sql

mysql -u [USERNAME] -p ielts_db < [path the the project]\ATI\ATI\src\main\java\com\ieltsgrading\ielts_evaluator\hook\writing.sql

3. Run the colab
   Run all box in the colab link: https://colab.research.google.com/drive/1NU9AM5eZQDTbzTFC5KWQ4-_mih-C6nqj?usp=sharing
4. Change the properties (add [Username] and [Password]  of your mysql server)
   spring.datasource.url=jdbc:mysql://localhost:3306/ielts_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=
   spring.datasource.password=
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

5. Build and Run Application

Build: Clean and install dependencies using Maven:

mvn clean install


Run: Start the Spring Boot application:

mvn spring-boot:run


The application will run on the configured port (default is 8082).

