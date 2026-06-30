<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Clm { id: number; clmNo: string; vdNm?: string; clmTypNm?: string; itemNm?: string; clmYmd?: string; clmAmt?: number; sts: string; stsNm?: string }
const router = useRouter()
const list = ref<Clm[]>([])
const keyword = ref('')
const sts = ref('')
const codes = ref<{ cd: string; cdNmKo: string }[]>([])
async function load() { list.value = (await http.get('/srm/claim', { params: { keyword: keyword.value, sts: sts.value } })).data.data }
async function loadCodes() { codes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'CLM_STS' } })).data.data }
function open(id: number | 'new') { router.push(`/srm/claim/${id}`) }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <h2>협력사 클레임</h2>
    <div class="toolbar">
      <input v-model="keyword" placeholder="클레임번호/협력사/품목" @keyup.enter="load" />
      <select v-model="sts" @change="load"><option value="">전체 상태</option><option v-for="c in codes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="open('new')">+ 클레임 등록</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>클레임번호</th><th>협력사</th><th>유형</th><th>품목</th><th>발생일</th><th class="r">손해액</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="c in list" :key="c.id" class="row" @click="open(c.id)">
            <td>{{ c.clmNo }}</td><td>{{ c.vdNm }}</td><td>{{ c.clmTypNm }}</td><td>{{ c.itemNm }}</td><td>{{ c.clmYmd }}</td>
            <td class="r">{{ fmt(c.clmAmt) }}</td>
            <td><span class="badge" :class="c.sts">{{ c.stsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="empty">클레임이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; } .empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.OPEN { background: #eef; color: #556; } .badge.PROC { background: #fff3d6; color: #9a6b00; } .badge.RESOLVED { background: #dbeafe; color: #1a4f9c; } .badge.CLOSED { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
</style>
