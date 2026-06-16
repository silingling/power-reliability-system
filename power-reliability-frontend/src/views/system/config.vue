<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="list" border stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="70" /><el-table-column prop="configName" label="配置名称" width="200" /><el-table-column prop="configKey" label="配置键" width="200" />
      <el-table-column prop="configValue" label="配置值" min-width="300" />
      <el-table-column label="操作" width="150" fixed="right"><template slot-scope="{row}"><el-button size="mini" type="text" @click="handleEdit(row)">编辑</el-button></template></el-table-column>
    </el-table>
    <el-dialog title="编辑配置" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置名称">{{ form.configName }}</el-form-item>
        <el-form-item label="配置键">{{ form.configKey }}</el-form-item>
        <el-form-item label="配置值"><el-input v-model="form.configValue" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <span slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave">确定</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import { getConfigList, updateConfig } from '@/api/system'
export default {
  name: 'SystemConfig',
  data() { return { loading: false, list: [], dialogVisible: false, form: {} } },
  created() { this.fetchList() },
  methods: {
    fetchList() { this.loading = true; getConfigList({}).then(r => { this.list = r.data.list || [] }).finally(() => { this.loading = false }) },
    handleEdit(row) { this.form = { ...row }; this.dialogVisible = true },
    handleSave() { updateConfig(this.form).then(() => { this.$message.success('更新成功'); this.dialogVisible = false; this.fetchList() }) }
  }
}
</script>
<style scoped>
.app-container { padding: 20px; }
</style>
