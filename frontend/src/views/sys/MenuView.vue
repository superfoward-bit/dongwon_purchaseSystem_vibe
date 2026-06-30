<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Menu {
  id?: number; menuId: string; menuNm: string; upMenuId?: string | null;
  menuLvl?: number; url?: string; icon?: string; sortNo?: number; useYn?: string
}

const list = ref<Menu[]>([])
const editing = ref<Menu | null>(null)
const message = ref('')

async function load() {
  const { data } = await http.get('/menu/flat')
  list.value = data.data
}
function addNew() {
  editing.value = { menuId: '', menuNm: '', upMenuId: null, menuLvl: 1, url: '', icon: '', sortNo: 1, useYn: 'Y' }
}
function edit(m: Menu) { editing.value = { ...m } }
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/menu/${editing.value.id}`, editing.value)
    else await http.post('/menu', editing.value)
    message.value = '저장되었습니다.'
    editing.value = null
    await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(m: Menu) {
  if (!m.id || !confirm(`[${m.menuNm}] 삭제하시겠습니까?`)) return
  try {
    await http.delete(`/menu/${m.id}`, { params: { menuId: m.menuId } })
    message.value = '삭제되었습니다.'
    await load()
  } catch (e: any) { message.value = e.message }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <h2>메뉴 관리</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="panel-head">메뉴 목록 <button class="btn" @click="addNew">+ 추가</button></div>
      <table>
        <thead><tr><th>메뉴ID</th><th>메뉴명</th><th>상위</th><th>레벨</th><th>경로</th><th>정렬</th><th>사용</th><th></th></tr></thead>
        <tbody>
          <tr v-for="m in list" :key="m.id">
            <td>{{ m.menuId }}</td>
            <td :style="{ paddingLeft: ((m.menuLvl || 1) - 1) * 16 + 12 + 'px' }">{{ m.menuNm }}</td>
            <td>{{ m.upMenuId }}</td><td>{{ m.menuLvl }}</td><td>{{ m.url }}</td>
            <td>{{ m.sortNo }}</td><td>{{ m.useYn }}</td>
            <td>
              <button class="link" @click="edit(m)">수정</button>
              <button class="link del" @click="remove(m)">삭제</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '메뉴 수정' : '메뉴 등록' }}</div>
      <div class="form">
        <label>메뉴ID <input v-model="editing.menuId" :disabled="!!editing.id" /></label>
        <label>메뉴명 <input v-model="editing.menuNm" /></label>
        <label>상위메뉴
          <select v-model="editing.upMenuId">
            <option :value="null">(최상위)</option>
            <option v-for="m in list" :key="m.menuId" :value="m.menuId">{{ m.menuId }} - {{ m.menuNm }}</option>
          </select>
        </label>
        <label>레벨 <input v-model.number="editing.menuLvl" type="number" /></label>
        <label>경로(URL) <input v-model="editing.url" placeholder="/sys/xxx" /></label>
        <label>아이콘 <input v-model="editing.icon" /></label>
        <label>정렬 <input v-model.number="editing.sortNo" type="number" /></label>
        <label>사용 <select v-model="editing.useYn"><option value="Y">Y</option><option value="N">N</option></select></label>
        <div class="actions">
          <button class="btn primary" @click="save">저장</button>
          <button class="btn" @click="editing = null">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
h2 { margin: 0 0 16px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; }
.panel-head .btn { margin-left: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; }
th { background: #fafafa; }
.btn { padding: 5px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-right: 6px; }
.link.del { color: #d33; }
.editor { margin-top: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.actions { display: flex; gap: 8px; }
</style>
