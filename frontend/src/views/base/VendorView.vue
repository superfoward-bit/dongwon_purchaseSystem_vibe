<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '@/api/http'
import UserPicker from '@/components/UserPicker.vue'
import AttachPanel from '@/components/AttachPanel.vue'

interface Contact { contactNm?: string; position?: string; dept?: string; tel?: string; mobile?: string; email?: string; repYn?: string }
interface License { licTyp?: string; licTypNm?: string; licNm?: string; licNo?: string; issueOrg?: string; issueYmd?: string; expireYmd?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; lines: Aline[] }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Vendor {
  id?: number; vdCd?: string; erpVdCd?: string; vdNm?: string; vdNmEn?: string; bizNo?: string; corpNo?: string; ceoNm?: string;
  vdTyp?: string; vdTypNm?: string; bizCond?: string; bizItem?: string; addr?: string; addrEn?: string; zipCd?: string;
  tel?: string; fax?: string; email?: string; mobile?: string; homepage?: string; payCond?: string; payTermCd?: string; payMethodCd?: string; creditLimit?: number;
  indSectCd?: string; indSectNm?: string; tradFormCd?: string; tradFormNm?: string; vdSizeCd?: string; vdSizeNm?: string;
  listedYn?: string; fndDate?: string; currCd?: string; bankCd?: string; bankNm?: string; acctNo?: string; acctHolder?: string;
  taxBillTyp?: string; taxBillTypNm?: string; invVeriYn?: string; purcChrgId?: string; purcChrgNm?: string; tradStartYmd?: string;
  vdSts?: string; vdStsNm?: string; regSts?: string; regStsNm?: string; gradeCd?: string; gradeNm?: string; segCd?: string; segNm?: string; remark?: string;
  riskGrade?: string; nextEvalYmd?: string; stopYmd?: string; stopReason?: string; stopHis?: { action: string; reason: string; actorId: string; actionDt: string }[];
  contacts: Contact[]; licenses: License[]; actions?: Action[]; history?: His[]; approval?: Approval
}
interface Code { cd: string; cdNmKo: string }

const route = useRoute()
const list = ref<Vendor[]>([])
const keyword = ref('')
const vdSts = ref('')
const editing = ref<Vendor | null>(null)
const message = ref('')
const tab = ref('basic')
const codes = ref<Record<string, Code[]>>({})
const segments = ref<{ segCd: string; segNm: string }[]>([])
const showApproverModal = ref(false)
const showUserPicker = ref(false)
const pickTarget = ref<'chrg' | 'approver' | null>(null)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])

const editable = computed(() => !editing.value?.id || editing.value?.regSts === 'DRAFT')

async function load() {
  list.value = (await http.get('/base/vendor', { params: { keyword: keyword.value, vdSts: vdSts.value } })).data.data
}
async function loadCodes() {
  for (const g of ['VD_TYP', 'VD_STS', 'IND_SECT', 'TRAD_FORM', 'VD_SIZE', 'TAX_BILL_TYP', 'LIC_TYP', 'PAY_TERM', 'PAY_METHOD']) {
    codes.value[g] = (await http.get('/sys/code/codes', { params: { grpCd: g } })).data.data
  }
  segments.value = (await http.get('/srm/segment')).data.data
}
function onSegChange() {
  const g = segments.value.find(s => s.segCd === editing.value!.segCd)
  editing.value!.segNm = g ? g.segNm : undefined
}
function blank(): Vendor { return { vdTyp: 'MFR', currCd: 'KRW', listedYn: 'N', invVeriYn: 'N', contacts: [], licenses: [] } }
function addNew() { tab.value = 'basic'; editing.value = blank() }
async function edit(v: Vendor) {
  tab.value = 'basic'
  const d = (await http.get(`/base/vendor/${v.id}`)).data.data
  editing.value = { ...d, contacts: d.contacts || [], licenses: d.licenses || [] }
}
async function reload() { if (editing.value?.id) await edit({ id: editing.value.id }) }
function addContact() { editing.value!.contacts.push({ repYn: 'N' }) }
function delContact(i: number) { editing.value!.contacts.splice(i, 1) }
function addLicense() { editing.value!.licenses.push({}) }
function delLicense(i: number) { editing.value!.licenses.splice(i, 1) }

