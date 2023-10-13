package ua.tartemchuk.miniurl.controllers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ua.tartemchuk.miniurl.services.impl.ShortenerServiceImpl;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortenerController.class)
class ShortenerControllerTest {

    public static final String LONG_URL = "https://example.com";
    public static final String SHORT_URL_PATH = "shortUrlPath";
    private final String BASE_URI = "/urls/shortUrl";

    @Value(value = "${url-shortener.short-url-prefix}")
    private String prefix;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortenerServiceImpl shortenerService;

    @Test
    void shorten_ok() throws Exception {
        doReturn(SHORT_URL_PATH).when(shortenerService).shortenUrl(LONG_URL);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URI)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(LONG_URL)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString(prefix + SHORT_URL_PATH)))
                .andDo(print());
    }

    @Test
    void redirectLongUrl_ok() throws Exception {
        doReturn(LONG_URL).when(shortenerService).obtainRedirectionTarget(SHORT_URL_PATH);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/" + SHORT_URL_PATH).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LONG_URL))
                .andDo(print());
    }

}
