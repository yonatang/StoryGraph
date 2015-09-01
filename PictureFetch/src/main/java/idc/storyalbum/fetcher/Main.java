package idc.storyalbum.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;

/**
 * Created by yonatan on 15/4/2015.
 */
@SpringBootApplication(exclude = ConfigurableEmbeddedServletContainer.class)
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
