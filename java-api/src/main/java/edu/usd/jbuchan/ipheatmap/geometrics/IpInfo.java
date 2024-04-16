package edWu.usd.jbuchan.ipheatmap.geometrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class IpInfo {
    @Id
    private String ip;
    @Column
    private String loc;
    @Column
    private String postal;
    @Column
    private String city;
    @Column
    private String country;
}
