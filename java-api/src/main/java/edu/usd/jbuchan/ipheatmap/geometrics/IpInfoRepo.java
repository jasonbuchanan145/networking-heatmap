package edu.usd.jbuchan.ipheatmap.geometrics;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpInfoRepo extends JpaRepository<IpInfo, String> {
    @Transactional
    default void insertIfNotExists(IpInfo ipInfo) {
        findById(ipInfo.getIp()).orElseGet(() -> save(ipInfo));
    }
}
