<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" size="small">
        <el-form-item label="分析周期"><el-select v-model="period" placeholder="请选择"><el-option label="近12个月" value="12m" /><el-option label="近6个月" value="6m" /><el-option label="近3个月" value="3m" /></el-select></el-form-item>
        <el-form-item label="指标类型"><el-select v-model="indicator" placeholder="请选择"><el-option label="ASI" value="asi" /><el-option label="SAIDI" value="saidi" /><el-option label="SAIFI" value="saifi" /><el-option label="RS" value="rs" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="refreshChart">分析</el-button></el-form-item>
      </el-form>
    </div>
    <el-card><div id="trendChart" style="width:100%;height:400px"></div></el-card>
  </div>
</template>
<script>
import * as echarts from 'echarts'
import { getTrendData } from '@/api/index'
export default {
  name: 'IndexTrend',
  data() { return { period: '12m', indicator: 'asi' } },
  mounted() { this.refreshChart() },
  methods: {
    refreshChart() {
      getTrendData({ period: this.period, indicator: this.indicator }).then(r => {
        const data = r.data || { months: [], values: [] }
        const chart = echarts.init(document.getElementById('trendChart'))
        chart.setOption({
          title: { text: '可靠性指标趋势分析', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: data.months },
          yAxis: { type: 'value' },
          series: [{ type: 'line', smooth: true, data: data.values, areaStyle: { opacity: 0.3 } }],
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
        })
      })
    }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
</style>
