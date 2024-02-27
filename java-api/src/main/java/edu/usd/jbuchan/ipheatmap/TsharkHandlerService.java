package edu.usd.jbuchan.ipheatmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@Slf4j
public class TsharkHandlerService {
    @Autowired
    private ObjectMapper objectMapper;
    public void run(String device) throws IOException {
        //-i for the device, on mine it's "Wi-Fi"
        // -T ek formats it in the form of elastic search which outputs as json (but as opposed to -T json outputs it all on one line to make it easier for the object mapper)
        // however it also outputs an "index" object which I don't care about so have grep throw out that line
        String[] args = new String[]{"tshark", "-i", device, "-T", "ek", "-e", "ip.src", "-e", "ip.dst", "-e", "frame.len", "-Y", "ip", "|", "grep", "-v", "'\"index\"'"};
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();
        try {
            //get the stream from the process for execution in the json factory

            //use flux to better manage the buffer and allow for multiprocessing
            Flux<WiresharkFrame> flux = Flux.create(sink -> {
                new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines()
                        .forEach(line -> sink.next(objectMapper.convertValue(line, WiresharkFrame.class)));
            });
            //run in parallel because processing this can be resource intensive
            flux.parallel().subscribe(frame -> {

            });
        }finally{
            process.destroy();
        }
    }
    public String buildCmd(String[] args) throws ParseException {
        Options options = new Options();
        Option networkInterface = new Option("n","network-interface",true,"the network interface that wireshark should listen to");
        networkInterface.setRequired(true);
        options.addOption(networkInterface);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("wireshark-heat-map",options);
            throw e;
        }
        return cmd.getOptionValue('n');
    }
}
