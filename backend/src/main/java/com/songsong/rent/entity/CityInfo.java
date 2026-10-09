package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("city_info")
public class CityInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("create_time")
    private LocalDateTime createTime;
}
