<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Rate {
  id?: number; vdCd?: string; adjTyp?: string; adjTypNm?: string; calcBase?: string;
  rate?: number; unitAmt?: number; signTyp?: string; validSd?: string; validEd?: string; remark?: string; useYn?: string
}
const list = ref<Rate[]>([])
const adjTypes = ref<{ cd: string; cdNmKo: string }[]>([])
const message = ref('')
const showForm = ref(false)
const form = ref<Rate>(blank())

function blank(): Rate { return { calcBase: 'SUPL', signTyp: '-', rate: 0, unitAmt: 0, validSd: '2026-01-01', validEd: '2099-12-31', useYn: 'Y' } }
async function load() { list.value = (await http.get('/pro/cladj')).data.data }
async function loadCodes() { adjTypes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'CL_ADJ_TYP' } })).data.data }
function openNew() { form.value = blank(); showForm.value = true }
function openEdit(r: Rate) { form.value = { ...r }; showForm.value = true }
async function save() {
  message.value = ''
  if (!form.value.adjTyp) { message.value = '조정유형을 선택하세요.'; return }
  try {
    if (form.value.id) await http.put(`/pro/cladj/${form.value.id}`, form.value)
    else await http.post('/pro/cladj', form.value)
    showForm.value = false; await load(); message.value = '저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function remove(r: Rate) {
  if (!confirm('삭제하시겠습니까?')) return
  await http.delete(`/pro/cladj/${r.id}`); await load()
}
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <div class="head"><h2>정산 조정율 (갭정산)</h2><button class="btn primary" @click="openNew">+ 신규</button></div>
    <p v-if="message" class="msg">{{ message }}</p>
    <p class="desc">물류수수료·피킹료·장려금 등 정산 조정율을 협력사/공통 단위로 관리합니다. 협력사 전용이 공통보다 우선 적용됩니다.</p>

    <div class="panel">
      <table>
        <thead><tr><th>조정유형</th><th>대상</th><th>계산기준</th><th class="r">요율</th><th class="r">건당/정액</th><th>부호</th><th>유효기간</th><th>사용</th><th></th></tr></thead>
        <tbody>
          <tr v-for="r in list" :key="r.id">
            <td>{{ r.adjTypNm }}</td>
            <td>{{ r.vdCd || '공통' }}</td>
            <td>{{ r.calcBase }}</td>
            <td class="r">{{ r.rate ? (r.rate * 100).toFixed(2) + '%' : '-' }}</td>
            <td class="r">{{ r.unitAmt ? fmt(r.unitAmt) : '-' }}</td>
            <td>{{ r.signTyp === '-' ? '차감' : '가산' }}</td>
            <td>{{ r.validSd }} ~ {{ r.validEd }}</td>
            <td><span class="badge" :class="r.useYn === 'Y' ? 'on' : 'off'">{{ r.useYn === 'Y' ? '사용' : '중지' }}</span></td>
            <td class="rgt"><button class="link" @click="openEdit(r)">수정</button><button class="link del" @click="remove(r)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">등록된 조정율이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-bg" @click.self="showForm = false">
      <div class="modal">
        <div class="modal-head">조정율 {{ form.id ? '수정' : '등록' }} <button class="x" @click="showForm = false">×</button></div>
        <div class="modal-body">
          <label>조정유형 <select v-model="form.adjTyp"><option value="">선택</option><option v-for="c in adjTypes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
          <label>대상 협력사 <input v-model="form.vdCd" placeholder="비우면 전협력사 공통" /></label>
          <label>계산기준 <select v-model="form.calcBase"><option value="SUPL">공급가(요율)</option><option value="CNT">입고건수(건당)</option><option value="FIXED">정액</option></select></label>
          <label>요율 (예 0.015 = 1.5%) <input type="number" step="0.0001" v-model.number="form.rate" /></label>
          <label>건당/정액 금액 <input type="number" v-model.number="form.unitAmt" /></label>
          <label>부호 <select v-model="form.signTyp"><option value="-">차감</option><option value="+">가산</option></select></label>
          <label>유효 시작 <input type="date" v-model="form.validSd" /></label>
          <label>유효 종료 <input type="date" v-model="form.validEd" /></label>
          <label>사용 <select v-model="form.useYn"><option value="Y">사용</option><option value="N">중지</option></select></label>
          <label class="w2">비고 <input v-model="form.remark" /></label>
        </div>
        <div class="modal-foot"><button class="btn primary" @click="save">저장</button><button class="btn" @click="showForm = false">취소</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; } .head .primary { margin-left: auto; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; } td.rgt { text-align: right; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-left: 6px; } .link.del { color: #d33; }
.badge { padding: 2px 9px; border-radius: 10px; font-size: 12px; } .badge.on { background: #e3f6e8; color: #1a7f37; } .badge.off { background: #eee; color: #888; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 460px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 16px 18px; display: flex; flex-wrap: wrap; gap: 12px; } .modal-body label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 45%; } .modal-body .w2 { flex: 1 1 100%; }
.modal-body input, .modal-body select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.modal-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
</style>
