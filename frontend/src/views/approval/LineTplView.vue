<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'
import UserPicker from '@/components/UserPicker.vue'

interface Step { stepNo?: number; lineTyp?: string; aprvUsrId?: string; aprvUsrNm?: string; finalYn?: string }
interface Tpl { id?: number; docTyp?: string; tplNm?: string; amtFrom?: number; amtTo?: number; sortNo?: number; useYn?: string; steps?: Step[] }
const list = ref<Tpl[]>([])
const message = ref('')
const showForm = ref(false)
const form = ref<Tpl>(blank())
const pickRow = ref<number | null>(null)
const DOC_TYPES = ['PR', 'PO', 'GR', 'CL', 'CT', 'IP']

function blank(): Tpl { return { docTyp: 'PO', amtFrom: 0, amtTo: 999999999999, sortNo: 1, useYn: 'Y', steps: [] } }
async function load() { list.value = (await http.get('/approval/template')).data.data }
async function openNew() { form.value = blank(); showForm.value = true }
async function openEdit(t: Tpl) {
  const { data } = await http.get(`/approval/template/${t.id}`)
  form.value = { ...data.data, steps: data.data.steps || [] }
  showForm.value = true
}
function addStep() { form.value.steps!.push({ lineTyp: 'APRV', finalYn: 'N' }) }
function delStep(i: number) { form.value.steps!.splice(i, 1) }
function onUserSelect(u: { usrId: string; usrNm: string }) {
  if (pickRow.value !== null) { const s = form.value.steps![pickRow.value]; s.aprvUsrId = u.usrId; s.aprvUsrNm = u.usrNm }
  pickRow.value = null
}
async function save() {
  message.value = ''
  if (!form.value.tplNm) { message.value = '템플릿명을 입력하세요.'; return }
  try {
    if (form.value.id) await http.put(`/approval/template/${form.value.id}`, form.value)
    else await http.post('/approval/template', form.value)
    showForm.value = false; await load(); message.value = '저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function remove(t: Tpl) { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/approval/template/${t.id}`); await load() }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head"><h2>결재선 템플릿 (금액조건별)</h2><button class="btn primary" @click="openNew">+ 신규</button></div>
    <p v-if="message" class="msg">{{ message }}</p>
    <p class="desc">문서유형·금액구간별 결재선을 미리 정의합니다. 상신 시 금액에 맞는 결재선이 자동 추천됩니다. (결재/합의/참조, 전결권 지정)</p>

    <div class="panel">
      <table>
        <thead><tr><th>문서유형</th><th>템플릿명</th><th class="r">금액 하한</th><th class="r">금액 상한</th><th>사용</th><th></th></tr></thead>
        <tbody>
          <tr v-for="t in list" :key="t.id">
            <td>{{ t.docTyp }}</td><td>{{ t.tplNm }}</td>
            <td class="r">{{ fmt(t.amtFrom) }}</td><td class="r">{{ fmt(t.amtTo) }}</td>
            <td><span class="badge" :class="t.useYn === 'Y' ? 'on' : 'off'">{{ t.useYn === 'Y' ? '사용' : '중지' }}</span></td>
            <td class="rgt"><button class="link" @click="openEdit(t)">수정</button><button class="link del" @click="remove(t)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="6" class="empty">등록된 템플릿이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-bg" @click.self="showForm = false">
      <div class="modal">
        <div class="modal-head">결재선 템플릿 {{ form.id ? '수정' : '등록' }} <button class="x" @click="showForm = false">×</button></div>
        <div class="modal-body">
          <div class="row">
            <label>문서유형 <select v-model="form.docTyp"><option v-for="d in DOC_TYPES" :key="d" :value="d">{{ d }}</option></select></label>
            <label class="grow">템플릿명 <input v-model="form.tplNm" /></label>
            <label>사용 <select v-model="form.useYn"><option value="Y">사용</option><option value="N">중지</option></select></label>
          </div>
          <div class="row">
            <label>금액 하한 <input type="number" v-model.number="form.amtFrom" /></label>
            <label>금액 상한 <input type="number" v-model.number="form.amtTo" /></label>
            <label>정렬 <input type="number" v-model.number="form.sortNo" style="width:70px" /></label>
          </div>
          <div class="steps">
            <div class="steps-head">결재 단계 <button class="btn sm" @click="addStep">+ 단계추가</button></div>
            <table class="stbl">
              <thead><tr><th>#</th><th>유형</th><th>결재자</th><th>전결권</th><th></th></tr></thead>
              <tbody>
                <tr v-for="(s, i) in form.steps" :key="i">
                  <td>{{ i + 1 }}</td>
                  <td><select v-model="s.lineTyp"><option value="APRV">결재</option><option value="AGRE">합의</option><option value="REFR">참조</option></select></td>
                  <td><div class="picker"><input :value="s.aprvUsrNm ? `${s.aprvUsrNm} (${s.aprvUsrId})` : ''" readonly /><button class="btn sm" @click="pickRow = i">찾기</button></div></td>
                  <td><input type="checkbox" :checked="s.finalYn === 'Y'" @change="s.finalYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'" /></td>
                  <td><button class="link del" @click="delStep(i)">삭제</button></td>
                </tr>
                <tr v-if="!form.steps || !form.steps.length"><td colspan="5" class="empty">단계를 추가하세요.</td></tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-foot"><button class="btn primary" @click="save">저장</button><button class="btn" @click="showForm = false">취소</button></div>
      </div>
    </div>
    <UserPicker v-if="pickRow !== null" @select="onUserSelect" @close="pickRow = null" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; } .head .primary { margin-left: auto; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; } td.rgt { text-align: right; }
.empty { text-align: center; color: #aaa; padding: 18px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 9px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-left: 6px; } .link.del { color: #d33; }
.badge { padding: 2px 9px; border-radius: 10px; font-size: 12px; } .badge.on { background: #e3f6e8; color: #1a7f37; } .badge.off { background: #eee; color: #888; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 620px; max-height: 86vh; overflow: auto; background: #fff; border-radius: 10px; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 16px 18px; }
.row { display: flex; gap: 12px; margin-bottom: 12px; } .row label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .row .grow { flex: 1; }
.row input, .row select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.steps { margin-top: 8px; } .steps-head { font-weight: bold; display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.stbl td input, .stbl td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; } .picker { display: flex; gap: 5px; } .picker input { flex: 1; }
.modal-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
</style>
