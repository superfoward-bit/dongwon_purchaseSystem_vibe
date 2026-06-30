<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import ItemPicker from '@/components/ItemPicker.vue'
import RfpEvalPanel from '@/components/RfpEvalPanel.vue'
import AttachPanel from '@/components/AttachPanel.vue'

interface Item { id?: number; prItemId?: number; itemCd?: string; itemNm?: string; spec?: string; unitCd?: string; qty?: number; basePrc?: number }
interface Vendor { vdCd: string; vdNm?: string; respYn?: string; selYn?: string; quoteTotAmt?: number }
interface Quote { vdCd: string; rfxItemId: number; offerPrc?: number; offerAmt?: number }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Rfx {
  id?: number; rfxNo?: string; rfxTitle?: string; openYmd?: string; closeYmd?: string; evalTyp?: string;
  srcTyp?: string; srcId?: number; selVdCd?: string; selVdNm?: string; sts?: string; stsNm?: string; remark?: string;
  items: Item[]; vendors: Vendor[]; quoteItems: Quote[]; actions?: Action[]; history?: His[]
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const rfx = ref<Rfx>({ openYmd: today(), items: [], vendors: [], quoteItems: [] })
const itemPickerRow = ref<number | null>(null)
const showVendorPicker = ref(false)
const quoteVendor = ref<string>('')        // 견적입력 대상 협력사
const quoteRows = ref<Record<number, number>>({})  // rfxItemId -> offerPrc

function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || rfx.value.sts === 'TMP')
const isOpen = computed(() => rfx.value.sts === 'OPEN')
const respondedVendors = computed(() => rfx.value.vendors.filter(v => v.respYn === 'Y'))

