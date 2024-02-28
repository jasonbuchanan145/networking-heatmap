package edu.usd.jbuchan.ipheatmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TsharkHandlerService {
    private ConcurrentHashMap<String,IpInfo> geo;
    private final MeterRegistry meterRegistry;

    //built with pattern from the ip list https://stackoverflow.com/a/9482369 to attempt to filter out local traffic, although more lient
    // (ie does not validate that it's in the form of xxx.xxx.xxx.xxx, just if the string starts in a specific range denoted as localhost or local network)
    //since we don't want to add that to the heat map, the idea is the project is to show where the user is going
    //also obviously there is no geo information for a localhost or a local address.
    private final String regexIpTest="^(127|192\\.168|10|172\\.(1[6-9]|2[0-9]|3[0-1])|169\\.254|22[4-9]|23[0-9]|255\\.255\\.255\\.255).*";

    public TsharkHandlerService(@Autowired MeterRegistry meterRegistry){
        this.meterRegistry=meterRegistry;
    }
    WebClient webClient = WebClient.create("http://ipinfo.io");
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
            Flux<String> flux = Flux.create(sink -> {
                new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines()
                        .forEach(sink::next);
            });
            //run in parallel because processing this can be resource intensive, especially with a network call and mysql ops
            flux.parallel().subscribe(frame -> {
                WiresharkFrame rawFrame =objectMapper.convertValue(frame, WiresharkFrame.class);
                SharkDelist delist = new SharkDelist(rawFrame);
                //get the value of the geo located ip addresses if present, if not present fetch them
                //while also ensuring there isn't a race condition with the properties of the concurrent hashmap
                if(!StringUtils.hasText(delist.getDestIp())||!delist.getDestIp().matches(regexIpTest)) {
                    IpInfo destIpInfo = geo.computeIfAbsent(delist.getDestIp(), this::getIpGeo);
                    markGeo(destIpInfo,true);
                }
                if(!delist.getSrcIp().matches(regexIpTest)) {
                    IpInfo srcIpInfo = geo.computeIfAbsent(delist.getSrcIp(), this::getIpGeo);
                    markGeo(srcIpInfo,false);
                }
            });
        }finally{
            process.destroy();
        }
    }

    private void markGeo(IpInfo ipInfo, boolean isDst) {
        //WIP: to start tomorrow
        Counter.builder(String.format("ip.addr.%s}",isDst?"dest":"src"));
    }

    private IpInfo getIpGeo(String ip) {
        IpInfo ipInfo=null;
        try {
            log.info("getting ip geo info for"+ip);
            ipInfo = webClient.get().uri("/{ip}", ip).accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(IpInfo.class)
                    //since this is used to compute the key we want it now as opposed to lazy load
                    .block();
        }catch (Exception e){
            log.error("got an exception calling ip", e);
        }
        //see javadocs for concurrenthashmap computerIfAbsent if a value for a given key is null compute if absent will try again on the next pass. This is not desired behavior because
        //if an ip can't be located we don't want to try a thousand more times.
        return Objects.requireNonNullElseGet(ipInfo, IpInfo::new);
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
