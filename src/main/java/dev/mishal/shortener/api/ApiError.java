package dev.mishal.shortener.api;

import java.time.Instant;

public record ApiError(String message, Instant timestamp) {
}
