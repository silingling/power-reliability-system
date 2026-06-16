<template>
  <div class="app-container">
    <div class="table-header"><el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增部门</el-button></div>
    <el-table v-loading="loading" :data="list" border stripe row-key="id" default-expand-all :tree-props="{children:'children'}" style="width:100%">
      <el-table-column prop="deptName" label="部门名称" width="250" />
      <el-table-column prop="deptCode" label="部门编码" width="150" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column prop="status" label="状态" width="80"><template slot-scope="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button><el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(row)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog :title="dialogType==='add'?'新增部门':'编辑部门'" :visible.sync="dialogVisible" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="部门名称" prop="deptName"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="部门编码"><el-input v-model="form.deptCode" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.leader" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-radio-group v-model="form.status"><el-radio :label="1">启用</el-radio><el-radio :label="0">停用</el-radio></el-radio-group></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getDeptList, addDept, updateDept, deleteDept } from '@/api/system'
export default {
  name: 'SystemDept',
  data() { return { loading: false, list: [], dialogVisible: false, dialogType: 'add', form: { deptName: '', deptCode: '', leader: '', phone: '', sort: 0, status: 1 }, rules: { deptName: [{ required: true, trigger: 'blur' }] } } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getDeptList().then(r => { this.list = r.data.list || [] }).finally(() => { this.loading = false }) },
    handleAdd() { this.dialogType = 'add'; this.form = { deptName: '', deptCode: '', leader: '', phone: '', sort: 0, status: 1 }; this.dialogVisible = true },
    handleEdit(row) { this.dialogType = 'edit'; this.form = { ...row }; this.dialogVisible = true },
    handleDelete(row) { this.$confirm('确认删除？','提示',{type:'warning'}).then(()=>{ deleteDept(row.id).then(()=>{ this.$message.success('删除成功');this.fetchList() }) }).catch(()=>{}) },
    handleSave() { this.$refs.form.validate(v => { if (!v) return; (this.dialogType === 'add' ? addDept : updateDept)(this.form).then(() => { this.$message.success('操作成功'); this.dialogVisible = false; this.fetchList() }) }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
.table-header { margin-bottom: 15px; }
</style>
