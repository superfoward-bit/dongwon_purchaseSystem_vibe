<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'

interface Item { id: number; itemNm?: string; spec?: string; unitCd?: string; qty?: number; basePrc?: number }
interface Quote { rfxItemId: number; offerPrc?: number }
interface Vendor { vdCd: string; respYn?: string; quoteTotAmt?: number }
interface Rfx { id: number; rfxNo: string; rfxTitle: string; openYmd?: string; closeYmd?: string; sts?: string; stsNm?: string; selVdCd?: string; items: Item[]; quoteItems: Quote[]; vendors: Vendor[] }

const route = useRoute()
const router = useRouter()
const message = ref('')
const rfx = ref<Rfx | null>(null)
const offers = ref<Record<number, number>>({})

const canBid = computed(() => rfx.value?.sts === 'OPEN')
const total = computed(() => (rfx.value?.items || []).reduce((s, it) => s + (offers.value[it.id] || 0) * (Number(it.qty) || 0), 0))
const myVendor = computed(() => rfx.value?.vendors?.[0])

async function load() {
  const { data } = await http.get(`/portal/rfx/${route.params.id}`)
  rfx.value = { ...data.data, items: data.data.items || [], quoteItems: data.data.quoteItems || [], vendors: data.data.vendors || [] }
  const o: Record<number, number> = {}
  for (const it of rfx.value.items) {
    const ex = rfx.value.quoteItems.find(q => q.rfxItemId === it.id)
    o[it.id] = ex?.offerPrc ?? 0
  }
  offers.value = o
}
async function submit() {
  const items = rfx.value!.items.map(it => ({ rfxItemId: it.id, offerPrc: offers.value[it.id] || 0 }))
  try { await http.post(`/portal/rfx/${rfx.value!.id}/quote`, { items }); message.value = '견적이 제출되었습니다.'; await load() }
  catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page" v-if="rfx">
    <div class="head">
      <h2>견적 응찰 <span class="prno">{{ rfx.rfxNo }}</span><span class="badge" :class="rfx.sts">{{ rfx.stsNm }}</span></h2>
      <div><button class="btn" @click="router.push('/portal/rfx')">목록</button>
        <button v-if="canBid" class="btn primary" @click="submit">견적 제출</button></div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="panel-head">{{ rfx.rfxTitle }}
        <span class="info">공고 {{ rfx.openYmd }} ~ 마감 {{ rfx.closeYmd }}</span>
        <span v-if="myVendor?.respYn === 'Y'" class="resp">제출완료 ({{ fmt(myVendor.quoteTotAmt) }})</span>
      </div>
      <table>
        <thead><tr><th>품목</th><th>규격</th><th>단위</th><th class="r">수량</th><th class="r">기준단가</th><th class="r">제시단가</th><th class="r">금액</th></tr></thead>
        <tbody>
          <tr v-for="it in rfx.items" :key="it.id">
            <td>{{ it.itemNm }}</td><td>{{ it.spec }}</td><td>{{ it.unitCd }}</td><td class="r">{{ fmt(it.qty) }}</td>
            <td class="r">{{ fmt(it.basePrc) }}</td>
            <td class="r"><input v-if="canBid" type="number" v-model.number="offers[it.id]" class="num" /><span v-else>{{ fmt(offers[it.id]) }}</span></td>
            <td class="r">{{ fmt((offers[it.id] || 0) * (Number(it.qty) || 0)) }}</td>
          </tr>
          <tr class="tot"><td colspan="6" class="r">합계</td><td class="r"><b>{{ fmt(total) }}</b></td></tr>
        </tbody>
      </table>
      <p v-if="rfx.sts === 'SEL'" class="hint">{{ rfx.selVdCd === myVendor?.vdCd ? '★ 귀사가 선정되었습니다.' : '타사 선정으로 마감되었습니다.' }}</p>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.head > div { margin-left: auto; display: flex; gap: 8px; }
.prno { font-size: 14px; color: #888; font-weight: normal; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 12px; }
.info { font-weight: normal; color: #888; font-size: 13px; } .resp { margin-left: auto; color: #1a7f37; font-weight: normal; font-size: 13px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
td input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 110px; text-align: right; }
.tot td { background: #fafafa; } .hint { padding: 12px 16px; color: #1a3a6b; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; } .badge.OPEN { background: #fff3d6; color: #9a6b00; } .badge.SEL { background: #e3f6e8; color: #1a7f37; }
</style>
