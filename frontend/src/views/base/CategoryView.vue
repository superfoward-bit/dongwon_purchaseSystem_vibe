<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Cate {
  id?: number; cateCd?: string; cateNm?: string; upCateCd?: string | null;
  cateLvl?: number; sortNo?: number; leafYn?: string; useYn?: string
}

const list = ref<Cate[]>([])
const editing = ref<Cate | null>(null)
const message = ref('')

async function load() { list.value = (await http.get('/base/category')).data.data }
function addNew() { editing.value = { cateCd: '', cateNm: '', upCateCd: null, cateLvl: 1, sortNo: 1, leafYn: 'Y', useYn: 'Y' } }
function edit(c: Cate) { editing.value = { ...c } }
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/base/category/${editing.value.id}`, editing.value)
    else await http.post('/base/category', editing.value)
    message.value = '저장되었습니다.'; editing.value = null; await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(c: Cate) {
  if (!c.id || !confirm(`[${c.cateNm}] 삭제하시겠습니까?`)) return
  try { await http.delete(`/base/category/${c.id}`, { params: { cateCd: c.cateCd } }); message.value = '삭제되었습니다.'; await load() }
  catch (e: any) { message.value = e.message }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h2>품목분류 관리</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="panel-head">분류 목록 <button class="btn" @click="addNew">+ 추가</button></div>
      <table>
        <thead><tr><th>분류코드</th><th>분류명</th><th>상위</th><th>레벨</th><th>정렬</th><th>최하위</th><th>사용</th><th></th></tr></thead>
        <tbody>
          <tr v-for="c in list" :key="c.id">
            <td>{{ c.cateCd }}</td>
            <td :style="{ paddingLeft: ((c.cateLvl||1)-1)*18+12+'px' }">{{ c.cateNm }}</td>
            <td>{{ c.upCateCd }}</td><td>{{ c.cateLvl }}</td><td>{{ c.sortNo }}</td>
            <td>{{ c.leafYn }}</td><td>{{ c.useYn }}</td>
            <td><button class="link" @click="edit(c)">수정</button><button class="link del" @click="remove(c)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="8" class="empty">분류가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '분류 수정' : '분류 등록' }}</div>
      <div class="form">
        <label>분류코드 <input v-model="editing.cateCd" :disabled="!!editing.id" /></label>
        <label>분류명 <input v-model="editing.cateNm" /></label>
        <label>상위분류
          <select v-model="editing.upCateCd">
            <option :value="null">(최상위)</option>
            <option v-for="c in list" :key="c.cateCd" :value="c.cateCd">{{ c.cateCd }} - {{ c.cateNm }}</option>
          </select>
        </label>
        <label>레벨 <input type="number" v-model.number="editing.cateLvl" /></label>
        <label>정렬 <input type="number" v-model.number="editing.sortNo" /></label>
        <label>최하위 <select v-model="editing.leafYn"><option value="Y">Y</option><option value="N">N</option></select></label>
        <label>사용 <select v-model="editing.useYn"><option value="Y">Y</option><option value="N">N</option></select></label>
        <div class="actions"><button class="btn primary" @click="save">저장</button><button class="btn" @click="editing = null">취소</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; }
.panel-head .btn { margin-left: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-right: 6px; } .link.del { color: #d33; }
.editor { margin-top: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.actions { display: flex; gap: 8px; }
</style>
