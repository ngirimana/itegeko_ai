#!/usr/bin/env python3
"""Scrape official Rwanda laws from RLRC and seed the local legal database.

The script intentionally defaults to a small, bounded import. Increase
--max-docs after reviewing the first batch.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
import sys
import tempfile
import time
import uuid
from dataclasses import dataclass
from html.parser import HTMLParser
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import parse_qs, unquote, urljoin, urlparse
from urllib.request import Request, urlopen

try:
    from pypdf import PdfReader
except ImportError:  # pragma: no cover - PDF extraction validates this at runtime.
    PdfReader = None

try:
    import psycopg
except ImportError:  # pragma: no cover - direct DB mode validates this at runtime.
    psycopg = None


RLRC_BASE_URL = "https://www.rlrc.gov.rw"
RLRC_LAWS_PAGE_URL = "https://www.rlrc.gov.rw/mandate/laws-of-rwanda"
RLRC_LAWS_ROOT_PATH = "/user_upload/RLRC/Laws_of_Rwanda_v2/"
DOMESTIC_LAWS_ROOT_PATH = RLRC_LAWS_ROOT_PATH + "Domestic_laws/"
LAWS_IN_FORCE_ROOT_PATH = DOMESTIC_LAWS_ROOT_PATH + "Laws_in_force/"
LAWS_IN_FORCE_START_URL = (
    RLRC_LAWS_PAGE_URL +
    "?cHash=86926f044b8085a1b13c0c79949136f3"
    "&tx_filelist_filelist%5Baction%5D=list"
    "&tx_filelist_filelist%5Bcontroller%5D=File"
    "&tx_filelist_filelist%5Bpath%5D=%2Fuser_upload%2FRLRC%2FLaws_of_Rwanda_v2%2FDomestic_laws%2FLaws_in_force%2F"
)
COMPANIES_START_URL = (
    RLRC_LAWS_PAGE_URL +
    "?tx_filelist_filelist%5Baction%5D=list"
    "&tx_filelist_filelist%5Bcontroller%5D=File"
    "&tx_filelist_filelist%5Bpath%5D=%2Fuser_upload%2FRLRC%2FLaws_of_Rwanda_v2%2FDomestic_laws%2FLaws_in_force%2F10._Business%2F10.8._Organization_of_commercial_Activities%2F10.8.2._Companies%2F"
    "&cHash=e34a602eb628f7443a52edb01a3d74f0"
)
DEFAULT_START_URL = COMPANIES_START_URL
DEFAULT_SCOPE = "companies"
USER_AGENT = "ItegekoAI/0.1 local law seeder (contact: local-dev)"
RLRC_SOURCE_ID = uuid.UUID("44444444-4444-4444-4444-444444444444")
RLRC_CATEGORY_NAMESPACE = uuid.UUID("55555555-5555-5555-5555-555555555555")
DOCUMENT_NAMESPACE = uuid.UUID("66666666-6666-6666-6666-666666666666")
ARTICLE_NAMESPACE = uuid.UUID("77777777-7777-7777-7777-777777777777")


@dataclass(frozen=True)
class Link:
    url: str
    text: str


@dataclass(frozen=True)
class LawPdf:
    url: str
    title: str
    category_name: str
    category_slug: str
    document_type: str
    source_path: str


@dataclass(frozen=True)
class ArticleChunk:
    article_id: uuid.UUID
    article_number: str
    article_title: str
    article_text: str
    order_index: int


@dataclass(frozen=True)
class DocumentSeed:
    document_id: uuid.UUID
    title: str
    category_id: uuid.UUID
    category_name: str
    category_slug: str
    source_url: str
    source_path: str
    scraper_scope: str
    document_type: str
    content_hash: str
    law_number: str | None
    publication_date: str | None
    articles: list[ArticleChunk]


@dataclass(frozen=True)
class DatabaseConfig:
    mode: str
    host: str | None
    port: int
    name: str
    user: str
    password: str
    force: bool
    skip_existing: bool


@dataclass(frozen=True)
class ApplyResult:
    applied_documents: int
    skipped_documents: int
    applied_titles: list[str]
    skipped_titles: list[str]


class LinkParser(HTMLParser):
    def __init__(self, base_url: str) -> None:
        super().__init__()
        self.base_url = base_url
        self.links: list[Link] = []
        self._href: str | None = None
        self._text: list[str] = []

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        if tag != "a":
            return
        self._href = dict(attrs).get("href")
        self._text = []

    def handle_data(self, data: str) -> None:
        if self._href:
            self._text.append(data)

    def handle_endtag(self, tag: str) -> None:
        if tag != "a" or not self._href:
            return
        href = urljoin(self.base_url, self._href)
        text = clean_text(" ".join(self._text))
        self.links.append(Link(href, text))
        self._href = None
        self._text = []


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--scope",
        choices=("companies", "laws-in-force", "domestic-laws", "all"),
        default=os.getenv("SCRAPER_SCOPE", DEFAULT_SCOPE),
        help="companies imports the current focused folder; laws-in-force crawls all domestic laws and implementing orders in force",
    )
    parser.add_argument("--start-url", default=os.getenv("SCRAPER_START_URL"))
    parser.add_argument("--max-docs", type=int, default=env_int("SCRAPER_MAX_DOCS", 5))
    parser.add_argument("--max-folders", type=int, default=env_int("SCRAPER_MAX_FOLDERS", 250))
    parser.add_argument("--max-pages", type=int, default=env_int("SCRAPER_MAX_PAGES", 80))
    parser.add_argument("--chunk-chars", type=int, default=env_int("SCRAPER_CHUNK_CHARS", 6000))
    parser.add_argument("--delay", type=float, default=env_float("SCRAPER_DELAY", 0.4))
    parser.add_argument(
        "--document-types",
        default=os.getenv("SCRAPER_DOCUMENT_TYPES", ""),
        help="optional comma-separated filter: law,organic_law,presidential_order,prime_minister_order,ministerial_order,regulation,instruction,directive,decision,other",
    )
    parser.add_argument(
        "--apply",
        action=argparse.BooleanOptionalAction,
        default=env_bool("LAW_SCRAPER_APPLY", False),
        help="write scraped laws into the legal database",
    )
    parser.add_argument(
        "--index",
        action=argparse.BooleanOptionalAction,
        default=env_bool("LAW_SCRAPER_INDEX", False),
        help="trigger AI indexing after applying seeds",
    )
    parser.add_argument("--internal-api-key", default=os.getenv("INTERNAL_API_KEY", "local-dev-internal-key"))
    parser.add_argument("--ai-service-url", default=os.getenv("AI_SERVICE_URL", "http://localhost:8000"))
    parser.add_argument("--summary-path", default=os.getenv("SCRAPER_SUMMARY_PATH", "scraped-rwanda-laws-summary.json"))
    parser.add_argument(
        "--db-mode",
        choices=("auto", "direct", "docker"),
        default=os.getenv("LAW_SCRAPER_DB_MODE", "auto"),
        help="direct connects to Postgres; docker uses docker compose exec psql; auto chooses direct when a DB host is set",
    )
    parser.add_argument("--db-host", default=os.getenv("DATABASE_HOST") or os.getenv("LEGAL_DB_HOST"))
    parser.add_argument("--db-port", type=int, default=env_int("DATABASE_PORT", 5432))
    parser.add_argument("--db-name", default=os.getenv("LEGAL_DB_NAME", "itegeko_legal"))
    parser.add_argument("--db-user", default=os.getenv("LEGAL_DB_USER", "itegeko_legal"))
    parser.add_argument("--db-password", default=os.getenv("LEGAL_DB_PASSWORD", "itegeko_legal"))
    parser.add_argument(
        "--force",
        action="store_true",
        default=env_bool("LAW_SCRAPER_FORCE", False),
        help="re-import documents even when the scraped PDF content hash is unchanged",
    )
    parser.add_argument(
        "--skip-existing",
        action=argparse.BooleanOptionalAction,
        default=env_bool("LAW_SCRAPER_SKIP_EXISTING", True),
        help="skip already-imported documents when their PDF content hash has not changed",
    )
    args = parser.parse_args()

    start_urls, root_path = resolve_start_targets(args.scope, args.start_url)
    document_types = parse_document_types(args.document_types)
    pdfs = discover_pdfs(
        start_urls=start_urls,
        root_path=root_path,
        max_docs=args.max_docs,
        max_folders=args.max_folders,
        delay=args.delay,
        document_types=document_types,
    )
    if not pdfs:
        print("No PDFs discovered.", file=sys.stderr)
        return 1

    seeds: list[DocumentSeed] = []
    for law_pdf in pdfs:
        print(f"Scraping: {law_pdf.title} [{law_pdf.document_type}]")
        try:
            seed = build_seed(law_pdf, args.scope, args.max_pages, args.chunk_chars)
        except Exception as exc:
            print(f"  skipped: {exc}", file=sys.stderr)
            continue
        print(f"  chunks: {len(seed.articles)}")
        seeds.append(seed)
        time.sleep(args.delay)

    if not seeds:
        print("No readable PDFs were scraped.", file=sys.stderr)
        return 1

    write_summary(seeds, Path(args.summary_path))
    apply_result: ApplyResult | None = None
    if args.apply:
        db_config = build_database_config(args)
        apply_result = apply_to_database(seeds, db_config)
        print(
            f"Applied {apply_result.applied_documents} documents to legal database; "
            f"skipped {apply_result.skipped_documents} unchanged documents."
        )
    else:
        print("Dry run only. Re-run with --apply to seed the database.")

    if args.apply and args.index:
        if apply_result and apply_result.applied_documents == 0 and not args.force:
            print("No legal data changed; AI index refresh skipped.")
        else:
            indexed = trigger_index(args.internal_api_key, args.ai_service_url)
            print(f"AI index refreshed: {indexed} articles indexed.")

    return 0


def env_bool(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "y", "on"}


def env_int(name: str, default: int) -> int:
    value = os.getenv(name)
    if value is None:
        return default
    return int(value)


def env_float(name: str, default: float) -> float:
    value = os.getenv(name)
    if value is None:
        return default
    return float(value)


def resolve_start_targets(scope: str, start_url: str | None) -> tuple[list[str], str]:
    if start_url:
        root_path = filelist_path(start_url) or RLRC_LAWS_ROOT_PATH
        return [start_url], root_path
    if scope == "companies":
        return [COMPANIES_START_URL], filelist_path(COMPANIES_START_URL)
    if scope == "laws-in-force":
        return [RLRC_LAWS_PAGE_URL, LAWS_IN_FORCE_START_URL], LAWS_IN_FORCE_ROOT_PATH
    if scope == "domestic-laws":
        return [RLRC_LAWS_PAGE_URL], DOMESTIC_LAWS_ROOT_PATH
    return [RLRC_LAWS_PAGE_URL], RLRC_LAWS_ROOT_PATH


def parse_document_types(value: str) -> set[str]:
    if not value.strip():
        return set()
    return {part.strip().lower().replace("-", "_") for part in value.split(",") if part.strip()}


def discover_pdfs(
    start_urls: list[str],
    root_path: str,
    max_docs: int,
    max_folders: int,
    delay: float,
    document_types: set[str],
) -> list[LawPdf]:
    queue = list(start_urls)
    visited: set[str] = set()
    pdfs: list[LawPdf] = []
    folders_seen = 0
    while queue and not reached_doc_limit(pdfs, max_docs):
        url = queue.pop(0)
        visit_key = crawl_visit_key(url)
        if visit_key in visited:
            continue
        visited.add(visit_key)
        folders_seen += 1
        if max_folders > 0 and folders_seen > max_folders:
            print(f"Folder crawl limit reached ({max_folders}). Increase --max-folders to crawl more.", file=sys.stderr)
            break
        html = fetch_text(url)
        if html is None:
            continue
        links = parse_links(url, html)
        for link in links:
            if reached_doc_limit(pdfs, max_docs):
                break
            if is_pdf_url(link.url) and pdf_under_root(link.url, root_path):
                law_pdf = law_pdf_from_url(link.url)
                if not document_types or law_pdf.document_type in document_types:
                    pdfs.append(law_pdf)
            elif is_rlrc_filelist_url(link.url) and filelist_relevant_to_root(link.url, root_path):
                if should_follow_filelist(link, root_path):
                    queue.append(link.url)
        time.sleep(delay)
    unique_pdfs = dedupe_pdfs(pdfs)
    return unique_pdfs[:max_docs] if max_docs > 0 else unique_pdfs


def reached_doc_limit(pdfs: list[LawPdf], max_docs: int) -> bool:
    return max_docs > 0 and len(pdfs) >= max_docs


def should_follow_filelist(link: Link, root_path: str) -> bool:
    path = filelist_path(link.url)
    if not path or not filelist_relevant_to_root(link.url, root_path):
        return False
    text = link.text.strip()
    normalized = text.lower()
    if normalized in {"", "..", "documents", "domestic laws", "laws in force", "laws not in force", "international legal instruments ili"}:
        return True
    if normalized in {"next", "previous", "first", "last"}:
        return True
    if normalized.isdigit():
        return True
    return bool(re.match(r"^\d+(\.\d+)*\.?\s*", text))


def dedupe_pdfs(pdfs: list[LawPdf]) -> list[LawPdf]:
    seen: set[str] = set()
    unique: list[LawPdf] = []
    for pdf in pdfs:
        if pdf.url in seen:
            continue
        seen.add(pdf.url)
        unique.append(pdf)
    return unique


def law_pdf_from_url(url: str) -> LawPdf:
    path = unquote(urlparse(url).path)
    filename = path.rsplit("/", 1)[-1]
    title = title_from_filename(filename)
    category_name = category_from_path(path)
    category_slug = slugify(category_name)
    document_type = detect_document_type(title, path)
    return LawPdf(
        url=url,
        title=title,
        category_name=category_name,
        category_slug=category_slug,
        document_type=document_type,
        source_path=path.removeprefix("/fileadmin"),
    )


def build_seed(law_pdf: LawPdf, scraper_scope: str, max_pages: int, chunk_chars: int) -> DocumentSeed:
    pdf_bytes = fetch_bytes(law_pdf.url)
    content_hash = hashlib.sha256(pdf_bytes).hexdigest()
    text = extract_pdf_text(pdf_bytes, max_pages)
    text = clean_pdf_text(text)
    if not text:
        text = law_pdf.title
    document_id = uuid.uuid5(DOCUMENT_NAMESPACE, law_pdf.url)
    category_id = uuid.uuid5(RLRC_CATEGORY_NAMESPACE, law_pdf.category_slug)
    law_number = extract_law_number(law_pdf.title)
    publication_date = extract_publication_date(text)
    chunks = chunk_text(text, chunk_chars)
    articles = [
        ArticleChunk(
            article_id=uuid.uuid5(ARTICLE_NAMESPACE, f"{law_pdf.url}#{index}"),
            article_number=f"Part {index}",
            article_title=law_pdf.title,
            article_text=chunk,
            order_index=index,
        )
        for index, chunk in enumerate(chunks, start=1)
    ]
    return DocumentSeed(
        document_id=document_id,
        title=law_pdf.title,
        category_id=category_id,
        category_name=law_pdf.category_name,
        category_slug=law_pdf.category_slug,
        source_url=law_pdf.url,
        source_path=law_pdf.source_path,
        scraper_scope=scraper_scope,
        document_type=law_pdf.document_type,
        content_hash=content_hash,
        law_number=law_number,
        publication_date=publication_date,
        articles=articles,
    )


def fetch_text(url: str) -> str | None:
    try:
        return fetch_bytes(url).decode("utf-8", "ignore")
    except (HTTPError, URLError, TimeoutError) as exc:
        print(f"Skipping unreachable page: {url} ({exc})", file=sys.stderr)
        return None


def fetch_bytes(url: str) -> bytes:
    request = Request(url, headers={"User-Agent": USER_AGENT})
    with urlopen(request, timeout=60) as response:
        return response.read()


def parse_links(url: str, html: str) -> list[Link]:
    parser = LinkParser(url)
    parser.feed(html)
    return parser.links


def is_pdf_url(url: str) -> bool:
    return urlparse(url).path.lower().endswith(".pdf")


def is_rlrc_filelist_url(url: str) -> bool:
    parsed = urlparse(url)
    return parsed.netloc == "www.rlrc.gov.rw" and "tx_filelist_filelist" in parsed.query


def filelist_path(url: str) -> str:
    values = parse_qs(urlparse(url).query).get("tx_filelist_filelist[path]", [""])
    return values[0]


def filelist_under_root(url: str, root_path: str) -> bool:
    path = filelist_path(url)
    return bool(path and root_path and path.startswith(root_path))


def filelist_relevant_to_root(url: str, root_path: str) -> bool:
    path = filelist_path(url)
    return bool(path and root_path and (path.startswith(root_path) or root_path.startswith(path)))


def pdf_under_root(url: str, root_path: str) -> bool:
    path = unquote(urlparse(url).path)
    normalized_path = path.removeprefix("/fileadmin")
    return bool(root_path and normalized_path.startswith(root_path))


def crawl_visit_key(url: str) -> str:
    if not is_rlrc_filelist_url(url):
        return url
    query = parse_qs(urlparse(url).query)
    path = query.get("tx_filelist_filelist[path]", [""])[0]
    page = query.get("tx_filelist_filelist[currentPage]", ["1"])[0]
    return f"{path}#page={page}"


def extract_pdf_text(pdf_bytes: bytes, max_pages: int) -> str:
    if PdfReader is None:
        raise SystemExit("Missing dependency: pypdf. Install it with `python3 -m pip install pypdf`.")
    with tempfile.NamedTemporaryFile(suffix=".pdf") as pdf_file:
        pdf_file.write(pdf_bytes)
        pdf_file.flush()
        reader = PdfReader(pdf_file.name)
        pages = reader.pages[:max_pages]
        return "\n\n".join(page.extract_text() or "" for page in pages)


def chunk_text(text: str, chunk_chars: int) -> list[str]:
    paragraphs = [part.strip() for part in re.split(r"\n{2,}", text) if part.strip()]
    chunks: list[str] = []
    current = ""
    for paragraph in paragraphs:
        if len(current) + len(paragraph) + 2 <= chunk_chars:
            current = f"{current}\n\n{paragraph}".strip()
            continue
        if current:
            chunks.append(current)
        current = paragraph
    if current:
        chunks.append(current)
    return chunks


def clean_pdf_text(text: str) -> str:
    text = text.replace("\x00", " ")
    text = re.sub(r"[ \t]+", " ", text)
    text = re.sub(r"\n[ \t]+", "\n", text)
    text = re.sub(r"Official Gazette[^\n]*\n\s*\d+\s*", "\n", text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def clean_text(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()


def title_from_filename(filename: str) -> str:
    title = re.sub(r"\.pdf$", "", filename, flags=re.IGNORECASE)
    title = re.sub(r"^\d+(\.\d+)*\.?_?", "", title)
    title = title.replace("_", " ")
    title = re.sub(r"\s+", " ", title)
    return title.strip(" .-") or filename


def category_from_path(path: str) -> str:
    for marker in ("/Laws_in_force/", "/Laws_not_in_force/"):
        match = re.search(re.escape(marker) + r"([^/]+)/", path)
        if match:
            category = match.group(1).replace("_", " ")
            category = re.sub(r"^\d+\.?\s*", "", category)
            return clean_text(category).title()
    if "/International_Legal_Instruments" in path:
        return "International Legal Instruments"
    match = re.search(r"/Domestic_laws/([^/]+)/", path)
    if not match:
        return "General"
    category = match.group(1).replace("_", " ")
    category = re.sub(r"^\d+\.?\s*", "", category)
    return clean_text(category).title()


def slugify(value: str) -> str:
    value = value.lower()
    value = re.sub(r"[^a-z0-9]+", "-", value)
    return value.strip("-") or "general"


def extract_law_number(title: str) -> str | None:
    match = re.search(r"(?:law|organic law|order|regulation)[^\d]*(n[°o]*\s*)?([0-9][0-9A-Za-z/_-]*)", title, re.IGNORECASE)
    return match.group(0).strip() if match else None


def detect_document_type(title: str, path: str) -> str:
    value = clean_text(f"{title} {path}").lower()
    if re.search(r"\bpresidential\s+order\b", value):
        return "presidential_order"
    if re.search(r"\bprime\s+minister'?s?\s+order\b", value):
        return "prime_minister_order"
    if re.search(r"\bministerial\s+order\b", value):
        return "ministerial_order"
    if re.search(r"\borganic\s+law\b", value):
        return "organic_law"
    if re.search(r"\blaw\b", value):
        return "law"
    if re.search(r"\bregulation\b", value):
        return "regulation"
    if re.search(r"\binstruction\b", value):
        return "instruction"
    if re.search(r"\bdirective\b", value):
        return "directive"
    if re.search(r"\bdecision\b", value):
        return "decision"
    if "/international_legal_instruments" in value:
        return "international_legal_instrument"
    return "other"


def extract_publication_date(text: str) -> str | None:
    match = re.search(r"\b(\d{2})/(\d{2})/(\d{4})\b", text)
    if not match:
        return None
    day, month, year = match.groups()
    return f"{year}-{month}-{day}"


def write_summary(seeds: list[DocumentSeed], path: Path) -> None:
    payload = [
        {
            "documentId": str(seed.document_id),
            "title": seed.title,
            "category": seed.category_name,
            "documentType": seed.document_type,
            "scraperScope": seed.scraper_scope,
            "sourceUrl": seed.source_url,
            "sourcePath": seed.source_path,
            "contentHash": seed.content_hash,
            "chunks": len(seed.articles),
        }
        for seed in seeds
    ]
    path.write_text(json.dumps(payload, indent=2), encoding="utf-8")
    print(f"Wrote summary: {path}")


def build_database_config(args: argparse.Namespace) -> DatabaseConfig:
    mode = args.db_mode
    if mode == "auto":
        mode = "direct" if args.db_host else "docker"
    return DatabaseConfig(
        mode=mode,
        host=args.db_host,
        port=args.db_port,
        name=args.db_name,
        user=args.db_user,
        password=args.db_password,
        force=args.force,
        skip_existing=args.skip_existing,
    )


def apply_to_database(seeds: list[DocumentSeed], config: DatabaseConfig) -> ApplyResult:
    if config.mode == "direct":
        return apply_to_database_direct(seeds, config)
    return apply_to_database_with_docker(seeds, config)


def apply_to_database_with_docker(seeds: list[DocumentSeed], config: DatabaseConfig) -> ApplyResult:
    sql = build_sql(seeds)
    subprocess.run(
        ["docker", "compose", "exec", "-T", "legal-postgres", "psql", "-U", config.user, "-d", config.name],
        input=sql,
        text=True,
        check=True,
    )
    return ApplyResult(
        applied_documents=len(seeds),
        skipped_documents=0,
        applied_titles=[seed.title for seed in seeds],
        skipped_titles=[],
    )


def apply_to_database_direct(seeds: list[DocumentSeed], config: DatabaseConfig) -> ApplyResult:
    if psycopg is None:
        raise SystemExit(
            "Missing dependency: psycopg. Install it with `python3 -m pip install 'psycopg[binary]'` "
            "or run with --db-mode docker."
        )
    if not config.host:
        raise SystemExit("Direct DB mode requires --db-host or DATABASE_HOST.")

    applied_titles: list[str] = []
    skipped_titles: list[str] = []
    with psycopg.connect(
        host=config.host,
        port=config.port,
        dbname=config.name,
        user=config.user,
        password=config.password,
    ) as connection:
        with connection.cursor() as cursor:
            cursor.execute("SET search_path TO legal, public")
            ensure_scraper_metadata(cursor)
            upsert_rlrc_source(cursor)
            for seed in seeds:
                if should_skip_seed(cursor, seed, config):
                    skipped_titles.append(seed.title)
                    continue
                upsert_seed(cursor, seed)
                applied_titles.append(seed.title)
        connection.commit()

    return ApplyResult(
        applied_documents=len(applied_titles),
        skipped_documents=len(skipped_titles),
        applied_titles=applied_titles,
        skipped_titles=skipped_titles,
    )


def ensure_scraper_metadata(cursor) -> None:
    cursor.execute(
        """
        CREATE TABLE IF NOT EXISTS legal.scraped_legal_documents (
          source_url TEXT PRIMARY KEY,
          document_id UUID UNIQUE NOT NULL REFERENCES legal.legal_documents(id) ON DELETE CASCADE,
          source_name TEXT NOT NULL DEFAULT 'RLRC',
          source_path TEXT,
          scraper_scope TEXT NOT NULL DEFAULT 'custom',
          document_type TEXT NOT NULL DEFAULT 'other',
          content_hash TEXT NOT NULL,
          article_count INT NOT NULL DEFAULT 0,
          first_scraped_at TIMESTAMPTZ NOT NULL DEFAULT now(),
          last_scraped_at TIMESTAMPTZ NOT NULL DEFAULT now()
        )
        """
    )
    cursor.execute("ALTER TABLE legal.scraped_legal_documents ADD COLUMN IF NOT EXISTS source_path TEXT")
    cursor.execute(
        "ALTER TABLE legal.scraped_legal_documents ADD COLUMN IF NOT EXISTS scraper_scope TEXT NOT NULL DEFAULT 'custom'"
    )
    cursor.execute(
        "ALTER TABLE legal.scraped_legal_documents ADD COLUMN IF NOT EXISTS document_type TEXT NOT NULL DEFAULT 'other'"
    )


def upsert_rlrc_source(cursor) -> None:
    cursor.execute(
        """
        INSERT INTO legal.legal_sources (
          id, name, source_type, official_url, institution, trust_level, verified
        ) VALUES (
          %s::uuid,
          'Rwanda Law Reform Commission',
          'official_portal',
          'https://www.rlrc.gov.rw',
          'RLRC',
          'official',
          true
        ) ON CONFLICT (id) DO UPDATE SET
          name = EXCLUDED.name,
          official_url = EXCLUDED.official_url,
          institution = EXCLUDED.institution,
          trust_level = EXCLUDED.trust_level,
          verified = EXCLUDED.verified
        """,
        (str(RLRC_SOURCE_ID),),
    )


def should_skip_seed(cursor, seed: DocumentSeed, config: DatabaseConfig) -> bool:
    if config.force or not config.skip_existing:
        return False
    cursor.execute(
        """
        SELECT content_hash
        FROM legal.scraped_legal_documents
        WHERE source_url = %s
        """,
        (seed.source_url,),
    )
    row = cursor.fetchone()
    return bool(row and row[0] == seed.content_hash)


def upsert_seed(cursor, seed: DocumentSeed) -> None:
    cursor.execute(
        """
        INSERT INTO legal.legal_categories (id, name, slug, description, status)
        VALUES (
          %s::uuid,
          %s,
          %s,
          'Official laws scraped from the Rwanda Law Reform Commission Laws of Rwanda collection.',
          'active'
        ) ON CONFLICT (slug) DO UPDATE SET
          name = EXCLUDED.name,
          description = EXCLUDED.description,
          status = EXCLUDED.status
        RETURNING id
        """,
        (str(seed.category_id), seed.category_name, seed.category_slug),
    )
    category_id = cursor.fetchone()[0]
    cursor.execute(
        "DELETE FROM legal.article_embeddings "
        "WHERE article_id IN (SELECT id FROM legal.legal_articles WHERE document_id = %s::uuid)",
        (str(seed.document_id),),
    )
    cursor.execute("DELETE FROM legal.legal_articles WHERE document_id = %s::uuid", (str(seed.document_id),))
    cursor.execute(
        """
        INSERT INTO legal.legal_documents (
          id, category_id, source_id, title, law_number, publication_date, effective_date,
          language, status, source_url, last_verified_date, extraction_status
        ) VALUES (
          %s::uuid,
          %s::uuid,
          %s::uuid,
          %s,
          %s,
          %s::date,
          %s::date,
          'English',
          'scraped',
          %s,
          CURRENT_DATE,
          'completed'
        ) ON CONFLICT (id) DO UPDATE SET
          category_id = EXCLUDED.category_id,
          source_id = EXCLUDED.source_id,
          title = EXCLUDED.title,
          law_number = EXCLUDED.law_number,
          publication_date = EXCLUDED.publication_date,
          effective_date = EXCLUDED.effective_date,
          language = EXCLUDED.language,
          status = EXCLUDED.status,
          source_url = EXCLUDED.source_url,
          last_verified_date = EXCLUDED.last_verified_date,
          extraction_status = EXCLUDED.extraction_status,
          updated_at = now()
        """,
        (
            str(seed.document_id),
            str(category_id),
            str(RLRC_SOURCE_ID),
            seed.title,
            seed.law_number,
            seed.publication_date,
            seed.publication_date,
            seed.source_url,
        ),
    )
    for article in seed.articles:
        cursor.execute(
            """
            INSERT INTO legal.legal_articles (
              id, document_id, article_number, article_title, article_text, language, status, order_index
            ) VALUES (
              %s::uuid,
              %s::uuid,
              %s,
              %s,
              %s,
              'English',
              'scraped',
              %s
            )
            """,
            (
                str(article.article_id),
                str(seed.document_id),
                article.article_number,
                article.article_title,
                article.article_text,
                article.order_index,
            ),
        )
    cursor.execute(
        """
        INSERT INTO legal.scraped_legal_documents (
          source_url, document_id, source_name, source_path, scraper_scope,
          document_type, content_hash, article_count, last_scraped_at
        ) VALUES (
          %s,
          %s::uuid,
          'RLRC',
          %s,
          %s,
          %s,
          %s,
          %s,
          now()
        ) ON CONFLICT (source_url) DO UPDATE SET
          document_id = EXCLUDED.document_id,
          source_name = EXCLUDED.source_name,
          source_path = EXCLUDED.source_path,
          scraper_scope = EXCLUDED.scraper_scope,
          document_type = EXCLUDED.document_type,
          content_hash = EXCLUDED.content_hash,
          article_count = EXCLUDED.article_count,
          last_scraped_at = now()
        """,
        (
            seed.source_url,
            str(seed.document_id),
            seed.source_path,
            seed.scraper_scope,
            seed.document_type,
            seed.content_hash,
            len(seed.articles),
        ),
    )


def build_sql(seeds: list[DocumentSeed]) -> str:
    lines = [
        "BEGIN;",
        "SET search_path TO legal, public;",
        """
