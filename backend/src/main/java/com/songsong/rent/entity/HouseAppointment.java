package com.songsong.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("house_appointment")
public class HouseAppointment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("house_id")
    private Long houseId;

    @TableField("user_id")
    private Long userId;

    @TableField("viewer_name")
    private String viewerName;

    private String phone;

    @TableField("appointment_time")
    private LocalDateTime appointmentTime;

    private String remark;

    /**
     * 状态: 0待确认,1已确认,2已拒绝,3已完成,4已取消
     */
    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}

