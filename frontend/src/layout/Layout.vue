<template>
  <div class="layout-wrapper">
    <el-container style="height: 100vh">
      <!-- Sidebar -->
      <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="sidebar">
        <div class="logo-container">
          <i class="el-icon-document" style="font-size: 24px; color: #409eff"></i>
          <span v-show="!sidebarCollapsed" class="logo-text">DocFlow</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :collapse-transition="false"
          background-color="#1a1a2e"
          text-color="#a0aec0"
          active-text-color="#409eff"
          router
          class="sidebar-menu"
        >
          <template v-for="item in menuItems">
            <el-menu-item
              v-if="!item.hidden && hasPermission(item.roles)"
              :key="item.path"
              :index="item.path"
            >
              <i :class="item.icon"></i>
              <span slot="title">{{ item.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- Header -->
        <el-header class="header">
          <div class="header-left">
            <i
              :class="sidebarCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
              class="collapse-btn"
              @click="toggleSidebar"
            ></i>
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.title && $route.name !== 'Dashboard'">
                {{ $route.meta.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" class="user-avatar">
                  {{ user ? user.nickname.charAt(0) : '' }}
                </el-avatar>
                <span class="user-name">{{ user ? user.nickname : '' }}</span>
                <i class="el-icon-arrow-down"></i>
              </div>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile" icon="el-icon-user">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" icon="el-icon-switch-button" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- Main Content -->
        <el-main class="main-content">
          <transition name="fade" mode="out-in">
            <router-view :key="$route.fullPath" />
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  data() {
    return {
      menuItems: [
        { path: '/dashboard', title: '工作台', icon: 'el-icon-s-home' },
        { path: '/documents', title: '文档管理', icon: 'el-icon-document' },
        { path: '/approval/my', title: '我的审批', icon: 'el-icon-s-claim' },
        { path: '/approval/flows', title: '审批流程', icon: 'el-icon-s-check', roles: ['admin'] },
        { path: '/categories', title: '分类管理', icon: 'el-icon-folder', roles: ['admin'] },
        { path: '/users', title: '用户管理', icon: 'el-icon-user', roles: ['admin'] }
      ]
    }
  },
  computed: {
    ...mapGetters(['user', 'isAdmin', 'sidebarCollapsed']),
    activeMenu() {
      const path = this.$route.path
      if (path.startsWith('/documents/')) return '/documents'
      return path
    }
  },
  methods: {
    hasPermission(roles) {
      if (!roles || roles.length === 0) return true
      return this.user && roles.includes(this.user.role)
    },
    toggleSidebar() {
      this.$store.dispatch('toggleSidebar')
    },
    handleCommand(command) {
      if (command === 'profile') {
        this.$router.push('/profile')
      } else if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$store.dispatch('logout')
          this.$router.push('/login')
        }).catch(() => {})
      }
    }
  },
  created() {
    if (this.user) {
      this.$store.dispatch('fetchUser')
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-wrapper {
  height: 100vh;
}

.sidebar {
  background-color: #1a1a2e;
  transition: width 0.3s ease;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;

  .el-menu-item {
    height: 50px;
    line-height: 50px;
    margin: 4px 8px;
    border-radius: 8px;

    &:hover {
      background-color: rgba(64, 158, 255, 0.1) !important;
    }

    &.is-active {
      background-color: rgba(64, 158, 255, 0.2) !important;
      color: #409eff !important;
    }

    i {
      font-size: 18px;
      margin-right: 10px;
    }
  }
}

.header {
  height: 60px !important;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
  &:hover {
    color: #409eff;
  }
}

.breadcrumb {
  line-height: 60px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.3s;
  &:hover {
    background: #f5f7fa;
  }
}

.user-avatar {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
