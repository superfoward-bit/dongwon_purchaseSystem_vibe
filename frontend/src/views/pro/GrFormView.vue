<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import UserPicker from '@/components/UserPicker.vue'
import AttachPanel from '@/components/AttachPanel.vue'

interface Aline { stepNo: number; aprvUsrNm: string; lineSts: string; lineStsNm?: string; opinion?: string }
interface Approval { aprvNo: string; aprvStsNm?: string; lines: Aline[] }
interface Item {
  poItemId?: number; itemCd?: string; itemNm?: string; spec?: string; unitCd?: string;
  poQty?: number; remainQty?: number; grQty?: number; inspPassQty?: number; inspFailQty?: number;
  prc?: number; inspResult?: string; lotNo?: string; mfgYmd?: string; expYmd?: string; remark?: string
}
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Po { poId: number; poNo: string; vdNm?: string; totAmt?: number }
interface Gr {
  id?: number; grNo?: string; poId?: number; poNo?: string; vdCd?: string; vdNm?: string;
  grYmd?: string; inspYmd?: string; whCd?: string; whNm?: string; delyNo?: string; sts?: string; stsNm?: string; remark?: string;
  items: Item[]; actions?: Action[]; history?: His[]; approval?: Approval
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const gr = ref<Gr>({ grYmd: today(), inspYmd: today(), items: [] })
const pos = ref<Po[]>([])
const selectedPoId = ref<number | ''>('')
const inspCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const whCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const showApproverModal = ref(false)
const showUserPicker = ref(false)
const approvers = ref<{ usrId: string; usrNm: string }[]>([])

function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value)
const totAmt = computed(() => gr.value.items.reduce((s, it) => s + (Number(it.grQty) || 0) * (Number(it.prc) || 0), 0))

