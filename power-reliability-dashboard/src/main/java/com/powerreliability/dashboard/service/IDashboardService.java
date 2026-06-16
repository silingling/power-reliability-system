package com.powerreliability.dashboard.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IDashboardService {

    /** 全域总览 */
    Map<String, Object> overview();

    /** 当日/本周/本月停电统计 */
    Map<String, Object> outageStat(String period);

    /** SAIDI/SAIFI趋势 */
    List<Map<String, Object>> indexTrend();

    /** 抢修统计 */
    Map<String, Object> repairStat();

    /** 治理进度 */
    Map<String, Object> governanceProgress();

    /** 台区热力图 */
    List<Map<String, Object>> areaHeatmap();

    /** 预警清单 */
    List<Map<String, Object>> warningList();

    /** 班组排名 */
    List<Map<String, Object>> ranking();
}
