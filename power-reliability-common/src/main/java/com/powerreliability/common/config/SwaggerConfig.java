package com.powerreliability.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("低压供电可靠性全流程管控系统 API 文档")
                .version("1.0.0")
                .description("基于Spring Cloud微服务架构，聚焦0.4kV低压配网供电可靠性全流程管控")
                .contact(new Contact().name("同业电力")));
    }

    @Bean
    public GroupedOpenApi ledgerApi() {
        return GroupedOpenApi.builder().group("基础台账").pathsToMatch("/api/ledger/**").build();
    }
    @Bean
    public GroupedOpenApi outageApi() {
        return GroupedOpenApi.builder().group("停电管控").pathsToMatch("/api/outage/**").build();
    }
    @Bean
    public GroupedOpenApi governanceApi() {
        return GroupedOpenApi.builder().group("频繁停电治理").pathsToMatch("/api/governance/**").build();
    }
    @Bean
    public GroupedOpenApi indexApi() {
        return GroupedOpenApi.builder().group("指标核算").pathsToMatch("/api/index/**").build();
    }
    @Bean
    public GroupedOpenApi warningApi() {
        return GroupedOpenApi.builder().group("隐患预警").pathsToMatch("/api/warning/**").build();
    }
    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder().group("复盘考核").pathsToMatch("/api/review/**").build();
    }
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder().group("系统管理").pathsToMatch("/api/system/**").build();
    }
    @Bean
    public GroupedOpenApi dashboardApi() {
        return GroupedOpenApi.builder().group("可视化大屏").pathsToMatch("/api/dashboard/**").build();
    }
}
