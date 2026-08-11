# TaskFlow

TaskFlow is a full-stack task management application built with Angular on the frontend and Spring Boot on the backend. It allows teams to create, assign, track, and review tasks with role-based access, comments, attachments, time tracking, and analytics.

## Features

- User authentication and registration
- Role-based access for Admin, Manager, and Member users
- Task creation, updates, and status tracking
- Subtasks, comments, and attachments
- Team management and team-specific task views
- Dashboard and analytics overview
- Settings and profile management
- JWT-based secure API communication

## Tech Stack

### Frontend
- Angular 21
- TypeScript
- RxJS
- Chart.js
- Vitest for testing

### Backend
- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL
- JWT authentication

## Project Structure

- taskflowF-main: Angular frontend application
- taskflowB-main: Spring Boot backend application

## How It Works

1. Users register or log in through the Angular frontend.
2. The frontend sends authenticated requests to the Spring Boot REST API.
3. The backend validates users with JWT and manages tasks, comments, teams, and attachments in MySQL.
4. The dashboard and analytics views display task progress and team activity.

## Prerequisites

Make sure the following are installed:

- Node.js 20+
- npm 10+
- Java 21
- Maven or the provided Maven wrapper
- MySQL 8+

## Backend Setup

1. Open the backend folder:
   ```bash
   cd taskflowB-main
   ```

2. Create a MySQL database named `taskflow`.

3. Update database credentials in:
   ```text
   src/main/resources/application.properties
   ```

4. Run the backend:
   ```bash
   ./mvnw spring-boot:run
   ```

The backend API will run at:
```text
http://localhost:8080
```

## Frontend Setup

1. Open the frontend folder:
   ```bash
   cd taskflowF-main
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

The frontend will be available at:
```text
http://localhost:4200
```

## Environment Notes

- The frontend is configured to call the API at `http://localhost:8080`.
- The backend uses JWT authentication and a bootstrap option that allows the first registered user to become an administrator.
- For production, update secrets, CORS settings, and API base URLs before deployment.

## Demo Flow

A typical user journey in this application is:
1. Register a new account or sign in.
2. Create or join a team.
3. Add tasks and subtasks for the team.
4. Track progress, update statuses, and discuss work through comments.
5. Review analytics and project activity from the dashboard.

## Troubleshooting

If you run into issues:
- Verify that MySQL is running and the `taskflow` database exists.
- Confirm the backend port is `8080` and the frontend is pointing to the correct API URL.
- Make sure Java 21 and Node.js are installed and available in your terminal.

## Testing

Run frontend tests:
```bash
npm test
```

Run backend tests:
```bash
./mvnw test
```

## Future Improvements

Possible enhancements include:
- real-time notifications
- drag-and-drop task boards
- export/import of task data
- dark mode refinements
- improved reporting and charts



## License

This project is intended for educational and development purposes.
