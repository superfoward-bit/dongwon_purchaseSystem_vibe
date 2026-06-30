<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import UserPicker from '@/components/UserPicker.vue'

interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; lines: Aline[] }
interface Pa {
  id?: number; adjNo?: string; adjTyp?: string; adjTypNm?: string; vdCd?: string; vdNm?: string; grNo?: string; adjYm?: string;
  taxTyp?: string; suplAdjAmt?: number; vatAdjAmt?: number; totAdjAmt?: number; reason?: string; sts?: string; stsNm?: string; remark?: string;
  actions?: Action[]; history?: His[]; approval?: Approval
}
const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const pa = ref<Pa>({ adjTyp: 'PRICE', adjYm: thisMonth(), taxTyp: 'TAX', suplAdjAmt: 0 })
const typeCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const showVendorPicker = ref(false)
const showApproverModal = ref(false)
const showUserPicker = ref(false)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])

function thisMonth() { return new Date().toISOString().slice(0, 7).replace('-', '') }
const editable = computed(() => isNew.value || pa.value.sts === 'TMP')
const liveVat = computed(() => (!pa.value.taxTyp || pa.value.taxTyp === 'TAX') ? Math.round((Number(pa.value.suplAdjAmt) || 0) * 0.1) : 0)
const liveTot = computed(() => (Number(pa.value.suplAdjAmt) || 0) + liveVat.value)

async function load() {
  typeCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'PA_ADJ_TYP' } })).data.data
  if (!isNew.value) await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) { pa.value = (await http.get(`/pro/pa/${id}`)).data.data }
