package newsletter.api;

import newsletter.model.NewsItem;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple in-memory cache for news items with TTL.
 *
 * Behavior:
 * - If cache is empty or expired, the first thread that acquires the lock will refresh from NewsApiClient.
 * - If refresh fails and there is an existing cached value, the existing cached value is returned.
 * - If refresh fails and there is no cached value, the exception is propagated.
 */
public final class NewsCache {
    private volatile List<NewsItem> cached;
    private volatile long lastFetchEpochSec = 0L;
    private final int ttlSeconds;
    private final NewsApiClient client;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Construct a cache.
     *
     * @param ttlSeconds TTL in seconds for cached data (must be > 0)
     * @param client     NewsApiClient instance (non-null)
     */
    public NewsCache(int ttlSeconds, NewsApiClient client) {
        if (ttlSeconds <= 0) throw new IllegalArgumentException("ttlSeconds must be > 0");
        if (client == null) throw new IllegalArgumentException("client must not be null");
        this.ttlSeconds = ttlSeconds;
        this.client = client;
    }

    /**
     * Return the latest cached news. If cache is stale, attempt to refresh.
     *
     * @return list of NewsItem (may be empty)
     * @throws Exception if fetching fresh data fails and no cached data exists
     */
    public List<NewsItem> getLatest() throws Exception {
        long now = Instant.now().getEpochSecond();
        if (cached == null || (now - lastFetchEpochSec) > ttlSeconds) {
            // attempt to refresh; only one thread should perform the refresh
            if (lock.tryLock()) {
                try {
                    // double-check after acquiring lock
                    now = Instant.now().getEpochSecond();
                    if (cached == null || (now - lastFetchEpochSec) > ttlSeconds) {
                        try {
                            List<NewsItem> fresh = client.fetchTopHeadlines();
                            cached = fresh;
                            lastFetchEpochSec = Instant.now().getEpochSecond();
                        } catch (Exception e) {
                            // if we have cached data, return it; otherwise propagate
                            if (cached != null) {
                                return cached;
                            }
                            throw e;
                        }
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                // another thread is refreshing; if we have cached data return it,
                // otherwise wait briefly for the refresh to complete
                int waited = 0;
                while ((cached == null || (Instant.now().getEpochSecond() - lastFetchEpochSec) > ttlSeconds) && waited < 5000) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    waited += 100;
                }
                if (cached == null) {
                    // no cached data available after waiting; attempt one last time to fetch
                    try {
                        List<NewsItem> fresh = client.fetchTopHeadlines();
                        cached = fresh;
                        lastFetchEpochSec = Instant.now().getEpochSecond();
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
        return cached;
    }

    /**
     * Force clear the cache (useful for admin or tests).
     */
    public void clear() {
        cached = null;
        lastFetchEpochSec = 0L;
    }
}

