<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import AttachPanel from '@/components/AttachPanel.vue'

interface Result { inspItemNm?: string; itemTyp?: string; itemTypNm?: string; specVal?: string; specLower?: number; specUpper?: number; measureVal?: string; measureNum?: number; testMethod?: string; unit?: string; passYn?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Gr { grId: number; grNo: string; poNo?: string; vdNm?: string; inspQty?: number }
interface Insp {
  id?: number; inspNo?: string; grId?: number; grNo?: string; poNo?: string; vdCd?: string; vdNm?: string;
  qmLotNo?: string; lotNo?: string; mfgYmd?: string; expYmd?: string; inspQty?: number; inspResult?: string; inspResultNm?: string; inspYmd?: string;
  sts?: string; stsNm?: string; remark?: string; results: Result[]; actions?: Action[]; history?: His[]
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const q = ref<Insp>({ inspYmd: today(), results: [] })
const grs = ref<Gr[]>([])
const selGr = ref<number | ''>('')
const itemTypCodes = ref<{ cd: string; cdNmKo: string }[]>([])

function today() { return new Date().toISOString().slice(0, 10) }
const isIng = computed(() => isNew.value || q.value.sts === 'ING')
/** 규격 상·하한 + 측정값(수치) 입력 시 자동 합부 미리보기 */
function autoPass(r: Result): string {
  if (r.measureNum == null || (r.specLower == null && r.specUpper == null)) return ''
  const ok = (r.specLower == null || Number(r.measureNum) >= Number(r.specLower))
          && (r.specUpper == null || Number(r.measureNum) <= Number(r.specUpper))
  return ok ? '자동합격' : '자동불합격'
}

async function load() {
  itemTypCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'INSP_ITEM_TYP' } })).data.data
  if (isNew.value) { grs.value = (await http.get('/qlt/inspection/receivable-gr')).data.data; addRow(); return }
  await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/qlt/inspection/${id}`)
  q.value = { ...data.data, results: data.data.results || [] }
}
async function onSelectGr() {
  if (!selGr.value) return
  const { data } = await http.get(`/qlt/inspection/from-gr/${selGr.value}`)
  const h = data.data
  q.value.grId = h.grId; q.value.grNo = h.grNo; q.value.poNo = h.poNo; q.value.vdCd = h.vdCd; q.value.vdNm = h.vdNm; q.value.inspQty = h.inspQty
  q.value.lotNo = h.lotNo; q.value.mfgYmd = h.mfgYmd; q.value.expYmd = h.expYmd   // 입고 LOT/제조일/유통기한 전파
}
function addRow() { q.value.results.push({ passYn: 'Y' }) }
function delRow(i: number) { q.value.results.splice(i, 1) }

async function save() {
  message.value = ''
  if (isNew.value && !q.value.grId) { message.value = '입고건을 선택하세요.'; return }
  try {
    if (isNew.value) {
      const { data } = await http.post('/qlt/inspection', q.value)
      router.replace(`/qlt/inspection/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.'
    } else {
      await http.post(`/qlt/inspection/${q.value.id}/results`, { results: q.value.results })
      await reloadById(q.value.id!); message.value = '검사결과가 저장되어 합부가 판정되었습니다.'
    }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/qlt/inspection/${q.value.id}/action`, { action: a.action, reason }); await reloadById(q.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` } catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/qlt/inspection/${q.value.id}`); router.push('/qlt/inspection') }
function toNc() {
  router.push({ path: '/qlt/nc/new', query: { vdCd: q.value.vdCd, vdNm: q.value.vdNm, refNo: q.value.inspNo, srcId: q.value.id } })
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>입고검사 {{ isNew ? '등록' : '상세' }}
        <span v-if="q.inspNo" class="prno">{{ q.inspNo }}</span>
        <span v-if="q.stsNm" class="badge" :class="q.sts">{{ q.stsNm }}</span>
        <span v-if="q.inspResult" class="res" :class="q.inspResult">{{ q.inspResultNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/qlt/inspection')">목록</button>
        <button v-if="isIng" class="btn primary" @click="save">{{ isNew ? '등록' : '검사결과 저장' }}</button>
        <button v-if="!isNew && q.sts === 'ING'" class="btn del" @click="remove">삭제</button>
        <button v-if="!isNew && (q.inspResult === 'FAIL' || q.inspResult === 'PARTIAL')" class="btn warn" @click="toNc">부적합 등록</button>
        <button v-for="a in q.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">검사 정보</div>
      <div class="form">
        <label v-if="isNew" class="w2">입고건(검사대상)
          <select v-model="selGr" @change="onSelectGr"><option value="">입고확정 건 선택</option><option v-for="g in grs" :key="g.grId" :value="g.grId">{{ g.grNo }} - {{ g.vdNm }} ({{ fmt(g.inspQty) }})</option></select>
        </label>
        <template v-else>
          <label>입고번호 <input :value="q.grNo" disabled /></label>
        </template>
        <label>발주번호 <input :value="q.poNo" disabled /></label>
        <label>협력사 <input :value="q.vdNm" disabled /></label>
        <label>검사수량 <input :value="fmt(q.inspQty)" disabled style="width:100px" /></label>
        <label>QM로트 <input v-model="q.qmLotNo" :disabled="!isIng" /></label>
        <label>LOT번호 <input v-model="q.lotNo" :disabled="!isIng" /></label>
        <label>제조일자 <input type="date" v-model="q.mfgYmd" :disabled="!isIng" /></label>
        <label>유통기한 <input type="date" v-model="q.expYmd" :disabled="!isIng" /></label>
        <label>검사일 <input type="date" v-model="q.inspYmd" :disabled="!isIng" /></label>
        <label class="w3">비고 <input v-model="q.remark" :disabled="!isIng" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">검사 항목 <button v-if="isIng" class="btn sm" @click="addRow">+ 행추가</button></div>
      <table>
        <thead><tr><th>#</th><th>검사항목</th><th>분류</th><th>규격(기준)</th><th class="r">하한</th><th class="r">상한</th><th class="r">측정값(수치)</th><th>측정값(텍스트)</th><th>단위</th><th style="width:110px">합부</th><th v-if="isIng"></th></tr></thead>
        <tbody>
          <tr v-for="(r, i) in q.results" :key="i">
            <td>{{ i + 1 }}</td>
            <td><input v-model="r.inspItemNm" :disabled="!isIng" style="width:120px" /></td>
            <td><select v-model="r.itemTyp" :disabled="!isIng" style="width:84px"><option value="">-</option><option v-for="c in itemTypCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></td>
            <td><input v-model="r.specVal" :disabled="!isIng" style="width:90px" /></td>
            <td class="r"><input type="number" v-model.number="r.specLower" :disabled="!isIng" class="num" /></td>
            <td class="r"><input type="number" v-model.number="r.specUpper" :disabled="!isIng" class="num" /></td>
            <td class="r"><input type="number" v-model.number="r.measureNum" :disabled="!isIng" class="num" /></td>
            <td><input v-model="r.measureVal" :disabled="!isIng" style="width:90px" /></td>
            <td><input v-model="r.unit" :disabled="!isIng" style="width:50px" /></td>
            <td>
              <select v-if="isIng && !autoPass(r)" v-model="r.passYn"><option value="Y">합격</option><option value="N">불합격</option></select>
              <span v-else-if="isIng" class="res" :class="autoPass(r) === '자동합격' ? 'PASS' : 'FAIL'">{{ autoPass(r) }}</span>
              <span v-else class="res" :class="r.passYn === 'Y' ? 'PASS' : 'FAIL'">{{ r.passYn === 'Y' ? '합격' : '불합격' }}</span>
            </td>
            <td v-if="isIng"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
          <tr v-if="!q.results.length"><td :colspan="isIng ? 11 : 10" class="empty">검사 항목을 추가하세요.</td></tr>
        </tbody>
      </table>
      <p v-if="isIng" class="hint">※ 규격 상·하한 + 측정값(수치)을 입력하면 합부가 <b>자동 판정</b>됩니다(범위 내=합격). 미입력 항목은 수동 선택. 저장 시 종합 합부(합격/부분합격/불합격)가 산출됩니다.</p>
    </div>

    <div v-if="!isNew && q.id" class="panel">
      <div class="panel-head">첨부파일 (시험성적서 CoA)</div>
      <div style="padding:8px 12px"><AttachPanel refTyp="QL" :refId="q.id" :readonly="!isIng" /></div>
    </div>

    <div v-if="q.history && q.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in q.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>
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
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
td input, td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
.hint { padding: 0 16px 14px; color: #888; font-size: 13px; }
.empty { text-align: center; color: #aaa; padding: 20px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.sm { padding: 4px 8px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; }
.btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.ING { background: #fff3d6; color: #9a6b00; } .badge.DONE { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.res { padding: 2px 10px; border-radius: 10px; font-weight: bold; font-size: 13px; }
.res.PASS { background: #d6f5e0; color: #0f7a35; } .res.PARTIAL { background: #fff3d6; color: #9a6b00; } .res.FAIL { background: #fde2e2; color: #b3261e; }
</style>
