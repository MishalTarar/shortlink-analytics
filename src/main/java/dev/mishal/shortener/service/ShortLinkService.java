package dev.mishal.shortener.service;

import dev.mishal.shortener.api.LinkResponse;
import dev.mishal.shortener.api.LinkStatsResponse;
import dev.mishal.shortener.domain.ShortLink;
import dev.mishal.shortener.repository.ShortLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

@Service
public class ShortLinkService {

    private static final String CODE_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShortLinkRepository repository;
    private final String baseUrl;

    public ShortLinkService(
            ShortLinkRepository repository,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl
    ) {
        this.repository = repository;
        this.baseUrl = removeTrailingSlash(baseUrl);
    }

    @Transactional
    public LinkResponse create(String submittedUrl) {
        String originalUrl = validateAndNormalizeUrl(submittedUrl);
        String code = generateAvailableCode();
        ShortLink saved = repository.save(new ShortLink(code, originalUrl));
        return toLinkResponse(saved);
    }

    @Transactional(readOnly = true)
    public LinkStatsResponse getStats(String code) {
        ShortLink link = findByCode(code);
        return new LinkStatsResponse(
                link.getCode(),
                link.getOriginalUrl(),
                shortUrlFor(link.getCode()),
                link.getClickCount(),
                link.getCreatedAt(),
                link.getLastClickedAt()
        );
    }

    @Transactional
    public URI recordClickAndGetDestination(String code) {
        ShortLink link = findByCode(code);
        int updatedRows = repository.recordClick(code, Instant.now());
        if (updatedRows == 0) {
            throw new LinkNotFoundException(code);
        }
        return URI.create(link.getOriginalUrl());
    }

    private ShortLink findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));
    }

    private String validateAndNormalizeUrl(String submittedUrl) {
        String value = submittedUrl == null ? "" : submittedUrl.trim();
        try {
            URI uri = URI.create(value);
            String scheme = Optional.ofNullable(uri.getScheme()).orElse("");
            boolean supportedScheme = scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https");
            if (!supportedScheme || uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidDestinationUrlException();
            }
            return uri.toASCIIString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidDestinationUrlException();
        }
    }

    private String generateAvailableCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String candidate = randomCode();
            if (!repository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique short code. Please retry.");
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }

    private LinkResponse toLinkResponse(ShortLink link) {
        return new LinkResponse(
                link.getCode(),
                link.getOriginalUrl(),
                shortUrlFor(link.getCode()),
                link.getClickCount(),
                link.getCreatedAt()
        );
    }

    private String shortUrlFor(String code) {
        return baseUrl + "/" + code;
    }

    private static String removeTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8080";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
