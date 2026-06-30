<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const loginId = ref('admin')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function onSubmit() {
  error.value = ''
  loading.value = true
  try {
    const { data } = await http.post('/auth/login', {
      loginId: loginId.value,
      password: password.value,
    })
    auth.setLogin(data.data)
    router.push('/')
  } catch (e: any) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <form class="login-box" @submit.prevent="onSubmit">
      <h1>구매시스템</h1>
      <p class="sub">Purchase System</p>
      <input v-model="loginId" placeholder="아이디" autocomplete="username" />
      <input v-model="password" type="password" placeholder="비밀번호" autocomplete="current-password" />
      <button type="submit" :disabled="loading">{{ loading ? '로그인 중...' : '로그인' }}</button>
      <p v-if="error" class="err">{{ error }}</p>
      <p class="hint">초기 계정: admin / admin1234</p>
    </form>
  </div>
</template>

<style scoped>
.login-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #f0f2f5;
  font-family: 'Malgun Gothic', sans-serif;
}
.login-box {
  width: 340px;
  padding: 40px 32px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
}
h1 { margin: 0; font-size: 24px; text-align: center; color: #1a3a6b; }
.sub { margin: 4px 0 24px; text-align: center; color: #999; font-size: 13px; }
input {
  padding: 12px; margin-bottom: 12px; border: 1px solid #ddd;
  border-radius: 6px; font-size: 14px;
}
button {
  padding: 12px; background: #1a3a6b; color: #fff; border: none;
  border-radius: 6px; font-size: 15px; cursor: pointer; margin-top: 8px;
}
button:disabled { opacity: 0.6; }
.err { color: #d33; font-size: 13px; margin: 12px 0 0; text-align: center; }
.hint { color: #aaa; font-size: 12px; margin: 16px 0 0; text-align: center; }
</style>
