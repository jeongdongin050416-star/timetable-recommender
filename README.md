# Timetable Recommender

This repository contains the initial project scaffold for a timetable recommendation service.

At this stage, the project only includes the basic application setup, PostgreSQL connectivity, a backend health-check API, and a basic React page. Entities, authentication, the recommendation algorithm, and the actual user interface have not yet been implemented.

## Tech Stack

- Backend: Java 21, Spring Boot, Gradle
- Frontend: React, TypeScript, Vite
- Database: PostgreSQL
- Local Infrastructure: Docker Compose

## Prerequisites

- JDK 21
- Node.js 20.19 or later, or 22.12 or later
- Docker and Docker Compose

## Running the Project

Start PostgreSQL from the project root directory.

```bash
docker compose up -d
```

Start the backend application.

```bash
cd backend
./gradlew bootRun
```

The backend runs at `http://localhost:8080`. You can check its status with the following request:

```bash
curl http://localhost:8080/api/health
```

Open a new terminal and start the frontend application.

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` in your browser.

## Stopping the Project

Stop the backend and frontend applications by pressing `Ctrl+C` in their respective terminals.

Stop PostgreSQL from the project root directory.

```bash
docker compose down
```

To remove the PostgreSQL data volume as well, run:

```bash
docker compose down -v
```

## Database Environment Variables

| Variable | Default Value |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/timetable` |
| `DB_USERNAME` | `timetable` |
| `DB_PASSWORD` | `timetable` |
