package edu.usd.jbuchan.ipheatmap;

import lombok.Data;

@Data
public class SharkDelist {
    private String destIp;
    private long time;
    private String srcIp;
    private Integer packetSize;
    SharkDelist(WiresharkFrame frame){
        this.destIp=frame.getDestIp().getFirst();
        this.time=frame.getTime();
        this.srcIp=frame.getSrcIp().getFirst();
        this.packetSize=frame.getPacketSize().getFirst();
    }
}
