<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'

interface Item { itemCd?: string; itemNm?: string; lotNo?: string; unitCd?: string; bookQty?: number; realQty?: number }
interface Count { id?: number; countNo?: string; whCd?: string; whNm?: string; countYmd?: string; sts?: string; stsNm?: string; remark?: string; items: Item[] }
const route = useRoute(); const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const cnt = ref<Count>({ countYmd: today(), items: [] })
const whCodes = ref<{ cd: string; cdNmKo: string }[]>([])
function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || cnt.value.sts === 'TMP')
function diff(it: Item) { return (Number(it.realQty) || 0) - (Number(it.bookQty) || 0) }

async function load() {
  whCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'WH' } })).data.data
  if (isNew.value) return
  cnt.value = { ...(await http.get(`/inv/count/${route.params.id}`)).data.data }
  cnt.value.items = cnt.value.items || []
}
async function loadStock() {
  if (!cnt.value.whCd) { message.value = '창고를 먼저 선택하세요.'; return }
  const rows = (await http.get('/inv/stock', { params: { whCd: cnt.value.whCd } })).data.data
  cnt.value.items = rows.map((s: any) => ({ itemCd: s.itemCd, itemNm: s.itemNm, lotNo: s.lotNo, unitCd: s.unitCd, bookQty: s.qty, realQty: s.qty }))
  message.value = `장부재고 ${rows.length}건 불러왔습니다. 실사수량을 입력하세요.`
}
async function save() {
  message.value = ''
  if (!cnt.value.whCd) { message.value = '창고를 선택하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/inv/count', cnt.value); router.replace(`/inv/count/${data.data}`); await reload(data.data) }
    else { await http.put(`/inv/count/${cnt.value.id}`, cnt.value); await reload(cnt.value.id!) }
    message.value = '저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function reload(id: any) { cnt.value = { ...(await http.get(`/inv/count/${id}`)).data.data }; cnt.value.items = cnt.value.items || [] }
async function confirm() {
  if (!confirm('실사 확정 시 차이만큼 재고가 조정됩니다. 진행할까요?')) return
  try { await http.post(`/inv/count/${cnt.value.id}/confirm`, {}); await reload(cnt.value.id); message.value = '실사 확정(재고 조정) 완료' }
  catch (e: any) { message.value = e.message }
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head"><h2>재고실사 {{ isNew ? '등록' : cnt.countNo }} <span v-if="cnt.stsNm" class="badge" :class="cnt.sts">{{ cnt.stsNm }}</span></h2>
      <div class="ha">
        <button class="btn" @click="router.push('/inv/count')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">저장</button>
        <button v-if="!isNew && cnt.sts === 'TMP'" class="btn go" @click="confirm">실사확정</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="form">
        <label>창고 <select v-model="cnt.whCd" :disabled="!editable"><option value="">선택</option><option v-for="c in whCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>실사일 <input type="date" v-model="cnt.countYmd" :disabled="!editable" /></label>
        <label class="w3">비고 <input v-model="cnt.remark" :disabled="!editable" /></label>
        <button v-if="editable" class="btn" @click="loadStock">장부재고 불러오기</button>
      </div>
    </div>
    <div class="panel">
      <div class="panel-head">실사 품목</div>
      <table>
        <thead><tr><th>품목코드</th><th>품목명</th><th>LOT</th><th>단위</th><th class="r">장부수량</th><th class="r">실사수량</th><th class="r">차이</th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in cnt.items" :key="i" :class="{ diff: diff(it) !== 0 }">
            <td>{{ it.itemCd }}</td><td>{{ it.itemNm }}</td><td>{{ it.lotNo === '*' ? '-' : it.lotNo }}</td><td>{{ it.unitCd }}</td>
            <td class="r">{{ fmt(it.bookQty) }}</td>
            <td class="r"><input type="number" v-model.number="it.realQty" :disabled="!editable" class="num" /></td>
            <td class="r" :class="diff(it) < 0 ? 'minus' : diff(it) > 0 ? 'plus' : ''">{{ diff(it) > 0 ? '+' : '' }}{{ fmt(diff(it)) }}</td>
          </tr>
          <tr v-if="!cnt.items.length"><td colspan="7" class="empty">창고 선택 후 '장부재고 불러오기'를 누르세요.</td></tr>
        </tbody>
      </table>
      <p class="hint">※ 실사확정 시 차이(실사−장부)만큼 재고가 조정(ADJ)되고 수불부에 기록됩니다.</p>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; } .ha { margin-left: auto; display: flex; gap: 6px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 14px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; }
.form { padding: 14px 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 3px; } .form .w3 { flex: 1 1 40%; } .form input, .form select { padding: 7px 10px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; } .num { width: 90px; text-align: right; }
tr.diff { background: #fffbe9; } .minus { color: #c0392b; font-weight: bold; } .plus { color: #1a7f37; font-weight: bold; }
.empty { text-align: center; color: #aaa; padding: 20px; } .hint { padding: 8px 16px; color: #888; font-size: 12px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; } .badge.TMP { background: #eef; color: #556; } .badge.CFM { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.btn { padding: 7px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
</style>
