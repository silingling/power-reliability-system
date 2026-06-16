<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" size="small">
        <el-form-item label="对比维度"><el-select v-model="dimension" placeholder="请选择"><el-option label="按台区" value="station" /><el-option label="按线路" value="line" /><el-option label="按区域" value="area" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="refreshChart">分析</el-button></el-form-item>
      </el-form>
    </div>
    <el-row :gutter="20">
      <el-col :span="14"><el-card><div id="compareChart" style="width:100%;height:400px"></div></el-card></el-col>
      <el-col :span="10"><el-card><div id="pieChart" style="width:100%;height:400px"></div></el-card></el-col>
    </el-row>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%;margin-top:15px">
      <el-table-column prop="name" label="名称" width="140" /><el-table-column prop="asi" label="ASI" width="100" /><el-table-column prop="saidi" label="SAIDI" width="100" />
      <el-table-column prop="saifi" label="SAIFI" width="100" /><el-table-column prop="rs" label="RS(%)" width="100" /><el-table-column prop="outageCount" label="停电次数" width="100" />
    </el-table>
  </div>
</template>
<script>
import * as echarts from 'echarts'
import { getCompareData } from '@/api/index'
export default {
  name: 'IndexCompare',
  data() { return { loading: false, list: [], dimension: 'station' } },
  mounted() { this.refreshChart() },
  methods: {
    refreshChart() {
      getCompareData({ dimension: this.dimension }).then(r => {
        this.list = r.data.list || []
        const chart = echarts.init(document.getElementById('compareChart'))
        chart.setOption({
          title: { text: '可靠性指标对比', left: 'center' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['ASI', 'SAIDI', 'SAIFI'], bottom: 0 },
          xAxis: { type: 'category', data: (r.data.list || []).map(i => i.name) },
          yAxis: { type: 'value' },
          series: [
            { name: 'ASI', type: 'bar', data: (r.data.list || []).map(i => i.asi) },
            { name: 'SAIDI', type: 'bar', data: (r.data.list || []).map(i => i.saidi) },
            { name: 'SAIFI', type: 'bar', data: (r.data.list || []).map(i => i.saifi) }
          ]
        })
        const pie = echarts.init(document.getElementById('pieChart'))
        pie.setOption({
          title: { text: '停电原因分布', left: 'center' },
          tooltip: { trigger: 'item' },
          series: [{ type: 'pie', radius: '60%', data: (r.data.reasons || []), label: { formatter: '{b}:{d}%' } }]
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
