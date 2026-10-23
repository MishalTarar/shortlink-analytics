package dev.mishal.shortener.api;

import java.time.Instant;

public record LinkStatsResponse(
        String code,
        String originalUrl,
        String shortUrl,
        long clickCount,
        Instant createdAt,
        Instant lastClickedAt
) {
}