async function load() {
  inspCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'INSP_RESULT' } })).data.data
  whCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'WH' } })).data.data
  if (isNew.value) {
    pos.value = (await http.get('/pro/gr/receivable-po')).data.data
    return
  }
  const { data } = await http.get(`/pro/gr/${route.params.id}`)
  gr.value = { ...data.data, items: data.data.items || [] }
}
async function onSelectPo() {
  if (!selectedPoId.value) return
  const { data } = await http.get(`/pro/gr/from-po/${selectedPoId.value}`)
  const h = data.data
  gr.value = {
    grYmd: today(), inspYmd: today(),
    poId: h.poId, poNo: h.poNo, vdCd: h.vdCd, vdNm: h.vdNm, whCd: h.whCd,
    items: (h.items || []).map((it: Item) => ({ ...it, inspResult: 'PASS', inspPassQty: it.grQty, inspFailQty: 0 })),
  }
}
async function save() {
  message.value = ''
  if (!gr.value.poId) { message.value = '발주를 선택하세요.'; return }
  try {
    const { data } = await http.post('/pro/gr', gr.value)
    router.replace(`/pro/gr/${data.data}`)
    await reloadById(data.data)
    message.value = '등록되었습니다. (입고확정 시 발주 입고수량에 반영됩니다)'
  } catch (e: any) { message.value = e.message }
}
async function reloadById(id: number | string) {
  const { data } = await http.get(`/pro/gr/${id}`)
  gr.value = { ...data.data, items: data.data.items || [] }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return }
  else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try {
    await http.post(`/pro/gr/${gr.value.id}/action`, { action: a.action, reason })
    await reloadById(gr.value.id!)
    message.value = `[${a.actionNm}] 처리되었습니다.`
  } catch (e: any) { message.value = e.message }
}
async function remove() {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/pro/gr/${gr.value.id}`)
  router.push('/pro/gr')
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
    await http.post(`/pro/gr/${gr.value.id}/submit`, { approvers: approvers.value })
    showApproverModal.value = false; await reloadById(gr.value.id!); message.value = '상신되었습니다. (결재 진행)'
  } catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>입고/검수 {{ isNew ? '등록' : '상세' }}
        <span v-if="gr.grNo" class="prno">{{ gr.grNo }}</span>
        <span v-if="gr.stsNm" class="badge" :class="gr.sts">{{ gr.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/gr')">목록</button>
        <button v-if="isNew" class="btn primary" @click="save">등록</button>
        <button v-if="!isNew && gr.sts === 'TMP'" class="btn go" @click="openSubmit">상신</button>
        <button v-if="!isNew && gr.sts === 'TMP'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in gr.actions" :key="a.action"
                class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')"
                @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <!-- 발주 선택 (신규) -->
    <div v-if="isNew" class="panel">
      <div class="panel-head">발주 선택 (발주확정된 건)</div>
      <div class="form">
        <label class="w2">발주
          <select v-model="selectedPoId" @change="onSelectPo">
            <option value="">발주를 선택하세요</option>
            <option v-for="p in pos" :key="p.poId" :value="p.poId">{{ p.poNo }} - {{ p.vdNm }} ({{ fmt(p.totAmt) }})</option>
          </select>
        </label>
        <label>협력사 <input :value="gr.vdNm" disabled /></label>
        <label>입고일 <input type="date" v-model="gr.grYmd" /></label>
        <label>검수일 <input type="date" v-model="gr.inspYmd" /></label>
        <label>입고창고 <select v-model="gr.whCd"><option value="">선택</option><option v-for="c in whCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>거래명세서번호 <input v-model="gr.delyNo" placeholder="협력사 납품서 번호" /></label>
      </div>
    </div>
    <div v-else class="panel">
      <div class="panel-head">기본정보</div>
      <div class="form">
        <label>발주번호 <input :value="gr.poNo" disabled /></label>
        <label>협력사 <input :value="gr.vdNm" disabled /></label>
        <label>입고일 <input :value="gr.grYmd" disabled /></label>
        <label>검수일 <input :value="gr.inspYmd" disabled /></label>
        <label>입고창고 <input :value="gr.whNm" disabled /></label>
        <label>거래명세서번호 <input :value="gr.delyNo" disabled /></label>
      </div>
    </div>

    <!-- 품목 -->
    <div class="panel">
      <div class="panel-head">입고 품목 <span class="tot">입고금액 합계: {{ fmt(totAmt) }}</span></div>
      <table>
        <thead><tr><th>#</th><th>품목</th><th>규격</th><th>단위</th><th class="r">발주수량</th><th class="r">입고수량</th><th>검사결과</th><th>LOT번호</th><th>제조일자</th><th>유통기한</th><th class="r">단가</th><th class="r">금액</th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in gr.items" :key="i">
            <td>{{ i + 1 }}</td><td>{{ it.itemNm }}</td><td>{{ it.spec }}</td><td>{{ it.unitCd }}</td>
            <td class="r">{{ fmt(it.poQty) }}</td>
            <td class="r"><input v-if="editable" type="number" v-model.number="it.grQty" class="num" /><span v-else>{{ fmt(it.grQty) }}</span></td>
            <td>
              <select v-if="editable" v-model="it.inspResult"><option v-for="c in inspCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
              <span v-else>{{ inspCodes.find(c => c.cd === it.inspResult)?.cdNmKo }}</span>
            </td>
            <td><input v-if="editable" v-model="it.lotNo" style="width:96px" /><span v-else>{{ it.lotNo }}</span></td>
            <td><input v-if="editable" type="date" v-model="it.mfgYmd" /><span v-else>{{ it.mfgYmd }}</span></td>
            <td><input v-if="editable" type="date" v-model="it.expYmd" /><span v-else>{{ it.expYmd }}</span></td>
            <td class="r">{{ fmt(it.prc) }}</td>
            <td class="r">{{ fmt((Number(it.grQty)||0)*(Number(it.prc)||0)) }}</td>
          </tr>
          <tr v-if="!gr.items.length"><td colspan="12" class="empty">발주를 선택하면 미입고 품목이 표시됩니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="gr.approval" class="panel">
      <div class="panel-head">결재 현황 <span class="ap-no">{{ gr.approval.aprvNo }}</span>
        <span class="badge ap" :class="gr.approval.aprvStsNm">{{ gr.approval.aprvStsNm }}</span>
      </div>
      <div class="aline">
        <div v-for="(l, i) in gr.approval.lines" :key="i" class="astep" :class="l.lineSts">
          <div class="anum">{{ l.stepNo }}</div><div class="ainfo"><b>{{ l.aprvUsrNm }}</b><span>{{ l.lineStsNm }}</span></div>
          <div v-if="l.opinion" class="aop">"{{ l.opinion }}"</div>
          <div v-if="i < gr.approval.lines.length - 1" class="aarrow">→</div>
        </div>
      </div>
    </div>

    <div v-if="!isNew && gr.id" class="panel">
      <div class="panel-head">첨부파일 (거래명세서/인수증)</div>
      <div style="padding:8px 12px"><AttachPanel refTyp="GR" :refId="gr.id" :readonly="!editable" /></div>
    </div>

    <div v-if="gr.history && gr.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody>
          <tr v-for="h in gr.history" :key="h.seq">
            <td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td>
            <td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td>
          </tr>
        </tbody>
      </table>
    </div>

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
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form .w2 { flex: 1 1 360px; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
td input, td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; }
td .num { text-align: right; width: 90px; }
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
