package edu.usd.jbuchan.ipheatmap.geometrics;

import edu.usd.jbuchan.ipheatmap.tshark.Shark;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MetricsService {
    private final ConcurrentHashMap<String, IpInfo> geo = new ConcurrentHashMap<>();
    private final IpInfoRepo ipInfoRepo;
    private final TimescaleRepo timescaleRepo;
    @Value("${geolocation.enabled}")
    private boolean getGeo;
    private final WebClient webClient;
    //built with pattern from the ip list https://stackoverflow.com/a/9482369 to attempt to filter out local traffic, although more lient
    // (ie does not validate that it's in the form of xxx.xxx.xxx.xxx, just if the string starts in a specific range denoted as localhost or local network)
    //since we don't want to add that to the heat map, the idea is the project is to show where the user is going
    //also obviously there is no geo information for a localhost or a local address.
    private final String regexIpTest = "^(127|192\\.168|10|172\\.(1[6-9]|2[0-9]|3[0-1])|169\\.254|22[4-9]|23[0-9]|255\\.255\\.255\\.255).*";

    @Autowired
    public MetricsService(@Autowired IpInfoRepo ipInfoRepo, @Autowired TimescaleRepo timescaleRepo) {
        this.ipInfoRepo = ipInfoRepo;
        this.webClient = WebClient.create("http://ipinfo.io");
        this.timescaleRepo = timescaleRepo;
    }

    @PostConstruct
    public void init() {
        buildMap();
    }
    //prefetch from db on startup and populate the cache
    protected void buildMap() {
        ipInfoRepo.findAll().forEach(loc -> geo.put(loc.getIp(), loc));
    }

    public void makeMetrics(Shark shark) {
        //get the value of the geo located ip addresses if present, if not present fetch them
        //while also ensuring there isn't a race condition with the properties of the concurrent hashmap
        handleIp(shark.getDestIp());
        handleIp(shark.getSrcIp());
    }
    private void handleIp(String ip){
        if (StringUtils.hasText(ip)&& !ip.matches(regexIpTest)) {
            geo.computeIfAbsent(ip, this::fetchAndSaveIpLocation);
            timescaleRepo.incrementOrInsertByIp(ip);
        }
    }
    private IpInfo fetchAndSaveIpLocation(String ip) {
        IpInfo ipInfo = null;
        if (getGeo) {
            try {
                log.info("getting ip geo info for" + ip);
                ipInfo = webClient.get().uri("/{ip}", ip).accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(IpInfo.class)
                        //since this is used to compute the key we want it now as opposed to lazy load
                        .block();
            } catch (Exception e) {
                log.error("got an exception calling ip", e);
            }
            if (ipInfo != null) {
                ipInfoRepo.save(ipInfo);
            }
            //see javadocs for concurrenthashmap computerIfAbsent if a value for a given key is null compute if absent will try again on the next pass. This is not desired behavior because
            //if an ip can't be located we don't want to try a thousand more times.
            return Objects.requireNonNullElseGet(ipInfo, IpInfo::new);
        } else {
            ipInfo = new IpInfo();
            ipInfo.setIp(ip);
            ipInfoRepo.save(ipInfo);
            return ipInfo;
        }
    }
}
