package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("house_price_history")
public class HousePriceHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("house_id")
    private Long houseId;

    @TableField("old_price")
    private BigDecimal oldPrice;

    @TableField("new_price")
    private BigDecimal newPrice;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
