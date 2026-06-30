<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Stock { whCd?: string; whNm?: string; itemCd?: string; itemNm?: string; lotNo?: string; mfgYmd?: string; expYmd?: string; unitCd?: string; qty?: number; lastInYmd?: string }
const list = ref<Stock[]>([])
const keyword = ref('')
const whCd = ref('')
const zeroYn = ref('N')
const whCodes = ref<{ cd: string; cdNmKo: string }[]>([])

async function load() {
  list.value = (await http.get('/inv/stock', { params: { keyword: keyword.value, whCd: whCd.value, zeroYn: zeroYn.value } })).data.data
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
const todayStr = new Date().toISOString().slice(0, 10)
function expClass(s: Stock) {
  if (!s.expYmd) return ''
  if (s.expYmd < todayStr) return 'exp-over'
  const d = (new Date(s.expYmd).getTime() - Date.now()) / 86400000
  return d <= 30 ? 'exp-soon' : ''
}
onMounted(async () => { whCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'WH' } })).data.data; await load() })
</script>

<template>
  <div class="page">
    <h2>재고현황</h2>
    <p class="desc">창고·품목·LOT 단위 현재고입니다. 입고(GR) 확정 시 합격수량이 자동 입고됩니다. 유통기한 임박(30일 이내)/경과는 색상으로 표시됩니다.</p>
    <div class="toolbar">
      <input v-model="keyword" placeholder="품목코드/명/LOT" @keyup.enter="load" />
      <select v-model="whCd" @change="load"><option value="">전체 창고</option><option v-for="c in whCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
      <label class="chk"><input type="checkbox" :checked="zeroYn === 'Y'" @change="zeroYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'; load()" /> 0재고 포함</label>
      <button class="btn" @click="load">검색</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>품목코드</th><th>품목명</th><th>창고</th><th>LOT</th><th>제조일</th><th>유통기한</th><th class="r">현재고</th><th>단위</th><th>최근입고</th></tr></thead>
        <tbody>
          <tr v-for="(s, i) in list" :key="i" :class="expClass(s)">
            <td>{{ s.itemCd }}</td><td>{{ s.itemNm }}</td><td>{{ s.whNm }}</td><td>{{ s.lotNo === '*' ? '-' : s.lotNo }}</td>
            <td>{{ s.mfgYmd }}</td><td>{{ s.expYmd }}<span v-if="expClass(s) === 'exp-over'" class="tag over">경과</span><span v-else-if="expClass(s) === 'exp-soon'" class="tag soon">임박</span></td>
            <td class="r"><b>{{ fmt(s.qty) }}</b></td><td>{{ s.unitCd }}</td><td>{{ s.lastInYmd }}</td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">재고가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; align-items: center; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .chk { font-size: 13px; color: #555; display: flex; align-items: center; gap: 4px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.empty { text-align: center; color: #aaa; padding: 22px; }
tr.exp-over { background: #fdf0f0; } tr.exp-soon { background: #fffbe9; }
.tag { font-size: 11px; padding: 1px 6px; border-radius: 8px; margin-left: 6px; } .tag.over { background: #f3d6d6; color: #a33; } .tag.soon { background: #fbeec0; color: #946b00; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #1a3a6b; color: #fff; border-radius: 5px; cursor: pointer; }
</style>
