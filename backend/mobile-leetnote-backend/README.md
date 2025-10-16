## LeetNode App

  LeetNode is a full-stack application designed to help users practice and evaluate coding problems efficiently. It provides an interactive interface for problem-solving, pseudocode evaluation, and algorithm learning.
## 🚀 Key Features

### Frontend
- Built using Jetpack Compose for a modern, declarative UI.
- Hilt for dependency injection, ensuring clean architecture.

Folder structure:

- data – API interfaces and local database models

- repository – Handles data operations and business logic

- ui – Screens and navigation with a Navigation Graph

- viewmodel – State management and feature-specific logic

### Backend

Developed with Spring Boot following a layered architecture:

- Controller: REST endpoints.

- Service: Business logic.

- Repository: Database access layer.

- Entity: Data models.

Connects to PostgreSQL for persistent storage.

Integrates with Firebase Authentication for secure user login.



## Containerization & Deployment

- Backend and database are containerized with Docker.

- Images built and pushed to Docker Hub.

- AWS EC2 hosts the backend.

- PostgreSQL database hosted on AWS RDS.


## 🧪 Testing

Frontend: Unit tests for ViewModels to verify state management and business logic.

Backend: Unit and integration tests for services and controllers using Spring Boot Test framework.


## 🚀 Setup & Deployment
Backend is containerized with Docker and hosted on EC2. PostgreSQL database is hosted on AWS.

Frontend is built with Jetpack Compose and can be run via Android Studio.

### Optional Local Setup

The app can also be run locally for development or testing purposes:

Backend:

- Configure environment variables in application.properties.

- Set up Firebase Authentication (see Firebase Docs
).

- Run the Spring Boot backend using your IDE or ./gradlew bootRun.

Frontend:

- Open in Android Studio and run on an emulator or physical device.

## 🔮 Future Plans

Complete user profile features, including customization and progress tracking.

Integrate LeetCode profile connection to fetch user stats and problem history.

Expand testing coverage for both frontend and backend to ensure robust functionality.

