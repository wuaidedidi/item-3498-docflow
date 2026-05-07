<template>
  <div class="page-container">
    <el-card>
      <div slot="header" class="page-header">
        <h3>用户管理</h3>
      </div>

      <div class="filter-bar">
        <el-input v-model="query.keyword" placeholder="搜索用户名或昵称" prefix-icon="el-icon-search" clearable style="width: 240px" @clear="fetchData" @keyup.enter.native="fetchData" />
        <el-select v-model="query.role" placeholder="选择角色" clearable @change="fetchData" style="width: 140px">
          <el-option label="管理员" value="admin" />
          <el-option label="普通用户" value="user" />
        </el-select>
        <el-select v-model="query.status" placeholder="选择状态" clearable @change="fetchData" style="width: 140px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" icon="el-icon-search" @click="fetchData">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </div>

      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.role === 'admin' ? 'danger' : ''">
              {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)" :disabled="scope.row.role === 'admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        :current-page="query.page"
        :page-sizes="[10, 20, 50]"
        :page-size="query.size"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog title="编辑用户" :visible.sync="editVisible" width="500px" :close-on-click-modal="false">
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="用户名">
          <el-input :value="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="editForm.role" :disabled="isSelf(editForm.id)">
            <el-radio label="admin">管理员</el-radio>
            <el-radio label="user">普通用户</el-radio>
          </el-radio-group>
          <div v-if="isSelf(editForm.id)" class="form-tip">不能修改自己的角色</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.statusBool" active-text="启用" inactive-text="禁用" :disabled="isSelf(editForm.id)" />
          <div v-if="isSelf(editForm.id)" class="form-tip">不能修改自己的状态</div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, updateUser, deleteUser } from '@/api/user'
import { mapGetters } from 'vuex'

const validateEmail = (rule, value, callback) => {
  if (!value || value === '') { callback() }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) { callback(new Error('邮箱格式不正确')) }
  else { callback() }
}
const validatePhone = (rule, value, callback) => {
  if (!value || value === '') { callback() }
  else if (!/^1[3-9]\d{9}$/.test(value)) { callback(new Error('手机号格式不正确')) }
  else { callback() }
}

export default {
  name: 'UserList',
  data() {
    return {
      users: [],
      total: 0,
      loading: false,
      query: { page: 1, size: 10, keyword: '', role: '', status: null },
      editVisible: false,
      submitLoading: false,
      editForm: { id: null, username: '', nickname: '', email: '', phone: '', role: 'user', statusBool: true },
      editRules: {
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        email: [{ validator: validateEmail, trigger: 'blur' }],
        phone: [{ validator: validatePhone, trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['user'])
  },
  methods: {
    isSelf(id) { return this.user && this.user.id === id },
    async fetchData() {
      this.loading = true
      try {
        const res = await getUserList(this.query)
        this.users = res.data.records
        this.total = res.data.total
      } catch (e) {} finally { this.loading = false }
    },
    resetQuery() {
      this.query = { page: 1, size: 10, keyword: '', role: '', status: null }
      this.fetchData()
    },
    handlePageChange(page) { this.query.page = page; this.fetchData() },
    handleSizeChange(size) { this.query.size = size; this.query.page = 1; this.fetchData() },
    showEditDialog(row) {
      this.editForm = {
        id: row.id,
        username: row.username,
        nickname: row.nickname,
        email: row.email || '',
        phone: row.phone || '',
        role: row.role,
        statusBool: row.status === 1
      }
      this.editVisible = true
      this.$nextTick(() => { this.$refs.editForm && this.$refs.editForm.clearValidate() })
    },
    async submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) return
        this.submitLoading = true
        try {
          await updateUser(this.editForm.id, {
            nickname: this.editForm.nickname,
            email: this.editForm.email || null,
            phone: this.editForm.phone || null,
            role: this.editForm.role,
            status: this.editForm.statusBool ? 1 : 0
          })
          this.$message.success('用户信息更新成功')
          this.editVisible = false
          this.fetchData()
        } catch (e) {} finally { this.submitLoading = false }
      })
    },
    handleDelete(row) {
      this.$confirm(`确定要删除用户「${row.nickname}」吗？`, '确认删除', {
        confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
      }).then(async () => {
        try {
          await deleteUser(row.id)
          this.$message.success('删除成功')
          if (this.users.length === 1 && this.query.page > 1) {
            this.query.page--
          }
          this.fetchData()
        } catch (e) {}
      }).catch(() => {})
    }
  },
  created() { this.fetchData() }
}
</script>

<style lang="scss" scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; h3 { font-size: 18px; font-weight: 600; color: #1a1a2e; } }
.form-tip { font-size: 12px; color: #e6a23c; margin-top: 4px; }
</style>
