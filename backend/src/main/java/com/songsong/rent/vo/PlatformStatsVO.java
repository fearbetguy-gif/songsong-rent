package com.songsong.rent.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlatformStatsVO {

    private Long houseCount;

    private Long cityCount;

    private Long districtCount;

    private BigDecimal avgRentPrice;
}
