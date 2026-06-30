<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Nc { id: number; ncNo: string; vdNm?: string; refNo?: string; itemNm?: string; ncTypNm?: string; severity?: string; severityNm?: string; detectYmd?: string; sts: string; stsNm?: string }
const router = useRouter()
const list = ref<Nc[]>([])
const keyword = ref('')
const sts = ref('')
const codes = ref<{ cd: string; cdNmKo: string }[]>([])
async function load() { list.value = (await http.get('/qlt/nc', { params: { keyword: keyword.value, sts: sts.value } })).data.data }
async function loadCodes() { codes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'QC_STS' } })).data.data }
function open(id: number | 'new') { router.push(`/qlt/nc/${id}`) }
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <h2>부적합 / 개선(CAPA)</h2>
    <div class="toolbar">
      <input v-model="keyword" placeholder="부적합번호/협력사/품목" @keyup.enter="load" />
      <select v-model="sts" @change="load"><option value="">전체 상태</option><option v-for="c in codes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="open('new')">+ 부적합 등록</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>부적합번호</th><th>협력사</th><th>품목</th><th>유형</th><th>심각도</th><th>발생일</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="n in list" :key="n.id" class="row" @click="open(n.id)">
            <td>{{ n.ncNo }}</td><td>{{ n.vdNm }}</td><td>{{ n.itemNm }}</td><td>{{ n.ncTypNm }}</td>
            <td><span class="sev" :class="n.severity">{{ n.severityNm }}</span></td>
            <td>{{ n.detectYmd }}</td>
            <td><span class="badge" :class="n.sts">{{ n.stsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="empty">부적합이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.sev { padding: 2px 9px; border-radius: 10px; font-size: 12px; font-weight: bold; }
.sev.MINOR { background: #eef; color: #556; } .sev.MAJOR { background: #fff3d6; color: #9a6b00; } .sev.CRITICAL { background: #fde2e2; color: #b3261e; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.OPEN { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.ACTION { background: #dbeafe; color: #1a4f9c; } .badge.CLOSED { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
</style>
