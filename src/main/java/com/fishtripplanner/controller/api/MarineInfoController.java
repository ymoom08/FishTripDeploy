package com.fishtripplanner.controller.api;

import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.mapper.MarineInfoMapper;
import com.fishtripplanner.api.khoa.FishingIndexService;
import com.fishtripplanner.api.khoa.TripMarineInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/marine-info")
@RequiredArgsConstructor
public class MarineInfoController {

    private final TripMarineInfoService tripMarineInfoService;
    private final FishingIndexService fishingIndexService;

    @GetMapping
    public MarineInfoResponseDto getMarineInfo(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String area,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String fishType
    ) {
        // 통합 데이터 수집
        var result = tripMarineInfoService.getMarineInfo(lat, lon, area, date);

        // 어종별 추천 시간대 선택 (선택사항)
        var recommended = (fishType != null)
                ? fishingIndexService.recommendBestTime(result.getFishingIndexList(), fishType)
                : java.util.Optional.empty();

        return MarineInfoMapper.toResponseDto(result, recommended);
    }
}