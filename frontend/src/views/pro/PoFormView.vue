<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import ItemPicker from '@/components/ItemPicker.vue'
import UserPicker from '@/components/UserPicker.vue'

interface Item { prItemId?: number; itemCd?: string; itemNm?: string; spec?: string; unitCd?: string; qty?: number; prc?: number; taxTyp?: string; reqDlvYmd?: string; remark?: string }
interface DlvSch { itemCd?: string; itemNm?: string; planQty?: number; planYmd?: string; dlvPlace?: string; sts?: string }
interface PayPlan { payTyp?: string; payTypNm?: string; payNm?: string; rate?: number; amt?: number; planYmd?: string; paidYn?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Po {
  id?: number; poNo?: string; poTitle?: string; vdCd?: string; vdNm?: string; poYmd?: string; dlvYmd?: string;
  reqDlvYmd?: string; dlvPlace?: string; whCd?: string; whNm?: string;
  dlvCond?: string; payCond?: string; payTermCd?: string; payMethodCd?: string; currCd?: string; srcTyp?: string; srcId?: number; sts?: string; stsNm?: string; remark?: string;
  poTyp?: string; poTypNm?: string; purcTyp?: string; purcTypNm?: string;
  performBondYn?: string; performBondRate?: number; maintBondYn?: string; maintBondRate?: number; payPlanYn?: string;
  items: Item[]; payPlans: PayPlan[]; dlvSchedules?: DlvSch[]; actions?: Action[]; history?: His[]; approval?: Approval
}
interface Code { cd: string; cdNmKo: string }
interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; lines: Aline[] }

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const po = ref<Po>({ currCd: 'KRW', poYmd: today(), poTyp: 'STD', purcTyp: 'MT', performBondYn: 'N', maintBondYn: 'N', payPlanYn: 'N', items: [], payPlans: [], dlvSchedules: [] })
const poTypCodes = ref<Code[]>([])
const whCodes = ref<Code[]>([])
const purcTypCodes = ref<Code[]>([])
const showVendorPicker = ref(false)
const itemPickerRow = ref<number | null>(null)
const showApproverModal = ref(false)
const showUserPicker = ref(false)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])

function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || po.value.sts === 'TMP')
function lineSupl(it: Item) { return (Number(it.qty) || 0) * (Number(it.prc) || 0) }
function isTaxable(it: Item) { return !it.taxTyp || it.taxTyp === 'TAX' }
function lineVat(it: Item) { return isTaxable(it) ? Math.round(lineSupl(it) * 0.1) : 0 }
const supl = computed(() => po.value.items.reduce((s, it) => s + lineSupl(it), 0))
const vat = computed(() => po.value.items.reduce((s, it) => s + lineVat(it), 0))
const tot = computed(() => supl.value + vat.value)

