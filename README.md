## LeetNote App
LeetNote is a full-stack Android application designed to help users **practice, review, and evaluate coding problems** in a more flexible and intuitive way.

The idea behind LeetNote came from the need to have a **personal coding notebook** — a place where LeetCoders can review concepts, track progress, and jot down problem-solving ideas directly from their phone. Instead of relying on a laptop or PC every time, users can quickly explore problems, write pseudocode, and reflect on their approach on the go.

What makes LeetNote unique is its **AI-powered pseudocode evaluation**, which allows users to express their logic naturally without worrying about syntax. The AI provides structured feedback and a rating based on the reasoning, helping users refine their algorithmic thinking and problem-solving approach.

### Features
- **User Authentication** – Secure Google Sign-In with Firebase Authentication

- **Problem Exploration** – Browse and search coding problems with detailed views of each problem and solution.

- **Smart Evaluation** – Write pseudocode and get AI-powered feedback that evaluates your logic and problem-solving approach.

- **Bookmark & Track Progress** – Mark problems as favorite or solved to easily revisit and monitor your learning journey.

- **Profile Integration** – Connect your LeetCode profile to synchronize solved problems and performance.

- **Learning Resources** – Explore categorized problem patterns to strengthen your understanding of algorithms and data structures.

- **Onboarding Experience**: First-time user guidance and setup

## 📱 App Screenshot
<p align="center">
  <img src="https://github.com/TQuanNg/leetnote-mobile-app/blob/main/assets/Screenshot_home.png" width="250"/>
  <img src="https://github.com/TQuanNg/leetnote-mobile-app/blob/main/assets/Screenshot_onboarding.png" width="250"/>
  <img src="https://github.com/TQuanNg/leetnote-mobile-app/blob/main/assets/Screenshot_learning.png  " width="250"/>
  <img src="https://github.com/TQuanNg/leetnote-mobile-app/blob/main/assets/Screenshot_problem.png" width="250"/>
  <img src="https://github.com/TQuanNg/leetnote-mobile-app/blob/main/assets/Screenshot_evaluation_detail.png" width="250"/>
</p>

## 🚀 Tech Stack & Architecture
### Frontend

#### Design Pattern
- **Jetpack Compose**: Modern declarative UI toolkit
- **MVVM (Model-View-ViewModel)**: Clean architecture with separation of concerns
- **Repository Pattern**: Data abstraction layer
- **Dependency Injection**: **[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)** for managing dependencies
- **Reactive Programming**: Kotlin Flows and Coroutines

#### Networking & Data
- **Retrofit 3.0.0**: REST API communication
- **OkHttp 5.2.1**: HTTP client with logging interceptor
- **Gson**: JSON serialization/deserialization
- **Paging 3**: Efficient data pagination

#### Local Storage
- **DataStore Preferences**: Key-value storage for settings
- **Firebase Storage**: Image and file storage

#### Dependency Injection
- **Hilt 2.57.2**: Dagger-based DI framework
- **Hilt Navigation Compose**: ViewModel injection for Compose

#### Image Loading
- **Coil 3.3.0**: Modern image loading library with OkHttp integration

#### Testing
- **JUnit 4**: Unit testing framework
- **Kotlin Test**: Kotlin-specific testing utilities
- **Coroutines Test**: Testing coroutines and flows
- **MockK**: Mocking framework for Kotlin
- **Turbine**: Flow testing library
- **Paging Testing**: Paging 3 testing utilities

### Backend

Developed with **[Spring Boot](https://spring.io/projects/spring-boot)** following **[layered architecture](https://www.geeksforgeeks.org/springboot/spring-boot-architecture/)**:

#### Core Framework
- **Java 21** - Programming language
- **Spring Boot 3.5.3** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication & authorization
- **Spring WebFlux** - Reactive web client for external APIs

#### Architecture
- **Security Layer**: Handles authentication with Firebase JWT validation and Spring Security configuration
- **Controller Layer**: Exposes REST API endpoints and handles HTTP request/response flow
- **Service Layer**: Contains business logic, orchestrates operations, and integrates with external APIs
- **Repository Layer**: Manages database persistence with JPA and implements custom queries
- **Model Layer**: Defines data structures through Entities (database mapping) and DTOs (API contracts)

#### Database
- **PostgreSQL** - Primary database (AWS RDS)
- **Hibernate** - ORM framework

#### External Integrations
- **LeetCode GraphQL API** - Fetch user statistics and problem data
- **Together AI API** - AI-powered code evaluation and feedback

#### Authentication & Security
- **Firebase Authentication** - User authentication
- **JWT** - Token-based authentication
- Custom `FirebaseAuthenticationFilter` for request validation

#### Code Quality & Utilities
- **Lombok 1.18.38** - Reduce boilerplate code
- **Hibernate Types 60** - Advanced Hibernate type mappings (JSONB support)
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework for tests


## 📦 Containerization & Deployment

- Backend is containerized with Docker.

- Images are multi-stage built and pushed to Docker Hub.

- Backend hosted on AWS EC2 (t3.micro) with Elastic IP for a consistent public IP.

- PostgreSQL database hosted on AWS RDS.

## 🔄 CI/CD Pipeline
**[GitHub Actions](https://github.com/features/actions)** triggers are set up to run on every push or pull request, with separate workflows for the frontend and backend.

- Frontend: Runs lint checks, unit tests, and builds the APK.

- Backend: Runs unit and integration tests, builds the Docker image, pushes it to Docker Hub, and deploys to the AWS EC2 instance.

## 🧪 Testing

Frontend: Unit tests for ViewModels to verify state management and business logic.

Backend: Unit and integration tests for services and controllers using Spring Boot Test framework.


## 🚀 Setup & Deployment
The backend is containerized with Docker and hosted on EC2, with a PostgreSQL database also hosted on AWS.

The frontend, built with Jetpack Compose, is **fully runnable in Android Studio** and connected to the **live** backend.

### Setup
Frontend:
1. **Clone the repository**
   ```bash
   git clone https://github.com/TQuanNg/leetnote-mobile-app.git
   cd mobile-leetnode-frontend
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Firebase Authentication with Google Sign-In
   - Set up Firebase Storage

3. **Configure Google Sign-In**
   - Add your Web Client ID to `res/values/strings.xml`:
     ```xml
     <string name="web_client_id">YOUR_WEB_CLIENT_ID</string>
     ```

### Optional local setup
The app can also be run locally for development or testing purposes.

1. **Backend Configuration**:
- Configure environment variables in application.properties.
- Set up a local PostgreSQL database (table schemas provided in the resources folder)
- Set up Firebase Authentication (see Firebase Docs).
- Run the Spring Boot backend using your IDE or ./gradlew bootRun.

2. **Frontend Configuration**
- Update the base URL in your Retrofit configuration
- Ensure backend API is running and accessible


## 🔮 Future Plans

- Complete user profile features, including customization and progress tracking.

- Expand testing coverage for both frontend and backend to ensure robust functionality.

