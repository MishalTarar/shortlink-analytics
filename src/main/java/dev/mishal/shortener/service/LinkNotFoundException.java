package dev.mishal.shortener.service;

public class LinkNotFoundException extends RuntimeException {

    public LinkNotFoundException(String code) {
        super("No short link exists for code '" + code + "'");
    }
}
