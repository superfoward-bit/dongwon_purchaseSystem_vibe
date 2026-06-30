<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'

interface Result { id?: number; sheetItemSeq?: number; evalItemNm?: string; cateNm?: string; weight?: number; score?: number; weightedScore?: number; opinion?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Sheet { sheetCd: string; sheetNm: string }
interface Eval {
  id?: number; evalNo?: string; evalTyp?: string; sheetCd?: string; sheetNm?: string; vdCd?: string; vdNm?: string;
  segCd?: string; segNm?: string; useCateYn?: string;
  evalPeriod?: string; totScore?: number; gradeCd?: string; gradeNm?: string; sts?: string; stsNm?: string; remark?: string;
  results: Result[]; actions?: Action[]; history?: His[]
}

const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const ev = ref<Eval>({ evalTyp: 'REGULAR', evalPeriod: thisMonth(), results: [] })
const sheets = ref<Sheet[]>([])
const showVendorPicker = ref(false)

function thisMonth() { return new Date().toISOString().slice(0, 7) }
const isIng = computed(() => ev.value.sts === 'ING')
const twoLevel = computed(() => ev.value.useCateYn === 'Y')
const liveScore = computed(() => ev.value.results.reduce((s, r) => s + (Number(r.score) || 0) * (Number(r.weight) || 0) / 100, 0))

async function load() {
  if (isNew.value) { sheets.value = (await http.get('/srm/sheet')).data.data; return }
  await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) {
  const { data } = await http.get(`/srm/eval/${id}`)
  ev.value = { ...data.data, results: data.data.results || [] }
}
function onVendorSelect(v: { vdCd: string; vdNm: string }) { ev.value.vdCd = v.vdCd; ev.value.vdNm = v.vdNm; showVendorPicker.value = false }

async function start() {
  message.value = ''
  if (!ev.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  if (!ev.value.sheetCd) { message.value = '평가시트를 선택하세요.'; return }
  try {
    const { data } = await http.post('/srm/eval', ev.value)
    router.replace(`/srm/eval/${data.data}`); await reloadById(data.data); message.value = '평가가 시작되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function saveScores() {
  const scores = ev.value.results.map(r => ({ sheetItemSeq: r.sheetItemSeq, score: r.score || 0, opinion: r.opinion }))
  try {
    await http.post(`/srm/eval/${ev.value.id}/scores`, { scores })
    await reloadById(ev.value.id!); message.value = '점수가 저장되어 등급이 산출되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유를 입력하세요`) || ''; if (!reason) return }
  else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/srm/eval/${ev.value.id}/action`, { action: a.action, reason }); await reloadById(ev.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` }
  catch (e: any) { message.value = e.message }
}
async function remove() {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/srm/eval/${ev.value.id}`); router.push('/srm/eval')
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
function gcls(g?: string) { return g ? 'g' + g.charAt(0) : '' }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>협력사 평가 {{ isNew ? '시작' : '상세' }}
        <span v-if="ev.evalNo" class="prno">{{ ev.evalNo }}</span>
        <span v-if="ev.stsNm" class="badge" :class="ev.sts">{{ ev.stsNm }}</span>
        <span v-if="ev.gradeNm" class="grade" :class="gcls(ev.gradeCd)">{{ ev.gradeNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/srm/eval')">목록</button>
        <button v-if="isNew" class="btn primary" @click="start">평가 시작</button>
        <button v-if="isIng" class="btn primary" @click="saveScores">점수 저장</button>
        <button v-if="isIng" class="btn del" @click="remove">삭제</button>
        <button v-for="a in ev.actions" :key="a.action"
                class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')"
                @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <!-- 신규: 대상 선택 -->
    <div v-if="isNew" class="panel">
      <div class="panel-head">평가 대상</div>
      <div class="form">
        <label class="w2">협력사
          <div class="picker"><input :value="ev.vdNm ? `${ev.vdNm} (${ev.vdCd})` : ''" placeholder="협력사 선택" readonly /><button class="btn sm" @click="showVendorPicker = true">찾기</button></div>
        </label>
        <label>평가시트 <select v-model="ev.sheetCd"><option value="">선택</option><option v-for="s in sheets" :key="s.sheetCd" :value="s.sheetCd">{{ s.sheetNm }}</option></select></label>
        <label>평가유형 <select v-model="ev.evalTyp"><option value="REGULAR">정기평가</option><option value="REGISTER">등록평가</option></select></label>
        <label>평가기간 <input v-model="ev.evalPeriod" placeholder="2026-06" /></label>
      </div>
    </div>

    <!-- 상세: 정보 -->
    <div v-else class="panel">
      <div class="panel-head">평가 정보</div>
      <div class="info">
        <div><span>협력사</span><b>{{ ev.vdNm }}</b></div>
        <div><span>세그먼트</span><b>{{ ev.segNm || '미지정' }}</b></div>
        <div><span>평가시트</span><b>{{ ev.sheetNm }} <span v-if="twoLevel" class="two">2단계</span></b></div>
        <div><span>평가기간</span><b>{{ ev.evalPeriod }}</b></div>
        <div><span>종합점수</span><b class="amt">{{ fmt(ev.totScore) }}</b></div>
        <div><span>등급</span><b><span v-if="ev.gradeNm" class="grade" :class="gcls(ev.gradeCd)">{{ ev.gradeNm }}</span></b></div>
      </div>
    </div>

    <!-- 점수 입력/조회 -->
    <div v-if="!isNew" class="panel">
      <div class="panel-head">평가 항목
        <span v-if="!twoLevel" class="tot">실시간 환산점수: {{ liveScore.toFixed(2) }} / 100</span>
        <span v-else class="tot">2단계 가중 — 점수 저장 시 카테고리 가중 산출</span>
      </div>
      <table>
        <thead><tr><th v-if="twoLevel">카테고리</th><th>항목</th><th class="r">배점</th><th class="r">점수(0~100)</th><th class="r">환산점수</th><th>의견</th></tr></thead>
        <tbody>
          <tr v-for="r in ev.results" :key="r.sheetItemSeq">
            <td v-if="twoLevel"><span class="catetag">{{ r.cateNm }}</span></td>
            <td>{{ r.evalItemNm }}</td>
            <td class="r">{{ r.weight }}{{ twoLevel ? '%' : '' }}</td>
            <td class="r"><input v-if="isIng" type="number" v-model.number="r.score" class="num" min="0" max="100" /><span v-else>{{ r.score }}</span></td>
            <td class="r">{{ twoLevel ? (Number(r.weightedScore)||0).toFixed(2) : ((Number(r.score)||0) * (Number(r.weight)||0) / 100).toFixed(2) }}</td>
            <td><input v-if="isIng" v-model="r.opinion" /><span v-else>{{ r.opinion }}</span></td>
          </tr>
        </tbody>
      </table>
      <p v-if="isIng" class="hint">※ 점수를 입력하고 <b>점수 저장</b>을 누르면 등급이 산출됩니다. 이후 <b>평가완료</b> 시 협력사 등급에 반영됩니다.</p>
    </div>

    <div v-if="ev.history && ev.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in ev.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
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
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; }
.tot { margin-left: auto; color: #1a3a6b; font-weight: normal; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .form .w2 { flex: 1 1 320px; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.picker { display: flex; gap: 6px; align-items: center; } .picker input { flex: 1; }
.info { padding: 16px; display: flex; flex-wrap: wrap; gap: 28px; }
.info div { display: flex; flex-direction: column; gap: 4px; } .info span { color: #888; font-size: 13px; } .info .amt { color: #1a3a6b; font-size: 18px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
td input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; } td .num { text-align: right; width: 90px; }
.hint { padding: 0 16px 14px; color: #888; font-size: 13px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.sm { padding: 4px 8px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; }
.btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.ING { background: #fff3d6; color: #9a6b00; } .badge.DONE { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.grade { padding: 2px 12px; border-radius: 10px; font-weight: bold; font-size: 14px; }
.grade.gA { background: #d6f5e0; color: #0f7a35; } .grade.gB { background: #dbeafe; color: #1a4f9c; }
.grade.gC { background: #fff3d6; color: #9a6b00; } .grade.gD { background: #fde2e2; color: #b3261e; }
.two { font-size: 11px; background: #efe7f8; color: #6b3fa0; padding: 1px 6px; border-radius: 8px; }
.catetag { font-size: 12px; background: #f0f0f0; color: #555; padding: 2px 8px; border-radius: 8px; }
</style>
