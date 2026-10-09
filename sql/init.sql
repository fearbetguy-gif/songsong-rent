-- 城市表
CREATE TABLE IF NOT EXISTS city_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(64) NOT NULL COMMENT '城市名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_city_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='城市';

-- 区域表
CREATE TABLE IF NOT EXISTS district_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    city_id BIGINT NOT NULL COMMENT '所属城市',
    name VARCHAR(64) NOT NULL COMMENT '区域名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_city_id (city_id),
    UNIQUE KEY uk_city_district (city_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区县';

-- 房源主表
CREATE TABLE IF NOT EXISTS house (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(128) NOT NULL COMMENT '房源标题',
    city_id BIGINT NOT NULL COMMENT '城市',
    district_id BIGINT NOT NULL COMMENT '区域',
    area_name VARCHAR(64) DEFAULT NULL COMMENT '商圈/片区',
    address VARCHAR(255) NOT NULL COMMENT '详细地址',
    latitude DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    longitude DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    rent_price DECIMAL(10,2) NOT NULL COMMENT '租金',
    room_type VARCHAR(32) DEFAULT NULL COMMENT '户型',
    area_size DECIMAL(10,2) DEFAULT NULL COMMENT '面积(平米)',
    floor_info VARCHAR(64) DEFAULT NULL COMMENT '楼层信息',
    orientation VARCHAR(32) DEFAULT NULL COMMENT '朝向',
    decoration VARCHAR(32) DEFAULT NULL COMMENT '装修',
    house_type VARCHAR(32) DEFAULT NULL COMMENT '房屋类型',
    rent_type TINYINT NOT NULL DEFAULT 1 COMMENT '租赁类型:1整租,2合租',
    description TEXT COMMENT '房源描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1正常,0删除',
    publish_status TINYINT NOT NULL DEFAULT 1 COMMENT '发布状态:1已发布,0未发布',
    audit_status TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态:1通过,0待审,2驳回',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    creator_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_city_district (city_id, district_id),
    KEY idx_status_publish (status, publish_status),
    KEY idx_update_time (update_time),
    KEY idx_creator_id (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源主表';

-- 房源图片表
CREATE TABLE IF NOT EXISTS house_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    house_id BIGINT NOT NULL COMMENT '房源ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片地址',
    sort_num INT NOT NULL DEFAULT 0 COMMENT '排序值',
    is_cover TINYINT NOT NULL DEFAULT 0 COMMENT '是否封面:1是,0否',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_house_id (house_id),
    KEY idx_house_sort (house_id, sort_num),
    KEY idx_house_cover (house_id, is_cover)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源图片表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    nickname VARCHAR(64) NOT NULL COMMENT '昵称',
    is_admin TINYINT NOT NULL DEFAULT 0 COMMENT '是否管理员:1是,0否',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员账号（密码为 123456 的 BCrypt 哈希值）
INSERT INTO `user` (username, nickname, is_admin, password)
SELECT 'admin', '系统管理员', 1, '$2a$10$wN1G2N/7WJ9b4nFz6fD6v.e7u4oW5d7P.j2h8x3j1k2l3m4n5o6p7'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'admin');

-- 标签字典表
CREATE TABLE IF NOT EXISTS label_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(64) NOT NULL COMMENT '标签名称',
    sort_num INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_sort_num (sort_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签字典';

-- 房源-标签关联表
CREATE TABLE IF NOT EXISTS room_label (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    room_id BIGINT NOT NULL COMMENT '房源ID',
    label_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_room_label (room_id, label_id),
    KEY idx_room_id (room_id),
    KEY idx_label_id (label_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源-标签关联';

-- 房源审核日志表
CREATE TABLE IF NOT EXISTS house_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    house_id BIGINT NOT NULL COMMENT '房源ID',
    from_status TINYINT DEFAULT NULL COMMENT '变更前审核状态',
    to_status TINYINT NOT NULL COMMENT '变更后审核状态',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_house_time (house_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源审核日志';

-- 房源价格历史表
CREATE TABLE IF NOT EXISTS house_price_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    house_id BIGINT NOT NULL COMMENT '房源ID',
    old_price DECIMAL(10,2) NOT NULL COMMENT '变更前价格',
    new_price DECIMAL(10,2) NOT NULL COMMENT '变更后价格',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    effective_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_house_time (house_id, create_time),
    KEY idx_operator_time (operator_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源价格历史表';

-- 预约看房表
CREATE TABLE IF NOT EXISTS house_appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    house_id BIGINT NOT NULL COMMENT '房源ID',
    user_id BIGINT NOT NULL COMMENT '预约用户ID',
    viewer_name VARCHAR(64) NOT NULL COMMENT '看房人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    appointment_time DATETIME NOT NULL COMMENT '预约看房时间',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待确认,1已确认,2已拒绝,3已完成,4已取消',
    reject_reason VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
    operator_id BIGINT DEFAULT NULL COMMENT '管理员操作人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_house_id (house_id),
    KEY idx_user_id (user_id),
    KEY idx_status_time (status, appointment_time),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约看房';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS `user_favorite` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `house_id` bigint NOT NULL COMMENT '房源ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_house` (`user_id`,`house_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';
