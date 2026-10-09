package com.songsong.rent.vo;

import com.songsong.rent.entity.LabelInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LandlordHouseVO {

    private Long id;
    private String title;
    private BigDecimal rentPrice;
    private String district;
    private String description;
    private List<LabelInfo> labels;

    private Integer status;
    private Integer publishStatus;
    private Integer auditStatus;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
