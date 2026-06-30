<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface User {
  id?: number; compCd?: string; usrId: string; usrNm: string; loginId: string;
  deptCd?: string; posCd?: string; email?: string; mobile?: string;
  usrTyp?: string; vdCd?: string; useYn?: string; roles?: string[]
}
interface Role { roleId: string; roleNm: string }

const list = ref<User[]>([])
const allRoles = ref<Role[]>([])
const keyword = ref('')
const editing = ref<User | null>(null)
const message = ref('')

async function load() {
  const { data } = await http.get('/sys/user', { params: { keyword: keyword.value } })
  list.value = data.data
}
async function loadRoles() {
  const { data } = await http.get('/sys/role')
  allRoles.value = data.data
}
function addNew() {
  editing.value = { usrId: '', usrNm: '', loginId: '', email: '', mobile: '', usrTyp: 'EMP', useYn: 'Y', roles: [] }
}
async function edit(u: User) {
  const { data } = await http.get(`/sys/user/${u.compCd}/${u.usrId}/roles`)
  editing.value = { ...u, roles: data.data }
}
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/sys/user/${editing.value.id}`, editing.value)
    else await http.post('/sys/user', editing.value)
    message.value = editing.value.id ? '수정되었습니다.' : '등록되었습니다. (초기 비밀번호: 1234)'
    editing.value = null
    await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(u: User) {
  if (!u.id || !confirm(`[${u.usrNm}] 사용자를 삭제하시겠습니까?`)) return
  await http.delete(`/sys/user/${u.id}`)
  message.value = '삭제되었습니다.'
  await load()
}
async function resetPwd(u: User) {
  if (!u.id || !confirm(`[${u.usrNm}] 비밀번호를 1234로 초기화할까요?`)) return
  await http.put(`/sys/user/${u.id}/password`)
  message.value = '비밀번호가 1234로 초기화되었습니다.'
}
function toggleRole(roleId: string) {
  if (!editing.value) return
  const r = editing.value.roles || (editing.value.roles = [])
  const i = r.indexOf(roleId)
  if (i >= 0) r.splice(i, 1); else r.push(roleId)
}

onMounted(async () => { await loadRoles(); await load() })
</script>

<template>
  <div class="page">
    <h2>사용자 관리</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="panel-head">
        사용자 목록
        <input class="search" v-model="keyword" placeholder="검색(ID/이름/로그인ID)" @keyup.enter="load" />
        <button class="btn" @click="load">검색</button>
        <button class="btn primary" @click="addNew">+ 추가</button>
      </div>
      <table>
        <thead><tr><th>사용자ID</th><th>이름</th><th>로그인ID</th><th>이메일</th><th>유형</th><th>사용</th><th></th></tr></thead>
        <tbody>
          <tr v-for="u in list" :key="u.id">
            <td>{{ u.usrId }}</td><td>{{ u.usrNm }}</td><td>{{ u.loginId }}</td>
            <td>{{ u.email }}</td><td>{{ u.usrTyp }}</td><td>{{ u.useYn }}</td>
            <td>
              <button class="link" @click="edit(u)">수정</button>
              <button class="link" @click="resetPwd(u)">비번초기화</button>
              <button class="link del" @click="remove(u)">삭제</button>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="empty">사용자가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '사용자 수정' : '사용자 등록' }}</div>
      <div class="form">
        <label>사용자ID <input v-model="editing.usrId" :disabled="!!editing.id" /></label>
        <label>이름 <input v-model="editing.usrNm" /></label>
        <label>로그인ID <input v-model="editing.loginId" :disabled="!!editing.id" /></label>
        <label>이메일 <input v-model="editing.email" /></label>
        <label>휴대폰 <input v-model="editing.mobile" /></label>
        <label>유형 <select v-model="editing.usrTyp"><option value="EMP">임직원</option><option value="VENDOR">협력사</option></select></label>
        <label v-if="editing.usrTyp === 'VENDOR'">협력사코드(VD) <input v-model="editing.vdCd" placeholder="VD-00001" /></label>
        <label>사용 <select v-model="editing.useYn"><option value="Y">Y</option><option value="N">N</option></select></label>
        <div class="roles">
          <span class="roles-title">역할</span>
          <label v-for="r in allRoles" :key="r.roleId" class="chk">
            <input type="checkbox" :checked="editing.roles?.includes(r.roleId)" @change="toggleRole(r.roleId)" />
            {{ r.roleNm }}
          </label>
        </div>
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
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.search { margin-left: auto; padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; }
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
.roles { display: flex; flex-direction: column; gap: 6px; width: 100%; }
.roles-title { font-size: 13px; color: #555; }
.chk { flex-direction: row !important; align-items: center; gap: 6px; }
.actions { display: flex; gap: 8px; }
</style>
