package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.entity.FishTypeEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ReservationDetailResponseDto {

    private Long id;
    private String title;
    private String imageUrl;

    // ✅ 지역 이름들을 리스트로 응답
    private List<String> regionNames;

    private String companyName;
    private String type;        // ENUM 이름 (ex: FISHING)
    private String typeLower;   // ENUM 소문자 (ex: fishing)
    private String typeKorean;  // ENUM 한글명 (ex: 일반 낚시)
    private Integer price;
    private String content;

    // 🎣 낚시 종류
    private List<String> fishTypes;

    // 📆 날짜별 예약 가능 정보
    private List<AvailableDateDto> availableDates;

    @Getter
    @Builder
    public static class AvailableDateDto {
        private String rawDate;     // ✅ 전송용: yyyy-MM-dd (서버 전달 시 사용)
        private String date;        // ✅ 출력용: yyyy-MM-dd(요일) (화면 표시용)
        private String time;        // 예약 시간대 (예: 06:00~14:00)
        private Integer capacity;   // 최대 정원
        private Integer remaining;  // 남은 인원 수
    }

    /**
     * ✅ 요일 포함한 날짜 포맷 함수
     */
    private static String formatDateWithDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String dayKor = switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
        return date.toString() + "(" + dayKor + ")";
    }

    /**
     * ✅ ReservationPost + 예약 가능 날짜 리스트 → DTO 변환
     * - 출력용 날짜 (`date`)에 요일 추가
     * - 전송용 날짜 (`rawDate`)는 순수 yyyy-MM-dd 포맷 유지
     */
    public static ReservationDetailResponseDto from(ReservationPost post, List<AvailableDateDto> dateDtos) {

        List<String> regionNames = post.getRegions().stream()
                .map(region -> {
                    String parent = region.getParent() != null ? "(" + region.getParent().getName() + ")" : "";
                    return parent + region.getName();
                })
                .collect(Collectors.toList());

        // 요일 가공해서 두 날짜 형태로 나눔
        List<AvailableDateDto> formattedDateDtos = dateDtos.stream()
                .map(dto -> {
                    LocalDate parsedDate = LocalDate.parse(dto.getDate());
                    return AvailableDateDto.builder()
                            .rawDate(parsedDate.toString())                       // ex: 2025-06-28
                            .date(formatDateWithDay(parsedDate))                 // ex: 2025-06-28(토)
                            .time(dto.getTime())
                            .capacity(dto.getCapacity())
                            .remaining(dto.getRemaining())
                            .build();
                })
                .collect(Collectors.toList());

        return ReservationDetailResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .imageUrl(
                        post.getImageUrl() != null && !post.getImageUrl().isEmpty()
                                ? post.getImageUrl()
                                : "/images/" + post.getType().name().toLowerCase() + ".jpg"
                )
                .regionNames(regionNames)
                .companyName(post.getCompanyName())
                .type(post.getType().name())
                .typeLower(post.getType().name().toLowerCase())
                .typeKorean(post.getType().getKorean())
                .price(post.getPrice())
                .content(post.getContent())
                .fishTypes(
                        post.getFishTypes().stream()
                                .map(FishTypeEntity::getName)
                                .collect(Collectors.toList())
                )
                .availableDates(formattedDateDtos)
                .build();
    }

}
