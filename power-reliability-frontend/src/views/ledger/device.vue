<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="设备名称"><el-input v-model="query.deviceName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="设备类型"><el-select v-model="query.deviceType" placeholder="请选择" clearable><el-option label="变压器" value="transformer" /><el-option label="开关" value="switch" /><el-option label="配电箱" value="distributionBox" /><el-option label="电表" value="meter" /><el-option label="其他" value="other" /></el-select></el-form-item>
        <el-form-item label="所在台区"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增设备</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="80" /><el-table-column prop="deviceName" label="设备名称" min-width="140" /><el-table-column prop="deviceType" label="设备类型" width="100"><template slot-scope="{row}">{{ {transformer:'变压器',switch:'开关',distributionBox:'配电箱',meter:'电表',other:'其他'}[row.deviceType]||row.deviceType }}</template></el-table-column>
      <el-table-column prop="deviceCode" label="设备编码" width="150" /><el-table-column prop="stationName" label="所在台区" width="140" /><el-table-column prop="installDate" label="安装日期" width="120" /><el-table-column prop="manufacturer" label="生产厂家" min-width="160" />
      <el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'正常':'异常' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新增设备':'编辑设备'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName"><el-input v-model="form.deviceName" /></el-form-item>
        <el-form-item label="设备类型" prop="deviceType"><el-select v-model="form.deviceType" style="width:100%"><el-option label="变压器" value="transformer" /><el-option label="开关" value="switch" /><el-option label="配电箱" value="distributionBox" /><el-option label="电表" value="meter" /><el-option label="其他" value="other" /></el-select></el-form-item>
        <el-form-item label="设备编码" prop="deviceCode"><el-input v-model="form.deviceCode" /></el-form-item>
        <el-form-item label="所在台区"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="安装日期"><el-date-picker v-model="form.installDate" type="date" value-format="yyyy-MM-dd" style="width:100%" /></el-form-item>
        <el-form-item label="生产厂家"><el-input v-model="form.manufacturer" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :label="1">正常</el-radio><el-radio :label="0">异常</el-radio></el-radio-group></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getDeviceList, addDevice, updateDevice, deleteDevice } from '@/api/ledger'
export default {
  name: 'LedgerDevice',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { deviceName: '', deviceType: '', stationName: '' }, dialogVisible: false, dialogType: 'add', form: { deviceName: '', deviceType: 'transformer', deviceCode: '', stationName: '', installDate: '', manufacturer: '', status: 1 }, rules: { deviceName: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getDeviceList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { deviceName: '', deviceType: '', stationName: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { deviceName: '', deviceType: 'transformer', deviceCode: '', stationName: '', installDate: '', manufacturer: '', status: 1 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？', '提示', { type: 'warning' }).then(() => { deleteDevice(row.id).then(() => { this.$message.success('删除成功'); this.fetchList() }) }).catch(() => {}) },
    handleSubmit() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addDevice : updateDevice)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>
