package app.stats.domain.service;

import app.stats.application.exception.InvalidJsonFormatException;
import app.stats.application.exception.NotFoundDataException;
import app.stats.application.payload.DailyStatsDto;
import app.stats.application.payload.HourlyStatsDto;
import app.stats.domain.model.DailyStats;
import app.stats.domain.model.HourlyStats;
import app.stats.domain.repository.DailyStatsRepository;
import app.stats.domain.repository.HourlyStatsRepository;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final Gson gson;
    private final DailyStatsRepository dailyStatsRepository;
    private final HourlyStatsRepository hourlyStatsRepository;

    @Transactional
    public void store(String json) {
        DailyStats inputData; // 입력 데이터
        DailyStats existingData; // 기존 데이터

        try {
            inputData = gson.fromJson(json, DailyStatsDto.class).toEntity();
        } catch (JsonParseException | NullPointerException e) {
            // JSON 파일을 Dto 객체로 변환할 수 없을 경우
            throw new InvalidJsonFormatException();
        }

        existingData = dailyStatsRepository.findByDate(inputData.getDate());
        if (existingData == null) {
            // 날짜가 없을 경우 -> 전체 데이터를 등록한다.
            for (HourlyStats hourlyStats : new ArrayList<>(inputData.getHourlyStatsList())) {
                // 등록할 시각 데이터의 개수만큼 반복하며 매핑한다.
                hourlyStats.setDailyStats(inputData);
            }
            dailyStatsRepository.save(inputData);
        } else {
            // 날짜가 있을 경우 -> 시각별 데이터를 등록 또는 갱신한다.
            for (HourlyStats sourceStats : inputData.getHourlyStatsList()) {
                // 등록/갱신할 시각별 데이터의 개수만큼 반복한다.
                HourlyStats targetStats = hourlyStatsRepository.findByDailyStatsAndHour(existingData, sourceStats.getHour());

                if (targetStats == null) {
                    // 날짜가 있고 시각이 없을 경우 -> 없는 시각 데이터를 등록한다.
                    sourceStats.setDailyStats(existingData);
                } else {
                    // 날짜가 있고 시각이 있을 경우 -> 있는 시각 데이터를 갱신한다.
                    targetStats.update(sourceStats);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public String read(String date) {
        DailyStats dailyStats = dailyStatsRepository.findByDate(date);

        // 날짜로 조회한 데이터가 없을 경우
        if (dailyStats == null) throw new NotFoundDataException();

        // 요청, 응답, 클릭 횟수 합계
        int totalRequest = 0, totalResponse = 0, totalClick = 0;
        for (HourlyStats hourlyStats : dailyStats.getHourlyStatsList()) {
            totalRequest += hourlyStats.getRequest();
            totalResponse += hourlyStats.getResponse();
            totalClick += hourlyStats.getClick();
        }

        // Entity -> Dto
        DailyStatsDto dto = DailyStatsDto.builder()
                .totalDate(dailyStats.getDate())
                .totalRequest(totalRequest)
                .totalResponse(totalResponse)
                .totalClick(totalClick)
                .build();

        // Dto -> String
        return gson.toJson(dto);
    }

    @Transactional(readOnly = true)
    public String read(String date, int hour) {
        DailyStats dailyStats = dailyStatsRepository.findByDate(date);
        HourlyStats hourlyStats = hourlyStatsRepository.findByDailyStatsAndHour(dailyStats, hour);

        // 날짜와 시각으로 조회한 데이터가 없을 경우
        if (dailyStats == null || hourlyStats == null) throw new NotFoundDataException();

        // Entity -> Dto
        HourlyStatsDto dto = HourlyStatsDto.builder()
                .hour(hourlyStats.getHour())
                .request(hourlyStats.getRequest())
                .response(hourlyStats.getResponse())
                .click(hourlyStats.getClick())
                .build();

        // Dto -> String
        return gson.toJson(dto);
    }
}