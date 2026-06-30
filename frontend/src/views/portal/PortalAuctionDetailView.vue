<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'

interface Item { itemNm?: string; spec?: string; unitCd?: string; qty?: number; startPrc?: number }
interface Vendor { vdCd: string; lastBidAmt?: number; bidCnt?: number; rankNo?: number; awardYn?: string }
interface Bid { bidAmt?: number; bidDt?: string; rankNo?: number }
interface Auc { id: number; auctionNo: string; auctionTitle: string; startPrc?: number; minDownPrc?: number; awardVdCd?: string; sts?: string; stsNm?: string; items: Item[]; vendors: Vendor[]; bids: Bid[] }

const route = useRoute()
const router = useRouter()
const message = ref('')
const au = ref<Auc | null>(null)
const bidAmt = ref<number | null>(null)

const me = computed(() => au.value?.vendors?.[0])
const isOpen = computed(() => au.value?.sts === 'OPEN')

async function load() {
  const { data } = await http.get(`/portal/au/${route.params.id}`)
  au.value = { ...data.data, items: data.data.items || [], vendors: data.data.vendors || [], bids: data.data.bids || [] }
}
async function placeBid() {
  if (!bidAmt.value) { message.value = '입찰가를 입력하세요.'; return }
  try { const { data } = await http.post(`/portal/au/${au.value!.id}/bid`, { bidAmt: bidAmt.value }); message.value = `입찰 완료 — 현재 ${data.data.rank}위`; bidAmt.value = null; await load() }
  catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page" v-if="au">
    <div class="head">
      <h2>역경매 입찰 <span class="prno">{{ au.auctionNo }}</span><span class="badge" :class="au.sts">{{ au.stsNm }}</span></h2>
      <button class="btn" @click="router.push('/portal/au')">목록</button>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="cards">
      <div class="card"><span>시작가(상한)</span><b>{{ fmt(au.startPrc) }}</b></div>
      <div class="card"><span>나의 최종입찰</span><b>{{ me?.lastBidAmt ? fmt(me.lastBidAmt) : '-' }}</b></div>
      <div class="card hl"><span>나의 현재순위</span><b>{{ me?.rankNo ? me.rankNo + '위' : '-' }}</b></div>
    </div>

    <div class="panel">
      <div class="panel-head">{{ au.auctionTitle }} <span class="info">최소 하향폭 {{ fmt(au.minDownPrc) }}</span></div>
      <table>
        <thead><tr><th>품목</th><th>규격</th><th class="r">수량</th><th class="r">시작단가</th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in au.items" :key="i"><td>{{ it.itemNm }}</td><td>{{ it.spec }}</td><td class="r">{{ fmt(it.qty) }}</td><td class="r">{{ fmt(it.startPrc) }}</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="isOpen" class="panel bidbox">
      <div class="panel-head">입찰하기 (총액, 이전보다 낮게)</div>
      <div class="bidform">
        <input type="number" v-model.number="bidAmt" :placeholder="`현재 ${me?.lastBidAmt ? fmt(me.lastBidAmt) : fmt(au.startPrc)} 이하`" />
        <button class="btn go" @click="placeBid">입찰</button>
      </div>
    </div>
    <div v-else-if="au.sts === 'AWARD'" class="panel">
      <div class="panel-head">결과</div>
      <p class="hint">{{ au.awardVdCd === me?.vdCd ? '★ 귀사가 낙찰되었습니다!' : '타사 낙찰로 마감되었습니다.' }}</p>
    </div>

    <div v-if="au.bids.length" class="panel">
      <div class="panel-head">나의 입찰내역</div>
      <table>
        <thead><tr><th>일시</th><th class="r">입찰가</th><th>당시순위</th></tr></thead>
        <tbody><tr v-for="(b, i) in au.bids" :key="i"><td>{{ b.bidDt }}</td><td class="r">{{ fmt(b.bidAmt) }}</td><td>{{ b.rankNo }}위</td></tr></tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; } .head .btn { margin-left: auto; }
.prno { font-size: 14px; color: #888; font-weight: normal; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 16px; }
.card { background: #fff; border-radius: 10px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.card span { color: #888; font-size: 13px; } .card b { display: block; font-size: 24px; margin-top: 6px; } .card.hl b { color: #1a7f37; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; gap: 12px; align-items: center; } .info { font-weight: normal; color: #888; font-size: 13px; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.bidbox .bidform { padding: 16px; display: flex; gap: 10px; } .bidform input { padding: 10px; border: 1px solid #ddd; border-radius: 6px; width: 240px; text-align: right; font-size: 16px; }
.hint { padding: 12px 16px; color: #1a3a6b; font-weight: bold; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; padding: 10px 22px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; } .badge.OPEN { background: #fff3d6; color: #9a6b00; } .badge.AWARD { background: #e3f6e8; color: #1a7f37; }
</style>
