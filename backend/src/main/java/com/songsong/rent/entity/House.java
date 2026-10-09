package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("house")
public class House {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("city_id")
    private Long cityId;

    @TableField("district_id")
    private Long districtId;

    @TableField(exist = false)
    private String city;

    @TableField(exist = false)
    private String district;

    @TableField("area_name")
    private String areaName;

    private String address;

    private Double latitude;

    private Double longitude;

    @TableField("rent_price")
    private BigDecimal rentPrice;

    @TableField("room_type")
    private String roomType;

    @TableField("area_size")
    private BigDecimal areaSize;

    @TableField("floor_info")
    private String floorInfo;

    private String orientation;

    private String decoration;

    @TableField("house_type")
    private String houseType;

    @TableField("rent_type")
    private Integer rentType;

    private String description;

    private Integer status;

    @TableField("publish_status")
    private Integer publishStatus;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("cover_image")
    private String coverImage;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
