package edu.usd.jbuchan.ipheatmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

//Wireshark frame data that we care about for the heatmap. @JsonProperty is used to backwards map the
//object from the form it's in wireshark output to the form I want it in here.
//it's strange that ek treats everything as an array even the things that obviously aren't like the ip dest
@Data
public class WiresharkFrame {
    @JsonProperty("layers.ip_dst")
    private List<String> destIp;

    @JsonProperty("timestamp")
    private Long time;

    @JsonProperty("layers.ip_src")
    private List<String> srcIp;

    @JsonProperty("layers.frame_len")
    private List<Integer> packetSize;
}
