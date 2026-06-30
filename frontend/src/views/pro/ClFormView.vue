<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import UserPicker from '@/components/UserPicker.vue'

interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; lines: Aline[] }
interface Gr { grId?: number; grNo?: string; grYmd?: string; poNo?: string; suplAmt?: number; vatAmt?: number; amt?: number }
interface Adj { adjTyp?: string; adjTypNm?: string; calcBase?: string; baseAmt?: number; rate?: number; unitAmt?: number; signTyp?: string; adjAmt?: number }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Vendor { vdCd: string; vdNm: string }
interface Cl {
  id?: number; closeNo?: string; closeYm?: string; vdCd?: string; vdNm?: string;
  totSuplAmt?: number; totVatAmt?: number; totAmt?: number; adjAmt?: number; netAmt?: number; grCnt?: number;
  payTermNm?: string; payMethodNm?: string; payDueYmd?: string; sts?: string; stsNm?: string;
  grs: Gr[]; adjs?: Adj[]; actions?: Action[]; history?: His[]; approval?: Approval
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const cl = ref<Cl>({ grs: [] })
const vendors = ref<Vendor[]>([])
const selVd = ref('')
const monthVal = ref(thisMonth())     // YYYY-MM
const eligible = ref<Gr[]>([])
const searched = ref(false)
const showApproverModal = ref(false)
const showUserPicker = ref(false)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])

function thisMonth() { return new Date().toISOString().slice(0, 7) }
function ymKey() { return monthVal.value.replace('-', '') }
const eligSupl = computed(() => eligible.value.reduce((s, g) => s + (g.suplAmt || 0), 0))
const eligVat = computed(() => eligible.value.reduce((s, g) => s + (g.vatAmt || 0), 0))
const eligTot = computed(() => eligible.value.reduce((s, g) => s + (g.amt || 0), 0))