function onVendorSelect(v: { vdCd: string; vdNm: string }) { pa.value.vdCd = v.vdCd; pa.value.vdNm = v.vdNm; showVendorPicker.value = false }
async function save() {
  message.value = ''
  if (!pa.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  if (!pa.value.adjYm || pa.value.adjYm.length !== 6) { message.value = '반영 정산월(YYYYMM)을 입력하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/pro/pa', pa.value); router.replace(`/pro/pa/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.' }
    else { await http.put(`/pro/pa/${pa.value.id}`, pa.value); await reloadById(pa.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/pro/pa/${pa.value.id}/action`, { action: a.action, reason }); await reloadById(pa.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` } catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/pro/pa/${pa.value.id}`); router.push('/pro/pa') }
function openSubmit() { approvers.value = []; showApproverModal.value = true }
function onUserSelect(u: { usrId: string; usrNm: string }) { if (!approvers.value.some(a => a.usrId === u.usrId)) approvers.value.push(u); showUserPicker.value = false }
function removeApprover(i: number) { approvers.value.splice(i, 1) }
async function doSubmit() {
  if (!approvers.value.length) { message.value = '결재자를 1명 이상 지정하세요.'; return }
  try { await http.post(`/pro/pa/${pa.value.id}/submit`, { approvers: approvers.value }); showApproverModal.value = false; await reloadById(pa.value.id!); message.value = '상신되었습니다.' }
  catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>매입조정 {{ isNew ? '등록' : '상세' }}
        <span v-if="pa.adjNo" class="prno">{{ pa.adjNo }}</span>
        <span v-if="pa.stsNm" class="badge" :class="pa.sts">{{ pa.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/pa')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && pa.sts === 'TMP'" class="btn go" @click="openSubmit">상신</button>
        <button v-if="!isNew && pa.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in pa.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">매입조정 정보 <span class="hint">확정(CFM) 시 해당 정산월 협력사 정산에 자동 반영됩니다.</span></div>
      <div class="form">
        <label>조정유형 <select v-model="pa.adjTyp" :disabled="!editable"><option v-for="c in typeCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>과세구분 <select v-model="pa.taxTyp" :disabled="!editable"><option value="TAX">과세</option><option value="FREE">면세</option><option value="ZERO">영세율</option><option value="NOND">불공제</option></select></label>
        <label class="w2">협력사 <div class="picker"><input :value="pa.vdNm ? `${pa.vdNm} (${pa.vdCd})` : ''" readonly /><button v-if="editable" class="btn sm" @click="showVendorPicker = true">찾기</button></div></label>
        <label>반영 정산월 <input v-model="pa.adjYm" :disabled="!editable" placeholder="202606" /></label>
        <label>대상 입고번호 <input v-model="pa.grNo" :disabled="!editable" placeholder="선택" /></label>
        <label>공급가 조정액(±) <input type="number" v-model.number="pa.suplAdjAmt" :disabled="!editable" /></label>
        <label>부가세(자동) <input :value="fmt(isNew || pa.sts==='TMP' ? liveVat : pa.vatAdjAmt)" disabled /></label>
        <label>합계 조정액(자동) <input :value="fmt(isNew || pa.sts==='TMP' ? liveTot : pa.totAdjAmt)" disabled class="amt" /></label>
        <label class="w3">사유 <input v-model="pa.reason" :disabled="!editable" /></label>
      </div>
    </div>

    <div v-if="pa.approval" class="panel">
      <div class="panel-head">결재 현황 <span class="ap-no">{{ pa.approval.aprvNo }}</span><span class="badge ap" :class="pa.approval.aprvStsNm">{{ pa.approval.aprvStsNm }}</span></div>
      <div class="aline">
        <div v-for="(l, i) in pa.approval.lines" :key="i" class="astep" :class="l.lineSts">
          <div class="anum">{{ l.stepNo }}</div><div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
          <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
        </div>
      </div>
    </div>

    <div v-if="pa.history && pa.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in pa.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>

    <VendorPicker v-if="showVendorPicker" @select="onVendorSelect" @close="showVendorPicker = false" />
    <UserPicker v-if="showUserPicker" @select="onUserSelect" @close="showUserPicker = false" />
    <div v-if="showApproverModal" class="modal-bg" @click.self="showApproverModal = false">
      <div class="modal">
        <div class="modal-head">결재선 지정 <button class="x" @click="showApproverModal = false">×</button></div>
        <div class="modal-body">
          <ol class="ap-list">
            <li v-for="(a, i) in approvers" :key="a.usrId"><span>{{ i + 1 }}. <b>{{ a.usrNm }}</b> ({{ a.usrId }})</span><button class="link del" @click="removeApprover(i)">제거</button></li>
            <li v-if="!approvers.length" class="empty">결재자를 추가하세요.</li>
          </ol>
          <button class="btn" @click="showUserPicker = true">+ 결재자 추가</button>
        </div>
        <div class="modal-foot"><button class="btn primary" @click="doSubmit">상신</button><button class="btn" @click="showApproverModal = false">취소</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.prno { font-size: 14px; color: #888; font-weight: normal; } .head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; } .hint { margin-left: auto; color: #999; font-weight: normal; font-size: 12px; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 200px; } .form .w2 { flex: 1 1 300px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .form .amt { font-weight: bold; color: #1a3a6b; } .picker { display: flex; gap: 6px; } .picker input { flex: 1; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.CFM { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.ap-no { font-size: 13px; color: #888; font-weight: normal; } .badge.ap { margin-left: 6px; } .badge.결재중 { background: #fff3d6; color: #9a6b00; } .badge.결재완료 { background: #e3f6e8; color: #1a7f37; } .badge.반려 { background: #f3e3e3; color: #a33; }
.aline { padding: 18px 16px; display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.astep { display: flex; align-items: center; gap: 8px; border: 1px solid #eee; border-radius: 8px; padding: 8px 12px; }
.astep.APPROVE { background: #f3faf5; border-color: #bfe6cc; } .astep.ING { background: #fff8ec; border-color: #f0d79a; } .astep.REJECT { background: #fdf0f0; border-color: #e6b8b8; }
.anum { width: 22px; height: 22px; border-radius: 50%; background: #1a3a6b; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.ainfo { display: flex; flex-direction: column; } .ainfo span { font-size: 12px; color: #888; } .aop { font-size: 12px; color: #777; font-style: italic; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 440px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 16px 18px; } .ap-list { margin: 0 0 12px; padding-left: 0; list-style: none; } .ap-list li { display: flex; justify-content: space-between; padding: 6px 0; }
.modal-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
</style>
