<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import http from '@/api/http'

const year = ref(String(new Date().getFullYear()))
const tab = ref('grMonth')
const summary = ref<any>({})
const rows = ref<any[]>([])
const settleYm = ref('')

const TABS = [
  { key: 'grMonth', nm: '월별 입고실적', url: '/report/gr-monthly' },
  { key: 'grVendor', nm: '협력사별 입고', url: '/report/gr-vendor' },
  { key: 'grItem', nm: '품목별 입고', url: '/report/gr-item' },
  { key: 'poMonth', nm: '월별 발주실적', url: '/report/po-monthly' },
  { key: 'price', nm: '단가변동 이력', url: '/report/price-history' },
  { key: 'settle', nm: '정산조정/장려금', url: '/report/settle-adj' },
]
const maxAmt = computed(() => Math.max(1, ...rows.value.map(r => Math.abs(Number(r.amt) || 0))))

async function loadSummary() { summary.value = (await http.get('/report/summary', { params: { year: year.value } })).data.data || {} }
async function loadTab() {
  const t = TABS.find(x => x.key === tab.value)!
  const params: any = tab.value === 'settle' ? { closeYm: settleYm.value } : (tab.value === 'price' ? {} : { year: year.value })
  rows.value = (await http.get(t.url, { params })).data.data || []
}
function switchTab(k: string) { tab.value = k; loadTab() }
function reload() { loadSummary(); loadTab() }
function fmt(n: any) { return (Number(n) || 0).toLocaleString() }
function bar(n: any) { return Math.round(Math.abs(Number(n) || 0) / maxAmt.value * 100) }
onMounted(async () => { await loadSummary(); await loadTab() })
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>실적 리포트</h2>
      <div class="yr"><label>연도 <input v-model="year" style="width:80px" @keyup.enter="reload" /></label><button class="btn" @click="reload">조회</button></div>
    </div>

    <div class="cards">
      <div class="card"><span>입고실적({{ year }})</span><b>{{ fmt(summary.grAmt) }}</b><i>{{ fmt(summary.grCnt) }}건</i></div>
      <div class="card"><span>발주실적({{ year }})</span><b>{{ fmt(summary.poAmt) }}</b><i>{{ fmt(summary.poCnt) }}건</i></div>
    </div>

    <div class="tabs">
      <button v-for="t in TABS" :key="t.key" :class="{ on: tab === t.key }" @click="switchTab(t.key)">{{ t.nm }}</button>
    </div>

    <div v-if="tab === 'settle'" class="subbar"><label>정산월 <input v-model="settleYm" placeholder="전체(YYYYMM)" /></label><button class="btn" @click="loadTab">조회</button></div>

    <div class="panel">
      <!-- 월별(막대) -->
      <table v-if="tab === 'grMonth' || tab === 'poMonth'">
        <thead><tr><th>월</th><th class="r">건수</th><th class="r">금액</th><th class="bar-col">추이</th></tr></thead>
        <tbody>
          <tr v-for="r in rows" :key="r.ym"><td>{{ r.ym }}</td><td class="r">{{ fmt(r.cnt) }}</td><td class="r">{{ fmt(r.amt) }}</td>
            <td class="bar-col"><div class="bar" :style="{ width: bar(r.amt) + '%' }"></div></td></tr>
          <tr v-if="!rows.length"><td colspan="4" class="empty">데이터가 없습니다.</td></tr>
        </tbody>
      </table>
      <!-- 협력사별 -->
      <table v-else-if="tab === 'grVendor'">
        <thead><tr><th>협력사</th><th class="r">입고건수</th><th class="r">입고금액</th><th class="bar-col"></th></tr></thead>
        <tbody>
          <tr v-for="r in rows" :key="r.vdCd"><td>{{ r.vdNm }} <span class="dim">({{ r.vdCd }})</span></td><td class="r">{{ fmt(r.cnt) }}</td><td class="r">{{ fmt(r.amt) }}</td>
            <td class="bar-col"><div class="bar" :style="{ width: bar(r.amt) + '%' }"></div></td></tr>
          <tr v-if="!rows.length"><td colspan="4" class="empty">데이터가 없습니다.</td></tr>
        </tbody>
      </table>
      <!-- 품목별 -->
      <table v-else-if="tab === 'grItem'">
        <thead><tr><th>품목</th><th class="r">입고수량</th><th class="r">입고금액</th><th class="bar-col"></th></tr></thead>
        <tbody>
          <tr v-for="r in rows" :key="r.itemCd"><td>{{ r.itemNm }} <span class="dim">({{ r.itemCd }})</span></td><td class="r">{{ fmt(r.qty) }}</td><td class="r">{{ fmt(r.amt) }}</td>
            <td class="bar-col"><div class="bar" :style="{ width: bar(r.amt) + '%' }"></div></td></tr>
          <tr v-if="!rows.length"><td colspan="4" class="empty">데이터가 없습니다.</td></tr>
        </tbody>
      </table>
      <!-- 단가변동 -->
      <table v-else-if="tab === 'price'">
        <thead><tr><th>협력사</th><th>품목</th><th>변경유형</th><th class="r">이전단가</th><th class="r">신규단가</th><th>적용기간</th><th>일시</th></tr></thead>
        <tbody>
          <tr v-for="(r, i) in rows" :key="i"><td>{{ r.vdCd }}</td><td>{{ r.itemCd }}</td><td>{{ r.chgTyp }}</td>
            <td class="r">{{ r.oldPrc != null ? fmt(r.oldPrc) : '-' }}</td><td class="r">{{ r.newPrc != null ? fmt(r.newPrc) : '-' }}</td>
            <td>{{ r.newSd }} ~ {{ r.newEd }}</td><td class="dim">{{ r.regDt }}</td></tr>
          <tr v-if="!rows.length"><td colspan="7" class="empty">단가변동 이력이 없습니다.</td></tr>
        </tbody>
      </table>
      <!-- 정산조정 -->
      <table v-else>
        <thead><tr><th>조정유형</th><th class="r">건수</th><th class="r">금액</th></tr></thead>
        <tbody>
          <tr v-for="(r, i) in rows" :key="i"><td>{{ r.adjTypNm }}</td><td class="r">{{ fmt(r.cnt) }}</td><td class="r" :class="{ minus: Number(r.amt)<0 }">{{ fmt(r.amt) }}</td></tr>
          <tr v-if="!rows.length"><td colspan="3" class="empty">정산 조정 데이터가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 14px; } .head h2 { margin: 0; } .yr { margin-left: auto; display: flex; gap: 8px; align-items: center; } .yr input { padding: 7px; border: 1px solid #ddd; border-radius: 5px; }
.cards { display: flex; gap: 14px; margin-bottom: 16px; }
.card { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); padding: 16px; display: flex; flex-direction: column; gap: 4px; }
.card span { color: #888; font-size: 13px; } .card b { font-size: 22px; color: #1a3a6b; } .card i { color: #999; font-size: 13px; font-style: normal; }
.tabs { display: flex; gap: 2px; border-bottom: 2px solid #eee; margin-bottom: 0; flex-wrap: wrap; }
.tabs button { padding: 9px 16px; border: none; background: none; cursor: pointer; color: #888; border-bottom: 2px solid transparent; margin-bottom: -2px; font-size: 14px; }
.tabs button.on { color: #1a3a6b; font-weight: bold; border-bottom-color: #1a3a6b; }
.subbar { padding: 10px 0; display: flex; gap: 8px; align-items: center; } .subbar input { padding: 7px; border: 1px solid #ddd; border-radius: 5px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-top: 14px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.bar-col { width: 32%; } .bar { height: 14px; background: linear-gradient(90deg, #1a3a6b, #4a78c8); border-radius: 7px; }
.dim { color: #aaa; font-size: 12px; } .minus { color: #c0392b; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
</style>
