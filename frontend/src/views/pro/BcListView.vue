<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Bc { id: number; bcNo: string; bcRev: number; bcTitle: string; bcTypNm?: string; vdNm?: string; validSd?: string; validEd?: string; sts: string; stsNm?: string; chrgUsrNm?: string; autoRenewYn?: string; ddayLeft?: number }
const router = useRouter()
const tab = ref<'all' | 'expiring'>('all')
const list = ref<Bc[]>([])
const expiring = ref<Bc[]>([])
const keyword = ref('')
const sts = ref('')
const expDays = ref(60)
const codes = ref<{ cd: string; cdNmKo: string }[]>([])

async function load() { list.value = (await http.get('/pro/bc', { params: { keyword: keyword.value, sts: sts.value } })).data.data }
async function loadExpiring() { expiring.value = (await http.get('/pro/bc/expiring', { params: { days: expDays.value } })).data.data }
async function loadCodes() { codes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'BC_STS' } })).data.data }
function open(id: number | 'new') { router.push(`/pro/bc/${id}`) }
function switchTab(t: 'all' | 'expiring') { tab.value = t; t === 'expiring' ? loadExpiring() : load() }
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <h2>표준거래계약</h2>
    <div class="tabs">
      <button :class="{ on: tab === 'all' }" @click="switchTab('all')">전체 계약</button>
      <button :class="{ on: tab === 'expiring' }" @click="switchTab('expiring')">만료 예정</button>
    </div>

    <div v-if="tab === 'all'" class="panel">
      <div class="toolbar">
        <input v-model="keyword" placeholder="계약번호/제목/협력사" @keyup.enter="load" />
        <select v-model="sts" @change="load"><option value="">전체 상태</option><option v-for="c in codes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
        <button class="btn" @click="load">검색</button>
        <button class="btn primary" @click="open('new')">+ 신규 계약</button>
      </div>
      <table>
        <thead><tr><th>계약번호</th><th>차수</th><th>유형</th><th>제목</th><th>협력사</th><th>유효기간</th><th>담당</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="c in list" :key="c.id" class="row" @click="open(c.id)">
            <td>{{ c.bcNo }}</td><td>{{ c.bcRev }}차</td><td>{{ c.bcTypNm }}</td><td>{{ c.bcTitle }}</td><td>{{ c.vdNm }}</td>
            <td>{{ c.validSd }} ~ {{ c.validEd }}</td><td>{{ c.chrgUsrNm }}</td>
            <td><span class="badge" :class="c.sts">{{ c.stsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="8" class="empty">계약이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-else class="panel">
      <div class="toolbar">
        <label class="d">종료 <input type="number" v-model.number="expDays" style="width:70px" /> 일 이내</label>
        <button class="btn" @click="loadExpiring">조회</button>
      </div>
      <table>
        <thead><tr><th>계약번호</th><th>차수</th><th>제목</th><th>협력사</th><th>종료일</th><th>자동갱신</th><th>잔여</th></tr></thead>
        <tbody>
          <tr v-for="c in expiring" :key="c.id" class="row" @click="open(c.id)">
            <td>{{ c.bcNo }}</td><td>{{ c.bcRev }}차</td><td>{{ c.bcTitle }}</td><td>{{ c.vdNm }}</td><td>{{ c.validEd }}</td>
            <td>{{ c.autoRenewYn === 'Y' ? '자동갱신' : '-' }}</td>
            <td><span class="dday" :class="{ urgent: (c.ddayLeft ?? 99) <= 14, over: (c.ddayLeft ?? 0) < 0 }">{{ (c.ddayLeft ?? 0) < 0 ? '만료초과' : 'D-' + c.ddayLeft }}</span></td>
          </tr>
          <tr v-if="!expiring.length"><td colspan="7" class="empty">만료 예정 계약이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.tabs { display: flex; gap: 4px; margin-bottom: 12px; border-bottom: 2px solid #eee; }
.tabs button { padding: 9px 18px; border: none; background: none; cursor: pointer; font-size: 14px; color: #888; border-bottom: 2px solid transparent; margin-bottom: -2px; }
.tabs button.on { color: #1a3a6b; font-weight: bold; border-bottom-color: #1a3a6b; }
.toolbar { display: flex; gap: 8px; padding: 12px; align-items: center; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; } .d { font-size: 13px; color: #555; display: flex; align-items: center; gap: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.ACTIVE { background: #e3f6e8; color: #1a7f37; }
.badge.EXPIRE { background: #eee; color: #777; } .badge.TERMINATE { background: #f3e3e3; color: #a33; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.dday { font-weight: bold; color: #1a7f37; } .dday.urgent { color: #c0392b; } .dday.over { color: #fff; background: #c0392b; padding: 2px 8px; border-radius: 8px; }
</style>
