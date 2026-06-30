<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import UserPicker from '@/components/UserPicker.vue'

interface Repay { repayAmt?: number; repayYmd?: string; closeNo?: string; remark?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; lines: Aline[] }
interface Av {
  id?: number; advNo?: string; vdCd?: string; vdNm?: string; advAmt?: number; payYmd?: string; purpose?: string;
  balance?: number; repaidAmt?: number; sts?: string; stsNm?: string; remark?: string;
  repays: Repay[]; actions?: Action[]; history?: His[]; approval?: Approval
}
const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const av = ref<Av>({ payYmd: today(), advAmt: 0, repays: [] })
const showVendorPicker = ref(false)
const showApproverModal = ref(false)
const showUserPicker = ref(false)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])
const newRepay = ref<Repay>({ repayYmd: today() })

function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || av.value.sts === 'TMP')
const canRepay = computed(() => av.value.sts === 'CFM')

async function load() { if (!isNew.value) await reloadById(route.params.id as string) }
async function reloadById(id: string | number) {
  const { data } = await http.get(`/pro/av/${id}`)
  av.value = { ...data.data, repays: data.data.repays || [] }
}
function onVendorSelect(v: { vdCd: string; vdNm: string }) { av.value.vdCd = v.vdCd; av.value.vdNm = v.vdNm; showVendorPicker.value = false }
async function save() {
  message.value = ''
  if (!av.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  if (!av.value.advAmt || av.value.advAmt <= 0) { message.value = '선급금액을 입력하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/pro/av', av.value); router.replace(`/pro/av/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.' }
    else { await http.put(`/pro/av/${av.value.id}`, av.value); await reloadById(av.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/pro/av/${av.value.id}/action`, { action: a.action, reason }); await reloadById(av.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` } catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/pro/av/${av.value.id}`); router.push('/pro/av') }
async function addRepay() {
  if (!newRepay.value.repayAmt || newRepay.value.repayAmt <= 0) { message.value = '반제금액을 입력하세요.'; return }
  try { await http.post(`/pro/av/${av.value.id}/repay`, newRepay.value); newRepay.value = { repayYmd: today() }; await reloadById(av.value.id!); message.value = '반제 처리되었습니다.' }
  catch (e: any) { message.value = e.message }
}
function openSubmit() { approvers.value = []; showApproverModal.value = true }
function onUserSelect(u: { usrId: string; usrNm: string }) { if (!approvers.value.some(a => a.usrId === u.usrId)) approvers.value.push(u); showUserPicker.value = false }
function removeApprover(i: number) { approvers.value.splice(i, 1) }
async function doSubmit() {
  if (!approvers.value.length) { message.value = '결재자를 1명 이상 지정하세요.'; return }
  try { await http.post(`/pro/av/${av.value.id}/submit`, { approvers: approvers.value }); showApproverModal.value = false; await reloadById(av.value.id!); message.value = '상신되었습니다.' }
  catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>선급금 {{ isNew ? '등록' : '상세' }}
        <span v-if="av.advNo" class="prno">{{ av.advNo }}</span>
        <span v-if="av.stsNm" class="badge" :class="av.sts">{{ av.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/av')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && av.sts === 'TMP'" class="btn go" @click="openSubmit">상신</button>
        <button v-if="!isNew && av.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in av.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">선급금 정보</div>
      <div class="form">
        <label class="w2">협력사 <div class="picker"><input :value="av.vdNm ? `${av.vdNm} (${av.vdCd})` : ''" readonly /><button v-if="editable" class="btn sm" @click="showVendorPicker = true">찾기</button></div></label>
        <label>선급금액 <input type="number" v-model.number="av.advAmt" :disabled="!editable" /></label>
        <label>지급예정일 <input type="date" v-model="av.payYmd" :disabled="!editable" /></label>
        <label v-if="!isNew">미반제 잔액 <input :value="fmt(av.balance)" disabled class="amt" /></label>
        <label class="w2">용도 <input v-model="av.purpose" :disabled="!editable" /></label>
        <label class="w3">비고 <input v-model="av.remark" :disabled="!editable" /></label>
      </div>
    </div>

    <!-- 반제 -->
    <div v-if="!isNew && (av.sts === 'CFM' || av.sts === 'CLOSED')" class="panel">
      <div class="panel-head">반제(상계) 내역
        <span class="tot">선급 {{ fmt(av.advAmt) }} · 반제 {{ fmt(av.repaidAmt) }} · <b :class="{ zero: av.balance===0 }">잔액 {{ fmt(av.balance) }}</b></span>
      </div>
      <table>
        <thead><tr><th>#</th><th class="r">반제금액</th><th>반제일</th><th>연계 정산</th><th>비고</th></tr></thead>
        <tbody>
          <tr v-for="(r, i) in av.repays" :key="i"><td>{{ i + 1 }}</td><td class="r">{{ fmt(r.repayAmt) }}</td><td>{{ r.repayYmd }}</td><td>{{ r.closeNo }}</td><td>{{ r.remark }}</td></tr>
          <tr v-if="!av.repays.length"><td colspan="5" class="empty">반제 내역이 없습니다.</td></tr>
        </tbody>
      </table>
      <div v-if="canRepay" class="repay-add">
        <input type="number" v-model.number="newRepay.repayAmt" placeholder="반제금액" />
        <input type="date" v-model="newRepay.repayYmd" />
        <input v-model="newRepay.closeNo" placeholder="정산번호(선택)" />
        <input v-model="newRepay.remark" placeholder="비고" />
        <button class="btn go" @click="addRepay">+ 반제 추가</button>
      </div>
    </div>

    <div v-if="av.approval" class="panel">
      <div class="panel-head">결재 현황 <span class="ap-no">{{ av.approval.aprvNo }}</span><span class="badge ap" :class="av.approval.aprvStsNm">{{ av.approval.aprvStsNm }}</span></div>
      <div class="aline">
        <div v-for="(l, i) in av.approval.lines" :key="i" class="astep" :class="l.lineSts">
          <div class="anum">{{ l.stepNo }}</div><div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
          <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
        </div>
      </div>
    </div>

    <div v-if="av.history && av.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in av.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
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
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; } .tot { margin-left: auto; color: #1a3a6b; font-weight: normal; } .tot .zero { color: #1a7f37; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 200px; } .form .w2 { flex: 1 1 300px; } .form .w3 { flex: 1 1 100%; }
.form input { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .form .amt { font-weight: bold; color: #1a3a6b; } .picker { display: flex; gap: 6px; } .picker input { flex: 1; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.empty { text-align: center; color: #aaa; padding: 18px; }
.repay-add { padding: 12px 16px; display: flex; gap: 8px; align-items: center; border-top: 1px dashed #eee; } .repay-add input { padding: 7px; border: 1px solid #ddd; border-radius: 5px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.CFM { background: #dbeafe; color: #1a4f9c; } .badge.CLOSED { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
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
