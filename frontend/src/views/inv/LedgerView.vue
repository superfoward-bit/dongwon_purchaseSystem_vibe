<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Led { transTyp?: string; transTypNm?: string; whNm?: string; itemCd?: string; itemNm?: string; lotNo?: string; inQty?: number; outQty?: number; balQty?: number; unitCd?: string; srcTyp?: string; srcNo?: string; transYmd?: string; remark?: string }
const list = ref<Led[]>([])
const keyword = ref('')
const transTyp = ref('')

async function load() { list.value = (await http.get('/inv/ledger', { params: { keyword: keyword.value, transTyp: transTyp.value } })).data.data }
function fmt(n?: number) { return n ? n.toLocaleString() : '-' }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>수불부</h2>
    <p class="desc">모든 재고 입·출고·조정 이력입니다.</p>
    <div class="toolbar">
      <input v-model="keyword" placeholder="품목코드/명/출처번호" @keyup.enter="load" />
      <select v-model="transTyp" @change="load"><option value="">전체 유형</option><option value="IN">입고</option><option value="OUT">출고</option><option value="ADJ">조정</option></select>
      <button class="btn" @click="load">검색</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>일자</th><th>유형</th><th>품목</th><th>LOT</th><th>창고</th><th class="r">입고</th><th class="r">출고</th><th class="r">잔량</th><th>단위</th><th>출처</th><th>비고</th></tr></thead>
        <tbody>
          <tr v-for="(l, i) in list" :key="i">
            <td>{{ l.transYmd }}</td>
            <td><span class="badge" :class="l.transTyp">{{ l.transTypNm }}</span></td>
            <td>{{ l.itemNm }} <small>({{ l.itemCd }})</small></td><td>{{ l.lotNo === '*' ? '-' : l.lotNo }}</td><td>{{ l.whNm }}</td>
            <td class="r in">{{ fmt(l.inQty) }}</td><td class="r out">{{ fmt(l.outQty) }}</td><td class="r"><b>{{ (l.balQty ?? 0).toLocaleString() }}</b></td>
            <td>{{ l.unitCd }}</td><td>{{ l.srcTyp }} {{ l.srcNo }}</td><td>{{ l.remark }}</td>
          </tr>
          <tr v-if="!list.length"><td colspan="11" class="empty">수불 이력이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; } small { color: #999; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.in { color: #1a7f37; } .out { color: #c0392b; }
.badge { padding: 2px 9px; border-radius: 10px; font-size: 12px; } .badge.IN { background: #e3f6e8; color: #1a7f37; } .badge.OUT { background: #fbe4e4; color: #c0392b; } .badge.ADJ { background: #eef; color: #556; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #1a3a6b; color: #fff; border-radius: 5px; cursor: pointer; }
</style>
