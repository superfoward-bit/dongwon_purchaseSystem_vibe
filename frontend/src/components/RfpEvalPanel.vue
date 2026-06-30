<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'
import UserPicker from '@/components/UserPicker.vue'

const props = defineProps<{ rfxId: number; readonly?: boolean }>()
const emit = defineEmits<{ (e: 'select', vdCd: string): void }>()

interface Criteria { id?: number; criteriaNm?: string; weight?: number }
interface Evaluator { id?: number; usrId?: string; usrNm?: string }
interface Score { vdCd: string; criteriaId: number; evaluatorId: number; score: number }
interface Result { vdCd: string; vdNm?: string; quoteAmt?: number; nonPriceScore?: number; priceScore?: number; totalScore?: number; rankNo?: number }

const evalYn = ref('N')
const priceWeight = ref(30)
const priceMethod = ref('MR')
const criteria = ref<Criteria[]>([])
const evaluators = ref<Evaluator[]>([])
const results = ref<Result[]>([])
const scoreMap = ref<Record<string, number>>({})   // key vdCd|critId|evalId
const showUserPicker = ref(false)
const message = ref('')

function key(vdCd: string, cId: number, eId: number) { return `${vdCd}|${cId}|${eId}` }
async function loadSetup() {
  const { data } = await http.get(`/pro/rx/${props.rfxId}/eval/setup`)
  evalYn.value = data.data.evalYn || 'N'
  priceWeight.value = data.data.priceWeight ?? 30
  priceMethod.value = data.data.priceMethod || 'MR'
  criteria.value = data.data.criteria || []
  evaluators.value = data.data.evaluators || []
}
async function loadResult() { results.value = (await http.get(`/pro/rx/${props.rfxId}/eval/result`)).data.data }
async function loadScores() {
  const scores: Score[] = (await http.get(`/pro/rx/${props.rfxId}/eval/scores`)).data.data
  const m: Record<string, number> = {}
  scores.forEach(s => { m[key(s.vdCd, s.criteriaId, s.evaluatorId)] = s.score })
  scoreMap.value = m
}
async function reloadAll() { await loadSetup(); await loadResult(); await loadScores() }

function addCriteria() { criteria.value.push({ criteriaNm: '', weight: 0 }) }
function delCriteria(i: number) { criteria.value.splice(i, 1) }
function onUserSelect(u: { usrId: string; usrNm: string }) {
  if (!evaluators.value.some(e => e.usrId === u.usrId)) evaluators.value.push({ usrId: u.usrId, usrNm: u.usrNm })
  showUserPicker.value = false
}
function delEvaluator(i: number) { evaluators.value.splice(i, 1) }

