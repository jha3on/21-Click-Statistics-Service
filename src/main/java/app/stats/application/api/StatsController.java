package app.stats.application.api;

import app.stats.application.exception.InvalidFileTypeException;
import app.stats.application.payload.ApiResponse;
import app.stats.domain.service.StatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
@Api(value = "Stats Controller")
public class StatsController {
    private final StatsService statsService;
    private static final String ACCEPT_TYPE = "application/json";

    @GetMapping("/daily")
    @ApiOperation(value = "데이터 조회", notes = "날짜(2020-01-01)를 이용한 데이터 조회")
    public ApiResponse read(
        @ApiParam(value = "날짜", required = true, example = "2020-01-01") @RequestParam String date
    ) {
        return new ApiResponse(statsService.read(date));
    }

    @GetMapping("/hourly")
    @ApiOperation(value = "데이터 조회", notes = "날짜(2020-01-01)와 시각(21)을 이용한 데이터 조회")
    public ApiResponse read(
        @ApiParam(value = "날짜", required = true, example = "2020-01-01") @RequestParam String date,
        @ApiParam(value = "시각", required = true, example = "21") @RequestParam Integer hour
    ) {
        return new ApiResponse(statsService.read(date, hour));
    }

    @PostMapping("/")
    @ApiOperation(value = "데이터 등록", notes = "날짜/시각을 기록한 JSON 파일 등록")
    public ApiResponse store(
        @ApiParam(value = "파일", required = true, example = ".json") @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (!ACCEPT_TYPE.equals(file.getContentType())) {
            throw new InvalidFileTypeException();
        }

        statsService.store(new String(file.getBytes()));

        return new ApiResponse("");
    }
}