<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import ItemPicker from '@/components/ItemPicker.vue'

interface Item { id?: number; prItemId?: number; itemCd?: string; itemNm?: string; spec?: string; unitCd?: string; qty?: number; startPrc?: number }
interface Vendor { vdCd: string; vdNm?: string; joinYn?: string; awardYn?: string; lastBidAmt?: number; bidCnt?: number; rankNo?: number }
interface Bid { vdNm?: string; bidAmt?: number; bidDt?: string; rankNo?: number }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Auc {
  id?: number; auctionNo?: string; auctionTitle?: string; startYmd?: string; endYmd?: string;
  startPrc?: number; minDownPrc?: number; srcTyp?: string; srcId?: number; awardVdCd?: string; awardVdNm?: string; awardAmt?: number;
  autoExtYn?: string; endDt?: string; extTriggerMin?: number; extMin?: number; maxExtCnt?: number; extCnt?: number;
  sts?: string; stsNm?: string; remark?: string; items: Item[]; vendors: Vendor[]; bids: Bid[]; actions?: Action[]; history?: His[]
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const au = ref<Auc>({ startYmd: today(), minDownPrc: 0, autoExtYn: 'N', extTriggerMin: 5, extMin: 5, maxExtCnt: 3, items: [], vendors: [], bids: [] })
const itemPickerRow = ref<number | null>(null)
const showVendorPicker = ref(false)
const bidVendor = ref('')
const bidAmt = ref<number | null>(null)

function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || au.value.sts === 'TMP')
const isOpen = computed(() => au.value.sts === 'OPEN')
const startTotal = computed(() => au.value.items.reduce((s, it) => s + (Number(it.qty) || 0) * (Number(it.startPrc) || 0), 0))