async function load() {
  if (isNew.value) {
    addRow()
    if (route.query.prId) await loadFromPr(String(route.query.prId))
    return
  }
  await reloadById(route.params.id as string)
}
async function loadFromPr(prId: string) {
  const { data } = await http.get(`/pro/pr/${prId}`)
  const pr = data.data
  rfx.value.rfxTitle = pr.prTitle
  rfx.value.srcTyp = 'PR'; rfx.value.srcId = pr.id
  rfx.value.items = (pr.items || []).map((it: any) => ({
    prItemId: it.id, itemCd: it.itemCd, itemNm: it.itemNm, spec: it.spec, unitCd: it.unitCd, qty: it.qty, basePrc: it.estPrc,
  }))
  message.value = `구매요청 ${pr.prNo} 기반으로 견적을 생성합니다.`
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/pro/rx/${id}`)
  rfx.value = { ...data.data, items: data.data.items || [], vendors: data.data.vendors || [], quoteItems: data.data.quoteItems || [] }
}
function addRow() { rfx.value.items.push({ qty: 1, basePrc: 0 }) }
function delRow(i: number) { rfx.value.items.splice(i, 1) }
function onItemSelect(it: any) {
  if (itemPickerRow.value !== null) {
    const r = rfx.value.items[itemPickerRow.value]
    r.itemCd = it.itemCd; r.itemNm = it.itemNm; r.spec = it.spec; r.unitCd = it.unitCd
  }
  itemPickerRow.value = null
}
function onVendorSelect(v: { vdCd: string; vdNm: string }) {
  if (!rfx.value.vendors.some(x => x.vdCd === v.vdCd)) rfx.value.vendors.push({ vdCd: v.vdCd, vdNm: v.vdNm })
  showVendorPicker.value = false
}
function removeVendor(vdCd: string) { rfx.value.vendors = rfx.value.vendors.filter(v => v.vdCd !== vdCd) }

async function save() {
  message.value = ''
  try {
    if (isNew.value) {
      const { data } = await http.post('/pro/rx', rfx.value)
      router.replace(`/pro/rx/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.'
    } else {
      await http.put(`/pro/rx/${rfx.value.id}`, rfx.value); await reloadById(rfx.value.id!); message.value = '수정되었습니다.'
    }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return }
  else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/pro/rx/${rfx.value.id}/action`, { action: a.action, reason }); await reloadById(rfx.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` }
  catch (e: any) { message.value = e.message }
}
async function remove() {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/pro/rx/${rfx.value.id}`); router.push('/pro/rx')
}
function startQuote(vdCd: string) {
  quoteVendor.value = vdCd
  const rows: Record<number, number> = {}
  for (const it of rfx.value.items) {
    const existing = rfx.value.quoteItems.find(q => q.vdCd === vdCd && q.rfxItemId === it.id)
    rows[it.id!] = existing?.offerPrc ?? 0
  }
  quoteRows.value = rows
}
async function saveQuote() {
  const items = rfx.value.items.map(it => ({ rfxItemId: it.id, offerPrc: quoteRows.value[it.id!] || 0 }))
  try {
    await http.post(`/pro/rx/${rfx.value.id}/quote`, { vdCd: quoteVendor.value, items })
    quoteVendor.value = ''; await reloadById(rfx.value.id!); message.value = '견적이 저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function selectVendor(vdCd: string) {
  if (!confirm('이 협력사를 선정하시겠습니까?')) return
  try { await http.post(`/pro/rx/${rfx.value.id}/select`, { vdCd }); await reloadById(rfx.value.id!); message.value = '선정되었습니다.' }
  catch (e: any) { message.value = e.message }
}
function goToPo() { router.push(`/pro/po/new?rfxId=${rfx.value.id}`) }
async function reannounce() {
  if (!confirm('이 견적을 재공고(신규 복제)하시겠습니까?')) return
  try { const { data } = await http.post(`/pro/rx/${rfx.value.id}/reannounce`, {}); router.push(`/pro/rx/${data.data}`) }
  catch (e: any) { message.value = e.message }
}

function offerOf(vdCd: string, rfxItemId?: number) { return rfx.value.quoteItems.find(q => q.vdCd === vdCd && q.rfxItemId === rfxItemId)?.offerPrc }
function isLowest(vdCd: string, rfxItemId?: number) {
  const offers = respondedVendors.value.map(v => offerOf(v.vdCd, rfxItemId)).filter(x => x != null) as number[]
  if (!offers.length) return false
  return offerOf(vdCd, rfxItemId) === Math.min(...offers)
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>견적 {{ isNew ? '등록' : '상세' }}
        <span v-if="rfx.rfxNo" class="prno">{{ rfx.rfxNo }}</span>
        <span v-if="rfx.srcTyp === 'PR'" class="src">← 구매요청</span>
        <span v-if="rfx.stsNm" class="badge" :class="rfx.sts">{{ rfx.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/rx')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && rfx.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-if="rfx.sts === 'SEL'" class="btn go" @click="goToPo">발주 생성</button>
        <button v-if="rfx.sts === 'FAIL' || rfx.sts === 'CANCEL'" class="btn primary" @click="reannounce">재공고</button>
        <button v-for="a in rfx.actions" :key="a.action"
                class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')"
                @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">기본정보</div>
      <div class="form">
        <label class="w2">제목 <input v-model="rfx.rfxTitle" :disabled="!editable" /></label>
        <label>공고일 <input type="date" v-model="rfx.openYmd" :disabled="!editable" /></label>
        <label>마감일 <input type="date" v-model="rfx.closeYmd" :disabled="!editable" /></label>
        <label>평가방식 <input value="최저가" disabled style="width:80px" /></label>
        <label v-if="rfx.selVdNm">선정협력사 <input :value="rfx.selVdNm" disabled /></label>
        <label class="w3">비고 <input v-model="rfx.remark" :disabled="!editable" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">견적 품목 <button v-if="editable" class="btn sm" @click="addRow">+ 행추가</button></div>
      <table>
        <thead><tr><th>#</th><th>품목코드</th><th>품목명</th><th>규격</th><th>단위</th><th class="r">수량</th><th class="r">기준단가</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in rfx.items" :key="i">
            <td>{{ i + 1 }}</td>
            <td><div class="picker"><input :value="it.itemCd" readonly style="width:80px" /><button v-if="editable" class="btn sm" @click="itemPickerRow = i">🔍</button></div></td>
            <td><input v-model="it.itemNm" :disabled="!editable" /></td>
            <td><input v-model="it.spec" :disabled="!editable" /></td>
            <td><input v-model="it.unitCd" :disabled="!editable" style="width:56px" /></td>
            <td class="r"><input type="number" v-model.number="it.qty" :disabled="!editable" class="num" /></td>
            <td class="r"><input type="number" v-model.number="it.basePrc" :disabled="!editable" class="num" /></td>
            <td v-if="editable"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="panel">
      <div class="panel-head">초대 협력사 <button v-if="editable" class="btn sm" @click="showVendorPicker = true">+ 협력사 추가</button></div>
      <table>
        <thead><tr><th>코드</th><th>상호</th><th>응답</th><th class="r">견적총액</th><th>선정</th><th></th></tr></thead>
        <tbody>
          <tr v-for="v in rfx.vendors" :key="v.vdCd" :class="{ sel: v.selYn === 'Y' }">
            <td>{{ v.vdCd }}</td><td>{{ v.vdNm }}</td>
            <td><span :class="v.respYn === 'Y' ? 'ok' : 'no'">{{ v.respYn === 'Y' ? '응답' : '미응답' }}</span></td>
            <td class="r">{{ v.respYn === 'Y' ? fmt(v.quoteTotAmt) : '-' }}</td>
            <td>{{ v.selYn === 'Y' ? '★선정' : '' }}</td>
            <td>
              <button v-if="editable" class="link del" @click="removeVendor(v.vdCd)">제외</button>
              <button v-if="isOpen" class="link" @click="startQuote(v.vdCd)">견적입력</button>
              <button v-if="isOpen && v.respYn === 'Y'" class="link go" @click="selectVendor(v.vdCd)">선정</button>
            </td>
          </tr>
          <tr v-if="!rfx.vendors.length"><td colspan="6" class="empty">초대 협력사를 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 견적 입력 -->
    <div v-if="quoteVendor" class="panel">
      <div class="panel-head">견적 입력 — {{ rfx.vendors.find(v => v.vdCd === quoteVendor)?.vdNm }}
        <button class="btn primary sm" @click="saveQuote">저장</button>
        <button class="btn sm" @click="quoteVendor = ''">취소</button>
      </div>
      <table>
        <thead><tr><th>품목</th><th>규격</th><th class="r">수량</th><th class="r">제시단가</th><th class="r">금액</th></tr></thead>
        <tbody>
          <tr v-for="it in rfx.items" :key="it.id">
            <td>{{ it.itemNm }}</td><td>{{ it.spec }}</td><td class="r">{{ fmt(it.qty) }}</td>
            <td class="r"><input type="number" v-model.number="quoteRows[it.id!]" class="num" /></td>
            <td class="r">{{ fmt((quoteRows[it.id!] || 0) * (Number(it.qty) || 0)) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 견적 비교 -->
    <div v-if="respondedVendors.length" class="panel">
      <div class="panel-head">견적 비교 (최저가 강조)</div>
      <table>
        <thead><tr><th>품목</th><th class="r">수량</th><th v-for="v in respondedVendors" :key="v.vdCd" class="r">{{ v.vdNm }}</th></tr></thead>
        <tbody>
          <tr v-for="it in rfx.items" :key="it.id">
            <td>{{ it.itemNm }}</td><td class="r">{{ fmt(it.qty) }}</td>
            <td v-for="v in respondedVendors" :key="v.vdCd" class="r" :class="{ low: isLowest(v.vdCd, it.id) }">{{ fmt(offerOf(v.vdCd, it.id)) }}</td>
          </tr>
          <tr class="totrow"><td>합계</td><td></td><td v-for="v in respondedVendors" :key="v.vdCd" class="r"><b>{{ fmt(v.quoteTotAmt) }}</b></td></tr>
        </tbody>
      </table>
    </div>

    <!-- RFP 평가입찰 -->
    <div v-if="!isNew && rfx.id && respondedVendors.length" class="panel">
      <div class="panel-head">RFP 평가입찰 (비가격 + 가격 종합평가)</div>
      <RfpEvalPanel :rfxId="rfx.id" :readonly="!isOpen" @select="selectVendor" />
    </div>

    <div v-if="!isNew && rfx.id" class="panel">
      <div class="panel-head">첨부파일 (견적서/사양서)</div>
      <div style="padding:8px 12px"><AttachPanel refTyp="RX" :refId="rfx.id" :readonly="!editable" /></div>
    </div>

    <div v-if="rfx.history && rfx.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in rfx.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
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
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form .w2 { flex: 1 1 280px; } .form .w3 { flex: 1 1 100%; }
.form input { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.picker { display: flex; gap: 6px; align-items: center; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
td input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
td .num { text-align: right; }
tr.sel { background: #eafaef; }
.ok { color: #1a7f37; } .no { color: #aaa; }
.low { background: #fff7d6; font-weight: bold; }
.totrow td { background: #fafafa; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; }
.btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-right: 6px; }
.link.del { color: #d33; } .link.go { color: #1a7f37; font-weight: bold; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.TMP { background: #eef; color: #556; } .badge.OPEN { background: #fff3d6; color: #9a6b00; }
.badge.SEL { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
</style>
