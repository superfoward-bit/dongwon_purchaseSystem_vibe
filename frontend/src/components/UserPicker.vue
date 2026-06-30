<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface User { usrId: string; usrNm: string; deptCd?: string; posCd?: string; email?: string }

const emit = defineEmits<{ (e: 'select', u: User): void; (e: 'close'): void }>()
const list = ref<User[]>([])
const keyword = ref('')

async function load() { list.value = (await http.get('/sys/user', { params: { keyword: keyword.value } })).data.data }
onMounted(load)
</script>

<template>
  <div class="modal-bg" @click.self="emit('close')">
    <div class="modal">
      <div class="modal-head">사용자 선택 <button class="x" @click="emit('close')">×</button></div>
      <div class="modal-search"><input v-model="keyword" placeholder="ID/이름" @keyup.enter="load" autofocus /><button class="btn" @click="load">검색</button></div>
      <div class="modal-body">
        <table>
          <thead><tr><th>ID</th><th>이름</th><th>이메일</th></tr></thead>
          <tbody>
            <tr v-for="u in list" :key="u.usrId" class="row" @click="emit('select', u)">
              <td>{{ u.usrId }}</td><td>{{ u.usrNm }}</td><td>{{ u.email }}</td>
            </tr>
            <tr v-if="!list.length"><td colspan="3" class="empty">결과 없음</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1100; }
.modal { width: 520px; max-height: 80vh; background: #fff; border-radius: 10px; display: flex; flex-direction: column; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; }
.x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-search { padding: 12px 18px; display: flex; gap: 8px; } .modal-search input { flex: 1; padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; }
.modal-body { overflow: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 14px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; position: sticky; top: 0; }
.row { cursor: pointer; } .row:hover { background: #eef3fb; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #1a3a6b; color: #fff; border-radius: 5px; cursor: pointer; }
</style>
