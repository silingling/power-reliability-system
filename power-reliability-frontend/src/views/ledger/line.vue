<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="线路名称"><el-input v-model="query.lineName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="线路编号"><el-input v-model="query.lineCode" placeholder="请输入" clearable /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增线路</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="80" /><el-table-column prop="lineName" label="线路名称" min-width="150" /><el-table-column prop="lineCode" label="线路编号" width="150" />
      <el-table-column prop="voltageLevel" label="电压等级" width="120" /><el-table-column prop="startStation" label="起始站" width="140" /><el-table-column prop="endStation" label="终点站" width="140" />
      <el-table-column prop="totalLength" label="总长度(km)" width="120" /><el-table-column prop="stationCount" label="台区数量" width="100" />
      <el-table-column label="操作" width="200" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新增线路':'编辑线路'" :visible.sync="dialogVisible" width="550px">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="线路名称" prop="lineName"><el-input v-model="form.lineName" /></el-form-item>
        <el-form-item label="线路编号" prop="lineCode"><el-input v-model="form.lineCode" /></el-form-item>
        <el-form-item label="电压等级"><el-input v-model="form.voltageLevel" /></el-form-item>
        <el-form-item label="起始站"><el-input v-model="form.startStation" /></el-form-item>
        <el-form-item label="终点站"><el-input v-model="form.endStation" /></el-form-item>
        <el-form-item label="总长度(km)"><el-input-number v-model="form.totalLength" :min="0" :precision="2" style="width:100%" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getLineList, addLine, updateLine, deleteLine } from '@/api/ledger'
export default {
  name: 'LedgerLine',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { lineName: '', lineCode: '' }, dialogVisible: false, dialogType: 'add', form: { lineName: '', lineCode: '', voltageLevel: '10kV', startStation: '', endStation: '', totalLength: 0 }, rules: { lineName: [{ required: true, trigger: 'blur' }], lineCode: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getLineList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { lineName: '', lineCode: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { lineName: '', lineCode: '', voltageLevel: '10kV', startStation: '', endStation: '', totalLength: 0 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？', '提示', { type: 'warning' }).then(() => { deleteLine(row.id).then(() => { this.$message.success('删除成功'); this.fetchList() }) }).catch(() => {}) },
    handleSubmit() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addLine : updateLine)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>
