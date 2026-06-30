<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
const router = useRouter()
interface Count { id: number; countNo: string; whNm?: string; countYmd?: string; sts: string; stsNm?: string }
const list = ref<Count[]>([])
const keyword = ref('')
async function load() { list.value = (await http.get('/inv/count', { params: { keyword: keyword.value } })).data.data }
onMounted(load)
</script>
<template>
  <div class="page">
    <h2>재고실사</h2>
    <div class="toolbar">
      <input v-model="keyword" placeholder="실사번호" @keyup.enter="load" />
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="router.push('/inv/count/new')">+ 실사 등록</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>실사번호</th><th>창고</th><th>실사일</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="c in list" :key="c.id" class="row" @click="router.push(`/inv/count/${c.id}`)">
            <td>{{ c.countNo }}</td><td>{{ c.whNm }}</td><td>{{ c.countYmd }}</td>
            <td><span class="badge" :class="c.sts">{{ c.stsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="4" class="empty">재고실사 내역이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 12px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #eef3fb; } .empty { text-align: center; color: #aaa; padding: 22px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; } .badge.TMP { background: #eef; color: #556; } .badge.CFM { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
</style>
