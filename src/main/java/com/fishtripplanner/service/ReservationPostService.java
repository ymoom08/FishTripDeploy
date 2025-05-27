package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.dto.ReservationPostResponse;
import com.fishtripplanner.dto.reservation.ReservationCreateRequestDto;
import com.fishtripplanner.dto.reservation.ReservationDetailResponseDto;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import com.fishtripplanner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationPostService {

    private final ReservationPostRepository reservationPostRepository;
    private final RegionRepository regionRepository;
    private final FishTypeRepository fishTypeRepository;
    private final ReservationPostAvailableDateRepository availableDateRepository;
    private final UserRepository userRepository;
    private final ReservationOrderRepository reservationOrderRepository;

    public List<ReservationPostResponse> createReservationPosts(ReservationPostRequest request, User user) {
        List<RegionEntity> regions = regionRepository.findAllById(request.getRegionIds());

        return regions.stream().map(region -> {
            ReservationPost post = request.toEntity(region);
            post.setOwner(user);

            // ✅ 어종 ID 기반으로 처리
            List<FishTypeEntity> fishTypes = fishTypeRepository.findAllById(request.getFishTypeIds());
            post.setFishTypes(fishTypes);

            reservationPostRepository.save(post);

            List<ReservationPostAvailableDate> availableDates = request.getAvailableDates().stream()
                    .map(date -> ReservationPostAvailableDate.builder()
                            .reservationPost(post)
                            .availableDate(date)
                            .build())
                    .toList();

            availableDateRepository.saveAll(availableDates);

            return ReservationPostResponse.from(post);
        }).toList();
    }

    public Page<ReservationPost> filterPosts(
            ReservationType type,
            List<Long> regionIds,
            List<LocalDate> dates,
            List<String> fishTypes,
            String keyword,
            String sortKey,
            Pageable pageable
    ) {
        List<Long> safeRegionIds = (regionIds == null || regionIds.isEmpty()) ? null : regionIds;
        List<String> safeFishTypes = (fishTypes == null || fishTypes.isEmpty()) ? null : fishTypes;
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;

        Sort sort = switch (sortKey) {
            case "priceAsc"  -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "latest"    -> Sort.by("createdAt").descending();
            default          -> Sort.by("createdAt").descending();
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        String conditionKey = String.format("%s-%s-%s",
                safeRegionIds != null,
                dates != null,
                safeFishTypes != null
        );

        return switch (conditionKey) {
            case "true-true-true"   -> reservationPostRepository.findByFiltersStrict(type, safeRegionIds, dates, safeFishTypes, sortedPageable);
            case "true-true-false"  -> reservationPostRepository.findByTypeAndRegionIdsAndDate(type, safeRegionIds, dates, sortedPageable);
            case "false-true-true"  -> reservationPostRepository.findByDateAndFishTypes(type, dates, safeFishTypes, sortedPageable);
            case "true-false-true"  -> reservationPostRepository.findByRegionIdsAndFishTypes(type, safeRegionIds, safeFishTypes, sortedPageable);
            case "false-false-true" -> reservationPostRepository.findByFishTypes(type, safeFishTypes, sortedPageable);
            case "false-true-false" -> reservationPostRepository.findByTypeAndDate(type, dates, sortedPageable);
            case "true-false-false" -> reservationPostRepository.findByTypeAndRegionIds(type, safeRegionIds, sortedPageable);
            default -> {
                if (safeRegionIds == null && dates == null && safeFishTypes == null && safeKeyword == null) {
                    yield reservationPostRepository.findByType(type, sortedPageable);
                }
                yield reservationPostRepository.findByFilters(type, safeRegionIds, dates, safeFishTypes, safeKeyword, sortedPageable);
            }
        };
    }

    public List<String> getFishTypeNames() {
        return reservationPostRepository.findAllFishTypeNames().stream().sorted().toList();
    }

    public List<String> getUsedRegionNames() {
        return reservationPostRepository.findAllRegionNames();
    }

    public void saveReservation(ReservationCreateRequestDto dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (dto.getRegionIds() == null || dto.getRegionIds().isEmpty()) {
            throw new IllegalArgumentException("지역 ID 리스트는 비어 있을 수 없습니다.");
        }

        if (dto.getFishTypeNames() == null || dto.getFishTypeNames().isEmpty()) {
            throw new IllegalArgumentException("어종 리스트는 비어 있을 수 없습니다.");
        }

        if (dto.getAvailableDates() == null || dto.getAvailableDates().isEmpty()) {
            throw new IllegalArgumentException("예약 가능 날짜는 1개 이상 입력되어야 합니다.");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<RegionEntity> regions = regionRepository.findAllById(dto.getRegionIds());
        List<FishTypeEntity> fishTypes = fishTypeRepository.findByNameIn(dto.getFishTypeNames());

        for (RegionEntity region : regions) {
            ReservationPost post = new ReservationPost();
            post.setTitle(dto.getTitle());
            post.setType(ReservationType.valueOf(dto.getType()));
            post.setRegion(region);
            post.setContent(dto.getContent());
            post.setPrice(dto.getPrice());
            post.setCompanyName(dto.getCompanyName());
            post.setOwner(user);
            post.setFishTypes(fishTypes);

            // 이미지 저장
            if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
                try {
                    String fileName = UUID.randomUUID() + "_" + dto.getImageFile().getOriginalFilename();
                    String uploadDir = "uploads/reservation_images/";
                    Path savePath = Paths.get(uploadDir, fileName);

                    Files.createDirectories(savePath.getParent());
                    Files.write(savePath, dto.getImageFile().getBytes());

                    post.setImageUrl("/uploads/reservation_images/" + fileName);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 저장 실패", e);
                }
            }

            reservationPostRepository.save(post);

            List<ReservationPostAvailableDate> dates = dto.getAvailableDates().stream()
                    .map(d -> {
                        ReservationPostAvailableDate ad = new ReservationPostAvailableDate();
                        ad.setReservationPost(post);
                        ad.setAvailableDate(LocalDate.parse(d.getDate()));
                        ad.setCapacity(d.getCapacity());
                        ad.setTime(d.getTime());
                        return ad;
                    }).toList();

            availableDateRepository.saveAll(dates);
        }
    }



    public ReservationDetailResponseDto getReservationDetail(Long postId) {
        ReservationPost post = reservationPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약글이 존재하지 않습니다."));

        List<ReservationDetailResponseDto.AvailableDateDto> dateDtos =
                post.getAvailableDates().stream().map(ad -> {
                    int reserved = reservationOrderRepository.countByAvailableDate(ad.getAvailableDate());
                    return ReservationDetailResponseDto.AvailableDateDto.builder()
                            .date(ad.getAvailableDate().toString())
                            .time(ad.getTime())
                            .capacity(ad.getCapacity())
                            .remaining(ad.getCapacity() - reserved)
                            .build();
                }).toList();

        return ReservationDetailResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .imageUrl(post.getImageUrl())
                .regionName(post.getRegion().getName())
                .companyName(post.getCompanyName())
                .type(post.getType().name())
                .typeLower(post.getType().name().toLowerCase())
                .typeKorean(post.getType().getKorean())
                .price(post.getPrice())
                .content(post.getContent())
                .fishTypes(post.getFishTypes().stream().map(FishTypeEntity::getName).toList())
                .availableDates(dateDtos)
                .build();
    }
}