async function load() {
  if (isNew.value) { addRow(); if (route.query.prId) await loadFromPr(String(route.query.prId)); return }
  await reloadById(route.params.id as string)
}
async function loadFromPr(prId: string) {
  const { data } = await http.get(`/pro/pr/${prId}`)
  const pr = data.data
  au.value.auctionTitle = pr.prTitle; au.value.srcTyp = 'PR'; au.value.srcId = pr.id
  au.value.items = (pr.items || []).map((it: any) => ({ prItemId: it.id, itemCd: it.itemCd, itemNm: it.itemNm, spec: it.spec, unitCd: it.unitCd, qty: it.qty, startPrc: it.estPrc }))
  message.value = `구매요청 ${pr.prNo} 기반으로 역경매를 생성합니다.`
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/pro/au/${id}`)
  const d = data.data
  if (d.endDt) d.endDt = d.endDt.replace(' ', 'T').slice(0, 16)   // DB → datetime-local
  au.value = { ...d, items: d.items || [], vendors: d.vendors || [], bids: d.bids || [] }
}
function buildPayload() {
  const p: any = { ...au.value }
  if (p.autoExtYn === 'Y' && p.endDt) p.endDt = p.endDt.replace('T', ' ') + (p.endDt.length === 16 ? ':00' : '')
  else p.endDt = null
  return p
}
function addRow() { au.value.items.push({ qty: 1, startPrc: 0 }) }
function delRow(i: number) { au.value.items.splice(i, 1) }
function onItemSelect(it: any) { if (itemPickerRow.value !== null) { const r = au.value.items[itemPickerRow.value]; r.itemCd = it.itemCd; r.itemNm = it.itemNm; r.spec = it.spec; r.unitCd = it.unitCd } itemPickerRow.value = null }
function onVendorSelect(v: { vdCd: string; vdNm: string }) { if (!au.value.vendors.some(x => x.vdCd === v.vdCd)) au.value.vendors.push({ vdCd: v.vdCd, vdNm: v.vdNm }); showVendorPicker.value = false }
function removeVendor(vdCd: string) { au.value.vendors = au.value.vendors.filter(v => v.vdCd !== vdCd) }

async function save() {
  message.value = ''
  try {
    const payload = buildPayload()
    if (isNew.value) { const { data } = await http.post('/pro/au', payload); router.replace(`/pro/au/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.' }
    else { await http.put(`/pro/au/${au.value.id}`, payload); await reloadById(au.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/pro/au/${au.value.id}/action`, { action: a.action, reason }); await reloadById(au.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` } catch (e: any) { message.value = e.message }
}
async function placeBid() {
  if (!bidVendor.value || !bidAmt.value) { message.value = '협력사와 입찰가를 입력하세요.'; return }
  try {
    const { data } = await http.post(`/pro/au/${au.value.id}/bid`, { vdCd: bidVendor.value, bidAmt: bidAmt.value })
    let msg = `입찰 완료 — 현재 순위 ${data.data.rank}위`
    if (data.data.extended) msg += ` · ⏱ 마감 자동연장 (${data.data.extCnt}회, ~${data.data.endDt})`
    message.value = msg
    bidAmt.value = null; await reloadById(au.value.id!)
  } catch (e: any) { message.value = e.message }
}
async function award(vdCd: string) {
  if (!confirm('이 협력사를 낙찰하시겠습니까?')) return
  try { await http.post(`/pro/au/${au.value.id}/award`, { vdCd }); await reloadById(au.value.id!); message.value = '낙찰되었습니다.' } catch (e: any) { message.value = e.message }
}
function goToPo() { router.push(`/pro/po/new?auctionId=${au.value.id}`) }
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/pro/au/${au.value.id}`); router.push('/pro/au') }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>역경매 {{ isNew ? '등록' : '상세' }}
        <span v-if="au.auctionNo" class="prno">{{ au.auctionNo }}</span>
        <span v-if="au.srcTyp === 'PR'" class="src">← 구매요청</span>
        <span v-if="au.stsNm" class="badge" :class="au.sts">{{ au.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/au')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && au.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-if="au.sts === 'AWARD'" class="btn go" @click="goToPo">발주 생성</button>
        <button v-for="a in au.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">기본정보 <span class="tot">시작가(상한): {{ fmt(isNew ? startTotal : au.startPrc) }}</span></div>
      <div class="form">
        <label class="w2">제목 <input v-model="au.auctionTitle" :disabled="!editable" /></label>
        <label>시작일 <input type="date" v-model="au.startYmd" :disabled="!editable" /></label>
        <label>종료일 <input type="date" v-model="au.endYmd" :disabled="!editable" /></label>
        <label>최소하향폭 <input type="number" v-model.number="au.minDownPrc" :disabled="!editable" /></label>
        <label v-if="au.awardVdNm">낙찰협력사 <input :value="`${au.awardVdNm} / ${fmt(au.awardAmt)}`" disabled /></label>
        <label class="w3">비고 <input v-model="au.remark" :disabled="!editable" /></label>
      </div>
      <div class="ext">
        <label class="chk"><input type="checkbox" :checked="au.autoExtYn === 'Y'" :disabled="!editable"
          @change="au.autoExtYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /> 자동연장 사용 (스나이핑 방지)</label>
        <template v-if="au.autoExtYn === 'Y'">
          <label>마감시각 <input type="datetime-local" v-model="au.endDt" :disabled="!editable" /></label>
          <label>마감 N분전 입찰시 <input type="number" v-model.number="au.extTriggerMin" :disabled="!editable" style="width:64px" /> 분</label>
          <label>연장 <input type="number" v-model.number="au.extMin" :disabled="!editable" style="width:64px" /> 분</label>
          <label>최대연장 <input type="number" v-model.number="au.maxExtCnt" :disabled="!editable" style="width:64px" /> 회</label>
          <span v-if="!isNew" class="extcnt">현재 연장 {{ au.extCnt || 0 }}회</span>
        </template>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">품목 (시작단가) <button v-if="editable" class="btn sm" @click="addRow">+ 행추가</button></div>
      <table>
        <thead><tr><th>#</th><th>품목코드</th><th>품목명</th><th>규격</th><th>단위</th><th class="r">수량</th><th class="r">시작단가</th><th class="r">금액</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in au.items" :key="i">
            <td>{{ i + 1 }}</td>
            <td><div class="picker"><input :value="it.itemCd" readonly style="width:80px" /><button v-if="editable" class="btn sm" @click="itemPickerRow = i">🔍</button></div></td>
            <td><input v-model="it.itemNm" :disabled="!editable" /></td><td><input v-model="it.spec" :disabled="!editable" /></td>
            <td><input v-model="it.unitCd" :disabled="!editable" style="width:56px" /></td>
            <td class="r"><input type="number" v-model.number="it.qty" :disabled="!editable" class="num" /></td>
            <td class="r"><input type="number" v-model.number="it.startPrc" :disabled="!editable" class="num" /></td>
            <td class="r">{{ fmt((Number(it.qty)||0)*(Number(it.startPrc)||0)) }}</td>
            <td v-if="editable"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="panel">
      <div class="panel-head">참여 협력사 / 현황 <button v-if="editable" class="btn sm" @click="showVendorPicker = true">+ 협력사 추가</button></div>
      <table>
        <thead><tr><th>순위</th><th>코드</th><th>상호</th><th class="r">최종입찰가</th><th class="r">입찰횟수</th><th>낙찰</th><th></th></tr></thead>
        <tbody>
          <tr v-for="v in au.vendors" :key="v.vdCd" :class="{ rank1: v.rankNo === 1, awarded: v.awardYn === 'Y' }">
            <td><span v-if="v.rankNo" class="rk" :class="{ top: v.rankNo === 1 }">{{ v.rankNo }}위</span></td>
            <td>{{ v.vdCd }}</td><td>{{ v.vdNm }}</td>
            <td class="r">{{ v.lastBidAmt ? fmt(v.lastBidAmt) : '-' }}</td><td class="r">{{ v.bidCnt || 0 }}</td>
            <td>{{ v.awardYn === 'Y' ? '★낙찰' : '' }}</td>
            <td>
              <button v-if="editable" class="link del" @click="removeVendor(v.vdCd)">제외</button>
              <button v-if="isOpen && v.lastBidAmt" class="link go" @click="award(v.vdCd)">낙찰</button>
            </td>
          </tr>
          <tr v-if="!au.vendors.length"><td colspan="7" class="empty">참여 협력사를 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 입찰 입력 -->
    <div v-if="isOpen" class="panel">
      <div class="panel-head">입찰 입력 (총액 하향)</div>
      <div class="form">
        <label>협력사 <select v-model="bidVendor"><option value="">선택</option><option v-for="v in au.vendors" :key="v.vdCd" :value="v.vdCd">{{ v.vdNm }}</option></select></label>
        <label>입찰가(총액) <input type="number" v-model.number="bidAmt" :placeholder="`≤ ${fmt(au.startPrc)}`" /></label>
        <button class="btn go" @click="placeBid">입찰</button>
      </div>
    </div>

    <!-- 입찰 내역 -->
    <div v-if="au.bids && au.bids.length" class="panel">
      <div class="panel-head">입찰 내역</div>
      <table>
        <thead><tr><th>일시</th><th>협력사</th><th class="r">입찰가</th><th>당시순위</th></tr></thead>
        <tbody><tr v-for="(b, i) in au.bids" :key="i"><td>{{ b.bidDt }}</td><td>{{ b.vdNm }}</td><td class="r">{{ fmt(b.bidAmt) }}</td><td>{{ b.rankNo }}위</td></tr></tbody>
      </table>
    </div>

    <div v-if="au.history && au.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in au.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>

    <VendorPicker v-if="showVendorPicker" @select="onVendorSelect" @close="showVendorPicker = false" />
    <ItemPicker v-if="itemPickerRow !== null" @select="onItemSelect" @close="itemPickerRow = null" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; }
.head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.prno { font-size: 14px; color: #888; font-weight: normal; }
.src { font-size: 12px; color: #1a3a6b; background: #eef3fb; padding: 2px 8px; border-radius: 10px; }
.head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.tot { margin-left: auto; color: #1a3a6b; font-weight: normal; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.ext { padding: 12px 16px; border-top: 1px dashed #eee; display: flex; flex-wrap: wrap; gap: 14px; align-items: flex-end; background: #fcfcff; }
.ext label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .ext .chk { flex-direction: row; align-items: center; gap: 6px; font-weight: bold; color: #1a3a6b; }
.ext input { padding: 7px; border: 1px solid #ddd; border-radius: 5px; } .ext .extcnt { color: #6b3fa0; font-size: 13px; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .form .w2 { flex: 1 1 280px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.picker { display: flex; gap: 6px; align-items: center; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
td input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; } td .num { text-align: right; }
tr.rank1 { background: #f3faf5; } tr.awarded { background: #eafaef; }
.rk { padding: 2px 8px; border-radius: 8px; background: #eef; color: #556; font-size: 12px; font-weight: bold; } .rk.top { background: #1a7f37; color: #fff; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.sm { padding: 4px 8px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; }
.btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-right: 6px; } .link.del { color: #d33; } .link.go { color: #1a7f37; font-weight: bold; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.TMP { background: #eef; color: #556; } .badge.OPEN { background: #fff3d6; color: #9a6b00; } .badge.AWARD { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
</style>
