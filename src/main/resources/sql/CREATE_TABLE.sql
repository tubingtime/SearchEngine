CREATE TABLE IF NOT EXISTS crawler_stats (
    page_url TEXT UNIQUE,
    snippet TEXT,
    title TEXT,
    content_length INTEGER,
    timestamp_crawled TIMESTAMP
);
