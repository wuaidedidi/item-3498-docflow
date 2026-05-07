import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'
import { getCurrentUser } from '@/api/user'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: Cookies.get('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    sidebarCollapsed: false
  },
  getters: {
    token: state => state.token,
    user: state => state.user,
    isAdmin: state => state.user && state.user.role === 'admin',
    sidebarCollapsed: state => state.sidebarCollapsed
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      if (token) {
        Cookies.set('token', token, { expires: 1 })
      } else {
        Cookies.remove('token')
      }
    },
    SET_USER(state, user) {
      state.user = user
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
    },
    TOGGLE_SIDEBAR(state) {
      state.sidebarCollapsed = !state.sidebarCollapsed
    }
  },
  actions: {
    setLoginData({ commit }, { token, user }) {
      commit('SET_TOKEN', token)
      commit('SET_USER', user)
    },
    async fetchUser({ commit }) {
      try {
        const res = await getCurrentUser()
        commit('SET_USER', res.data)
        return res.data
      } catch (e) {
        return null
      }
    },
    logout({ commit }) {
      commit('SET_TOKEN', '')
      commit('SET_USER', null)
    },
    toggleSidebar({ commit }) {
      commit('TOGGLE_SIDEBAR')
    }
  }
})
