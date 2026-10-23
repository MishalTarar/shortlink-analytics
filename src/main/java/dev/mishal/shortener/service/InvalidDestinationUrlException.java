package dev.mishal.shortener.service;

public class InvalidDestinationUrlException extends RuntimeException {

    public InvalidDestinationUrlException() {
        super("url must be a valid absolute http or https URL");
    }
}
