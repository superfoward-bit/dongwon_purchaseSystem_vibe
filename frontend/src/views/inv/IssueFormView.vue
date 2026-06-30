<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import ItemPicker from '@/components/ItemPicker.vue'

interface Item { itemCd?: string; itemNm?: string; lotNo?: string; unitCd?: string; qty?: number; _lots?: any[] }
interface Issue { id?: number; issueNo?: string; issueTyp?: string; whCd?: string; whNm?: string; reqDept?: string; issueYmd?: string; sts?: string; stsNm?: string; remark?: string; items: Item[] }
const route = useRoute(); const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const iss = ref<Issue>({ issueTyp: 'PROD', issueYmd: today(), items: [] })
const whCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const pickRow = ref<number | null>(null)
function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || iss.value.sts === 'TMP')

async function load() {
  whCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'WH' } })).data.data
  if (isNew.value) { addRow(); return }
  iss.value = { ...(await http.get(`/inv/issue/${route.params.id}`)).data.data }
  iss.value.items = iss.value.items || []
}
function addRow() { iss.value.items.push({ qty: 0 }) }
function delRow(i: number) { iss.value.items.splice(i, 1) }
async function onItemSelect(it: any) {
  if (pickRow.value === null) return
  const row = iss.value.items[pickRow.value]
  row.itemCd = it.itemCd; row.itemNm = it.itemNm; row.unitCd = it.unitCd
  // 해당 품목의 LOT별 재고 로드
  try { row._lots = (await http.get('/inv/stock/by-item', { params: { itemCd: it.itemCd, whCd: iss.value.whCd } })).data.data } catch { row._lots = [] }
  pickRow.value = null
}
async function save() {
  message.value = ''
  if (!iss.value.whCd) { message.value = '창고를 선택하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/inv/issue', iss.value); router.replace(`/inv/issue/${data.data}`); await reload(data.data) }
    else { await http.put(`/inv/issue/${iss.value.id}`, iss.value); await reload(iss.value.id!) }
    message.value = '저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function reload(id: any) { iss.value = { ...(await http.get(`/inv/issue/${id}`)).data.data }; iss.value.items = iss.value.items || [] }
async function confirm() {
  if (!confirm('출고 확정 시 재고가 차감됩니다. 진행할까요?')) return
  try { await http.post(`/inv/issue/${iss.value.id}/confirm`, {}); await reload(iss.value.id); message.value = '출고 확정(재고 차감) 완료' }
  catch (e: any) { message.value = e.message }
}
async function cancel() { if (!confirm('취소하시겠습니까?')) return; await http.post(`/inv/issue/${iss.value.id}/cancel`, {}); await reload(iss.value.id) }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head"><h2>출고 {{ isNew ? '등록' : iss.issueNo }} <span v-if="iss.stsNm" class="badge" :class="iss.sts">{{ iss.stsNm }}</span></h2>
      <div class="ha">
        <button class="btn" @click="router.push('/inv/issue')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">저장</button>
        <button v-if="!isNew && iss.sts === 'TMP'" class="btn go" @click="confirm">출고확정</button>
        <button v-if="!isNew && iss.sts === 'TMP'" class="btn del" @click="cancel">취소</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="form">
        <label>출고유형 <select v-model="iss.issueTyp" :disabled="!editable"><option value="PROD">생산출고</option><option value="DEPT">부서불출</option><option value="RTN">반품출고</option><option value="DISP">폐기</option></select></label>
        <label>창고 <select v-model="iss.whCd" :disabled="!editable"><option value="">선택</option><option v-for="c in whCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>요청부서 <input v-model="iss.reqDept" :disabled="!editable" /></label>
        <label>출고일 <input type="date" v-model="iss.issueYmd" :disabled="!editable" /></label>
        <label class="w3">비고 <input v-model="iss.remark" :disabled="!editable" /></label>
      </div>
    </div>
    <div class="panel">
      <div class="panel-head">출고 품목 <button v-if="editable" class="btn sm" @click="addRow">+ 행추가</button></div>
      <table>
        <thead><tr><th>품목코드</th><th>품목명</th><th>LOT</th><th>단위</th><th class="r">출고수량</th><th v-if="editable"></th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in iss.items" :key="i">
            <td><div class="picker"><input :value="it.itemCd" readonly style="width:80px" /><button v-if="editable" class="btn sm" @click="pickRow = i">🔍</button></div></td>
            <td><input v-model="it.itemNm" :disabled="!editable" /></td>
            <td>
              <select v-if="editable && it._lots && it._lots.length" v-model="it.lotNo">
                <option value="">LOT 선택</option>
                <option v-for="(l, j) in it._lots" :key="j" :value="l.lotNo">{{ l.lotNo === '*' ? '(無)' : l.lotNo }} · 재고 {{ fmt(l.qty) }}{{ l.expYmd ? ' · 만료 ' + l.expYmd : '' }}</option>
              </select>
              <input v-else v-model="it.lotNo" :disabled="!editable" style="width:96px" placeholder="LOT" />
            </td>
            <td><input v-model="it.unitCd" :disabled="!editable" style="width:50px" /></td>
            <td class="r"><input type="number" v-model.number="it.qty" :disabled="!editable" class="num" /></td>
            <td v-if="editable"><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
          <tr v-if="!iss.items.length"><td :colspan="editable ? 6 : 5" class="empty">출고 품목을 추가하세요.</td></tr>
        </tbody>
      </table>
      <p class="hint">※ 출고확정 시 선택한 창고·LOT의 재고에서 차감됩니다. 재고 부족 시 차단됩니다.</p>
    </div>
    <ItemPicker v-if="pickRow !== null" @select="onItemSelect" @close="pickRow = null" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; } .ha { margin-left: auto; display: flex; gap: 6px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 14px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; }
.form { padding: 14px 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 3px; } .form .w3 { flex: 1 1 100%; } .form input, .form select { padding: 7px 10px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; } .num { width: 90px; text-align: right; }
.picker { display: flex; gap: 4px; align-items: center; } .empty { text-align: center; color: #aaa; padding: 20px; } .hint { padding: 8px 16px; color: #888; font-size: 12px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; } .badge.TMP { background: #eef; color: #556; } .badge.CFM { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 9px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.del { color: #d33; border-color: #d33; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; font-size: 13px; }
</style>
