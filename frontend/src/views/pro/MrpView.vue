<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
const router = useRouter()

interface Sug { id: number; itemCd?: string; itemNm?: string; unitCd?: string; curQty?: number; onOrderQty?: number; availQty?: number; safetyStock?: number; reorderPoint?: number; shortageQty?: number; suggestQty?: number; repVdNm?: string; sts: string; stsNm?: string; srcPrNo?: string }
const list = ref<Sug[]>([])
const sts = ref('SUGGEST')
const message = ref('')
const sel = ref<Set<number>>(new Set())

async function load() { list.value = (await http.get('/pro/mrp/suggests', { params: { sts: sts.value } })).data.data; sel.value = new Set() }
async function run() {
  message.value = 'MRP 실행 중...'
  const { data } = await http.post('/pro/mrp/run', {})
  message.value = data.message; sts.value = 'SUGGEST'; await load()
}
function toggle(id: number) { sel.value.has(id) ? sel.value.delete(id) : sel.value.add(id); sel.value = new Set(sel.value) }
function toggleAll() {
  const sg = list.value.filter(s => s.sts === 'SUGGEST').map(s => s.id)
  if (sg.every(id => sel.value.has(id))) sel.value = new Set()
  else sel.value = new Set(sg)
}
async function createPr() {
  if (!sel.value.size) { message.value = '발주제안을 선택하세요.'; return }
  try { const { data } = await http.post('/pro/mrp/create-pr', { ids: [...sel.value] }); message.value = data.message; await load() }
  catch (e: any) { message.value = e.message }
}
async function ignore(s: Sug) { await http.post(`/pro/mrp/${s.id}/ignore`, {}); await load() }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
const suggestCount = computed(() => list.value.filter(s => s.sts === 'SUGGEST').length)
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>MRP / 자동발주</h2>
    <p class="desc">발주정책(안전재고·발주점)이 설정된 품목에 대해 <b>현재고 + 입고예정</b>이 발주점 이하인 항목을 찾아 발주를 제안합니다. 선택 후 구매요청(PR)을 자동생성합니다.</p>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="toolbar">
      <button class="btn go" @click="run">▶ MRP 실행</button>
      <select v-model="sts" @change="load"><option value="">전체</option><option value="SUGGEST">제안</option><option value="ORDERED">발주생성</option><option value="IGNORE">무시</option></select>
      <span class="cnt">제안 {{ suggestCount }}건 · 선택 {{ sel.size }}건</span>
      <button class="btn primary" :disabled="!sel.size" @click="createPr">선택 PR 자동생성</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr>
          <th style="width:36px"><input type="checkbox" :checked="suggestCount > 0 && sel.size === suggestCount" @change="toggleAll" /></th>
          <th>품목</th><th class="r">현재고</th><th class="r">입고예정</th><th class="r">가용</th><th class="r">발주점</th><th class="r">안전재고</th><th class="r">부족</th><th class="r">제안발주량</th><th>대표협력사</th><th>상태</th><th></th>
        </tr></thead>
        <tbody>
          <tr v-for="s in list" :key="s.id" :class="{ picked: sel.has(s.id) }">
            <td><input v-if="s.sts === 'SUGGEST'" type="checkbox" :checked="sel.has(s.id)" @change="toggle(s.id)" /></td>
            <td>{{ s.itemNm }} <small>({{ s.itemCd }})</small></td>
            <td class="r">{{ fmt(s.curQty) }}</td><td class="r">{{ fmt(s.onOrderQty) }}</td>
            <td class="r"><b>{{ fmt(s.availQty) }}</b></td><td class="r">{{ fmt(s.reorderPoint) }}</td><td class="r">{{ fmt(s.safetyStock) }}</td>
            <td class="r minus">{{ fmt(s.shortageQty) }}</td><td class="r"><b class="sug">{{ fmt(s.suggestQty) }}</b> {{ s.unitCd }}</td>
            <td>{{ s.repVdNm }}</td>
            <td><span class="badge" :class="s.sts">{{ s.stsNm }}</span><a v-if="s.srcPrNo" class="prlink" @click="router.push('/pro/pr')"> {{ s.srcPrNo }}</a></td>
            <td><button v-if="s.sts === 'SUGGEST'" class="link" @click="ignore(s)">무시</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="12" class="empty">MRP를 실행하면 발주제안이 표시됩니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; align-items: center; } .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .cnt { color: #555; font-size: 13px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; } small { color: #999; }
tr.picked { background: #eef3fb; } .empty { text-align: center; color: #aaa; padding: 22px; }
.minus { color: #c0392b; } .sug { color: #1a3a6b; } .prlink { color: #1a3a6b; cursor: pointer; font-size: 12px; }
.badge { padding: 2px 9px; border-radius: 10px; font-size: 12px; } .badge.SUGGEST { background: #fff3d6; color: #9a6b00; } .badge.ORDERED { background: #e3f6e8; color: #1a7f37; } .badge.IGNORE { background: #eee; color: #888; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.primary { background: #1a3a6b; color: #fff; } .btn:disabled { opacity: 0.5; cursor: default; }
.link { border: none; background: none; color: #888; cursor: pointer; font-size: 13px; text-decoration: underline; }
</style>
