package edu.usd.jbuchan.ipheatmap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.usd.jbuchan.ipheatmap.tshark.Shark;
import edu.usd.jbuchan.ipheatmap.tshark.SharkDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
    //see comment at the top of SharkDeserializer.java for why this has to be a thing
    @Bean(name="sharkMapper")
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Shark.class,new SharkDeserializer());
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}