async function save() {
  if (!editing.value) return
  message.value = ''
  if (!editing.value.vdNm) { message.value = '상호를 입력하세요.'; return }
  try {
    if (editing.value.id) { await http.put(`/base/vendor/${editing.value.id}`, editing.value); await reload() }
    else { const { data } = await http.post('/base/vendor', editing.value); await edit({ id: data.data }) }
    message.value = '저장되었습니다. (신규 협력사는 심사요청 후 거래개시됩니다)'
    await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(v: Vendor) {
  if (!v.id || !confirm(`[${v.vdNm}] 삭제하시겠습니까?`)) return
  try { await http.delete(`/base/vendor/${v.id}`); message.value = '삭제되었습니다.'; editing.value = null; await load() }
  catch (e: any) { message.value = e.message }
}
function openSubmit() { approvers.value = []; showApproverModal.value = true }
function onUserSelect(u: { usrId: string; usrNm: string }) {
  if (pickTarget.value === 'chrg') { editing.value!.purcChrgId = u.usrId; editing.value!.purcChrgNm = u.usrNm }
  else if (pickTarget.value === 'approver') { if (!approvers.value.some(a => a.usrId === u.usrId)) approvers.value.push(u) }
  pickTarget.value = null; showUserPicker.value = false
}
function removeApprover(i: number) { approvers.value.splice(i, 1) }
async function doSubmit() {
  if (!approvers.value.length) { message.value = '심사 결재자를 1명 이상 지정하세요.'; return }
  try { await http.post(`/base/vendor/${editing.value!.id}/submit`, { approvers: approvers.value }); showApproverModal.value = false; await reload(); await load(); message.value = '심사요청되었습니다.' }
  catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/base/vendor/${editing.value!.id}/action`, { action: a.action, reason }); await reload(); await load(); message.value = `[${a.actionNm}] 처리되었습니다.` }
  catch (e: any) { message.value = e.message }
}
async function changeTrade(action: string) {
  const reason = window.prompt(`[${action === 'STOP' ? '거래중지' : '거래재개'}] 사유를 입력하세요`) || ''
  if (!reason) return
  try { await http.post(`/base/vendor/${editing.value!.id}/trade`, { action, reason }); await reload(); await load(); message.value = (action === 'STOP' ? '거래중지' : '거래재개') + ' 처리되었습니다.' }
  catch (e: any) { message.value = e.message }
}
function cd(g: string) { return codes.value[g] || [] }
onMounted(async () => { await loadCodes(); await load(); if (route.query.id) await edit({ id: Number(route.query.id) }) })
</script>

<template>
  <div class="page">
    <h2>협력사 관리</h2>
    <p v-if="message" class="msg">{{ message }}</p>

    <div v-if="!editing" class="panel">
      <div class="panel-head">
        협력사 목록
        <input class="search" v-model="keyword" placeholder="코드/상호/사업자번호" @keyup.enter="load" />
        <select v-model="vdSts" @change="load"><option value="">전체</option><option v-for="c in cd('VD_STS')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
        <button class="btn" @click="load">검색</button>
        <button class="btn primary" @click="addNew">+ 신규 협력사</button>
      </div>
      <table>
        <thead><tr><th>코드</th><th>상호</th><th>대표자</th><th>유형</th><th>사업자번호</th><th>등급</th><th>등록심사</th><th>거래상태</th></tr></thead>
        <tbody>
          <tr v-for="v in list" :key="v.id" class="row" @click="edit(v)">
            <td>{{ v.vdCd }}</td><td>{{ v.vdNm }}</td><td>{{ v.ceoNm }}</td><td>{{ v.vdTypNm }}</td><td>{{ v.bizNo }}</td>
            <td><span v-if="v.gradeNm" class="grade" :class="'g'+v.gradeCd">{{ v.gradeNm }}</span></td>
            <td><span class="badge" :class="v.regSts">{{ v.regStsNm }}</span></td>
            <td><span class="badge" :class="v.vdSts">{{ v.vdStsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="8" class="empty">협력사가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 편집/상세 -->
    <div v-else class="panel">
      <div class="panel-head">
        {{ editing.id ? '협력사 상세' : '신규 협력사 등록' }}
        <small v-if="editing.vdCd">({{ editing.vdCd }})</small>
        <span v-if="editing.regStsNm" class="badge" :class="editing.regSts">{{ editing.regStsNm }}</span>
        <span v-if="editing.vdStsNm" class="badge" :class="editing.vdSts">{{ editing.vdStsNm }}</span>
        <div class="ha">
          <button class="btn" @click="editing = null">목록</button>
          <button v-if="editable" class="btn primary" @click="save">저장</button>
          <button v-if="editing.id && editing.regSts === 'DRAFT'" class="btn go" @click="openSubmit">심사요청</button>
          <button v-if="editing.id && editing.regSts === 'DRAFT'" class="btn del" @click="remove(editing)">삭제</button>
          <button v-if="editing.id && editing.regSts === 'APPROVED' && editing.vdSts === 'ACTIVE'" class="btn warn" @click="changeTrade('STOP')">거래중지</button>
          <button v-if="editing.id && editing.vdSts === 'STOP'" class="btn go" @click="changeTrade('RESUME')">거래재개</button>
          <button v-for="a in editing.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : 'go'" @click="doAction(a)">{{ a.actionNm }}</button>
        </div>
      </div>

      <div v-if="editing.id" class="lifecycle">
        <span>리스크등급 <b :class="'rg-' + (editing.riskGrade || 'NONE')">{{ editing.riskGrade || '미평가' }}</b></span>
        <span>차기평가일 <b>{{ editing.nextEvalYmd || '-' }}</b></span>
        <span v-if="editing.vdSts === 'STOP'" class="stopmsg">거래중지({{ editing.stopYmd }}): {{ editing.stopReason }}</span>
        <span v-if="editing.stopHis && editing.stopHis.length" class="hislink" :title="editing.stopHis.map(h => `${h.actionDt} ${h.action} - ${h.reason}`).join('\n')">중지/재개 이력 {{ editing.stopHis.length }}건</span>
      </div>

      <div class="tabs">
        <button :class="{ on: tab === 'basic' }" @click="tab = 'basic'">기본정보</button>
        <button :class="{ on: tab === 'detail' }" @click="tab = 'detail'">상세·지급·계좌</button>
        <button :class="{ on: tab === 'contact' }" @click="tab = 'contact'">담당자 ({{ editing.contacts.length }})</button>
        <button :class="{ on: tab === 'license' }" @click="tab = 'license'">면허·인증 ({{ editing.licenses.length }})</button>
        <button v-if="editing.approval" :class="{ on: tab === 'aprv' }" @click="tab = 'aprv'">심사현황</button>
        <button v-if="editing.id" :class="{ on: tab === 'file' }" @click="tab = 'file'">첨부파일</button>
      </div>

      <!-- 기본정보 -->
      <div v-show="tab === 'basic'" class="form">
        <label class="w2">상호 <input v-model="editing.vdNm" :disabled="!editable" /></label>
        <label>영문상호 <input v-model="editing.vdNmEn" :disabled="!editable" /></label>
        <label>대표자 <input v-model="editing.ceoNm" :disabled="!editable" /></label>
        <label>사업자번호 <input v-model="editing.bizNo" :disabled="!editable" /></label>
        <label>법인번호 <input v-model="editing.corpNo" :disabled="!editable" /></label>
        <label>ERP(SAP)코드 <input v-model="editing.erpVdCd" :disabled="!editable" /></label>
        <label>협력사유형 <select v-model="editing.vdTyp" :disabled="!editable"><option v-for="c in cd('VD_TYP')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>산업부문 <select v-model="editing.indSectCd" :disabled="!editable"><option value="">선택</option><option v-for="c in cd('IND_SECT')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>거래형태 <select v-model="editing.tradFormCd" :disabled="!editable"><option value="">선택</option><option v-for="c in cd('TRAD_FORM')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>업태 <input v-model="editing.bizCond" :disabled="!editable" /></label>
        <label>종목 <input v-model="editing.bizItem" :disabled="!editable" /></label>
        <label>우편번호 <input v-model="editing.zipCd" :disabled="!editable" /></label>
        <label class="w3">주소 <input v-model="editing.addr" :disabled="!editable" /></label>
        <label>전화 <input v-model="editing.tel" :disabled="!editable" /></label>
        <label>휴대폰 <input v-model="editing.mobile" :disabled="!editable" /></label>
        <label>FAX <input v-model="editing.fax" :disabled="!editable" /></label>
        <label>이메일 <input v-model="editing.email" :disabled="!editable" /></label>
        <label>홈페이지 <input v-model="editing.homepage" :disabled="!editable" /></label>
      </div>

      <!-- 상세·지급·계좌 -->
      <div v-show="tab === 'detail'" class="form">
        <label>회사규모 <select v-model="editing.vdSizeCd" :disabled="!editable"><option value="">선택</option><option v-for="c in cd('VD_SIZE')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>상장여부 <select v-model="editing.listedYn" :disabled="!editable"><option value="N">비상장</option><option value="Y">상장</option></select></label>
        <label>설립일 <input type="date" v-model="editing.fndDate" :disabled="!editable" /></label>
        <label>거래시작일 <input type="date" v-model="editing.tradStartYmd" :disabled="!editable" /></label>
        <label>구매담당자 <div class="picker"><input :value="editing.purcChrgNm" readonly /><button v-if="editable" class="btn sm" @click="pickTarget='chrg'; showUserPicker=true">찾기</button></div></label>
        <label>세그먼트 <select v-model="editing.segCd" :disabled="!editable" @change="onSegChange"><option value="">미지정</option><option v-for="g in segments" :key="g.segCd" :value="g.segCd">{{ g.segNm }}</option></select></label>
        <div class="sep">지급 정보</div>
        <label>결제조건(주기) <select v-model="editing.payTermCd" :disabled="!editable"><option value="">선택</option><option v-for="c in cd('PAY_TERM')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>지급방법 <select v-model="editing.payMethodCd" :disabled="!editable"><option value="">선택</option><option v-for="c in cd('PAY_METHOD')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>결제조건(메모) <input v-model="editing.payCond" :disabled="!editable" /></label>
        <label>통화 <input v-model="editing.currCd" :disabled="!editable" /></label>
        <label>세금계산서 발행 <select v-model="editing.taxBillTyp" :disabled="!editable"><option value="">선택</option><option v-for="c in cd('TAX_BILL_TYP')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>송장검증 <select v-model="editing.invVeriYn" :disabled="!editable"><option value="N">미사용</option><option value="Y">사용</option></select></label>
        <label>여신한도(0=무제한) <input type="number" v-model.number="editing.creditLimit" :disabled="!editable" /></label>
        <div class="sep">계좌 정보</div>
        <label>은행코드 <input v-model="editing.bankCd" :disabled="!editable" /></label>
        <label>은행명 <input v-model="editing.bankNm" :disabled="!editable" /></label>
        <label>계좌번호 <input v-model="editing.acctNo" :disabled="!editable" /></label>
        <label>예금주 <input v-model="editing.acctHolder" :disabled="!editable" /></label>
        <label class="w3">비고 <input v-model="editing.remark" :disabled="!editable" /></label>
      </div>

      <!-- 담당자 -->
      <div v-show="tab === 'contact'" class="sub">
        <div class="sub-head"><button v-if="editable" class="btn sm" @click="addContact">+ 담당자 추가</button></div>
        <table>
          <thead><tr><th>이름</th><th>직위</th><th>부서</th><th>전화</th><th>휴대폰</th><th>이메일</th><th>대표</th><th v-if="editable"></th></tr></thead>
          <tbody>
            <tr v-for="(c, i) in editing.contacts" :key="i">
              <td><input v-model="c.contactNm" :disabled="!editable" /></td><td><input v-model="c.position" :disabled="!editable" /></td>
              <td><input v-model="c.dept" :disabled="!editable" /></td><td><input v-model="c.tel" :disabled="!editable" /></td>
              <td><input v-model="c.mobile" :disabled="!editable" /></td><td><input v-model="c.email" :disabled="!editable" /></td>
              <td><input type="checkbox" :checked="c.repYn === 'Y'" :disabled="!editable" @change="c.repYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /></td>
              <td v-if="editable"><button class="link del" @click="delContact(i)">삭제</button></td>
            </tr>
            <tr v-if="!editing.contacts.length"><td :colspan="editable ? 8 : 7" class="empty">담당자를 추가하세요.</td></tr>
          </tbody>
        </table>
      </div>

      <!-- 면허·인증 -->
      <div v-show="tab === 'license'" class="sub">
        <div class="sub-head"><button v-if="editable" class="btn sm" @click="addLicense">+ 면허/인증 추가</button></div>
        <table>
          <thead><tr><th>유형</th><th>명칭</th><th>번호</th><th>발급기관</th><th>발급일</th><th>만료일</th><th v-if="editable"></th></tr></thead>
          <tbody>
            <tr v-for="(l, i) in editing.licenses" :key="i">
              <td><select v-model="l.licTyp" :disabled="!editable"><option value="">선택</option><option v-for="c in cd('LIC_TYP')" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></td>
              <td><input v-model="l.licNm" :disabled="!editable" /></td><td><input v-model="l.licNo" :disabled="!editable" /></td>
              <td><input v-model="l.issueOrg" :disabled="!editable" /></td>
              <td><input type="date" v-model="l.issueYmd" :disabled="!editable" /></td><td><input type="date" v-model="l.expireYmd" :disabled="!editable" /></td>
              <td v-if="editable"><button class="link del" @click="delLicense(i)">삭제</button></td>
            </tr>
            <tr v-if="!editing.licenses.length"><td :colspan="editable ? 7 : 6" class="empty">면허/인증을 추가하세요.</td></tr>
          </tbody>
        </table>
      </div>

      <!-- 첨부파일 (사업자등록증/통장사본/인증서) -->
      <div v-show="tab === 'file'" class="sub" v-if="editing.id">
        <AttachPanel refTyp="VD" :refId="editing.id" />
      </div>

      <!-- 심사현황 -->
      <div v-show="tab === 'aprv'" class="sub" v-if="editing.approval">
        <div class="aline">
          <div v-for="(l, i) in editing.approval.lines" :key="i" class="astep" :class="l.lineSts">
            <div class="anum">{{ l.stepNo }}</div><div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
            <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 심사요청 모달 -->
    <div v-if="showApproverModal" class="modal-bg" @click.self="showApproverModal = false">
      <div class="modal">
        <div class="modal-head">등록심사 결재선 <button class="x" @click="showApproverModal = false">×</button></div>
        <div class="modal-body">
          <ol class="ap-list">
            <li v-for="(a, i) in approvers" :key="a.usrId"><span>{{ i + 1 }}. <b>{{ a.usrNm }}</b> ({{ a.usrId }})</span><button class="link del" @click="removeApprover(i)">제거</button></li>
            <li v-if="!approvers.length" class="empty">심사 결재자를 추가하세요.</li>
          </ol>
          <button class="btn" @click="pickTarget='approver'; showUserPicker=true">+ 결재자 추가</button>
        </div>
        <div class="modal-foot"><button class="btn primary" @click="doSubmit">심사요청</button><button class="btn" @click="showApproverModal = false">취소</button></div>
      </div>
    </div>
    <UserPicker v-if="showUserPicker" @select="onUserSelect" @close="showUserPicker = false; pickTarget = null" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.panel-head small { color: #888; font-weight: normal; } .ha { margin-left: auto; display: flex; gap: 6px; }
.search { padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; } .panel-head select { padding: 6px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.tabs { display: flex; gap: 2px; padding: 0 12px; border-bottom: 2px solid #eee; }
.tabs button { padding: 9px 16px; border: none; background: none; cursor: pointer; color: #888; border-bottom: 2px solid transparent; margin-bottom: -2px; font-size: 14px; }
.tabs button.on { color: #1a3a6b; font-weight: bold; border-bottom-color: #1a3a6b; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 200px; }
.form .w2 { flex: 1 1 260px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.sep { flex: 1 1 100%; border-top: 1px dashed #eee; padding-top: 8px; margin-top: 4px; font-weight: bold; color: #1a3a6b; font-size: 13px; }
.picker { display: flex; gap: 6px; } .picker input { flex: 1; padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.sub { padding: 12px 16px; } .sub-head { margin-bottom: 8px; }
.sub td input, .sub td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 9px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; } .link.del { color: #d33; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.ACTIVE { background: #e3f6e8; color: #1a7f37; } .badge.STOP { background: #f3e3e3; color: #a33; }
.badge.DRAFT { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.APPROVED { background: #e3f6e8; color: #1a7f37; } .badge.REJECTED { background: #f3e3e3; color: #a33; }
.grade { padding: 2px 8px; border-radius: 10px; font-weight: bold; font-size: 12px; }
.grade.gA { background: #d6f5e0; color: #0f7a35; } .grade.gB { background: #dbeafe; color: #1a4f9c; } .grade.gC { background: #fff3d6; color: #9a6b00; } .grade.gD { background: #fde2e2; color: #b3261e; }
.aline { display: flex; flex-wrap: wrap; gap: 8px; }
.astep { display: flex; align-items: center; gap: 8px; border: 1px solid #eee; border-radius: 8px; padding: 8px 12px; }
.astep.APPROVE { background: #f3faf5; border-color: #bfe6cc; } .astep.ING { background: #fff8ec; border-color: #f0d79a; } .astep.REJECT { background: #fdf0f0; border-color: #e6b8b8; }
.anum { width: 22px; height: 22px; border-radius: 50%; background: #1a3a6b; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.ainfo { display: flex; flex-direction: column; } .ainfo span { font-size: 12px; color: #888; } .aop { font-size: 12px; color: #777; font-style: italic; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 440px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 16px 18px; } .ap-list { margin: 0 0 12px; padding-left: 0; list-style: none; } .ap-list li { display: flex; justify-content: space-between; padding: 6px 0; }
.modal-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
.lifecycle { display: flex; gap: 18px; align-items: center; padding: 8px 16px; background: #f7f9fc; border-bottom: 1px solid #eee; font-size: 13px; color: #555; }
.lifecycle b { color: #1a3a6b; } .lifecycle b.rg-RISK { color: #c0392b; } .lifecycle b.rg-WATCH { color: #d98300; } .lifecycle b.rg-SAFE { color: #1a7f37; }
.lifecycle .stopmsg { color: #c0392b; font-weight: bold; } .lifecycle .hislink { margin-left: auto; color: #1a3a6b; cursor: help; text-decoration: underline dotted; }
</style>
