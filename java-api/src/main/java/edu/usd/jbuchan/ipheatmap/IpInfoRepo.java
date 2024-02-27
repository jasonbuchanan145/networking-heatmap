package edu.usd.jbuchan.ipheatmap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpInfoRepo extends JpaRepository<IpInfo,String> {
}
