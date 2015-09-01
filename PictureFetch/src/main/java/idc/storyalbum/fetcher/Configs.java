package idc.storyalbum.fetcher;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by yonatan on 16/4/2015.
 */
@Configuration
public class Configs {
    @Value("${flickr.key}")
    String flickrKey;

    @Value("${flickr.secret}")
    String flickrSecret;

    @Bean
    public Flickr flicker() {
        Flickr f = new Flickr(flickrKey, flickrSecret, new REST());
        return f;
    }

    @Bean
    RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate();
        rt.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        return rt;
    }
}
