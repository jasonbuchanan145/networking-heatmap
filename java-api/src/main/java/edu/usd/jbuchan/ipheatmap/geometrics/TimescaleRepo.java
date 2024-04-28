package edu.usd.jbuchan.ipheatmap.geometrics;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimescaleRepo extends JpaRepository<Timescale, Long> {
    //Find by IP that's within the last 2 minutes
    Optional<Timescale> findTimescaleByIpEqualsAndTimeGreaterThan(String ip, long twoMinutesAgo);

    //Increment count for an existing entry, quicker than saving the entire object again with an incremented count
    @Modifying
    @Query("UPDATE Timescale t SET t.count=t.count+1 WHERE t.id = ?1")
    void incrementCountById(long id);

    @Modifying
    @Transactional
    default void incrementOrInsertByIp(String ip) {
        findTimescaleByIpEqualsAndTimeGreaterThan(ip, System.currentTimeMillis() - 120000)
                .ifPresentOrElse(ipDb -> incrementCountById(ipDb.getId()), () -> {
            Timescale newEntry = new Timescale();
            newEntry.setIp(ip);
            newEntry.setTime(System.currentTimeMillis());
            newEntry.setCount(1);
            save(newEntry);
        });
    }
}
