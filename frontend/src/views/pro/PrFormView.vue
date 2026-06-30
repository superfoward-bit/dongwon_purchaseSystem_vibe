<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import ItemPicker from '@/components/ItemPicker.vue'
import UserPicker from '@/components/UserPicker.vue'
import AttachPanel from '@/components/AttachPanel.vue'

interface Item { itemCd?: string; itemNm?: string; spec?: string; unitCd?: string; qty?: number; estPrc?: number; remark?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Pr {
  id?: number; prNo?: string; prTitle?: string; prTyp?: string; reqYmd?: string; hopeDlvYmd?: string;
  currCd?: string; budgetCd?: string; budgetNm?: string; totAmt?: number; sts?: string; stsNm?: string; remark?: string;
  items: Item[]; actions?: Action[]; history?: His[]; approval?: Approval
}
interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; aprvDt?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; curStepNo?: number; lines: Aline[] }

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const pr = ref<Pr>({ prTyp: 'PRODUCT', currCd: 'KRW', reqYmd: today(), items: [] })
const itemPickerRow = ref<number | null>(null)
const showApproverModal = ref(false)
const showUserPicker = ref(false)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])
const budgets = ref<{ budgetCd: string; budgetNm: string; availableAmt: number }[]>([])

function today() { return new Date().toISOString().slice(0, 10) }
const selBudget = computed(() => budgets.value.find(b => b.budgetCd === pr.value.budgetCd))
const editable = computed(() => isNew.value || pr.value.sts === 'TMP')
const totAmt = computed(() => pr.value.items.reduce((s, it) => s + (Number(it.qty) || 0) * (Number(it.estPrc) || 0), 0))

