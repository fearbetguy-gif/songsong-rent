package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("label_info")
public class LabelInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("sort_num")
    private Integer sortNum;

    @TableField("create_time")
    private LocalDateTime createTime;
}
