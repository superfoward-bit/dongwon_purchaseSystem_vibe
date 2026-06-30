<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'

interface Result { auditItemNm?: string; result?: string; finding?: string; actionReq?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Audit {
  id?: number; auditNo?: string; vdCd?: string; vdNm?: string; auditTyp?: string; auditYmd?: string;
  resultGrade?: string; resultGradeNm?: string; findingCnt?: number; actionReqYn?: string;
  sts?: string; stsNm?: string; remark?: string; results: Result[]; actions?: Action[]; history?: His[]
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const a = ref<Audit>({ auditTyp: 'REGULAR', auditYmd: today(), results: [] })
const showVendorPicker = ref(false)
const GRADES = [{ cd: 'SAFE', nm: '양호' }, { cd: 'WATCH', nm: '관찰' }, { cd: 'RISK', nm: '위험' }]

function today() { return new Date().toISOString().slice(0, 10) }
const isIng = computed(() => isNew.value || a.value.sts === 'ING')

async function load() {
  if (isNew.value) { addRow(); return }
  await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/srm/audit/${id}`)
  a.value = { ...data.data, results: data.data.results || [] }
}
function onVendorSelect(v: { vdCd: string; vdNm: string }) { a.value.vdCd = v.vdCd; a.value.vdNm = v.vdNm; showVendorPicker.value = false }
function addRow() { a.value.results.push({ result: 'SAFE' }) }
function delRow(i: number) { a.value.results.splice(i, 1) }

async function save() {
  message.value = ''
  if (!a.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  try {
    if (isNew.value) {
      const { data } = await http.post('/srm/audit', a.value)
      router.replace(`/srm/audit/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.'
    } else {
      await http.post(`/srm/audit/${a.value.id}/results`, { results: a.value.results })
      await reloadById(a.value.id!); message.value = '점검결과가 저장되었습니다.'
    }
  } catch (e: any) { message.value = e.message }
}
async function doAction(act: Action) {
  let reason = ''
  if (act.rsnReqYn === 'Y') { reason = window.prompt(`[${act.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return }
  else if (!confirm(`[${act.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/srm/audit/${a.value.id}/action`, { action: act.action, reason }); await reloadById(a.value.id!); message.value = `[${act.actionNm}] 처리되었습니다.` }
  catch (e: any) { message.value = e.message }
}
async function remove() {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/srm/audit/${a.value.id}`); router.push('/srm/audit')
}
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>협력사 감시 {{ isNew ? '등록' : '상세' }}
        <span v-if="a.auditNo" class="prno">{{ a.auditNo }}</span>
        <span v-if="a.stsNm" class="badge" :class="a.sts">{{ a.stsNm }}</span>
        <span v-if="a.resultGrade" class="grade" :class="a.resultGrade">{{ a.resultGradeNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/srm/audit')">목록</button>
        <button v-if="isIng" class="btn primary" @click="save">{{ isNew ? '등록' : '점검결과 저장' }}</button>
        <button v-if="!isNew && a.sts === 'ING'" class="btn del" @click="remove">삭제</button>
        <button v-for="act in a.actions" :key="act.action"
                class="btn" :class="act.direction === 'BWD' ? 'warn' : (act.direction === 'END' ? 'del' : 'go')"
                @click="doAction(act)">{{ act.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">감시 정보</div>
      <div class="form">
        <label class="w2">협력사
          <div class="picker"><input :value="a.vdNm ? `${a.vdNm} (${a.vdCd})` : ''" placeholder="협력사 선택" readonly /><button v-if="isNew" class="btn sm" @click="showVendorPicker = true">찾기</button></div>
        </label>
        <label>감시유형 <select v-model="a.auditTyp" :disabled="!isNew"><option value="REGULAR">정기감시</option><option value="SPECIAL">특별감시</option></select></label>
        <label>감시일 <input type="date" v-model="a.auditYmd" :disabled="!isNew" /></label>
        <label v-if="!isNew">지적건수 <input :value="a.findingCnt" disabled style="width:70px" /></label>
        <label class="w3">비고 <input v-model="a.remark" :disabled="!isIng" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">점검 항목 <button v-if="isIng" class="btn sm" @click="addRow">+ 행추가</button></div>
      <table>
        <thead><tr><th>#</th><th>점검항목</th><th style="width:110px">결과</th><th>지적사항</th><th>개선요청</th><th v-if="isIng"></th></tr></thead>
        <tbody>
          <tr v-for="(r, i) in a.results" :key="i">
            <td>{{ i + 1 }}</td>
            <td><input v-model="r.auditItemNm" :disabled="!isIng" /></td>
            <td>
              <select v-if="isIng" v-model="r.result"><option v-for="g in GRADES" :key="g.cd" :value="g.cd">{{ g.nm }}</option></select>
              <span v-else class="grade" :class="r.result">{{ GRADES.find(g => g.cd === r.result)?.nm }}</span>
            </td>
            <td><input v-model="r.finding" :disabled="!isIng" /></td>
            <td><input v-model="r.actionReq" :disabled="!isIng" /></td>
            <td v-if="isIng"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
          <tr v-if="!a.results.length"><td :colspan="isIng ? 6 : 5" class="empty">점검 항목을 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="a.history && a.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in a.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>

    <VendorPicker v-if="showVendorPicker" @select="onVendorSelect" @close="showVendorPicker = false" />
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
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .form .w2 { flex: 1 1 320px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.picker { display: flex; gap: 6px; align-items: center; } .picker input { flex: 1; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
td input, td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.sm { padding: 4px 8px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; }
.btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.ING { background: #fff3d6; color: #9a6b00; } .badge.DONE { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.grade { padding: 2px 10px; border-radius: 10px; font-weight: bold; font-size: 13px; }
.grade.SAFE { background: #d6f5e0; color: #0f7a35; } .grade.WATCH { background: #fff3d6; color: #9a6b00; } .grade.RISK { background: #fde2e2; color: #b3261e; }
</style>
