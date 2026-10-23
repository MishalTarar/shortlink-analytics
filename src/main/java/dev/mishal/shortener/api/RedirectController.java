package dev.mishal.shortener.api;

import dev.mishal.shortener.service.ShortLinkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final ShortLinkService shortLinkService;

    public RedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        URI destination = shortLinkService.recordClickAndGetDestination(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(destination);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