CREATE TABLE IF NOT EXISTS legal.scraped_legal_documents (
  source_url TEXT PRIMARY KEY,
  document_id UUID UNIQUE NOT NULL REFERENCES legal.legal_documents(id) ON DELETE CASCADE,
  source_name TEXT NOT NULL DEFAULT 'RLRC',
  source_path TEXT,
  scraper_scope TEXT NOT NULL DEFAULT 'custom',
  document_type TEXT NOT NULL DEFAULT 'other',
  content_hash TEXT NOT NULL,
  article_count INT NOT NULL DEFAULT 0,
  first_scraped_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_scraped_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
ALTER TABLE legal.scraped_legal_documents ADD COLUMN IF NOT EXISTS source_path TEXT;
ALTER TABLE legal.scraped_legal_documents ADD COLUMN IF NOT EXISTS scraper_scope TEXT NOT NULL DEFAULT 'custom';
ALTER TABLE legal.scraped_legal_documents ADD COLUMN IF NOT EXISTS document_type TEXT NOT NULL DEFAULT 'other';
""",
        """
INSERT INTO legal.legal_sources (
  id, name, source_type, official_url, institution, trust_level, verified
) VALUES (
  '44444444-4444-4444-4444-444444444444',
  'Rwanda Law Reform Commission',
  'official_portal',
  'https://www.rlrc.gov.rw',
  'RLRC',
  'official',
  true
) ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  official_url = EXCLUDED.official_url,
  institution = EXCLUDED.institution,
  trust_level = EXCLUDED.trust_level,
  verified = EXCLUDED.verified;
""",
    ]

    for seed in seeds:
        lines.append(
            f"""
INSERT INTO legal.legal_categories (id, name, slug, description, status)
VALUES (
  {sql_literal(str(seed.category_id))}::uuid,
  {sql_literal(seed.category_name)},
  {sql_literal(seed.category_slug)},
  'Official laws scraped from the Rwanda Law Reform Commission Laws of Rwanda collection.',
  'active'
) ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  status = EXCLUDED.status;

DELETE FROM legal.article_embeddings
WHERE article_id IN (SELECT id FROM legal.legal_articles WHERE document_id = {sql_literal(str(seed.document_id))}::uuid);
DELETE FROM legal.legal_articles WHERE document_id = {sql_literal(str(seed.document_id))}::uuid;

INSERT INTO legal.legal_documents (
  id, category_id, source_id, title, law_number, publication_date, effective_date,
  language, status, source_url, last_verified_date, extraction_status
) VALUES (
  {sql_literal(str(seed.document_id))}::uuid,
  (SELECT id FROM legal.legal_categories WHERE slug = {sql_literal(seed.category_slug)}),
  {sql_literal(str(RLRC_SOURCE_ID))}::uuid,
  {sql_literal(seed.title)},
  {sql_literal(seed.law_number)},
  {sql_date(seed.publication_date)},
  {sql_date(seed.publication_date)},
  'English',
  'scraped',
  {sql_literal(seed.source_url)},
  CURRENT_DATE,
  'completed'
) ON CONFLICT (id) DO UPDATE SET
  category_id = EXCLUDED.category_id,
  source_id = EXCLUDED.source_id,
  title = EXCLUDED.title,
  law_number = EXCLUDED.law_number,
  publication_date = EXCLUDED.publication_date,
  effective_date = EXCLUDED.effective_date,
  language = EXCLUDED.language,
  status = EXCLUDED.status,
  source_url = EXCLUDED.source_url,
  last_verified_date = EXCLUDED.last_verified_date,
  extraction_status = EXCLUDED.extraction_status,
  updated_at = now();
"""
        )
        for article in seed.articles:
            lines.append(
                f"""
INSERT INTO legal.legal_articles (
  id, document_id, article_number, article_title, article_text, language, status, order_index
) VALUES (
  {sql_literal(str(article.article_id))}::uuid,
  {sql_literal(str(seed.document_id))}::uuid,
  {sql_literal(article.article_number)},
  {sql_literal(article.article_title)},
  {sql_literal(article.article_text)},
  'English',
  'scraped',
  {article.order_index}
);
"""
            )
        lines.append(
            f"""
INSERT INTO legal.scraped_legal_documents (
  source_url, document_id, source_name, source_path, scraper_scope,
  document_type, content_hash, article_count, last_scraped_at
) VALUES (
  {sql_literal(seed.source_url)},
  {sql_literal(str(seed.document_id))}::uuid,
  'RLRC',
  {sql_literal(seed.source_path)},
  {sql_literal(seed.scraper_scope)},
  {sql_literal(seed.document_type)},
  {sql_literal(seed.content_hash)},
  {len(seed.articles)},
  now()
) ON CONFLICT (source_url) DO UPDATE SET
  document_id = EXCLUDED.document_id,
  source_name = EXCLUDED.source_name,
  source_path = EXCLUDED.source_path,
  scraper_scope = EXCLUDED.scraper_scope,
  document_type = EXCLUDED.document_type,
  content_hash = EXCLUDED.content_hash,
  article_count = EXCLUDED.article_count,
  last_scraped_at = now();
"""
        )
    lines.extend(["COMMIT;", ""])
    return "\n".join(lines)


def sql_literal(value: str | None) -> str:
    if value is None:
        return "NULL"
    return "'" + value.replace("'", "''") + "'"


def sql_date(value: str | None) -> str:
    if value is None:
        return "NULL"
    return f"{sql_literal(value)}::date"


def trigger_index(internal_api_key: str, ai_service_url: str) -> int:
    url = ai_service_url.rstrip("/") + "/v1/legal/index"
    request = Request(url, method="POST", headers={"X-Internal-API-Key": internal_api_key})
    with urlopen(request, timeout=120) as response:
        payload = json.loads(response.read().decode("utf-8"))
    return int(payload.get("indexed", 0))


if __name__ == "__main__":
    raise SystemExit(main())
