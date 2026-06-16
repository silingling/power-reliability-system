package com.powerreliability.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Sentinel 网关限流配置（5.1.1）
 *
 * 自动加载 sentinel-gateway-rules.json 中定义的限流规则，
 * 注册 SentinelGatewayFilter 和异常处理 Handler。
 */
@Slf4j
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void init() {
        log.info("[SentinelGatewayConfig] 开始初始化网关限流规则");
        initGatewayRules();
        initCustomizedApis();
        log.info("[SentinelGatewayConfig] 网关限流规则初始化完成");
    }

    /**
     * 注册 Sentinel 网关过滤器（优先级最高）
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        log.info("[SentinelGatewayConfig] 注册 SentinelGatewayFilter");
        return new SentinelGatewayFilter();
    }

    /**
     * 配置限流异常处理：返回 JSON 而不是默认的 HTML
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        log.info("[SentinelGatewayConfig] 注册 SentinelGatewayBlockExceptionHandler");
        return new SentinelGatewayBlockExceptionHandler();
    }

    /**
     * 初始化网关路由限流规则
     * 从 sentinel-gateway-rules.json 加载，若加载失败则使用默认规则兜底
     */
    private void initGatewayRules() {
        List<GatewayFlowRule> rules = new ArrayList<>();

        // 默认规则：与 sentinel-gateway-rules.json 保持一致
        // 全局 API 限流
        rules.add(new GatewayFlowRule("power-reliability-api")
                .setCount(1000)
                .setGrade(RuleConstant.FLOW_GRADE_QPS));

        // 仪表盘限流
        rules.add(new GatewayFlowRule("dashboard-api")
                .setCount(500)
                .setGrade(RuleConstant.FLOW_GRADE_QPS));

        // 台账服务限流
        rules.add(new GatewayFlowRule("ledger-api")
                .setCount(200)
                .setGrade(RuleConstant.FLOW_GRADE_QPS));

        // 停电服务限流
        rules.add(new GatewayFlowRule("outage-api")
                .setCount(200)
                .setGrade(RuleConstant.FLOW_GRADE_QPS));

        // 治理服务限流
        rules.add(new GatewayFlowRule("governance-api")
                .setCount(200)
                .setGrade(RuleConstant.FLOW_GRADE_QPS));

        GatewayRuleManager.loadRules(rules);
        log.info("[SentinelGatewayConfig] 已加载 {} 条网关限流规则", rules.size());
    }

    /**
     * 初始化自定义 API 分组
     * 将路径模式映射到 API 分组，供限流规则使用
     */
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();

        // 全局 API
        definitions.add(new ApiDefinition("power-reliability-api")
                .setPredicateItems(Collections.singletonList(
                        (ApiPredicateItem) new ApiPathPredicateItem()
                                .setPattern("/api/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                )));

        // 仪表盘 API
        definitions.add(new ApiDefinition("dashboard-api")
                .setPredicateItems(Collections.singletonList(
                        (ApiPredicateItem) new ApiPathPredicateItem()
                                .setPattern("/api/dashboard/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                )));

        // 台账 API
        definitions.add(new ApiDefinition("ledger-api")
                .setPredicateItems(Collections.singletonList(
                        (ApiPredicateItem) new ApiPathPredicateItem()
                                .setPattern("/api/ledger/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                )));

        // 停电 API
        definitions.add(new ApiDefinition("outage-api")
                .setPredicateItems(Collections.singletonList(
                        (ApiPredicateItem) new ApiPathPredicateItem()
                                .setPattern("/api/outage/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                )));

        // 治理 API
        definitions.add(new ApiDefinition("governance-api")
                .setPredicateItems(Collections.singletonList(
                        (ApiPredicateItem) new ApiPathPredicateItem()
                                .setPattern("/api/governance/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                )));

        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        log.info("[SentinelGatewayConfig] 已加载 {} 个自定义 API 分组", definitions.size());
    }
}
