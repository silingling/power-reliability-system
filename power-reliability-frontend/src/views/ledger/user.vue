<template>
  <div class="app-container">
    <div class="search-container">
      <el-form :inline="true" :model="query" size="small">
        <el-form-item label="用户名称"><el-input v-model="query.userName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="用户编号"><el-input v-model="query.userCode" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="所属台区"><el-input v-model="query.stationName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button><el-button icon="el-icon-refresh" @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </div>
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用户</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="80" /><el-table-column prop="userName" label="用户名称" min-width="130" /><el-table-column prop="userCode" label="用户编号" width="150" /><el-table-column prop="stationName" label="所属台区" width="140" />
      <el-table-column prop="contactPhone" label="联系电话" width="130" /><el-table-column prop="address" label="地址" min-width="200" />
      <el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'启用':'停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template></el-table-column>
    </el-table>
    <el-pagination :current-page="page" :page-sizes="[10,20,50]" :page-size="pageSize" :total="total" layout="total,sizes,prev,pager,next,jumper" @size-change="s=>{pageSize=s;fetchList()}" @current-change="s=>{page=s;fetchList()}" />
    <el-dialog :title="dialogType==='add'?'新增用户':'编辑用户'" :visible.sync="dialogVisible" width="550px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名称" prop="userName"><el-input v-model="form.userName" /></el-form-item>
        <el-form-item label="用户编号" prop="userCode"><el-input v-model="form.userCode" /></el-form-item>
        <el-form-item label="所属台区"><el-input v-model="form.stationName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" type="textarea" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :label="1">启用</el-radio><el-radio :label="0">停用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getUserList, addUser, updateUser, deleteUser } from '@/api/ledger'
export default {
  name: 'LedgerUser',
  data() { return { loading: false, list: [], page: 1, pageSize: 10, total: 0, query: { userName: '', userCode: '', stationName: '' }, dialogVisible: false, dialogType: 'add', form: { userName: '', userCode: '', stationName: '', contactPhone: '', address: '', status: 1 }, rules: { userName: [{ required: true, trigger: 'blur' }], userCode: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getUserList({ ...this.query, page: this.page, pageSize: this.pageSize }).then(r => { this.list = r.data.list || []; this.total = r.data.total || 0 }).finally(() => { this.loading = false }) },
    handleSearch() { this.page = 1; this.fetchList() }, handleReset() { this.query = { userName: '', userCode: '', stationName: '' }; this.handleSearch() },
    handleAdd() { this.dialogType = 'add'; this.form = { userName: '', userCode: '', stationName: '', contactPhone: '', address: '', status: 1 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？', '提示', { type: 'warning' }).then(() => { deleteUser(row.id).then(() => { this.$message.success('删除成功'); this.fetchList() }) }).catch(() => {}) },
    handleSubmit() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addUser : updateUser)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.search-container { background: #fff; padding: 15px; margin-bottom: 15px; border-radius: 4px; }
.table-header { margin-bottom: 15px; }
</style>
