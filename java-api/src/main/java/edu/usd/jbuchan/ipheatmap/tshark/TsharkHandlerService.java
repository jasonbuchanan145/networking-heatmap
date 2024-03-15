package edu.usd.jbuchan.ipheatmap.tshark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.usd.jbuchan.ipheatmap.geometrics.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@Slf4j
public class TsharkHandlerService {
@Autowired
MetricsService metricsService;

    private final ObjectMapper objectMapper;

    public TsharkHandlerService(@Autowired ObjectMapper objectMapper){
        this.objectMapper=objectMapper;
    }
    @Async
    public void run(String device) throws IOException {
        // -i for the network interface that is being measured, on mine it's "Wi-Fi"
        // -T ek formats it in the form of elastic search which outputs as json (but as opposed to -T json outputs it all on one line to make it easier for the object mapper)
        String[] args = new String[]{"tshark", "-i", device, "-T", "ek", "-e", "ip.src", "-e", "ip.dst", "-e", "frame.len", "-Y", "ip"};
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();
        try {
            //get the stream from the process for execution in the json factory
            //use flux to better manage the buffer and allow for multiprocessing
            Flux<String> flux = Flux.create(sink -> new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines()
                    .forEach(sink::next));
            //run in parallel because processing this can be resource intensive, especially with a network call
            flux.parallel()
                    //tshark includes index frames, ignore
                    .filter(frame -> !frame.startsWith("{\"index\""))
                    .subscribe(frame -> {
                        try {
                            metricsService.makeMetrics(objectMapper.readValue(frame, Shark.class));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
            });
        }finally{
            process.destroy();
        }
    }
}
