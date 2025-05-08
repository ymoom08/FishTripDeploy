// 위치: com.fishtripplanner.domain.reservation.mapper.ReservationTypeMapper

package com.fishtripplanner.domain.reservation.mapper;

import com.fishtripplanner.domain.reservation.ReservationType;

public class ReservationTypeMapper {

    /**
     * ✅ 예약 타입(enum)을 한글로 변환
     */
    public static String toKorean(ReservationType type) {
        return switch (type) {
            case BOAT -> "선상낚시";
            case FLOAT -> "좌대";
            case ISLAND -> "섬";
            case ROCK -> "갯바위";
            case STAY -> "숙박/민박/캠핑";
        };
    }
}