async function saveSetup() {
  message.value = ''
  try {
    await http.put(`/pro/rx/${props.rfxId}/eval/setup`, { evalYn: evalYn.value, priceWeight: priceWeight.value, priceMethod: priceMethod.value, criteria: criteria.value, evaluators: evaluators.value })
    await reloadAll(); message.value = '평가설정이 저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function saveScores() {
  message.value = ''
  const scores: Score[] = []
  for (const r of results.value) for (const c of criteria.value) for (const ev of evaluators.value) {
    const v = scoreMap.value[key(r.vdCd, c.id!, ev.id!)]
    if (v != null && v !== undefined) scores.push({ vdCd: r.vdCd, criteriaId: c.id!, evaluatorId: ev.id!, score: Number(v) })
  }
  try { await http.put(`/pro/rx/${props.rfxId}/eval/scores`, scores); await loadResult(); message.value = '평가점수가 저장되고 종합점수가 산출되었습니다.' }
  catch (e: any) { message.value = e.message }
}
const totalWeight = () => (criteria.value.reduce((s, c) => s + (Number(c.weight) || 0), 0) + Number(priceWeight.value || 0))
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(reloadAll)
</script>

<template>
  <div class="rfp">
    <div class="rhead">
      <label class="chk"><input type="checkbox" :checked="evalYn === 'Y'" :disabled="readonly" @change="evalYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /> 평가입찰 사용 (비가격 + 가격 종합평가)</label>
      <span class="wsum" :class="{ warn: totalWeight() !== 100 }">배점합계 {{ totalWeight() }} / 100</span>
    </div>
    <p v-if="message" class="rmsg">{{ message }}</p>

    <template v-if="evalYn === 'Y'">
      <div class="grid2">
        <div class="box">
          <div class="bh">비가격 평가항목 <button v-if="!readonly" class="btn sm" @click="addCriteria">+ 항목</button></div>
          <table class="t">
            <thead><tr><th>항목명</th><th class="r">배점</th><th v-if="!readonly"></th></tr></thead>
            <tbody>
              <tr v-for="(c, i) in criteria" :key="i">
                <td><input v-model="c.criteriaNm" :disabled="readonly" /></td>
                <td class="r"><input type="number" v-model.number="c.weight" :disabled="readonly" class="num" /></td>
                <td v-if="!readonly"><button class="link del" @click="delCriteria(i)">삭제</button></td>
              </tr>
              <tr v-if="!criteria.length"><td :colspan="readonly ? 2 : 3" class="empty">항목을 추가하세요.</td></tr>
            </tbody>
          </table>
        </div>
        <div class="box">
          <div class="bh">가격 / 평가위원</div>
          <div class="cfg">
            <label>가격 배점 <input type="number" v-model.number="priceWeight" :disabled="readonly" /></label>
            <label>가격점수 방식
              <select v-model="priceMethod" :disabled="readonly"><option value="MR">최저가 비율(MR)</option><option value="LN">선형(LN)</option></select>
            </label>
          </div>
          <div class="evals">
            <span class="lbl">평가위원</span>
            <span v-for="(e, i) in evaluators" :key="i" class="echip">{{ e.usrNm }}<button v-if="!readonly" class="x" @click="delEvaluator(i)">×</button></span>
            <button v-if="!readonly" class="btn sm" @click="showUserPicker = true">+ 위원</button>
          </div>
        </div>
      </div>
      <div class="acts"><button v-if="!readonly" class="btn primary" @click="saveSetup">평가설정 저장</button></div>

      <div v-if="criteria.length && evaluators.length && results.length" class="box">
        <div class="bh">평가 점수입력 (0~100) <button v-if="!readonly" class="btn sm go" @click="saveScores">점수 저장 + 종합산출</button></div>
        <div class="scroll">
          <table class="t score">
            <thead>
              <tr><th>협력사</th>
                <template v-for="ev in evaluators" :key="ev.id">
                  <th v-for="c in criteria" :key="c.id" class="sc">{{ ev.usrNm }}<br><span>{{ c.criteriaNm }}</span></th>
                </template>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in results" :key="r.vdCd">
                <td>{{ r.vdNm }} <span class="dim">({{ r.vdCd }})</span></td>
                <template v-for="ev in evaluators" :key="ev.id">
                  <td v-for="c in criteria" :key="c.id"><input type="number" min="0" max="100" :disabled="readonly" v-model.number="scoreMap[key(r.vdCd, c.id!, ev.id!)]" class="num" /></td>
                </template>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="box">
        <div class="bh">종합 평가결과</div>
        <table class="t">
          <thead><tr><th>순위</th><th>협력사</th><th class="r">견적금액</th><th class="r">비가격</th><th class="r">가격</th><th class="r">종합점수</th><th v-if="!readonly"></th></tr></thead>
          <tbody>
            <tr v-for="r in results" :key="r.vdCd" :class="{ top: r.rankNo === 1 }">
              <td><span class="rank" :class="{ first: r.rankNo === 1 }">{{ r.rankNo }}</span></td>
              <td>{{ r.vdNm }} <span class="dim">({{ r.vdCd }})</span></td>
              <td class="r">{{ fmt(r.quoteAmt) }}</td><td class="r">{{ r.nonPriceScore }}</td><td class="r">{{ r.priceScore }}</td>
              <td class="r"><b>{{ r.totalScore }}</b></td>
              <td v-if="!readonly"><button v-if="r.rankNo === 1" class="btn sm go" @click="emit('select', r.vdCd)">선정</button></td>
            </tr>
            <tr v-if="!results.length"><td colspan="7" class="empty">견적 응답 협력사가 없습니다.</td></tr>
          </tbody>
        </table>
      </div>
    </template>
    <UserPicker v-if="showUserPicker" @select="onUserSelect" @close="showUserPicker = false" />
  </div>
</template>

<style scoped>
.rfp { padding: 12px 14px; }
.rhead { display: flex; align-items: center; gap: 14px; margin-bottom: 8px; }
.chk { display: flex; align-items: center; gap: 6px; font-weight: bold; color: #1a3a6b; }
.wsum { font-size: 13px; color: #1a7f37; } .wsum.warn { color: #c0392b; }
.rmsg { color: #1a7f37; background: #eafaef; padding: 6px 10px; border-radius: 6px; font-size: 13px; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 10px; }
.box { border: 1px solid #eee; border-radius: 8px; margin-bottom: 12px; }
.bh { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; font-weight: bold; font-size: 14px; display: flex; align-items: center; gap: 10px; }
.t { width: 100%; border-collapse: collapse; }
.t th, .t td { padding: 7px 10px; border-bottom: 1px solid #f3f3f3; font-size: 13px; text-align: left; } .t th { background: #fafafa; } .t th.r, .t td.r { text-align: right; }
.t input { padding: 5px 7px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; } .num { text-align: right; }
.cfg { display: flex; gap: 12px; padding: 12px; } .cfg label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .cfg input, .cfg select { padding: 7px; border: 1px solid #ddd; border-radius: 5px; }
.evals { padding: 0 12px 12px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; } .lbl { font-size: 13px; color: #555; }
.echip { background: #eef; color: #445; border-radius: 12px; padding: 3px 10px; font-size: 13px; } .echip .x { border: none; background: none; cursor: pointer; color: #889; margin-left: 4px; }
.acts { margin: 4px 0 12px; } .scroll { overflow-x: auto; }
.score th.sc { text-align: center; font-size: 11px; line-height: 1.3; } .score th.sc span { color: #888; }
.rank { display: inline-block; width: 22px; height: 22px; line-height: 22px; text-align: center; border-radius: 50%; background: #eee; color: #777; font-size: 12px; } .rank.first { background: #1a7f37; color: #fff; }
.top { background: #f3faf5; } .dim { color: #aaa; font-size: 12px; }
.empty { text-align: center; color: #aaa; padding: 16px; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 9px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; font-size: 13px; }
</style>
