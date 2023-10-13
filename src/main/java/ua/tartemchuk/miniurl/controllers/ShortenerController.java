package ua.tartemchuk.miniurl.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import ua.tartemchuk.miniurl.services.impl.ShortenerServiceImpl;

@RestController
@RequestMapping("/urls/shortUrl")
public class ShortenerController {

    @Value(value = "${url-shortener.short-url-prefix}")
    private String prefix;

    private final ShortenerServiceImpl shortenerService;

    public ShortenerController(ShortenerServiceImpl shortenerService) {
        this.shortenerService = shortenerService;
    }

    @PostMapping
    public String shorten(@RequestBody String longUrl) {
        return prefix + shortenerService.shortenUrl(longUrl);
    }

    @GetMapping("/{shortUrl}")
    public RedirectView redirectLongUrl(@PathVariable String shortUrl) {
        String longUrl = shortenerService.obtainRedirectionTarget(shortUrl);
        RedirectView redirectView = new RedirectView();
        redirectView.setStatusCode(HttpStatusCode.valueOf(301));
        redirectView.setUrl(longUrl);
        return redirectView;
    }

}
