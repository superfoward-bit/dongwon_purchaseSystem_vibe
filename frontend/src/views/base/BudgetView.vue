<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Budget { id?: number; budgetCd?: string; budgetNm?: string; fiscalYear?: string; acctCd?: string; budgetAmt?: number; usedAmt?: number; availableAmt?: number }
const list = ref<Budget[]>([])
const keyword = ref('')
const message = ref('')
const editing = ref<Budget | null>(null)

async function load() { list.value = (await http.get('/base/budget', { params: { keyword: keyword.value } })).data.data }
function add() { editing.value = { fiscalYear: String(new Date().getFullYear()), budgetAmt: 0 } }
function edit(b: Budget) { editing.value = { ...b } }
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/base/budget/${editing.value.id}`, editing.value)
    else await http.post('/base/budget', editing.value)
    editing.value = null; message.value = '저장되었습니다.'; await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(b: Budget) { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/base/budget/${b.id}`); await load() }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
function rate(b: Budget) { return b.budgetAmt ? Math.round((Number(b.usedAmt) || 0) / Number(b.budgetAmt) * 100) : 0 }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>예산관리</h2>
    <p class="desc">구매요청(PR) 상신 시 예산 잔액을 검증하여 초과 상신을 차단합니다. 집행액 = 상신·승인된 PR 합계.</p>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="toolbar">
      <input v-model="keyword" placeholder="예산코드/명" @keyup.enter="load" />
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="add">+ 예산 등록</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>예산코드</th><th>예산명</th><th>연도</th><th>계정</th><th class="r">예산액</th><th class="r">집행액</th><th class="r">잔액</th><th style="width:140px">집행률</th><th></th></tr></thead>
        <tbody>
          <tr v-for="b in list" :key="b.id">
            <td>{{ b.budgetCd }}</td><td>{{ b.budgetNm }}</td><td>{{ b.fiscalYear }}</td><td>{{ b.acctCd }}</td>
            <td class="r">{{ fmt(b.budgetAmt) }}</td><td class="r">{{ fmt(b.usedAmt) }}</td>
            <td class="r" :class="{ minus: (b.availableAmt || 0) < 0 }">{{ fmt(b.availableAmt) }}</td>
            <td><div class="bar"><div class="fill" :class="{ over: rate(b) > 100 }" :style="{ width: Math.min(rate(b), 100) + '%' }"></div></div><span class="pct">{{ rate(b) }}%</span></td>
            <td><button class="btn sm" @click="edit(b)">수정</button> <button class="btn sm del" @click="remove(b)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">예산이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="editing" class="modal-bg" @click.self="editing = null">
      <div class="modal">
        <div class="modal-head">{{ editing.id ? '예산 수정' : '예산 등록' }} <button class="x" @click="editing = null">×</button></div>
        <div class="modal-body form">
          <label>예산코드 <input v-model="editing.budgetCd" :disabled="!!editing.id" /></label>
          <label>예산명 <input v-model="editing.budgetNm" /></label>
          <label>회계연도 <input v-model="editing.fiscalYear" /></label>
          <label>계정코드 <input v-model="editing.acctCd" /></label>
          <label>예산액 <input type="number" v-model.number="editing.budgetAmt" /></label>
          <div class="actions"><button class="btn primary" @click="save">저장</button> <button class="btn" @click="editing = null">닫기</button></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.minus { color: #c0392b; font-weight: bold; }
.bar { display: inline-block; width: 80px; height: 8px; background: #eee; border-radius: 4px; overflow: hidden; vertical-align: middle; } .fill { height: 100%; background: #1a7f37; } .fill.over { background: #c0392b; } .pct { margin-left: 6px; font-size: 12px; color: #555; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 9px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; } .btn.del { color: #d33; border-color: #d33; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 440px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.form { padding: 14px 18px; } .form label { display: block; margin-bottom: 10px; font-size: 13px; color: #555; } .form input { width: 100%; padding: 7px 10px; border: 1px solid #ddd; border-radius: 5px; margin-top: 3px; }
.actions { margin-top: 14px; display: flex; gap: 6px; }
</style>
