FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

COPY law-scraper/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY law-scraper/scripts/scrape-rwanda-laws.py ./scrape-rwanda-laws.py

ENTRYPOINT ["python", "/app/scrape-rwanda-laws.py"]
CMD ["--apply", "--index"]
