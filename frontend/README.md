# 🌐 Itegeko Frontend

The **Itegeko Frontend** is a modern web application built with Next.js 15. It provides a seamless interface for legal search, AI-powered Q&A, and administrative document management.

## 🚀 Tech Stack

- **Framework**: Next.js 15 (App Router)
- **Language**: TypeScript
- **Styling**: Vanilla CSS + Mantine UI v8
- **Icons**: Lucide React
- **Auth**: Keycloak (OIDC) via `next-auth` (or direct integration)

## 📂 Project Structure

The project follows a feature-oriented SOLID structure:

```text
app/                 # Next.js routes and layouts
src/
  ├── components/    # Shared UI components (Buttons, Modals, etc.)
  ├── constants/     # API paths, labels, and fixed values
  ├── features/      # Business features (LegalSearch, UserProfile, etc.)
  │     ├── components/
  │     ├── hooks/
  │     └── types/
  ├── services/      # API abstraction layer
  └── utils/         # Pure helper functions
```

## 🛠️ Key Features

- **Search Dashboard**: Multi-faceted search for laws and articles.
- **AI Chat Interface**: Interactive Q&A for legal questions.
- **Admin Panel**: Upload and manage legal documents (Admin role required).
- **Activity Tracking**: View personal search history and interactions.

## 🌐 API Proxies

The frontend handles cross-service communication via Next.js proxies defined in `next.config.ts` (if configured) or environment variables:
- `/api/*` → `legal-service`
- `/identity-api/*` → `identity-service`

## 🛠️ Development Setup

1. **Install Dependencies**:
   ```bash
   npm install
   ```

2. **Environment Setup**:
   Create a `.env.local` or rely on the root `.env` when using Docker.
   ```bash
   NEXT_PUBLIC_LEGAL_API_BASE_URL=http://localhost:8080
   NEXT_PUBLIC_IDENTITY_API_BASE_URL=http://localhost:8082
   NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8081
   ```

3. **Run Locally**:
   ```bash
   npm run dev
   ```

## 🏗️ Build

```bash
npm run build
npm run start
```
