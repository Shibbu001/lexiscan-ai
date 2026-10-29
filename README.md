# LexiScan AI MVP
AI-powered Contract and Document Analyzer.

## Stack
Java 21 + Spring Boot + PostgreSQL + React/Vite + Gemini + PDFBox + JWT.

## Run
1. `docker compose up -d`
2. Set `GEMINI_API_KEY`.
3. Backend: `cd backend && mvn spring-boot:run`
4. Frontend: `cd frontend && npm install && npm run dev`

Frontend: http://localhost:5173
Backend: http://localhost:8080
Health: http://localhost:8080/api/health

Phase-1 MVP: register/login, PDF upload, PDF text extraction, Gemini summary/risk/clause analysis, dashboard.
