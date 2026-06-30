<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import UserPicker from '@/components/UserPicker.vue'
import AttachPanel from '@/components/AttachPanel.vue'

interface Term { termNm?: string; termContent?: string; remark?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; lines: Aline[] }
interface Bc {
  id?: number; bcNo?: string; bcRev?: number; bcTitle?: string; bcTyp?: string; bcTypNm?: string;
  vdCd?: string; vdNm?: string; chrgUsrId?: string; chrgUsrNm?: string; validSd?: string; validEd?: string;
  payCond?: string; delyCond?: string; currCd?: string; autoRenewYn?: string; noticeDays?: number; prevId?: number;
  sts?: string; stsNm?: string; remark?: string; terms: Term[]; actions?: Action[]; history?: His[]; approval?: Approval
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const bc = ref<Bc>({ bcTyp: 'BASIC', currCd: 'KRW', autoRenewYn: 'N', noticeDays: 30, validSd: today(), terms: [] })
const typeCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const showVendorPicker = ref(false)
const showUserPicker = ref(false)
const showApproverModal = ref(false)
const pickTarget = ref<'chrg' | 'approver' | null>(null)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])

function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || bc.value.sts === 'TMP')

async function load() {
  typeCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'BC_TYP' } })).data.data
  if (isNew.value) { addTerm(); return }
  await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/pro/bc/${id}`)
  bc.value = { ...data.data, terms: data.data.terms || [] }
}
function addTerm() { bc.value.terms.push({}) }
function delTerm(i: number) { bc.value.terms.splice(i, 1) }
function onVendorSelect(v: { vdCd: string; vdNm: string }) { bc.value.vdCd = v.vdCd; bc.value.vdNm = v.vdNm; showVendorPicker.value = false }
function onUserSelect(u: { usrId: string; usrNm: string }) {
  if (pickTarget.value === 'chrg') { bc.value.chrgUsrId = u.usrId; bc.value.chrgUsrNm = u.usrNm }
  else if (pickTarget.value === 'approver') { if (!approvers.value.some(a => a.usrId === u.usrId)) approvers.value.push(u) }
  pickTarget.value = null; showUserPicker.value = false
}
async function save() {
  message.value = ''
  if (!bc.value.bcTitle) { message.value = '계약 제목을 입력하세요.'; return }
  if (!bc.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/pro/bc', bc.value); router.replace(`/pro/bc/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.' }
    else { await http.put(`/pro/bc/${bc.value.id}`, bc.value); await reloadById(bc.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/pro/bc/${bc.value.id}/action`, { action: a.action, reason }); await reloadById(bc.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` } catch (e: any) { message.value = e.message }
}
async function revise() {
  if (!confirm('이 계약을 개정(다음 차수 작성본 생성)하시겠습니까?')) return
  try { const { data } = await http.post(`/pro/bc/${bc.value.id}/revise`, {}); router.push(`/pro/bc/${data.data}`); await reloadById(data.data); message.value = '개정본이 생성되었습니다. 내용 수정 후 상신하세요.' }
  catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/pro/bc/${bc.value.id}`); router.push('/pro/bc') }
function openSubmit() { approvers.value = []; showApproverModal.value = true }
function removeApprover(i: number) { approvers.value.splice(i, 1) }
async function doSubmit() {
  if (!approvers.value.length) { message.value = '결재자를 1명 이상 지정하세요.'; return }
  try { await http.post(`/pro/bc/${bc.value.id}/submit`, { approvers: approvers.value }); showApproverModal.value = false; await reloadById(bc.value.id!); message.value = '상신되었습니다.' }
  catch (e: any) { message.value = e.message }
}
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>표준거래계약 {{ isNew ? '등록' : '상세' }}
        <span v-if="bc.bcNo" class="prno">{{ bc.bcNo }} <b v-if="bc.bcRev">({{ bc.bcRev }}차 개정)</b></span>
        <span v-if="bc.stsNm" class="badge" :class="bc.sts">{{ bc.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/bc')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && bc.sts === 'TMP'" class="btn go" @click="openSubmit">상신</button>
        <button v-if="!isNew && bc.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-if="bc.sts === 'ACTIVE'" class="btn warn" @click="revise">개정</button>
        <button v-for="a in bc.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">기본정보</div>
      <div class="form">
        <label class="w2">계약 제목 <input v-model="bc.bcTitle" :disabled="!editable" /></label>
        <label>계약유형 <select v-model="bc.bcTyp" :disabled="!editable"><option v-for="c in typeCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label class="w2">협력사 <div class="picker"><input :value="bc.vdNm ? `${bc.vdNm} (${bc.vdCd})` : ''" readonly /><button v-if="editable" class="btn sm" @click="showVendorPicker = true">찾기</button></div></label>
        <label>담당자 <div class="picker"><input :value="bc.chrgUsrNm" readonly /><button v-if="editable" class="btn sm" @click="pickTarget='chrg'; showUserPicker=true">찾기</button></div></label>
        <label>유효 시작 <input type="date" v-model="bc.validSd" :disabled="!editable" /></label>
        <label>유효 종료 <input type="date" v-model="bc.validEd" :disabled="!editable" /></label>
        <label>통화 <input v-model="bc.currCd" :disabled="!editable" /></label>
        <label>자동갱신 <select v-model="bc.autoRenewYn" :disabled="!editable"><option value="N">미사용</option><option value="Y">자동갱신</option></select></label>
        <label>만료알림(일전) <input type="number" v-model.number="bc.noticeDays" :disabled="!editable" /></label>
        <label class="w2">결제조건 <input v-model="bc.payCond" :disabled="!editable" /></label>
        <label class="w2">납품조건 <input v-model="bc.delyCond" :disabled="!editable" /></label>
        <label class="w3">비고 <input v-model="bc.remark" :disabled="!editable" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">거래조건 조항 <button v-if="editable" class="btn sm" @click="addTerm">+ 조항추가</button></div>
      <table>
        <thead><tr><th style="width:200px">조항명</th><th>내용</th><th>비고</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(t, i) in bc.terms" :key="i">
            <td><input v-model="t.termNm" :disabled="!editable" /></td>
            <td><input v-model="t.termContent" :disabled="!editable" /></td>
            <td><input v-model="t.remark" :disabled="!editable" /></td>
            <td v-if="editable"><button class="link del" @click="delTerm(i)">삭제</button></td>
          </tr>
          <tr v-if="!bc.terms.length"><td :colspan="editable ? 4 : 3" class="empty">거래조건 조항을 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="!isNew && bc.id" class="panel">
      <div class="panel-head">첨부파일 (계약서 등)</div>
      <div style="padding:8px 12px"><AttachPanel refTyp="BC" :refId="bc.id" :readonly="bc.sts !== 'TMP'" /></div>
    </div>

    <div v-if="bc.approval" class="panel">
      <div class="panel-head">결재 현황 <span class="ap-no">{{ bc.approval.aprvNo }}</span><span class="badge ap" :class="bc.approval.aprvStsNm">{{ bc.approval.aprvStsNm }}</span></div>
      <div class="aline">
        <div v-for="(l, i) in bc.approval.lines" :key="i" class="astep" :class="l.lineSts">
          <div class="anum">{{ l.stepNo }}</div><div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
          <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
        </div>
      </div>
    </div>

    <div v-if="bc.history && bc.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in bc.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>

    <VendorPicker v-if="showVendorPicker" @select="onVendorSelect" @close="showVendorPicker = false" />
    <UserPicker v-if="showUserPicker" @select="onUserSelect" @close="showUserPicker = false; pickTarget = null" />
    <div v-if="showApproverModal" class="modal-bg" @click.self="showApproverModal = false">
      <div class="modal">
        <div class="modal-head">결재선 지정 <button class="x" @click="showApproverModal = false">×</button></div>
        <div class="modal-body">
          <ol class="ap-list">
            <li v-for="(a, i) in approvers" :key="a.usrId"><span>{{ i + 1 }}. <b>{{ a.usrNm }}</b> ({{ a.usrId }})</span><button class="link del" @click="removeApprover(i)">제거</button></li>
            <li v-if="!approvers.length" class="empty">결재자를 추가하세요.</li>
          </ol>
          <button class="btn" @click="pickTarget='approver'; showUserPicker=true">+ 결재자 추가</button>
        </div>
        <div class="modal-foot"><button class="btn primary" @click="doSubmit">상신</button><button class="btn" @click="showApproverModal = false">취소</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.prno { font-size: 14px; color: #888; font-weight: normal; } .prno b { color: #6b3fa0; } .head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 200px; } .form .w2 { flex: 1 1 300px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .picker { display: flex; gap: 6px; } .picker input { flex: 1; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
td input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #efe7f8; color: #6b3fa0; border-color: #d3c2ee; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.ACTIVE { background: #e3f6e8; color: #1a7f37; } .badge.EXPIRE { background: #eee; color: #777; } .badge.TERMINATE { background: #f3e3e3; color: #a33; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
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
