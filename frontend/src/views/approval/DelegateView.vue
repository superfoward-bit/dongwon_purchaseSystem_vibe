<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'
import UserPicker from '@/components/UserPicker.vue'

interface Deleg { id?: number; fromUsrId?: string; fromUsrNm?: string; toUsrId?: string; toUsrNm?: string; validSd?: string; validEd?: string; remark?: string; useYn?: string }
const list = ref<Deleg[]>([])
const message = ref('')
const showForm = ref(false)
const form = ref<Deleg>(blank())
const pickTarget = ref<'from' | 'to' | null>(null)

function blank(): Deleg { return { validSd: '2026-01-01', validEd: '2026-12-31', useYn: 'Y' } }
async function load() { list.value = (await http.get('/approval/delegate')).data.data }
function openNew() { form.value = blank(); showForm.value = true }
function openEdit(d: Deleg) { form.value = { ...d }; showForm.value = true }
function onUserSelect(u: { usrId: string; usrNm: string }) {
  if (pickTarget.value === 'from') { form.value.fromUsrId = u.usrId; form.value.fromUsrNm = u.usrNm }
  else if (pickTarget.value === 'to') { form.value.toUsrId = u.usrId; form.value.toUsrNm = u.usrNm }
  pickTarget.value = null
}
async function save() {
  message.value = ''
  if (!form.value.fromUsrId || !form.value.toUsrId) { message.value = '위임자/대결자를 선택하세요.'; return }
  try {
    if (form.value.id) await http.put(`/approval/delegate/${form.value.id}`, form.value)
    else await http.post('/approval/delegate', form.value)
    showForm.value = false; await load(); message.value = '저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function remove(d: Deleg) { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/approval/delegate/${d.id}`); await load() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head"><h2>결재 위임/대결</h2><button class="btn primary" @click="openNew">+ 신규</button></div>
    <p v-if="message" class="msg">{{ message }}</p>
    <p class="desc">위임자의 결재 차례를 대결자가 대신 처리할 수 있습니다. 유효기간 내 결재 건이 대결자 결재함에 함께 표시됩니다.</p>

    <div class="panel">
      <table>
        <thead><tr><th>위임자(원결재자)</th><th>대결자(수임자)</th><th>유효기간</th><th>사용</th><th>비고</th><th></th></tr></thead>
        <tbody>
          <tr v-for="d in list" :key="d.id">
            <td>{{ d.fromUsrNm }} <span class="dim">({{ d.fromUsrId }})</span></td>
            <td>{{ d.toUsrNm }} <span class="dim">({{ d.toUsrId }})</span></td>
            <td>{{ d.validSd }} ~ {{ d.validEd }}</td>
            <td><span class="badge" :class="d.useYn === 'Y' ? 'on' : 'off'">{{ d.useYn === 'Y' ? '사용' : '중지' }}</span></td>
            <td>{{ d.remark }}</td>
            <td class="rgt"><button class="link" @click="openEdit(d)">수정</button><button class="link del" @click="remove(d)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="6" class="empty">등록된 위임이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-bg" @click.self="showForm = false">
      <div class="modal">
        <div class="modal-head">위임/대결 {{ form.id ? '수정' : '등록' }} <button class="x" @click="showForm = false">×</button></div>
        <div class="modal-body">
          <label class="w2">위임자(원결재자)
            <div class="picker"><input :value="form.fromUsrNm ? `${form.fromUsrNm} (${form.fromUsrId})` : ''" readonly /><button class="btn sm" @click="pickTarget = 'from'">찾기</button></div>
          </label>
          <label class="w2">대결자(수임자)
            <div class="picker"><input :value="form.toUsrNm ? `${form.toUsrNm} (${form.toUsrId})` : ''" readonly /><button class="btn sm" @click="pickTarget = 'to'">찾기</button></div>
          </label>
          <label>유효 시작 <input type="date" v-model="form.validSd" /></label>
          <label>유효 종료 <input type="date" v-model="form.validEd" /></label>
          <label>사용 <select v-model="form.useYn"><option value="Y">사용</option><option value="N">중지</option></select></label>
          <label class="w2">비고 <input v-model="form.remark" /></label>
        </div>
        <div class="modal-foot"><button class="btn primary" @click="save">저장</button><button class="btn" @click="showForm = false">취소</button></div>
      </div>
    </div>
    <UserPicker v-if="pickTarget" @select="onUserSelect" @close="pickTarget = null" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; } .head .primary { margin-left: auto; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } td.rgt { text-align: right; }
.dim { color: #aaa; font-size: 12px; } .empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 5px 10px; } .btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-left: 6px; } .link.del { color: #d33; }
.badge { padding: 2px 9px; border-radius: 10px; font-size: 12px; } .badge.on { background: #e3f6e8; color: #1a7f37; } .badge.off { background: #eee; color: #888; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 460px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 16px 18px; display: flex; flex-wrap: wrap; gap: 12px; } .modal-body label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 45%; } .modal-body .w2 { flex: 1 1 100%; }
.modal-body input, .modal-body select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .picker { display: flex; gap: 6px; } .picker input { flex: 1; }
.modal-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
</style>