async function load() {
  if (isNew.value) { addRow(); return }
  await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/pro/pr/${id}`)
  pr.value = { ...data.data, items: data.data.items || [] }
}
function addRow() { pr.value.items.push({ qty: 1, estPrc: 0 }) }
function delRow(i: number) { pr.value.items.splice(i, 1) }
function onItemSelect(it: { itemCd: string; itemNm: string; spec?: string; unitCd?: string }) {
  if (itemPickerRow.value !== null) {
    const row = pr.value.items[itemPickerRow.value]
    row.itemCd = it.itemCd; row.itemNm = it.itemNm; row.spec = it.spec; row.unitCd = it.unitCd
  }
  itemPickerRow.value = null
}

async function save() {
  message.value = ''
  try {
    if (isNew.value) {
      const { data } = await http.post('/pro/pr', pr.value)
      router.replace(`/pro/pr/${data.data}`)
      await reloadById(data.data)
      message.value = '등록되었습니다.'
    } else {
      await http.put(`/pro/pr/${pr.value.id}`, pr.value)
      await reloadById(pr.value.id!)
      message.value = '수정되었습니다.'
    }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return }
  else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try {
    await http.post(`/pro/pr/${pr.value.id}/action`, { action: a.action, reason })
    await reloadById(pr.value.id!)
    message.value = `[${a.actionNm}] 처리되었습니다.`
  } catch (e: any) { message.value = e.message }
}
async function remove() {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/pro/pr/${pr.value.id}`)
  router.push('/pro/pr')
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
    await http.post(`/pro/pr/${pr.value.id}/submit`, { approvers: approvers.value })
    showApproverModal.value = false
    await reloadById(pr.value.id!)
    message.value = '상신되었습니다. (결재 진행)'
  } catch (e: any) { message.value = e.message }
}
function goToPo() { router.push(`/pro/po/new?prId=${pr.value.id}`) }
function goToRx() { router.push(`/pro/rx/new?prId=${pr.value.id}`) }
function goToAu() { router.push(`/pro/au/new?prId=${pr.value.id}`) }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(async () => { budgets.value = (await http.get('/base/budget')).data.data; await load() })
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>구매요청 {{ isNew ? '등록' : '상세' }}
        <span v-if="pr.prNo" class="prno">{{ pr.prNo }}</span>
        <span v-if="pr.stsNm" class="badge" :class="pr.sts">{{ pr.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/pr')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && pr.sts === 'TMP'" class="btn go" @click="openSubmit">상신</button>
        <button v-if="!isNew && pr.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-if="pr.sts === 'APRV'" class="btn go" @click="goToRx">견적 요청</button>
        <button v-if="pr.sts === 'APRV'" class="btn go" @click="goToAu">역경매</button>
        <button v-if="pr.sts === 'APRV'" class="btn go" @click="goToPo">발주 생성</button>
        <button v-for="a in pr.actions" :key="a.action"
                class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')"
                @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">기본정보</div>
      <div class="form">
        <label class="w2">제목 <input v-model="pr.prTitle" :disabled="!editable" /></label>
        <label>유형
          <select v-model="pr.prTyp" :disabled="!editable"><option value="PRODUCT">물품</option><option value="SERVICE">용역</option></select>
        </label>
        <label>요청일 <input type="date" v-model="pr.reqYmd" :disabled="!editable" /></label>
        <label>희망납기 <input type="date" v-model="pr.hopeDlvYmd" :disabled="!editable" /></label>
        <label>예산
          <select v-model="pr.budgetCd" :disabled="!editable"><option value="">미지정</option><option v-for="b in budgets" :key="b.budgetCd" :value="b.budgetCd">{{ b.budgetNm }}</option></select>
        </label>
        <label v-if="selBudget" class="budhint">예산잔액 <b :class="{ over: (selBudget.availableAmt || 0) < (pr.totAmt || 0) }">{{ (selBudget.availableAmt || 0).toLocaleString() }}원</b></label>
        <label class="w3">비고 <input v-model="pr.remark" :disabled="!editable" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">품목 <button v-if="editable" class="btn sm" @click="addRow">+ 행추가</button>
        <span class="tot">합계: {{ fmt(totAmt) }}</span>
      </div>
      <table>
        <thead><tr><th>#</th><th>품목코드</th><th>품목명</th><th>규격</th><th>단위</th><th class="r">수량</th><th class="r">예상단가</th><th class="r">금액</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in pr.items" :key="i">
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
            <td class="r"><input type="number" v-model.number="it.qty" :disabled="!editable" class="num" /></td>
            <td class="r"><input type="number" v-model.number="it.estPrc" :disabled="!editable" class="num" /></td>
            <td class="r">{{ fmt((Number(it.qty)||0) * (Number(it.estPrc)||0)) }}</td>
            <td v-if="editable"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
          <tr v-if="!pr.items.length"><td :colspan="editable ? 9 : 8" class="empty">품목을 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="!isNew && pr.id" class="panel">
      <div class="panel-head">첨부파일 (요청 근거)</div>
      <div style="padding:8px 12px"><AttachPanel refTyp="PR" :refId="pr.id" :readonly="!editable" /></div>
    </div>

    <!-- 결재 현황 -->
    <div v-if="pr.approval" class="panel">
      <div class="panel-head">결재 현황 <span class="ap-no">{{ pr.approval.aprvNo }}</span>
        <span class="badge ap" :class="pr.approval.aprvStsNm">{{ pr.approval.aprvStsNm }}</span>
      </div>
      <div class="aline">
        <div v-for="(l, i) in pr.approval.lines" :key="i" class="astep" :class="l.lineSts">
          <div class="anum">{{ l.stepNo }}</div>
          <div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
          <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
          <div v-if="i < pr.approval.lines.length - 1" class="aarrow">→</div>
        </div>
      </div>
    </div>

    <div v-if="pr.history && pr.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody>
          <tr v-for="h in pr.history" :key="h.seq">
            <td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td>
            <td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <ItemPicker v-if="itemPickerRow !== null" @select="onItemSelect" @close="itemPickerRow = null" />

    <!-- 상신: 결재선 지정 -->
    <div v-if="showApproverModal" class="modal-bg" @click.self="showApproverModal = false">
      <div class="ap-modal">
        <div class="modal-head">결재선 지정 <button class="x" @click="showApproverModal = false">×</button></div>
        <div class="ap-body">
          <p class="hint">결재 순서대로 결재자를 추가하세요.</p>
          <ol class="ap-list">
            <li v-for="(a, i) in approvers" :key="a.usrId">
              <span>{{ i + 1 }}단계 — <b>{{ a.usrNm }}</b> ({{ a.usrId }})</span>
              <button class="link del" @click="removeApprover(i)">제거</button>
            </li>
            <li v-if="!approvers.length" class="empty">결재자를 추가하세요.</li>
          </ol>
          <button class="btn" @click="showUserPicker = true">+ 결재자 추가</button>
        </div>
        <div class="ap-foot">
          <button class="btn primary" @click="doSubmit">상신</button>
          <button class="btn" @click="showApproverModal = false">취소</button>
        </div>
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
.tot { margin-left: auto; color: #1a3a6b; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form .w2 { flex: 1 1 280px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.picker { display: flex; gap: 6px; align-items: center; }
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
.badge.APRV { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.ap-no { font-size: 13px; color: #888; font-weight: normal; }
.badge.ap { margin-left: 6px; } .badge.결재중 { background: #fff3d6; color: #9a6b00; } .badge.결재완료 { background: #e3f6e8; color: #1a7f37; } .badge.반려 { background: #f3e3e3; color: #a33; } .badge.회수 { background: #eee; color: #777; }
.aline { padding: 18px 16px; display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.astep { display: flex; align-items: center; gap: 8px; border: 1px solid #eee; border-radius: 8px; padding: 8px 12px; position: relative; }
.astep.APPROVE { background: #f3faf5; border-color: #bfe6cc; } .astep.ING { background: #fff8ec; border-color: #f0d79a; } .astep.REJECT { background: #fdf0f0; border-color: #e6b8b8; }
.anum { width: 22px; height: 22px; border-radius: 50%; background: #1a3a6b; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.ainfo { display: flex; flex-direction: column; } .ainfo span { font-size: 12px; color: #888; }
.aop { font-size: 12px; color: #777; font-style: italic; }
.aarrow { color: #ccc; margin: 0 4px; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.ap-modal { width: 460px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.ap-body { padding: 16px 18px; } .hint { color: #888; font-size: 13px; margin: 0 0 10px; }
.ap-list { margin: 0 0 12px; padding-left: 18px; } .ap-list li { padding: 6px 0; display: flex; justify-content: space-between; align-items: center; } .ap-list .empty { color: #aaa; justify-content: center; }
.ap-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
.budhint { color: #555; font-size: 12px; align-self: center; } .budhint b { color: #1a7f37; } .budhint b.over { color: #c0392b; }
</style>
