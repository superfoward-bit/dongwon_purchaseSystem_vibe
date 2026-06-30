<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Cate { cateCd: string; cateNm: string }
interface Attr { attrNm?: string; attrTyp?: string; optionVals?: string; requiredYn?: string }
const cates = ref<Cate[]>([])
const cateCd = ref('')
const attrs = ref<Attr[]>([])
const message = ref('')

async function loadCates() { cates.value = (await http.get('/base/category')).data.data }
async function loadAttrs() {
  if (!cateCd.value) { attrs.value = []; return }
  attrs.value = (await http.get('/base/cate-attr', { params: { cateCd: cateCd.value } })).data.data
}
function addRow() { attrs.value.push({ attrTyp: 'TEXT', requiredYn: 'N' }) }
function delRow(i: number) { attrs.value.splice(i, 1) }
async function save() {
  if (!cateCd.value) { message.value = '분류를 선택하세요.'; return }
  try { await http.put(`/base/cate-attr?cateCd=${cateCd.value}`, attrs.value); message.value = '저장되었습니다.'; await loadAttrs() }
  catch (e: any) { message.value = e.message }
}
onMounted(loadCates)
</script>

<template>
  <div class="page">
    <h2>분류 속성정의</h2>
    <p class="desc">품목 분류별로 추가 속성(원산지·유통기한·보관조건 등)을 정의합니다. 품목 등록요청 시 해당 분류의 속성값을 입력하게 됩니다.</p>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="toolbar">
      <label>분류 <select v-model="cateCd" @change="loadAttrs"><option value="">선택</option><option v-for="c in cates" :key="c.cateCd" :value="c.cateCd">{{ c.cateNm }} ({{ c.cateCd }})</option></select></label>
      <button v-if="cateCd" class="btn" @click="addRow">+ 속성추가</button>
      <button v-if="cateCd" class="btn primary" @click="save">저장</button>
    </div>
    <div v-if="cateCd" class="panel">
      <table>
        <thead><tr><th>#</th><th>속성명</th><th>유형</th><th>선택옵션(콤마구분)</th><th>필수</th><th></th></tr></thead>
        <tbody>
          <tr v-for="(a, i) in attrs" :key="i">
            <td>{{ i + 1 }}</td>
            <td><input v-model="a.attrNm" /></td>
            <td><select v-model="a.attrTyp"><option value="TEXT">텍스트</option><option value="NUM">숫자</option><option value="DATE">날짜</option><option value="SELECT">선택</option></select></td>
            <td><input v-model="a.optionVals" :disabled="a.attrTyp !== 'SELECT'" placeholder="상온,냉장,냉동" /></td>
            <td><input type="checkbox" :checked="a.requiredYn === 'Y'" @change="a.requiredYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /></td>
            <td><button class="link del" @click="delRow(i)">삭제</button></td>
          </tr>
          <tr v-if="!attrs.length"><td colspan="6" class="empty">정의된 속성이 없습니다. 속성을 추가하세요.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; align-items: center; } .toolbar label { font-size: 13px; color: #555; } .toolbar select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
td input, td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; }
</style>
