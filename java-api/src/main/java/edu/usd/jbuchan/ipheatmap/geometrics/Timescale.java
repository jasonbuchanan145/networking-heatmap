package edu.usd.jbuchan.ipheatmap.geometrics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Timescale {
    @Id
    @GeneratedValue
    private long id;
    @Column
    private String ip;
    @Column
    private long time;
    @Column
    private long count;
}