async function load() {
  if (isNew.value) {
    // PR 또는 견적(RFx)에서 발주 생성
    if (route.query.prId) { await loadFromPr(String(route.query.prId)); return }
    if (route.query.rfxId) { await loadFromRfx(String(route.query.rfxId)); return }
    if (route.query.auctionId) { await loadFromSource(`/pro/au/${route.query.auctionId}/po-source`, '역경매 낙찰'); return }
    addRow()
    return
  }
  await reloadById(route.params.id as string)
}
async function loadFromRfx(rfxId: string) {
  await loadFromSource(`/pro/rx/${rfxId}/po-source`, '견적 선정결과')
}
async function loadFromSource(url: string, label: string) {
  const { data } = await http.get(url)
  const s = data.data
  po.value = {
    currCd: 'KRW', poYmd: today(), poTitle: s.poTitle, poTyp: 'STD', purcTyp: 'MT', performBondYn: 'N', maintBondYn: 'N', payPlanYn: 'N',
    srcTyp: s.srcTyp, srcId: s.srcId, vdCd: s.vdCd, vdNm: s.vdNm, payPlans: [],
    items: (s.items || []).map((it: any) => ({
      prItemId: it.prItemId, itemCd: it.itemCd, itemNm: it.itemNm, spec: it.spec,
      unitCd: it.unitCd, qty: it.qty, prc: it.prc, taxTyp: it.taxTyp || 'TAX',
    })),
  }
  message.value = `${label} 기반으로 발주를 생성합니다. 협력사: ${s.vdNm}`
}
async function loadFromPr(prId: string) {
  const { data } = await http.get(`/pro/pr/${prId}`)
  const pr = data.data
  po.value = {
    currCd: 'KRW', poYmd: today(), poTitle: pr.prTitle, poTyp: 'STD', purcTyp: 'MT', performBondYn: 'N', maintBondYn: 'N', payPlanYn: 'N',
    srcTyp: 'PR', srcId: pr.id, vdCd: '', vdNm: '', payPlans: [],
    items: (pr.items || []).map((it: any) => ({
      prItemId: it.id, itemCd: it.itemCd, itemNm: it.itemNm, spec: it.spec,
      unitCd: it.unitCd, qty: it.qty, prc: it.estPrc, taxTyp: it.taxTyp || 'TAX',
    })),
  }
  message.value = `구매요청 ${pr.prNo} 기반으로 발주를 생성합니다. 협력사를 선택하세요.`
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/pro/po/${id}`)
  po.value = { ...data.data, items: data.data.items || [], payPlans: data.data.payPlans || [], dlvSchedules: data.data.dlvSchedules || [] }
}
function addRow() { po.value.items.push({ qty: 1, prc: 0, taxTyp: 'TAX' }) }
function delRow(i: number) { po.value.items.splice(i, 1) }
function addPay() { po.value.payPlans.push({ payTyp: 'PROG', rate: 0 }) }
function delPay(i: number) { po.value.payPlans.splice(i, 1) }
function addSch() { (po.value.dlvSchedules ||= []).push({ planQty: 0, sts: 'PLAN' }) }
function delSch(i: number) { po.value.dlvSchedules?.splice(i, 1) }
const payRateSum = computed(() => po.value.payPlans.reduce((s, p) => s + (Number(p.rate) || 0), 0))
function openPrint() { if (po.value.id) window.open(`/pro/po/${po.value.id}/print`, '_blank') }

async function onVendorSelect(v: { vdCd: string; vdNm: string }) {
  po.value.vdCd = v.vdCd; po.value.vdNm = v.vdNm; showVendorPicker.value = false
  // 협력사 결제조건 스냅샷 프리필(거래시점 박제)
  try {
    const { data } = await http.get(`/base/vendor/pay-info/${v.vdCd}`)
    const p = data.data
    if (p) {
      po.value.payTermCd = p.payTermCd || po.value.payTermCd
      po.value.payMethodCd = p.payMethodCd || po.value.payMethodCd
      if (p.payCond) po.value.payCond = p.payCond
      if (p.currCd) po.value.currCd = p.currCd
    }
  } catch { /* 결제정보 미존재 무시 */ }
}
async function onItemSelect(it: { itemCd: string; itemNm: string; spec?: string; unitCd?: string; taxTyp?: string }) {
  if (itemPickerRow.value !== null) {
    const row = po.value.items[itemPickerRow.value]
    row.itemCd = it.itemCd; row.itemNm = it.itemNm; row.spec = it.spec; row.unitCd = it.unitCd
    row.taxTyp = it.taxTyp || 'TAX'   // 품목마스터 과세구분 자동전파
    await applyPrice(row)
  }
  itemPickerRow.value = null
}
/** 단가 자동적용: 계약단가 우선, 없으면 품목단가 */
async function applyPrice(row: any) {
  if (!po.value.vdCd || !row.itemCd) return
  try {
    const ymd = po.value.poYmd || new Date().toISOString().slice(0, 10)
    const { data } = await http.get('/pro/price/lookup', { params: { vdCd: po.value.vdCd, itemCd: row.itemCd, ymd } })
    const p = data.data
    if (p && p.srcTyp !== 'NONE' && p.unitPrc != null) {
      row.prc = p.unitPrc
      message.value = `[${row.itemCd}] ${p.srcTypNm} 자동적용: ${Number(p.unitPrc).toLocaleString()}`
    }
  } catch { /* 단가 미존재 시 무시 */ }
}

async function save() {
  message.value = ''
  if (!po.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  try {
    if (isNew.value) {
      const { data } = await http.post('/pro/po', po.value)
      router.replace(`/pro/po/${data.data}`)
      await reloadById(data.data)
      message.value = '등록되었습니다.'
    } else {
      await http.put(`/pro/po/${po.value.id}`, po.value)
      await reloadById(po.value.id!)
      message.value = '수정되었습니다.'
    }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return }
  else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try {
    await http.post(`/pro/po/${po.value.id}/action`, { action: a.action, reason })
    await reloadById(po.value.id!)
    message.value = `[${a.actionNm}] 처리되었습니다.`
  } catch (e: any) { message.value = e.message }
}
async function remove() {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/pro/po/${po.value.id}`)
  router.push('/pro/po')
}
function openSubmit() { approvers.value = []; showApproverModal.value = true }
function onUserSelect(u: { usrId: string; usrNm: string }) {
  if (!approvers.value.some(a => a.usrId === u.usrId)) approvers.value.push({ usrId: u.usrId, usrNm: u.usrNm })
  showUserPicker.value = false
}
function removeApprover(i: number) { approvers.value.splice(i, 1) }
async function doSubmit() {
  if (!approvers.value.length) { message.value = '결재자를 1명 이상 지정하세요.'; return }
  try {
    await http.post(`/pro/po/${po.value.id}/submit`, { approvers: approvers.value })
    showApproverModal.value = false
    await reloadById(po.value.id!)
    message.value = '상신되었습니다. (결재 진행)'
  } catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
async function loadCodes() {
  poTypCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'PO_TYP' } })).data.data
  purcTypCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'PURC_TYP' } })).data.data
  whCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'WH' } })).data.data
}
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>발주 {{ isNew ? '등록' : '상세' }}
        <span v-if="po.poNo" class="prno">{{ po.poNo }}</span>
        <span v-if="po.srcTyp === 'PR'" class="src">← 구매요청</span>
        <span v-if="po.stsNm" class="badge" :class="po.sts">{{ po.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/po')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && po.sts === 'TMP'" class="btn go" @click="openSubmit">상신</button>
        <button v-if="!isNew && po.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-if="!isNew && po.id" class="btn" @click="openPrint">발주서 출력</button>
        <button v-for="a in po.actions" :key="a.action"
                class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')"
                @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">기본정보</div>
      <div class="form">
        <label class="w2">제목 <input v-model="po.poTitle" :disabled="!editable" /></label>
        <label>발주유형 <select v-model="po.poTyp" :disabled="!editable"><option v-for="c in poTypCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>구매유형 <select v-model="po.purcTyp" :disabled="!editable"><option v-for="c in purcTypCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label class="w2">협력사
          <div class="picker">
            <input :value="po.vdNm ? `${po.vdNm} (${po.vdCd})` : ''" placeholder="협력사 선택" readonly />
            <button v-if="editable" class="btn sm" @click="showVendorPicker = true">찾기</button>
          </div>
        </label>
        <label>발주일 <input type="date" v-model="po.poYmd" :disabled="!editable" /></label>
        <label>요청납기 <input type="date" v-model="po.reqDlvYmd" :disabled="!editable" /></label>
        <label>확정납기 <input type="date" v-model="po.dlvYmd" :disabled="!editable" /></label>
        <label>입고창고 <select v-model="po.whCd" :disabled="!editable"><option value="">선택</option><option v-for="c in whCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label class="w2">납품지 <input v-model="po.dlvPlace" :disabled="!editable" placeholder="배송지 주소/장소" /></label>
        <label>납품조건 <input v-model="po.dlvCond" :disabled="!editable" /></label>
        <label>결제조건 <input v-model="po.payCond" :disabled="!editable" /></label>
        <label class="w3">비고 <input v-model="po.remark" :disabled="!editable" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">품목 <button v-if="editable" class="btn sm" @click="addRow">+ 행추가</button>
        <span class="tot">공급가 {{ fmt(supl) }} · 부가세 {{ fmt(vat) }} · <b>합계 {{ fmt(tot) }}</b></span>
      </div>
      <table>
        <thead><tr><th>#</th><th>품목코드</th><th>품목명</th><th>규격</th><th>단위</th><th>과세구분</th><th class="r">수량</th><th class="r">단가</th><th class="r">공급가</th><th class="r">부가세</th><th class="r">금액</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in po.items" :key="i">
            <td>{{ i + 1 }}</td>
            <td>
              <div class="picker">
                <input :value="it.itemCd" placeholder="-" readonly style="width:80px" />
                <button v-if="editable" class="btn sm" @click="itemPickerRow = i">🔍</button>
              </div>
            </td>
            <td><input v-model="it.itemNm" :disabled="!editable" /></td>
            <td><input v-model="it.spec" :disabled="!editable" /></td>
            <td><input v-model="it.unitCd" :disabled="!editable" style="width:56px" /></td>
            <td>
              <select v-model="it.taxTyp" :disabled="!editable" style="width:84px">
                <option value="TAX">과세</option>
                <option value="FREE">면세</option>
                <option value="ZERO">영세율</option>
                <option value="NOND">불공제</option>
              </select>
            </td>
            <td class="r"><input type="number" v-model.number="it.qty" :disabled="!editable" class="num" /></td>
            <td class="r"><input type="number" v-model.number="it.prc" :disabled="!editable" class="num" /></td>
            <td class="r">{{ fmt(lineSupl(it)) }}</td>
            <td class="r">{{ fmt(lineVat(it)) }}</td>
            <td class="r">{{ fmt(lineSupl(it) + lineVat(it)) }}</td>
            <td v-if="editable"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
          <tr v-if="!po.items.length"><td :colspan="editable ? 12 : 11" class="empty">품목을 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 보증·분할결제 (공사·용역 발주) -->
    <div class="panel">
      <div class="panel-head">계약이행·보증 / 분할결제</div>
      <div class="form">
        <label class="chk"><input type="checkbox" :checked="po.performBondYn === 'Y'" :disabled="!editable" @change="po.performBondYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /> 계약이행보증</label>
        <label v-if="po.performBondYn === 'Y'">이행보증율(%) <input type="number" v-model.number="po.performBondRate" :disabled="!editable" style="width:90px" /></label>
        <label class="chk"><input type="checkbox" :checked="po.maintBondYn === 'Y'" :disabled="!editable" @change="po.maintBondYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /> 하자유지보증</label>
        <label v-if="po.maintBondYn === 'Y'">하자보증율(%) <input type="number" v-model.number="po.maintBondRate" :disabled="!editable" style="width:90px" /></label>
        <label class="chk"><input type="checkbox" :checked="po.payPlanYn === 'Y'" :disabled="!editable" @change="po.payPlanYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /> 선금/기성/잔금 분할결제</label>
      </div>
      <div v-if="po.payPlanYn === 'Y'" class="paybox">
        <div class="pay-head">분할결제 계획 <button v-if="editable" class="btn sm" @click="addPay">+ 회차추가</button>
          <span class="sum" :class="{ bad: payRateSum !== 100 }">비율합 {{ payRateSum }} / 100</span>
        </div>
        <table>
          <thead><tr><th>회차</th><th>유형</th><th>명칭</th><th class="r">비율(%)</th><th class="r">금액(총액×비율)</th><th>지급예정일</th><th v-if="editable"></th></tr></thead>
          <tbody>
            <tr v-for="(p, i) in po.payPlans" :key="i">
              <td>{{ i + 1 }}</td>
              <td><select v-model="p.payTyp" :disabled="!editable"><option value="PRE">선금</option><option value="PROG">기성</option><option value="BAL">잔금</option></select></td>
              <td><input v-model="p.payNm" :disabled="!editable" /></td>
              <td class="r"><input type="number" v-model.number="p.rate" :disabled="!editable" class="num" /></td>
              <td class="r">{{ fmt(Math.round(tot * (Number(p.rate)||0) / 100)) }}</td>
              <td><input type="date" v-model="p.planYmd" :disabled="!editable" /></td>
              <td v-if="editable"><button class="link del" @click="delPay(i)">삭제</button></td>
            </tr>
            <tr v-if="!po.payPlans.length"><td :colspan="editable ? 7 : 6" class="empty">회차를 추가하세요. (비율 합계 100%)</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">분할납품 일정 <button v-if="editable" class="btn sm" @click="addSch">+ 일정추가</button>
        <span class="tot">분할납품 회차별 납기/수량을 관리합니다 (선택)</span>
      </div>
      <table>
        <thead><tr><th>회차</th><th>품목코드</th><th>품목명</th><th class="r">예정수량</th><th>납품예정일</th><th>납품지</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(s, i) in po.dlvSchedules" :key="i">
            <td>{{ i + 1 }}</td>
            <td><input v-model="s.itemCd" :disabled="!editable" style="width:90px" /></td>
            <td><input v-model="s.itemNm" :disabled="!editable" /></td>
            <td class="r"><input type="number" v-model.number="s.planQty" :disabled="!editable" class="num" /></td>
            <td><input type="date" v-model="s.planYmd" :disabled="!editable" /></td>
            <td><input v-model="s.dlvPlace" :disabled="!editable" /></td>
            <td v-if="editable"><button class="link del" @click="delSch(i)">삭제</button></td>
          </tr>
          <tr v-if="!po.dlvSchedules || !po.dlvSchedules.length"><td :colspan="editable ? 7 : 6" class="empty">분할납품이 필요하면 회차를 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="po.approval" class="panel">
      <div class="panel-head">결재 현황 <span class="ap-no">{{ po.approval.aprvNo }}</span>
        <span class="badge ap" :class="po.approval.aprvStsNm">{{ po.approval.aprvStsNm }}</span>
      </div>
      <div class="aline">
        <div v-for="(l, i) in po.approval.lines" :key="i" class="astep" :class="l.lineSts">
          <div class="anum">{{ l.stepNo }}</div>
          <div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
          <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
          <div v-if="i < po.approval.lines.length - 1" class="aarrow">→</div>
        </div>
      </div>
    </div>

    <div v-if="po.history && po.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody>
          <tr v-for="h in po.history" :key="h.seq">
            <td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td>
            <td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <VendorPicker v-if="showVendorPicker" @select="onVendorSelect" @close="showVendorPicker = false" />
    <ItemPicker v-if="itemPickerRow !== null" @select="onItemSelect" @close="itemPickerRow = null" />

    <div v-if="showApproverModal" class="modal-bg" @click.self="showApproverModal = false">
      <div class="ap-modal">
        <div class="modal-head">결재선 지정 <button class="x" @click="showApproverModal = false">×</button></div>
        <div class="ap-body">
          <p class="hint2">결재 순서대로 결재자를 추가하세요.</p>
          <ol class="ap-list">
            <li v-for="(a, i) in approvers" :key="a.usrId"><span>{{ i + 1 }}단계 — <b>{{ a.usrNm }}</b> ({{ a.usrId }})</span><button class="link del" @click="removeApprover(i)">제거</button></li>
            <li v-if="!approvers.length" class="empty2">결재자를 추가하세요.</li>
          </ol>
          <button class="btn" @click="showUserPicker = true">+ 결재자 추가</button>
        </div>
        <div class="ap-foot"><button class="btn primary" @click="doSubmit">상신</button><button class="btn" @click="showApproverModal = false">취소</button></div>
      </div>
    </div>
    <UserPicker v-if="showUserPicker" @select="onUserSelect" @close="showUserPicker = false" />
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
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; }
.tot { margin-left: auto; color: #1a3a6b; font-weight: normal; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form .w2 { flex: 1 1 280px; } .form .w3 { flex: 1 1 100%; }
.form input { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.picker { display: flex; gap: 6px; align-items: center; } .picker input { flex: 1; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
td input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
td .num { text-align: right; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; }
.btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; }
.badge.PC { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.ap-no { font-size: 13px; color: #888; font-weight: normal; }
.badge.ap { margin-left: 6px; } .badge.결재중 { background: #fff3d6; color: #9a6b00; } .badge.결재완료 { background: #e3f6e8; color: #1a7f37; } .badge.반려 { background: #f3e3e3; color: #a33; } .badge.회수 { background: #eee; color: #777; }
.aline { padding: 18px 16px; display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.astep { display: flex; align-items: center; gap: 8px; border: 1px solid #eee; border-radius: 8px; padding: 8px 12px; }
.astep.APPROVE { background: #f3faf5; border-color: #bfe6cc; } .astep.ING { background: #fff8ec; border-color: #f0d79a; } .astep.REJECT { background: #fdf0f0; border-color: #e6b8b8; }
.anum { width: 22px; height: 22px; border-radius: 50%; background: #1a3a6b; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.ainfo { display: flex; flex-direction: column; } .ainfo span { font-size: 12px; color: #888; }
.aop { font-size: 12px; color: #777; font-style: italic; } .aarrow { color: #ccc; margin: 0 4px; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.ap-modal { width: 460px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.ap-body { padding: 16px 18px; } .hint2 { color: #888; font-size: 13px; margin: 0 0 10px; }
.ap-list { margin: 0 0 12px; padding-left: 18px; } .ap-list li { padding: 6px 0; display: flex; justify-content: space-between; align-items: center; } .ap-list .empty2 { color: #aaa; justify-content: center; }
.ap-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
.form .chk { flex-direction: row; align-items: center; gap: 6px; color: #1a3a6b; font-weight: bold; }
.paybox { border-top: 1px dashed #eee; } .pay-head { padding: 10px 16px; font-weight: bold; display: flex; align-items: center; gap: 10px; } .pay-head .sum { margin-left: auto; color: #1a7f37; font-weight: normal; font-size: 13px; } .pay-head .sum.bad { color: #c0392b; }
.paybox td input, .paybox td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
</style>
