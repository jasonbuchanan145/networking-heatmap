package edu.usd.jbuchan.ipheatmap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IpheatmapApplicationTests {
    @Autowired
    private CommandLineRunner clr;

    @Test
    void contextLoads() throws Exception {
        this.clr.run();
    }

}
