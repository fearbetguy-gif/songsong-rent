package com.songsong.rent.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class HouseListVO {
    private Long id;
    private String title;
    private BigDecimal rentPrice;
    private String city;
    private String district;
    private String areaName;
    private Double latitude;
    private Double longitude;
    private String roomType;
    private BigDecimal areaSize;
    private String coverImage;
}
