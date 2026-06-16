<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="工单编号"><el-input v-model="query.orderCode" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="请选择" clearable><el-option label="待处理" value="pending" /><el-option label="处理中" value="processing" /><el-option label="已完成" value="completed" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新建预警工单</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="orderCode" label="工单编号" width="150" /><el-table-column prop="stationName" label="台区名称" width="140" />
      <el-table-column prop="alertDesc" label="预警内容" min-width="250" /><el-table-column prop="assignee" label="处理人" width="120" />
      <el-table-column prop="status" label="状态" width="90"><template slot-scope="{row}"><el-tag :type="{pending:'danger',processing:'warning',completed:'success'}[row.status]" size="small">{{ {pending:'待处理',processing:'处理中',completed:'已完成'}[row.status]||row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="150" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新建预警工单':'编辑预警工单'" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="工单编号" prop="orderCode"><el-input v-model="form.orderCode" /></el-form-item>
        <el-form-item label="台区名称" prop="stationName"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="预警内容" prop="alertDesc"><el-input v-model="form.alertDesc" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="处理人"><el-input v-model="form.assignee" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getWarningOrderList, addWarningOrder, updateWarningOrder } from '@/api/warning'
export default {
  name: 'WarningOrder',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { orderCode: '', status: '' }, dialogVisible: false, dialogType: 'add', form: { orderCode: '', stationName: '', alertDesc: '', assignee: '' }, rules: { orderCode: [{ required: true, trigger: 'blur' }], stationName: [{ required: true, trigger: 'blur' }], alertDesc: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getWarningOrderList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { orderCode: '', status: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { orderCode: '', stationName: '', alertDesc: '', assignee: '' }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addWarningOrder : updateWarningOrder)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>
