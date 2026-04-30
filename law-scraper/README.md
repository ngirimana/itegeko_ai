# 🕷️ Itegeko Law Scraper

The **Law Scraper** is a specialized utility for ingesting legal data from the Rwanda Law Reform Commission (RLRC). It automates the process of fetching PDFs, extracting text, chunking into articles, and indexing them in the AI Service.

## 🚀 Tech Stack

- **Language**: Python 3.11+
- **PDF Processing**: `pypdf`
- **Database**: `psycopg` (Direct DB access mode)
- **Networking**: Calls `ai-service` for indexing orchestration.

## 🛠️ Key Capabilities

- **Delta Imports**: Tracks document hashes to avoid re-importing unchanged PDFs.
- **Granular Chunking**: Breaks large legal documents into individual articles for precise RAG retrieval.
- **Scoped Crawling**: Supports multiple scopes:
  - `companies`: Core company law.
  - `laws-in-force`: Full domestic legal instrument tree.
  - `domestic-laws`: All domestic laws (active and inactive).
  - `all`: Full RLRC tree.
- **Auto-Indexing**: Automatically triggers the AI indexing pipeline after a successful import.

## 📂 Structure

```text
law-scraper/
  ├── scripts/             # Core scraping and processing logic
  ├── Dockerfile           # Standalone scraper container
  └── requirements.txt     # Python dependencies
```

## 🛠️ Usage (via Docker)

The recommended way to run the scraper is through the project's root `docker-compose`.

**Basic Import:**
```bash
docker compose run --rm law-scraper
```

**Full Crawl with Custom Scope:**
```bash
SCRAPER_SCOPE=laws-in-force SCRAPER_MAX_DOCS=0 \
  docker compose run --rm law-scraper
```

## ⚙️ Configuration

| Variable | Default | Description |
| :--- | :--- | :--- |
| `SCRAPER_SCOPE` | `companies` | The crawl depth/scope. |
| `SCRAPER_MAX_DOCS`| `5` | Stop after N documents (0 for unlimited). |
| `LAW_SCRAPER_APPLY`| `true` | Set to `false` for a dry run. |
| `LAW_SCRAPER_INDEX`| `true` | Trigger AI indexing after scraping. |

## 🧪 Local Run

1. **Setup**:
   ```bash
   pip install -r requirements.txt
   ```

2. **Execute**:
   ```bash
   python scripts/scrape-rwanda-laws.py --max-docs 5 --apply --index
   ```
   *Note: Requires database and AI service to be accessible.*
