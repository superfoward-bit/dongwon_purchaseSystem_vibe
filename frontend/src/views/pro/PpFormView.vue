<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import ItemPicker from '@/components/ItemPicker.vue'

interface Item { itemCd?: string; itemNm?: string; unitCd?: string; planQty?: number; planAmt?: number; actualAmt?: number; remark?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Pp {
  id?: number; planNo?: string; planYear?: string; planNm?: string; totPlanAmt?: number; totActualAmt?: number;
  sts?: string; stsNm?: string; remark?: string; items: Item[]; actions?: Action[]; history?: His[]
}
const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const pp = ref<Pp>({ planYear: String(new Date().getFullYear()), items: [] })
const itemPickerRow = ref<number | null>(null)

const editable = computed(() => isNew.value || pp.value.sts === 'DRAFT')
const totPlan = computed(() => pp.value.items.reduce((s, i) => s + (Number(i.planAmt) || 0), 0))
const totActual = computed(() => pp.value.items.reduce((s, i) => s + (Number(i.actualAmt) || 0), 0))
function rate(plan?: number, actual?: number) { const p = Number(plan) || 0; return p === 0 ? 0 : Math.round((Number(actual) || 0) / p * 100) }

async function load() { if (!isNew.value) { await reloadById(route.params.id as string); return } addRow() }
async function reloadById(id: string | number) { const { data } = await http.get(`/pro/pp/${id}`); pp.value = { ...data.data, items: data.data.items || [] } }
function addRow() { pp.value.items.push({ planQty: 0, planAmt: 0 }) }
function delRow(i: number) { pp.value.items.splice(i, 1) }
function onItemSelect(it: any) { if (itemPickerRow.value !== null) { const r = pp.value.items[itemPickerRow.value]; r.itemCd = it.itemCd; r.itemNm = it.itemNm; r.unitCd = it.unitCd } itemPickerRow.value = null }
async function save() {
  message.value = ''
  if (!pp.value.planNm) { message.value = '계획명을 입력하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/pro/pp', pp.value); router.replace(`/pro/pp/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.' }
    else { await http.put(`/pro/pp/${pp.value.id}`, pp.value); await reloadById(pp.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/pro/pp/${pp.value.id}/action`, { action: a.action, reason }); await reloadById(pp.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.` } catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/pro/pp/${pp.value.id}`); router.push('/pro/pp') }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>구매계획 {{ isNew ? '등록' : '상세' }}
        <span v-if="pp.planNo" class="prno">{{ pp.planNo }}</span>
        <span v-if="pp.stsNm" class="badge" :class="pp.sts">{{ pp.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/pro/pp')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && pp.sts === 'DRAFT'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in pp.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">기본정보</div>
      <div class="form">
        <label>계획연도 <input v-model="pp.planYear" :disabled="!editable" style="width:90px" /></label>
        <label class="w2">계획명 <input v-model="pp.planNm" :disabled="!editable" /></label>
        <label class="w3">비고 <input v-model="pp.remark" :disabled="!editable" /></label>
      </div>
    </div>

    <div class="panel">
      <div class="panel-head">품목별 계획 / 실적
        <button v-if="editable" class="btn sm" @click="addRow">+ 품목추가</button>
        <span class="tot">계획 {{ fmt(isNew ? totPlan : pp.totPlanAmt) }} · 실적 {{ fmt(totActual) }} · <b :class="{ over: totActual >= (pp.totPlanAmt||totPlan) && (pp.totPlanAmt||totPlan)>0 }">달성 {{ rate(pp.totPlanAmt || totPlan, totActual) }}%</b></span>
      </div>
      <table>
        <thead><tr><th>#</th><th>품목코드</th><th>품목명</th><th>단위</th><th class="r">계획수량</th><th class="r">계획금액</th><th class="r">입고실적</th><th class="r">달성률</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in pp.items" :key="i">
            <td>{{ i + 1 }}</td>
            <td><div class="picker"><input :value="it.itemCd" readonly style="width:80px" /><button v-if="editable" class="btn sm" @click="itemPickerRow = i">🔍</button></div></td>
            <td><input v-model="it.itemNm" :disabled="!editable" /></td>
            <td><input v-model="it.unitCd" :disabled="!editable" style="width:56px" /></td>
            <td class="r"><input type="number" v-model.number="it.planQty" :disabled="!editable" class="num" /></td>
            <td class="r"><input type="number" v-model.number="it.planAmt" :disabled="!editable" class="num" /></td>
            <td class="r">{{ fmt(it.actualAmt) }}</td>
            <td class="r"><span class="rate" :class="{ over: rate(it.planAmt, it.actualAmt) >= 100 }">{{ rate(it.planAmt, it.actualAmt) }}%</span></td>
            <td v-if="editable"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
          <tr v-if="!pp.items.length"><td :colspan="editable ? 9 : 8" class="empty">계획 품목을 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="pp.history && pp.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in pp.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>

    <ItemPicker v-if="itemPickerRow !== null" @select="onItemSelect" @close="itemPickerRow = null" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.prno { font-size: 14px; color: #888; font-weight: normal; } .head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; } .tot { margin-left: auto; color: #1a3a6b; font-weight: normal; } .tot .over { color: #1a7f37; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 180px; } .form .w2 { flex: 1 1 300px; } .form .w3 { flex: 1 1 100%; }
.form input { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
td input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; } .num { text-align: right; } .picker { display: flex; gap: 5px; }
.rate { font-weight: bold; color: #9a6b00; } .rate.over { color: #1a7f37; }
.empty { text-align: center; color: #aaa; padding: 18px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.DRAFT { background: #eef; color: #556; } .badge.CONFIRMED { background: #e3f6e8; color: #1a7f37; } .badge.CLOSED { background: #eee; color: #777; }
</style>
