package edu.usd.jbuchan.ipheatmap.tshark;

import lombok.Data;

@Data
public class Shark {
    private String destIp;
    private long time;
    private String srcIp;
    private Integer packetSize;

}
