# LexiScan AI — Intelligent Contract & Document Analyzer

An AI-powered web application that helps users review text-based PDF contracts and documents by generating summaries, highlighting potential risks, and extracting important information.

## Overview

LexiScan AI combines PDF text extraction with generative AI to provide an accessible starting point for document review. Users can register or log in, upload a supported PDF, view AI-generated analysis, and access saved document records through a dashboard.

**Note:** AI-generated observations are preliminary and should be verified against the original document. LexiScan AI does not replace professional legal advice.

## Features

- **User Authentication:** Registration and login using Spring Security and JWT.
- **PDF Upload:** Upload text-based PDF documents for analysis.
- **Text Extraction:** Extract readable PDF text using Apache PDFBox.
- **AI-Powered Analysis:** Use the Google Gemini API to generate document summaries and identify potential risks and important clauses.
- **Document Dashboard:** View documents and their associated analysis.
- **Persistent Storage:** Store document records and analysis results in PostgreSQL.
- **Cloud Deployment:** Access the deployed frontend and backend services online.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | React, Vite, JavaScript, Axios |
| Backend | Java 21, Spring Boot |
| Database | PostgreSQL (Neon) |
| AI Integration | Google Gemini API |
| PDF Processing | Apache PDFBox |
| Authentication | Spring Security, JWT |
| Build Tool | Maven |
| Deployment | Render, Docker |
| Version Control | Git, GitHub |

## Live Demo

- **Frontend:** https://lexiscan-frontend-cocf.onrender.com
- **Backend Health Check:** https://lexiscan-backend-jni0.onrender.com/api/health

The backend health endpoint returns an operational status when the service is available.

## Application Workflow

1. The user registers or logs in.
2. The user uploads a text-based PDF.
3. The backend validates the file and extracts readable text using Apache PDFBox.
4. The extracted text is sent to the Google Gemini API for analysis.
5. The application processes the generated observations and stores the document record and analysis in PostgreSQL.
6. The user views the results through the dashboard.

## Run Locally

### Prerequisites

Install the following tools:

- Java 21
- Maven
- Node.js and npm
- Docker Desktop (if using Docker Compose for the database)
- A Google Gemini API key
- PostgreSQL, either locally or through a configured database service

### 1. Clone the repository

```bash
git clone https://github.com/Shibbu001/lexiscan-ai.git
cd lexiscan-ai
```

### 2. Configure environment variables

Configure the required backend settings in your local environment. These may include:

- Database connection details
- JWT signing secret
- Gemini API key (`GEMINI_API_KEY`)

Use the exact variable names expected by your application configuration. Never commit API keys, database passwords, or JWT secrets to GitHub.

### 3. Start the backend

From the project root, run:

```bash
cd backend
mvn spring-boot:run
```

### 4. Start the frontend

Open another terminal from the project root:

```bash
cd frontend
npm install
npm run dev
```

Use the local frontend URL printed by Vite in your terminal. The backend's default local URL is `http://localhost:8080`, provided your configuration uses that port.

If your project includes a Docker Compose configuration for PostgreSQL, start it using the instructions in that configuration before launching the backend.

## Current Limitations

- Scanned PDFs containing only images are not supported because OCR has not been implemented.
- AI-generated analysis may omit information or produce inaccurate interpretations.
- Analysis quality has not been established through a verified numerical accuracy benchmark.
- Persistence of uploaded PDF files across hosting-service restarts has not been verified.

## Future Improvements

- Add OCR support for scanned documents.
- Include page or clause references to help users verify AI-generated observations.
- Evaluate analysis quality against human-reviewed documents.
- Expand automated testing for invalid files, API failures, and security scenarios.
- Improve document-management and error-handling workflows.

## Repository

Source code: https://github.com/Shibbu001/lexiscan-ai

## Disclaimer

LexiScan AI is intended for preliminary document understanding and educational purposes. It does not provide legal advice. Users should independently verify important findings against the original document and consult a qualified legal professional when necessary.
