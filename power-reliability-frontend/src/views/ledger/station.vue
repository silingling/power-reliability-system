<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="台区名称"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="所属线路"><el-input v-model="query.lineName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增台区</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="80" />
      <el-table-column prop="stationName" label="台区名称" min-width="150" />
      <el-table-column prop="stationCode" label="台区编码" width="150" />
      <el-table-column prop="lineName" label="所属线路" width="150" />
      <el-table-column prop="deviceCount" label="设备数量" width="100" />
      <el-table-column prop="userCount" label="用户数量" width="100" />
      <el-table-column prop="address" label="地址" min-width="200" />
      <el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'正常':'停运' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新增台区':'编辑台区'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="台区名称" prop="stationName"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="台区编码" prop="stationCode"><el-input v-model="form.stationCode" /></el-form-item>
        <el-form-item label="所属线路"><el-input v-model="form.lineName" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :label="1">正常</el-radio><el-radio :label="0">停运</el-radio></el-radio-group></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getStationList, addStation, updateStation, deleteStation } from '@/api/ledger'
export default {
  name: 'LedgerStation',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { stationName: '', lineName: '' }, dialogVisible: false, dialogType: 'add', form: { stationName: '', stationCode: '', lineName: '', address: '', status: 1 }, rules: { stationName: [{ required: true, trigger: 'blur' }], stationCode: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getStationList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { stationName: '', lineName: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { stationName: '', stationCode: '', lineName: '', address: '', status: 1 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？', '提示', { type: 'warning' }).then(() => { deleteStation(row.id).then(() => { this.$message.success('删除成功'); this.fetchList() }) }).catch(() => {}) },
    handleSubmit() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addStation : updateStation)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>
