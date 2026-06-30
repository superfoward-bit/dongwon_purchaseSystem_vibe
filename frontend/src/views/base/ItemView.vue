<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Item {
  id?: number; itemCd?: string; itemNm?: string; itemNmEn?: string; spec?: string;
  cateCd?: string; cateNm?: string; unitCd?: string; itemTyp?: string; itemTypNm?: string;
  taxTyp?: string; safetyStock?: number; reorderPoint?: number; orderQty?: number; moq?: number; leadTime?: number; repVdCd?: string; repVdNm?: string; itemSts?: string; itemStsNm?: string; remark?: string
}
interface Code { cd: string; cdNmKo: string }
interface Cate { cateCd: string; cateNm: string }

const list = ref<Item[]>([])
const keyword = ref('')
const cateCd = ref('')
const editing = ref<Item | null>(null)
const message = ref('')
const typeCodes = ref<Code[]>([])
const stsCodes = ref<Code[]>([])
const unitCodes = ref<Code[]>([])
const cates = ref<Cate[]>([])

async function load() {
  const { data } = await http.get('/base/item', { params: { keyword: keyword.value, cateCd: cateCd.value } })
  list.value = data.data
}
async function loadRefs() {
  typeCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'ITEM_TYP' } })).data.data
  stsCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'ITEM_STS' } })).data.data
  unitCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'UNIT_CD' } })).data.data
  cates.value = (await http.get('/base/category')).data.data
}
function addNew() { editing.value = { itemNm: '', itemTyp: 'RAW', unitCd: 'EA', itemSts: 'ACTIVE', taxTyp: 'TAX' } }
async function edit(it: Item) { editing.value = (await http.get(`/base/item/${it.id}`)).data.data }
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/base/item/${editing.value.id}`, editing.value)
    else await http.post('/base/item', editing.value)
    message.value = '저장되었습니다.'; editing.value = null; await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(it: Item) {
  if (!it.id || !confirm(`[${it.itemNm}] 삭제하시겠습니까?`)) return
  await http.delete(`/base/item/${it.id}`); message.value = '삭제되었습니다.'; await load()
}

onMounted(async () => { await loadRefs(); await load() })
</script>

<template>
  <div class="page">
    <h2>품목 관리</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="panel-head">
        품목 목록
        <input class="search" v-model="keyword" placeholder="코드/품목명" @keyup.enter="load" />
        <select v-model="cateCd" @change="load"><option value="">전체 분류</option><option v-for="c in cates" :key="c.cateCd" :value="c.cateCd">{{ c.cateNm }}</option></select>
        <button class="btn" @click="load">검색</button>
        <button class="btn primary" @click="addNew">+ 추가</button>
      </div>
      <table>
        <thead><tr><th>품목코드</th><th>품목명</th><th>규격</th><th>분류</th><th>단위</th><th>유형</th><th>상태</th><th></th></tr></thead>
        <tbody>
          <tr v-for="it in list" :key="it.id">
            <td>{{ it.itemCd }}</td><td>{{ it.itemNm }}</td><td>{{ it.spec }}</td><td>{{ it.cateNm }}</td>
            <td>{{ it.unitCd }}</td><td>{{ it.itemTypNm }}</td>
            <td><span class="badge" :class="it.itemSts">{{ it.itemStsNm }}</span></td>
            <td><button class="link" @click="edit(it)">수정</button><button class="link del" @click="remove(it)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="8" class="empty">품목이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '품목 수정' : '품목 등록' }}<small v-if="editing.itemCd"> ({{ editing.itemCd }})</small></div>
      <div class="form">
        <label class="w2">품목명 <input v-model="editing.itemNm" /></label>
        <label>영문명 <input v-model="editing.itemNmEn" /></label>
        <label class="w2">규격 <input v-model="editing.spec" /></label>
        <label>분류 <select v-model="editing.cateCd"><option value="">(선택)</option><option v-for="c in cates" :key="c.cateCd" :value="c.cateCd">{{ c.cateNm }}</option></select></label>
        <label>단위 <select v-model="editing.unitCd"><option v-for="c in unitCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>유형 <select v-model="editing.itemTyp"><option v-for="c in typeCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>과세 <select v-model="editing.taxTyp"><option value="TAX">과세</option><option value="FREE">면세</option></select></label>
        <label>상태 <select v-model="editing.itemSts"><option v-for="c in stsCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <div class="sep">발주정책 (MRP/자동발주)</div>
        <label>안전재고 <input type="number" v-model.number="editing.safetyStock" /></label>
        <label>발주점 <input type="number" v-model.number="editing.reorderPoint" /></label>
        <label>1회발주량 <input type="number" v-model.number="editing.orderQty" /></label>
        <label>최소발주량 <input type="number" v-model.number="editing.moq" /></label>
        <label>리드타임(일) <input type="number" v-model.number="editing.leadTime" /></label>
        <label>대표협력사코드 <input v-model="editing.repVdCd" placeholder="VD-00001" /></label>
        <label>대표협력사명 <input v-model="editing.repVdNm" /></label>
        <label class="w3">비고 <input v-model="editing.remark" /></label>
        <div class="actions"><button class="btn primary" @click="save">저장</button><button class="btn" @click="editing = null">취소</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.search { margin-left: auto; padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; }
.panel-head select { padding: 6px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-right: 6px; } .link.del { color: #d33; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.NEW { background: #fff3d6; color: #9a6b00; } .badge.ACTIVE { background: #e3f6e8; color: #1a7f37; } .badge.STOP { background: #f3e3e3; color: #a33; }
.editor { margin-top: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form .w2 { flex: 1 1 220px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.actions { display: flex; gap: 8px; width: 100%; }
.form .sep { flex: 1 1 100%; font-weight: bold; color: #1a3a6b; font-size: 13px; border-top: 1px dashed #ddd; padding-top: 8px; margin-top: 4px; }
</style>
