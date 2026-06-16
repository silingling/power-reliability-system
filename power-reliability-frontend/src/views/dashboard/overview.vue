<template>
  <div class="app-container">
    <el-row :gutter="15" style="margin-bottom:15px">
      <el-col :span="6" v-for="(card,i) in statCards" :key="i">
        <el-card shadow="hover" :body-style="{padding:'20px'}">
          <div class="stat-num">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="15">
      <el-col :span="14">
        <el-card><div slot="header"><span>停电趋势</span></div><div id="trendChart" style="width:100%;height:350px"></div></el-card>
      </el-col>
      <el-col :span="10">
        <el-card><div slot="header"><span>停电类型分布</span></div><div id="pieChart" style="width:100%;height:350px"></div></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="15" style="margin-top:15px">
      <el-col :span="24">
        <el-card><div slot="header"><span>台区可靠性排名</span></div>
          <el-table :data="rankList" border stripe size="small" style="width:100%">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="stationName" label="台区名称" min-width="150" />
            <el-table-column prop="asi" label="ASI" width="100" />
            <el-table-column prop="saidi" label="SAIDI" width="100" />
            <el-table-column prop="saifi" label="SAIFI" width="100" />
            <el-table-column prop="rs" label="RS(%)" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import * as echarts from 'echarts'
import { getOverviewData, getKeyIndicators, getOutageTrend, getStationRanking } from '@/api/dashboard'
export default {
  name: 'DashboardOverview',
  data() { return { statCards: [], rankList: [] } },
  mounted() { this.fetchData() },
  methods: {
    fetchData() {
      getKeyIndicators().then(r => { if (r.data) this.statCards = r.data })
      getStationRanking().then(r => { this.rankList = r.data.list || []; if (r.data.list) this.renderCharts(r.data.list) })
      getOutageTrend({}).then(r => { if (r.data) this.renderTrend(r.data) })
    },
    renderCharts(data) {
      const names = data.map(i => i.stationName)
      const pieChart = echarts.init(document.getElementById('pieChart'))
      pieChart.setOption({
        tooltip: { trigger: 'item' },
        series: [{ type: 'pie', radius: '60%', data: data.slice(0,10).map(i => ({ name: i.stationName, value: i.asi || 0 })), label: { formatter: '{b}: {d}%' } }]
      })
    },
    renderTrend(data) {
      const trendChart = echarts.init(document.getElementById('trendChart'))
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.months || [] },
        yAxis: { type: 'value' },
        series: [
          { name: '计划停电', type: 'bar', stack: 'total', data: data.planned || [] },
          { name: '故障停电', type: 'bar', stack: 'total', data: data.fault || [] },
          { name: 'RS(%)', type: 'line', yAxisIndex: 1, data: data.rs || [] }
        ],
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
      })
    }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.stat-num { font-size: 30px; font-weight: bold; color: #409eff; text-align: center; }
.stat-label { text-align: center; color: #666; margin-top: 5px; font-size: 13px; }
</style>
