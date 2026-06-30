<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Role { id?: number; roleId: string; roleNm: string; description?: string; useYn?: string }
interface Menu { menuId: string; menuNm: string; menuLvl?: number; upMenuId?: string | null }
interface RoleFunc { roleId?: string; menuId: string; authCd: string }

const roles = ref<Role[]>([])
const menus = ref<Menu[]>([])
const selectedRole = ref<Role | null>(null)
const editing = ref<Role | null>(null)
const authMap = ref<Record<string, string>>({})   // menuId -> authCd('' = 없음)
const message = ref('')
const AUTH_OPTIONS = ['', 'READ', 'SAVE', 'APPROVAL', 'MANAGEMENT']

async function loadRoles() {
  const { data } = await http.get('/sys/role')
  roles.value = data.data
}
async function loadMenus() {
  const { data } = await http.get('/menu/flat')
  menus.value = data.data
}
async function selectRole(r: Role) {
  selectedRole.value = r
  editing.value = null
  const { data } = await http.get(`/sys/role/${r.roleId}/funcs`)
  const map: Record<string, string> = {}
  for (const m of menus.value) map[m.menuId] = ''
  for (const f of data.data as RoleFunc[]) map[f.menuId] = f.authCd
  authMap.value = map
}
function addNew() { editing.value = { roleId: '', roleNm: '', description: '', useYn: 'Y' }; selectedRole.value = null }
function edit(r: Role) { editing.value = { ...r } }
async function saveRole() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/sys/role/${editing.value.id}`, editing.value)
    else await http.post('/sys/role', editing.value)
    message.value = '역할이 저장되었습니다.'
    editing.value = null
    await loadRoles()
  } catch (e: any) { message.value = e.message }
}
async function removeRole(r: Role) {
  if (!r.id || !confirm(`[${r.roleNm}] 역할을 삭제하시겠습니까?`)) return
  await http.delete(`/sys/role/${r.id}`)
  message.value = '삭제되었습니다.'
  if (selectedRole.value?.id === r.id) selectedRole.value = null
  await loadRoles()
}
async function savePermissions() {
  if (!selectedRole.value) return
  const funcs: RoleFunc[] = Object.entries(authMap.value)
    .filter(([, auth]) => auth)
    .map(([menuId, authCd]) => ({ menuId, authCd }))
  await http.put(`/sys/role/${selectedRole.value.roleId}/funcs`, funcs)
  message.value = '권한이 저장되었습니다.'
}

onMounted(async () => { await loadMenus(); await loadRoles() })
</script>

<template>
  <div class="page">
    <h2>권한 관리 (역할)</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="cols">
      <!-- 역할 목록 -->
      <div class="panel role-panel">
        <div class="panel-head">역할 <button class="btn" @click="addNew">+ 추가</button></div>
        <ul>
          <li v-for="r in roles" :key="r.roleId" :class="{ active: r.roleId === selectedRole?.roleId }" @click="selectRole(r)">
            <div><strong>{{ r.roleId }}</strong><span>{{ r.roleNm }}</span></div>
            <div class="row-actions">
              <button class="link" @click.stop="edit(r)">수정</button>
              <button class="link del" @click.stop="removeRole(r)">삭제</button>
            </div>
          </li>
        </ul>
      </div>

      <!-- 메뉴권한 매트릭스 -->
      <div class="panel">
        <div class="panel-head">
          메뉴 권한 <small v-if="selectedRole">({{ selectedRole.roleNm }})</small>
          <button v-if="selectedRole" class="btn primary" @click="savePermissions">권한 저장</button>
        </div>
        <table v-if="selectedRole">
          <thead><tr><th>메뉴</th><th style="width:160px">권한</th></tr></thead>
          <tbody>
            <tr v-for="m in menus" :key="m.menuId">
              <td :style="{ paddingLeft: ((m.menuLvl || 1) - 1) * 16 + 12 + 'px' }">{{ m.menuNm }}</td>
              <td>
                <select v-model="authMap[m.menuId]">
                  <option v-for="a in AUTH_OPTIONS" :key="a" :value="a">{{ a || '(없음)' }}</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty">좌측에서 역할을 선택하세요.</p>
      </div>
    </div>

    <!-- 역할 편집 -->
    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '역할 수정' : '역할 등록' }}</div>
      <div class="form">
        <label>역할ID <input v-model="editing.roleId" :disabled="!!editing.id" /></label>
        <label>역할명 <input v-model="editing.roleNm" /></label>
        <label>설명 <input v-model="editing.description" /></label>
        <label>사용 <select v-model="editing.useYn"><option value="Y">Y</option><option value="N">N</option></select></label>
        <div class="actions">
          <button class="btn primary" @click="saveRole">저장</button>
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
.role-panel { max-width: 320px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.panel-head .btn, .panel-head .primary { margin-left: auto; }
ul { list-style: none; margin: 0; padding: 0; }
li { padding: 10px 16px; cursor: pointer; border-bottom: 1px solid #f5f5f5; display: flex; justify-content: space-between; align-items: center; }
li.active { background: #eef3fb; }
li span { color: #888; font-size: 12px; margin-left: 8px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; }
th { background: #fafafa; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 5px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; }
.link.del { color: #d33; }
select { padding: 6px; border: 1px solid #ddd; border-radius: 5px; width: 100%; }
.editor { margin-top: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; }
.form input { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.actions { display: flex; gap: 8px; }
</style>
