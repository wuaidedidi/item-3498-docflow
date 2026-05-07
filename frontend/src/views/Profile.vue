<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-avatar-section">
            <el-avatar :size="80" class="profile-avatar">
              {{ user ? user.nickname.charAt(0) : '' }}
            </el-avatar>
            <h3>{{ user ? user.nickname : '' }}</h3>
            <p class="profile-role">
              <el-tag size="small" :type="user && user.role === 'admin' ? 'danger' : ''">
                {{ user && user.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </p>
          </div>
          <el-divider />
          <div class="profile-info">
            <div class="info-item"><i class="el-icon-user"></i><span>{{ user ? user.username : '' }}</span></div>
            <div class="info-item"><i class="el-icon-message"></i><span>{{ user && user.email ? user.email : '未设置' }}</span></div>
            <div class="info-item"><i class="el-icon-phone"></i><span>{{ user && user.phone ? user.phone : '未设置' }}</span></div>
            <div class="info-item"><i class="el-icon-time"></i><span>{{ user ? user.createTime : '' }}</span></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="info">
              <el-form ref="infoForm" :model="infoForm" :rules="infoRules" label-width="80px" style="max-width: 500px; margin-top: 12px">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="infoForm.email" placeholder="请输入邮箱（选填）" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="infoForm.phone" placeholder="请输入手机号（选填）" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="infoLoading" @click="submitInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 500px; margin-top: 12px">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdLoading" @click="submitPassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { updateCurrentUser, updatePassword } from '@/api/user'

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
  name: 'Profile',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) { callback(new Error('两次输入的密码不一致')) }
      else { callback() }
    }
    return {
      activeTab: 'info',
      infoForm: { nickname: '', email: '', phone: '' },
      infoRules: {
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        email: [{ validator: validateEmail, trigger: 'blur' }],
        phone: [{ validator: validatePhone, trigger: 'blur' }]
      },
      infoLoading: false,
      pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 30, message: '密码长度为6-30个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      },
      pwdLoading: false
    }
  },
  computed: {
    ...mapGetters(['user'])
  },
  watch: {
    user: {
      handler(val) {
        if (val) {
          this.infoForm = { nickname: val.nickname || '', email: val.email || '', phone: val.phone || '' }
        }
      },
      immediate: true
    }
  },
  methods: {
    async submitInfo() {
      this.$refs.infoForm.validate(async (valid) => {
        if (!valid) return
        this.infoLoading = true
        try {
          const res = await updateCurrentUser({
            nickname: this.infoForm.nickname,
            email: this.infoForm.email || null,
            phone: this.infoForm.phone || null
          })
          this.$store.commit('SET_USER', res.data)
          this.$message.success('个人信息更新成功')
        } catch (e) {} finally { this.infoLoading = false }
      })
    },
    async submitPassword() {
      this.$refs.pwdForm.validate(async (valid) => {
        if (!valid) return
        this.pwdLoading = true
        try {
          await updatePassword({
            oldPassword: this.pwdForm.oldPassword,
            newPassword: this.pwdForm.newPassword
          })
          this.$message.success('密码修改成功，请重新登录')
          this.pwdForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
          setTimeout(() => {
            this.$store.dispatch('logout')
            this.$router.push('/login')
          }, 1500)
        } catch (e) {} finally { this.pwdLoading = false }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-card { text-align: center; }
.profile-avatar-section { padding: 20px 0; }
.profile-avatar {
  background: linear-gradient(135deg, #409eff, #66b1ff); color: #fff;
  font-size: 32px; font-weight: 700;
}
.profile-avatar-section h3 { margin-top: 12px; font-size: 20px; color: #1a1a2e; }
.profile-role { margin-top: 8px; }
.profile-info { padding: 0 16px; }
.info-item {
  display: flex; align-items: center; gap: 12px; padding: 12px 0;
  border-bottom: 1px solid #f0f0f0; font-size: 14px; color: #606266;
  &:last-child { border-bottom: none; }
  i { font-size: 18px; color: #909399; width: 20px; text-align: center; }
}
</style>
