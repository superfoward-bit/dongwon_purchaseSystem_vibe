import { defineStore } from 'pinia'

interface AuthState {
  accessToken: string | null
  usrId: string | null
  usrNm: string | null
  compCd: string | null
  roles: string[]
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    accessToken: localStorage.getItem('accessToken'),
    usrId: localStorage.getItem('usrId'),
    usrNm: localStorage.getItem('usrNm'),
    compCd: localStorage.getItem('compCd'),
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
  }),
  getters: {
    isLoggedIn: (s) => !!s.accessToken,
  },
  actions: {
    setLogin(data: { accessToken: string; usrId: string; usrNm: string; compCd: string; roles: string[] }) {
      this.accessToken = data.accessToken
      this.usrId = data.usrId
      this.usrNm = data.usrNm
      this.compCd = data.compCd
      this.roles = data.roles || []
      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('usrId', data.usrId)
      localStorage.setItem('usrNm', data.usrNm)
      localStorage.setItem('compCd', data.compCd)
      localStorage.setItem('roles', JSON.stringify(this.roles))
    },
    logout() {
      this.accessToken = null
      this.usrId = null
      this.usrNm = null
      this.compCd = null
      this.roles = []
      localStorage.clear()
    },
  },
})
