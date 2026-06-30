import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 요청: JWT 토큰 첨부
http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

// 응답: 표준 래퍼 처리 + 401 핸들링
http.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      const auth = useAuthStore()
      auth.logout()
      router.push('/login')
    }
    const msg = error.response?.data?.message || '요청 처리 중 오류가 발생했습니다.'
    return Promise.reject(new Error(msg))
  },
)

export default http
