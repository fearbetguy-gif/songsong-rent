package com.songsong.rent.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final UserAuthInterceptor userAuthInterceptor;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void initDb() {
        try {
            try {
                jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");
            } catch (Exception ignored) {
            }

            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `user_favorite` (" +
                    "  `id` bigint NOT NULL AUTO_INCREMENT," +
                    "  `user_id` bigint NOT NULL COMMENT '用户ID'," +
                    "  `house_id` bigint NOT NULL COMMENT '房源ID'," +
                    "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间'," +
                    "  PRIMARY KEY (`id`)," +
                    "  UNIQUE KEY `uk_user_house` (`user_id`,`house_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表'");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `consult_message` (" +
                    "  `id` bigint NOT NULL AUTO_INCREMENT," +
                    "  `house_id` bigint NOT NULL COMMENT '房源ID'," +
                    "  `from_user_id` bigint NOT NULL COMMENT '发送人ID'," +
                    "  `to_user_id` bigint NOT NULL COMMENT '接收人ID'," +
                    "  `content` varchar(1000) NOT NULL COMMENT '消息内容'," +
                    "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间'," +
                    "  PRIMARY KEY (`id`)," +
                    "  KEY `idx_house_time` (`house_id`,`create_time`)," +
                    "  KEY `idx_to_user_time` (`to_user_id`,`create_time`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线咨询消息表'");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `lease_contract` (" +
                    "  `id` bigint NOT NULL AUTO_INCREMENT," +
                    "  `house_id` bigint NOT NULL COMMENT '房源ID'," +
                    "  `landlord_id` bigint NOT NULL COMMENT '房东ID'," +
                    "  `tenant_id` bigint NOT NULL COMMENT '租客ID'," +
                    "  `monthly_rent` decimal(10,2) NOT NULL COMMENT '月租金'," +
                    "  `start_date` date NOT NULL COMMENT '租约开始日期'," +
                    "  `end_date` date NOT NULL COMMENT '租约结束日期'," +
                    "  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态:0待生效,1履约中,2已结束,3已解约'," +
                    "  `remark` varchar(255) DEFAULT NULL COMMENT '备注'," +
                    "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)," +
                    "  KEY `idx_landlord_status` (`landlord_id`,`status`)," +
                    "  KEY `idx_house` (`house_id`)," +
                    "  KEY `idx_tenant` (`tenant_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租约表'");
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `rent_bill` (" +
                    "  `id` bigint NOT NULL AUTO_INCREMENT," +
                    "  `lease_id` bigint NOT NULL COMMENT '租约ID'," +
                    "  `billing_month` varchar(20) NOT NULL COMMENT '账期，例如 2026-04'," +
                    "  `amount` decimal(10,2) NOT NULL COMMENT '账单金额'," +
                    "  `due_date` date NOT NULL COMMENT '应付日期'," +
                    "  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态:0待支付,1已支付,2已逾期'," +
                    "  `paid_time` datetime DEFAULT NULL COMMENT '实际收款时间'," +
                    "  `payment_method` varchar(64) DEFAULT NULL COMMENT '收款方式'," +
                    "  `remark` varchar(255) DEFAULT NULL COMMENT '备注'," +
                    "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)," +
                    "  UNIQUE KEY `uk_lease_month` (`lease_id`,`billing_month`)," +
                    "  KEY `idx_status_due` (`status`,`due_date`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租金账单表'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/house/**",
                        "/city/**",
                        "/district/**",
                        "/label/**",
                        "/house-image/**",
                        "/house-label/**",
                        "/audit-log/**",
                        "/upload/**",
                        "/user/**",
                        "/appointment-admin/**"
                );
        
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns(
                        "/appointment/**",
                        "/favorite/**",
                        "/landlord/**",
                        "/consult/**"
                );
    }
}
