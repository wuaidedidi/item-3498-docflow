<template>
  <div class="login-wrapper">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <i class="el-icon-document" style="font-size: 32px; color: #fff"></i>
        </div>
        <h1 class="login-title">DocFlow</h1>
        <p class="login-subtitle">创建您的账号</p>
      </div>
      <el-form ref="registerForm" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" prefix-icon="el-icon-s-custom" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" prefix-icon="el-icon-lock" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" prefix-icon="el-icon-message" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" prefix-icon="el-icon-phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleRegister">
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
        <div class="login-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="register-link">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/auth'

const validateEmail = (rule, value, callback) => {
  if (!value || value === '') {
    callback()
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value || value === '') {
    callback()
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

export default {
  name: 'Register',
  data() {
    return {
      form: { username: '', nickname: '', password: '', email: '', phone: '' },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
        ],
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 30, message: '密码长度为6-30个字符', trigger: 'blur' }
        ],
        email: [{ validator: validateEmail, trigger: 'blur' }],
        phone: [{ validator: validatePhone, trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (!valid) return
        this.loading = true
        try {
          await register(this.form)
          this.$message.success('注册成功，请登录')
          this.$router.push('/login')
        } catch (e) {
          // handled by interceptor
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-wrapper {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f2f5 50%, #e6f0fa 100%);
  position: relative;
  overflow: hidden;
}
.login-bg { position: absolute; inset: 0; overflow: hidden; }
.bg-shape { position: absolute; border-radius: 50%; opacity: 0.08; }
.shape-1 { width: 400px; height: 400px; background: #409eff; top: -100px; right: -100px; }
.shape-2 { width: 300px; height: 300px; background: #67c23a; bottom: -80px; left: -80px; }
.shape-3 { width: 200px; height: 200px; background: #e6a23c; top: 50%; left: 10%; }
.login-card {
  width: 420px; background: #fff; border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.08); padding: 40px; position: relative; z-index: 1;
}
.login-header { text-align: center; margin-bottom: 28px; }
.logo-icon {
  width: 60px; height: 60px; background: linear-gradient(135deg, #409eff, #66b1ff);
  border-radius: 16px; display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px; box-shadow: 0 8px 24px rgba(64,158,255,0.3);
}
.login-title { font-size: 28px; font-weight: 700; color: #1a1a2e; margin-bottom: 6px; }
.login-subtitle { font-size: 14px; color: #909399; }
.login-form {
  .el-input__inner {
    height: 44px; line-height: 44px; font-size: 14px; border-radius: 10px;
    background: #f8f9fb; border: 1px solid #e4e7ed;
    &:focus { background: #fff; border-color: #409eff; }
  }
  .el-input__prefix { left: 12px; font-size: 16px; color: #909399; }
}
.login-btn {
  width: 100%; height: 44px; font-size: 16px; font-weight: 600; border-radius: 10px;
  background: linear-gradient(135deg, #409eff, #66b1ff); border: none;
  box-shadow: 0 6px 20px rgba(64,158,255,0.35); transition: all 0.3s ease;
  &:hover { transform: translateY(-1px); box-shadow: 0 8px 25px rgba(64,158,255,0.45); }
}
.login-footer { text-align: center; font-size: 14px; color: #909399; }
.register-link {
  color: #409eff; text-decoration: none; font-weight: 500; margin-left: 4px;
  &:hover { text-decoration: underline; }
}
</style>
