<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface CodeGrp { grpCd: string; grpNm: string }
interface Code {
  id?: number; grpCd: string; cd: string; cdNmKo: string; cdNmEn?: string;
  sortNo?: number; useYn?: string
}

const groups = ref<CodeGrp[]>([])
const codes = ref<Code[]>([])
const selectedGrp = ref<string>('')
const editing = ref<Code | null>(null)
const message = ref('')

async function loadGroups() {
  const { data } = await http.get('/sys/code/groups')
  groups.value = data.data
  if (groups.value.length && !selectedGrp.value) selectGroup(groups.value[0].grpCd)
}
async function selectGroup(grpCd: string) {
  selectedGrp.value = grpCd
  editing.value = null
  const { data } = await http.get('/sys/code/codes', { params: { grpCd } })
  codes.value = data.data
}
function addNew() {
  editing.value = { grpCd: selectedGrp.value, cd: '', cdNmKo: '', cdNmEn: '', sortNo: 1, useYn: 'Y' }
}
function edit(c: Code) {
  editing.value = { ...c }
}
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) {
      await http.put(`/sys/code/codes/${editing.value.id}`, editing.value)
    } else {
      await http.post('/sys/code/codes', editing.value)
    }
    message.value = '저장되었습니다.'
    editing.value = null
    await selectGroup(selectedGrp.value)
  } catch (e: any) {
    message.value = e.message
  }
}
async function remove(c: Code) {
  if (!c.id || !confirm(`[${c.cd}] 삭제하시겠습니까?`)) return
  await http.delete(`/sys/code/codes/${c.id}`)
  message.value = '삭제되었습니다.'
  await selectGroup(selectedGrp.value)
}

onMounted(loadGroups)
</script>

<template>
  <div class="page">
    <h2>공통코드 관리</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="cols">
      <!-- 코드그룹 -->
      <div class="panel grp-panel">
        <div class="panel-head">코드그룹</div>
        <ul>
          <li
            v-for="g in groups" :key="g.grpCd"
            :class="{ active: g.grpCd === selectedGrp }"
            @click="selectGroup(g.grpCd)"
          >
            <strong>{{ g.grpCd }}</strong><span>{{ g.grpNm }}</span>
          </li>
        </ul>
      </div>

      <!-- 코드상세 -->
      <div class="panel">
        <div class="panel-head">
          코드상세 <small>({{ selectedGrp }})</small>
          <button class="btn" @click="addNew">+ 추가</button>
        </div>
        <table>
          <thead>
            <tr><th>코드</th><th>코드명(국문)</th><th>코드명(영문)</th><th>정렬</th><th>사용</th><th></th></tr>
          </thead>
          <tbody>
            <tr v-for="c in codes" :key="c.id">
              <td>{{ c.cd }}</td><td>{{ c.cdNmKo }}</td><td>{{ c.cdNmEn }}</td>
              <td>{{ c.sortNo }}</td><td>{{ c.useYn }}</td>
              <td>
                <button class="link" @click="edit(c)">수정</button>
                <button class="link del" @click="remove(c)">삭제</button>
              </td>
            </tr>
            <tr v-if="!codes.length"><td colspan="6" class="empty">코드가 없습니다.</td></tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 편집 폼 -->
    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '코드 수정' : '코드 등록' }}</div>
      <div class="form">
        <label>코드 <input v-model="editing.cd" :disabled="!!editing.id" /></label>
        <label>코드명(국문) <input v-model="editing.cdNmKo" /></label>
        <label>코드명(영문) <input v-model="editing.cdNmEn" /></label>
        <label>정렬 <input v-model.number="editing.sortNo" type="number" /></label>
        <label>사용
          <select v-model="editing.useYn"><option value="Y">Y</option><option value="N">N</option></select>
        </label>
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
.cols { display: flex; gap: 16px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); flex: 1; }
.grp-panel { max-width: 280px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.panel-head .btn { margin-left: auto; }
ul { list-style: none; margin: 0; padding: 0; }
li { padding: 10px 16px; cursor: pointer; border-bottom: 1px solid #f5f5f5; display: flex; flex-direction: column; }
li.active { background: #eef3fb; }
li span { color: #888; font-size: 12px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; }
th { background: #fafafa; }
.empty { text-align: center; color: #aaa; padding: 24px; }
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