async function load() {
  if (isNew.value) { vendors.value = (await http.get('/base/vendor', { params: { vdSts: 'ACTIVE' } })).data.data; return }
  await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/pro/cl/${id}`)
  cl.value = { ...data.data, grs: data.data.grs || [] }
}
async function searchEligible() {
  if (!selVd.value) { message.value = '협력사를 선택하세요.'; return }
  message.value = ''
  eligible.value = (await http.get('/pro/cl/eligible', { params: { vdCd: selVd.value, closeYm: ymKey() } })).data.data
  searched.value = true
}
async function createClose() {
  if (!eligible.value.length) { message.value = '마감대상이 없습니다.'; return }
  const vd = vendors.value.find(v => v.vdCd === selVd.value)
  try {
    const { data } = await http.post('/pro/cl', { vdCd: selVd.value, vdNm: vd?.vdNm, closeYm: ymKey() })
    router.replace(`/pro/cl/${data.data}`)
    await reloadById(data.data)
    message.value = '정산마감이 생성되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return }
  else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try {
    await http.post(`/pro/cl/${cl.value.id}/action`, { action: a.action, reason })
    await reloadById(cl.value.id!)
    message.value = `[${a.actionNm}] 처리되었습니다.`
  } catch (e: any) { message.value = e.message }
}
async function remove() {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/pro/cl/${cl.value.id}`)
  router.push('/pro/cl')
}
async function recalcGap() {
  try {
    await http.post(`/pro/cl/${cl.value.id}/recalc-gap`, {})
    await reloadById(cl.value.id!)
    message.value = '갭정산이 재계산되었습니다.'
  } catch (e: any) { message.value = e.message }
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
    await http.post(`/pro/cl/${cl.value.id}/submit`, { approvers: approvers.value })
    showApproverModal.value = false; await reloadById(cl.value.id!); message.value = '상신되었습니다. (결재 진행)'
  } catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>정산/마감 {{ isNew ? '생성' : '상세' }}
        <span v-if="cl.closeNo" class="prno">{{ cl.closeNo }}</span>
        <span v-if="cl.stsNm" class="badge" :class="cl.sts">{{ cl.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/cl')">목록</button>
        <button v-if="!isNew && cl.sts === 'TMP'" class="btn go" @click="openSubmit">상신</button>
        <button v-if="!isNew && cl.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in cl.actions" :key="a.action"
                class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')"
                @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <!-- 신규: 대상 조회 -->
    <template v-if="isNew">
      <div class="panel">
        <div class="panel-head">마감 조건</div>
        <div class="form">
          <label>협력사
            <select v-model="selVd"><option value="">선택</option><option v-for="v in vendors" :key="v.vdCd" :value="v.vdCd">{{ v.vdNm }}</option></select>
          </label>
          <label>마감월 <input type="month" v-model="monthVal" /></label>
          <button class="btn primary" @click="searchEligible">대상 조회</button>
          <button v-if="eligible.length" class="btn go" @click="createClose">정산마감 생성</button>
        </div>
      </div>
      <div v-if="searched" class="panel">
        <div class="panel-head">마감대상 입고확정 건
          <span class="tot">공급가 {{ fmt(eligSupl) }} · 부가세 {{ fmt(eligVat) }} · <b>합계 {{ fmt(eligTot) }}</b></span>
        </div>
        <table>
          <thead><tr><th>입고번호</th><th>발주번호</th><th>입고일</th><th class="r">공급가</th><th class="r">부가세</th><th class="r">합계</th></tr></thead>
          <tbody>
            <tr v-for="g in eligible" :key="g.grId">
              <td>{{ g.grNo }}</td><td>{{ g.poNo }}</td><td>{{ g.grYmd }}</td>
              <td class="r">{{ fmt(g.suplAmt) }}</td><td class="r">{{ fmt(g.vatAmt) }}</td><td class="r">{{ fmt(g.amt) }}</td>
            </tr>
            <tr v-if="!eligible.length"><td colspan="6" class="empty">마감대상(입고확정 미마감) 건이 없습니다.</td></tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- 상세 -->
    <template v-else>
      <div class="panel">
        <div class="panel-head">정산 정보</div>
        <div class="info">
          <div><span>협력사</span><b>{{ cl.vdNm }}</b></div>
          <div><span>마감월</span><b>{{ cl.closeYm }}</b></div>
          <div><span>입고건수</span><b>{{ cl.grCnt }}</b></div>
          <div><span>공급가</span><b>{{ fmt(cl.totSuplAmt) }}</b></div>
          <div><span>부가세</span><b>{{ fmt(cl.totVatAmt) }}</b></div>
          <div><span>기초정산액</span><b>{{ fmt(cl.totAmt) }}</b></div>
          <div><span>조정합계</span><b :class="{ minus: (cl.adjAmt || 0) < 0 }">{{ fmt(cl.adjAmt) }}</b></div>
          <div><span>최종정산액</span><b class="amt">{{ fmt(cl.netAmt) }}</b></div>
          <div><span>결제조건</span><b>{{ cl.payTermNm || '-' }}</b></div>
          <div><span>지급방법</span><b>{{ cl.payMethodNm || '-' }}</b></div>
          <div><span>지급예정일</span><b>{{ cl.payDueYmd || '-' }}</b></div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">갭정산 조정명세 (자동집계)
          <button v-if="cl.sts === 'TMP'" class="btn sm" @click="recalcGap">재계산</button>
          <span class="tot">조정합계 <b :class="{ minus: (cl.adjAmt || 0) < 0 }">{{ fmt(cl.adjAmt) }}</b></span>
        </div>
        <table>
          <thead><tr><th>조정유형</th><th>계산기준</th><th class="r">기준값</th><th class="r">요율</th><th class="r">건당/정액</th><th>부호</th><th class="r">조정금액</th></tr></thead>
          <tbody>
            <tr v-for="(a, i) in cl.adjs" :key="i">
              <td>{{ a.adjTypNm }}</td>
              <td>{{ a.calcBase }}</td>
              <td class="r">{{ fmt(a.baseAmt) }}</td>
              <td class="r">{{ a.rate ? (a.rate * 100).toFixed(2) + '%' : '-' }}</td>
              <td class="r">{{ a.unitAmt ? fmt(a.unitAmt) : '-' }}</td>
              <td>{{ a.signTyp === '-' ? '차감' : '가산' }}</td>
              <td class="r" :class="{ minus: (a.adjAmt || 0) < 0 }">{{ fmt(a.adjAmt) }}</td>
            </tr>
            <tr v-if="!cl.adjs || !cl.adjs.length"><td colspan="7" class="empty">조정 항목이 없습니다. (조정율 미설정)</td></tr>
          </tbody>
        </table>
      </div>

      <div class="panel">
        <div class="panel-head">마감대상 입고</div>
        <table>
          <thead><tr><th>입고번호</th><th>발주번호</th><th>입고일</th><th class="r">공급가</th><th class="r">부가세</th><th class="r">합계</th></tr></thead>
          <tbody>
            <tr v-for="g in cl.grs" :key="g.id">
              <td>{{ g.grNo }}</td><td>{{ g.poNo }}</td><td>{{ g.grYmd }}</td>
              <td class="r">{{ fmt(g.suplAmt) }}</td><td class="r">{{ fmt(g.vatAmt) }}</td><td class="r">{{ fmt(g.amt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="cl.approval" class="panel">
        <div class="panel-head">결재 현황 <span class="ap-no">{{ cl.approval.aprvNo }}</span>
          <span class="badge ap" :class="cl.approval.aprvStsNm">{{ cl.approval.aprvStsNm }}</span>
        </div>
        <div class="aline">
          <div v-for="(l, i) in cl.approval.lines" :key="i" class="astep" :class="l.lineSts">
            <div class="anum">{{ l.stepNo }}</div><div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
            <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
            <div v-if="i < cl.approval.lines.length - 1" class="aarrow">→</div>
          </div>
        </div>
      </div>
      <div v-if="cl.history && cl.history.length" class="panel">
        <div class="panel-head">진행이력</div>
        <table>
          <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
          <tbody>
            <tr v-for="h in cl.history" :key="h.seq">
              <td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td>
              <td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

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
.head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; }
.tot { margin-left: auto; color: #1a3a6b; font-weight: normal; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form select, .form input { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.info { padding: 16px; display: flex; flex-wrap: wrap; gap: 24px; }
.info div { display: flex; flex-direction: column; gap: 4px; } .info span { color: #888; font-size: 13px; }
.info .amt { color: #1a3a6b; font-size: 18px; }
.minus { color: #c0392b; }
.btn.sm { padding: 4px 10px; font-size: 13px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; }
.btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.CFM { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
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
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
</style>
