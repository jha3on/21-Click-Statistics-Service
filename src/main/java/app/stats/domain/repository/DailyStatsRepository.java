package app.stats.domain.repository;

import app.stats.domain.model.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {
    DailyStats findByDate(String date);
}