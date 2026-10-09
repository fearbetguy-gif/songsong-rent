package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("house_audit_log")
public class HouseAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("house_id")
    private Long houseId;

    @TableField("from_status")
    private Integer fromStatus;

    @TableField("to_status")
    private Integer toStatus;

    @TableField("operator_id")
    private Long operatorId;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;
}
