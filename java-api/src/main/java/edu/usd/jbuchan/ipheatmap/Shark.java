package edu.usd.jbuchan.ipheatmap;

import lombok.Data;

@Data
public class Shark {
    private String destIp;
    private long time;
    private String srcIp;
    private Integer packetSize;

}
