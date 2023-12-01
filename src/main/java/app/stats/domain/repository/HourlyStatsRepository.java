package app.stats.domain.repository;

import app.stats.domain.model.DailyStats;
import app.stats.domain.model.HourlyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HourlyStatsRepository extends JpaRepository<HourlyStats, Long> {
    HourlyStats findByDailyStatsAndHour(DailyStats dailyStats, int hour);
}