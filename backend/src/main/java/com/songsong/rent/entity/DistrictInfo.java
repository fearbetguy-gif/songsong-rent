package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("district_info")
public class DistrictInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("city_id")
    private Long cityId;

    private String name;

    @TableField("create_time")
    private LocalDateTime createTime;
}
