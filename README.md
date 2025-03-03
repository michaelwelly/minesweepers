Minesweeper Game

📌 Description

Minesweeper Game is a classic “Minesweeper” game implemented using Spring Boot and PostgreSQL. The project supports user registration, game statistics tracking, and Kafka integration for event processing.

🚀 Technologies Used
	•	Java 17 – main development language
	•	Spring Boot – backend framework
	•	PostgreSQL – database
	•	Docker & Docker Compose – containerization
	•	Kubernetes – microservice orchestration
	•	Apache Kafka – event processing
	•	Grafana & Kibana – monitoring and logging
	•	Testcontainers & JUnit – testing framework

🎮 Features
	•	User registration and authentication
	•	Start new games with different difficulty levels
	•	Player statistics tracking
	•	Telegram bot integration (optional)
	•	Automated deployment via CI/CD

🔧 Running the Project
docker-compose up -d
After starting, the application will be available at: http://localhost:8080.

🛠 Development & Testing
mvn clean test
The project uses Testcontainers to enable real database testing.

📜 License
This project is distributed under the Apache 2.0.
