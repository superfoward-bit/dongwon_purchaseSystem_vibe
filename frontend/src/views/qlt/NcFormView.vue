<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import ItemPicker from '@/components/ItemPicker.vue'
import AttachPanel from '@/components/AttachPanel.vue'

interface Act { actionTyp?: string; actionTypNm?: string; rootCause?: string; actionDesc?: string; respNm?: string; dueYmd?: string; completeYmd?: string; resultSts?: string; resultStsNm?: string; verifyUsr?: string; verifyYmd?: string; effectResult?: string; recurPrevent?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Nc {
  id?: number; ncNo?: string; vdCd?: string; vdNm?: string; srcTyp?: string; refNo?: string; itemCd?: string; itemNm?: string;
  ncTyp?: string; severity?: string; detectYmd?: string; lotNo?: string; mfgYmd?: string; expYmd?: string; ncQty?: number; ncDesc?: string; sts?: string; stsNm?: string; remark?: string;
  actionList: Act[]; actions?: Action[]; history?: His[]
}
const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const nc = ref<Nc>({ ncTyp: 'QUALITY', severity: 'MAJOR', srcTyp: 'SELF', detectYmd: today(), actionList: [] })
const typeCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const sevCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const showVendorPicker = ref(false)
const showItemPicker = ref(false)

function today() { return new Date().toISOString().slice(0, 10) }
const todayStr = today()
const editable = computed(() => isNew.value || (nc.value.sts !== 'CLOSED' && nc.value.sts !== 'CANCEL'))

async function load() {
  typeCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'QC_NC_TYP' } })).data.data
  sevCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'QC_SEVERITY' } })).data.data
  if (!isNew.value) { await reloadById(route.params.id as string); return }
  // 입고검사 등에서 넘어온 프리필
  const q = route.query
  if (q.vdCd) { nc.value.vdCd = String(q.vdCd); nc.value.vdNm = q.vdNm ? String(q.vdNm) : '' }
  if (q.itemNm) nc.value.itemNm = String(q.itemNm)
  if (q.refNo) { nc.value.refNo = String(q.refNo); nc.value.srcTyp = 'QL' }
  if (q.srcId) nc.value.srcId = Number(q.srcId) as any
}
async function reloadById(id: string | number) { const { data } = await http.get(`/qlt/nc/${id}`); nc.value = { ...data.data, actionList: data.data.actionList || [] } }
function onVendorSelect(v: { vdCd: string; vdNm: string }) { nc.value.vdCd = v.vdCd; nc.value.vdNm = v.vdNm; showVendorPicker.value = false }
function onItemSelect(it: any) { nc.value.itemCd = it.itemCd; nc.value.itemNm = it.itemNm; showItemPicker.value = false }
function addAct() { nc.value.actionList.push({ actionTyp: 'CORRECT', resultSts: 'PLAN' }) }
function delAct(i: number) { nc.value.actionList.splice(i, 1) }
async function save() {
  message.value = ''
  if (!nc.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/qlt/nc', nc.value); router.replace(`/qlt/nc/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.' }
    else { await http.put(`/qlt/nc/${nc.value.id}`, nc.value); await reloadById(nc.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/qlt/nc/${nc.value.id}/action`, { action: a.action, reason }); await reloadById(nc.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.${a.action === 'REQUEST' ? ' (협력사 개선요청 알림 발송)' : ''}` }
  catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/qlt/nc/${nc.value.id}`); router.push('/qlt/nc') }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>부적합 {{ isNew ? '등록' : '상세' }}
        <span v-if="nc.ncNo" class="prno">{{ nc.ncNo }}</span>
        <span v-if="nc.stsNm" class="badge" :class="nc.sts">{{ nc.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/qlt/nc')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && nc.sts === 'OPEN'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in nc.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">부적합 정보</div>
      <div class="form">
        <label class="w2">협력사 <div class="picker"><input :value="nc.vdNm ? `${nc.vdNm} (${nc.vdCd})` : ''" readonly /><button v-if="editable" class="btn sm" @click="showVendorPicker = true">찾기</button></div></label>
        <label>품목 <div class="picker"><input :value="nc.itemNm" readonly /><button v-if="editable" class="btn sm" @click="showItemPicker = true">찾기</button></div></label>
        <label>부적합유형 <select v-model="nc.ncTyp" :disabled="!editable"><option v-for="c in typeCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>심각도 <select v-model="nc.severity" :disabled="!editable"><option v-for="c in sevCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>발생일 <input type="date" v-model="nc.detectYmd" :disabled="!editable" /></label>
        <label>LOT번호 <input v-model="nc.lotNo" :disabled="!editable" /></label>
        <label>제조일자 <input type="date" v-model="nc.mfgYmd" :disabled="!editable" /></label>
        <label>유통기한 <input type="date" v-model="nc.expYmd" :disabled="!editable" /></label>
        <label>부적합수량 <input type="number" v-model.number="nc.ncQty" :disabled="!editable" /></label>
        <label>참조(검사/입고) <input v-model="nc.refNo" :disabled="!editable" /></label>
        <label class="w3">부적합 내용 <input v-model="nc.ncDesc" :disabled="!editable" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">개선조치 (시정/예방 CAPA) <button v-if="editable" class="btn sm" @click="addAct">+ 조치추가</button></div>
      <table>
        <thead><tr><th>유형</th><th>원인분석</th><th>조치내용</th><th>담당</th><th>완료기한</th><th>완료일</th><th>상태</th><th>검증자</th><th>검증일</th><th>검증결과</th><th>재발방지</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(a, i) in nc.actionList" :key="i" :class="{ overdue: a.dueYmd && a.dueYmd < todayStr && a.resultSts !== 'DONE' && a.resultSts !== 'VERIFIED' }">
            <td><select v-model="a.actionTyp" :disabled="!editable"><option value="CORRECT">시정조치</option><option value="PREVENT">예방조치</option></select></td>
            <td><input v-model="a.rootCause" :disabled="!editable" /></td>
            <td><input v-model="a.actionDesc" :disabled="!editable" /></td>
            <td><input v-model="a.respNm" :disabled="!editable" style="width:80px" /></td>
            <td><input type="date" v-model="a.dueYmd" :disabled="!editable" /><span v-if="a.dueYmd && a.dueYmd < todayStr && a.resultSts !== 'DONE' && a.resultSts !== 'VERIFIED'" class="od">기한초과</span></td>
            <td><input type="date" v-model="a.completeYmd" :disabled="!editable" /></td>
            <td><select v-model="a.resultSts" :disabled="!editable"><option value="PLAN">계획</option><option value="DOING">진행중</option><option value="DONE">완료</option><option value="VERIFIED">검증완료</option></select></td>
            <td><input v-model="a.verifyUsr" :disabled="!editable" style="width:70px" /></td>
            <td><input type="date" v-model="a.verifyYmd" :disabled="!editable" /></td>
            <td><select v-model="a.effectResult" :disabled="!editable"><option value="">-</option><option value="PASS">유효</option><option value="FAIL">미흡</option></select></td>
            <td><input v-model="a.recurPrevent" :disabled="!editable" style="width:120px" /></td>
            <td v-if="editable"><button class="link del" @click="delAct(i)">삭제</button></td>
          </tr>
          <tr v-if="!nc.actionList.length"><td :colspan="editable ? 12 : 11" class="empty">시정/예방 조치를 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="!isNew && nc.id" class="panel">
      <div class="panel-head">첨부파일 (증빙사진 등)</div>
      <div style="padding:8px 12px"><AttachPanel refTyp="QC" :refId="nc.id" :readonly="!editable" /></div>
    </div>

    <div v-if="nc.history && nc.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in nc.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>

    <VendorPicker v-if="showVendorPicker" @select="onVendorSelect" @close="showVendorPicker = false" />
    <ItemPicker v-if="showItemPicker" @select="onItemSelect" @close="showItemPicker = false" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.prno { font-size: 14px; color: #888; font-weight: normal; } .head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 200px; } .form .w2 { flex: 1 1 300px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .picker { display: flex; gap: 6px; } .picker input { flex: 1; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
td input, td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
.empty { text-align: center; color: #aaa; padding: 18px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.OPEN { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.ACTION { background: #dbeafe; color: #1a4f9c; } .badge.CLOSED { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
tr.overdue { background: #fff5f5; } .od { color: #c0392b; font-size: 11px; margin-left: 4px; font-weight: bold; }
</style>
