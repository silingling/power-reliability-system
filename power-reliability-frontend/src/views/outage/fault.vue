<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="故障类型"><el-select v-model="query.faultType" placeholder="请选择" clearable><el-option label="线路故障" value="line" /><el-option label="设备故障" value="device" /><el-option label="外力破坏" value="external" /><el-option label="其他" value="other" /></el-select></el-form-item>
        <el-form-item label="处理状态"><el-select v-model="query.status" placeholder="请选择" clearable><el-option label="待派单" value="pending" /><el-option label="抢修中" value="repairing" /><el-option label="已恢复" value="restored" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增故障记录</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="stationName" label="台区名称" width="130" />
      <el-table-column prop="faultType" label="故障类型" width="100"><template slot-scope="{row}">{{ {line:'线路故障',device:'设备故障',external:'外力破坏',other:'其他'}[row.faultType]||row.faultType }}</template></el-table-column>
      <el-table-column prop="occurTime" label="发生时间" width="160" /><el-table-column prop="description" label="故障描述" min-width="200" />
      <el-table-column prop="status" label="状态" width="90"><template slot-scope="{row}"><el-tag :type="{pending:'danger',repairing:'warning',restored:'success'}[row.status]" size="small">{{ {pending:'待派单',repairing:'抢修中',restored:'已恢复'}[row.status]||row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" @click="handleDispatch(row)">派单</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新增故障记录':'编辑故障记录'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="台区名称" prop="stationName"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="故障类型" prop="faultType"><el-select v-model="form.faultType" style="width:100%"><el-option label="线路故障" value="line" /><el-option label="设备故障" value="device" /><el-option label="外力破坏" value="external" /><el-option label="其他" value="other" /></el-select></el-form-item>
        <el-form-item label="发生时间" prop="occurTime"><el-date-picker v-model="form.occurTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%" /></el-form-item>
        <el-form-item label="故障描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getFaultList, addFault, updateFault } from '@/api/outage'
export default {
  name: 'OutageFault',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', faultType: '', status: '' }, dialogVisible: false, dialogType: 'add', form: { stationName: '', faultType: 'line', occurTime: '', description: '' }, rules: { stationName: [{ required: true, trigger: 'blur' }], description: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getFaultList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', faultType: '', status: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { stationName: '', faultType: 'line', occurTime: '', description: '' }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDispatch() { this.$message.info('派单功能待接入后端') },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addFault : updateFault)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>
