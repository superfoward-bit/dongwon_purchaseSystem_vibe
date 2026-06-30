<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Seg { id: number; vdCd: string; vdNm?: string; itemCd: string; itemNm?: string; unitCd?: string; applySd: string; applyEd: string; unitPrc: number; srcTyp?: string }
interface Rsv { id: number; rsvNo: string; rsvTitle: string; vdNm?: string; applyDt?: string; sts: string; stsNm?: string }

const router = useRouter()
const tab = ref<'price' | 'rsv'>('price')
const segs = ref<Seg[]>([])
const rsvs = ref<Rsv[]>([])
const keyword = ref('')

async function loadSegs() { segs.value = (await http.get('/pro/ip/price', { params: { keyword: keyword.value } })).data.data }
async function loadRsvs() { rsvs.value = (await http.get('/pro/ip/rsv', { params: { keyword: keyword.value } })).data.data }
function reload() { keyword.value = ''; tab.value === 'price' ? loadSegs() : loadRsvs() }
function search() { tab.value === 'price' ? loadSegs() : loadRsvs() }
function switchTab(t: 'price' | 'rsv') { tab.value = t; keyword.value = ''; t === 'price' ? loadSegs() : loadRsvs() }
function openRsv(id: number | 'new') { router.push(`/pro/ip/rsv/${id}`) }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(loadSegs)
</script>

<template>
  <div class="page">
    <h2>품목단가 구간관리</h2>
    <div class="tabs">
      <button :class="{ active: tab === 'price' }" @click="switchTab('price')">확정단가 구간</button>
      <button :class="{ active: tab === 'rsv' }" @click="switchTab('rsv')">단가 변경예약</button>
    </div>

    <div class="toolbar">
      <input v-model="keyword" :placeholder="tab === 'price' ? '품목/협력사' : '예약번호/제목/협력사'" @keyup.enter="search" />
      <button class="btn" @click="search">검색</button>
      <button v-if="tab === 'rsv'" class="btn primary" @click="openRsv('new')">+ 신규 변경예약</button>
    </div>

    <div v-if="tab === 'price'" class="panel">
      <table>
        <thead><tr><th>협력사</th><th>품목</th><th>단위</th><th>적용기간</th><th class="r">단가</th><th>출처</th></tr></thead>
        <tbody>
          <tr v-for="s in segs" :key="s.id">
            <td>{{ s.vdNm }} <span class="dim">({{ s.vdCd }})</span></td>
            <td>{{ s.itemNm }} <span class="dim">({{ s.itemCd }})</span></td>
            <td>{{ s.unitCd }}</td><td>{{ s.applySd }} ~ {{ s.applyEd }}</td>
            <td class="r">{{ fmt(s.unitPrc) }}</td><td><span class="src">{{ s.srcTyp }}</span></td>
          </tr>
          <tr v-if="!segs.length"><td colspan="6" class="empty">확정 단가구간이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-else class="panel">
      <table>
        <thead><tr><th>예약번호</th><th>제목</th><th>협력사</th><th>적용예정일</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="r in rsvs" :key="r.id" class="row" @click="openRsv(r.id)">
            <td>{{ r.rsvNo }}</td><td>{{ r.rsvTitle }}</td><td>{{ r.vdNm }}</td><td>{{ r.applyDt }}</td>
            <td><span class="badge" :class="r.sts">{{ r.stsNm }}</span></td>
          </tr>
          <tr v-if="!rsvs.length"><td colspan="5" class="empty">변경예약이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.tabs { display: flex; gap: 4px; margin-bottom: 12px; border-bottom: 2px solid #eee; }
.tabs button { padding: 9px 18px; border: none; background: none; cursor: pointer; font-size: 14px; color: #888; border-bottom: 2px solid transparent; margin-bottom: -2px; }
.tabs button.active { color: #1a3a6b; font-weight: bold; border-bottom-color: #1a3a6b; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.toolbar input { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; min-width: 240px; }
.toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.dim { color: #aaa; font-size: 12px; } .src { font-size: 11px; color: #777; background: #f0f0f0; padding: 2px 6px; border-radius: 8px; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; }
.badge.APPLIED { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
</style>
