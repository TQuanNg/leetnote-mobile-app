## LeetNode App

LeetNode is a full-stack mobile application designed to help users **practice, review, and evaluate coding problems** in a more flexible and intuitive way.

The idea behind LeetNode came from the need to have a **personal coding notebook** — a place where LeetCoders can review concepts, track progress, and jot down problem-solving ideas directly from their phone. Instead of relying on a laptop or PC every time, users can quickly explore problems, write pseudocode, and reflect on their approach on the go.

What makes LeetNode unique is its **AI-powered pseudocode evaluation**, which allows users to express their logic naturally without worrying about syntax. The AI provides structured feedback and a rating based on the reasoning, helping users refine their algorithmic thinking and problem-solving approach.

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

