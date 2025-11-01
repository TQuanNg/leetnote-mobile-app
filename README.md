## LeetNote App
LeetNote is a full-stack Android application designed to help users **practice, review, and evaluate coding problems** in a more flexible and intuitive way.

The idea behind LeetNote came from the need to have a **personal coding notebook** — a place where LeetCoders can review concepts, track progress, and jot down problem-solving ideas directly from their phone. Instead of relying on a laptop or PC every time, users can quickly explore problems, write pseudocode, and reflect on their approach on the go.

What makes LeetNote unique is its **AI-powered pseudocode evaluation**, which allows users to express their logic naturally without worrying about syntax. The AI provides structured feedback and a rating based on the reasoning, helping users refine their algorithmic thinking and problem-solving approach.

## 📱 App Screenshot
<p align="center">
  <img src="https://github.com/user-attachments/assets/ff0671c5-9479-4682-945d-f2570fa70682" width="250"/>
  <img src="https://github.com/user-attachments/assets/54ecea16-66fd-418e-a3a4-02238a37a2e8" width="250"/>
  <img src="https://github.com/user-attachments/assets/e93f71c5-e777-4665-8e7c-971f651803b0" width="250"/>
  <img src="https://github.com/user-attachments/assets/d08df5fe-c1ee-4ff1-8238-8c72ec171881" width="250"/>
  <img src="https://github.com/user-attachments/assets/339353d1-e771-46c4-8ed5-041231428878" width="250"/>
</p>

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

- Entity: Data models and DTOs.

Connects to PostgreSQL for persistent storage.

Integrates with **Firebase Authentication** for secure user login.



## Containerization & Deployment

- Backend is containerized with Docker.

- Images are multi-stage built and pushed to Docker Hub.

- Backend hosted on AWS EC2 (t3.micro) with Elastic IP for consistent public IP.

- PostgreSQL database hosted on AWS RDS.

## CI/CD Pipeline
GitHub Actions triggers on every push or pull request with separate workflows for frontend and backend.

- Frontend: Runs lint checks, unit tests, and builds the APK.

- Backend: Runs unit and integration tests, builds the Docker image, pushes it to Docker Hub, and deploys to the AWS EC2 instance.

## 🧪 Testing

Frontend: Unit tests for ViewModels to verify state management and business logic.

Backend: Unit and integration tests for services and controllers using Spring Boot Test framework.


## 🚀 Setup & Deployment
The backend is containerized with Docker and hosted on EC2, with a PostgreSQL database also hosted on AWS.

The frontend, built with Jetpack Compose, is **fully runnable in Android Studio** and connected to the **live** backend.


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

