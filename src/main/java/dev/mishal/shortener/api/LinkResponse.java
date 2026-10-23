package dev.mishal.shortener.api;

import java.time.Instant;

public record LinkResponse(
        String code,
        String originalUrl,
        String shortUrl,
        long clickCount,
        Instant createdAt
) {
}
