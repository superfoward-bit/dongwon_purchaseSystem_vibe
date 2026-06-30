<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Pay { id: number; payNo: string; srcNo?: string; vdNm?: string; payDueYmd?: string; payAmt?: number; payMethodNm?: string; payYmd?: string; paySts: string; payStsNm?: string }
interface Close { id: number; closeNo: string; vdNm?: string; closeYm?: string; netAmt?: number; totAmt?: number }
const list = ref<Pay[]>([])
const keyword = ref('')
const message = ref('')
const showPick = ref(false)
const closes = ref<Close[]>([])
const payTarget = ref<Pay | null>(null)
const payForm = ref({ payYmd: today(), noteNo: '', noteDueYmd: '' })

function today() { return new Date().toISOString().slice(0, 10) }
async function load() { list.value = (await http.get('/pro/payment', { params: { keyword: keyword.value } })).data.data }
async function openPick() { closes.value = (await http.get('/pro/cl', { params: { sts: 'CFM' } })).data.data; showPick.value = true }
async function fromClose(c: Close) {
  try { await http.post('/pro/payment/from-close', { closeId: c.id }); showPick.value = false; message.value = `${c.closeNo} 지급내역 생성`; await load() }
  catch (e: any) { message.value = e.message }
}
function openPay(p: Pay) { payTarget.value = p; payForm.value = { payYmd: today(), noteNo: '', noteDueYmd: '' } }
async function doPay() {
  if (!payTarget.value) return
  try { await http.post(`/pro/payment/${payTarget.value.id}/pay`, payForm.value); payTarget.value = null; message.value = '지급 실행 완료'; await load() }
  catch (e: any) { message.value = e.message }
}
async function cancel(p: Pay) { if (!confirm('취소하시겠습니까?')) return; await http.post(`/pro/payment/${p.id}/cancel`, {}); await load() }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>지급관리</h2>
    <p class="desc">마감확정된 정산을 기준으로 지급예정을 생성하고(지급예정일·지급방법·계좌 스냅샷), 실지급일·어음정보를 기록합니다.</p>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="toolbar">
      <input v-model="keyword" placeholder="지급번호/협력사/정산번호" @keyup.enter="load" />
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="openPick">+ 정산에서 생성</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>지급번호</th><th>정산</th><th>협력사</th><th>지급예정일</th><th class="r">지급액</th><th>지급방법</th><th>실지급일</th><th>상태</th><th></th></tr></thead>
        <tbody>
          <tr v-for="p in list" :key="p.id">
            <td>{{ p.payNo }}</td><td>{{ p.srcNo }}</td><td>{{ p.vdNm }}</td><td>{{ p.payDueYmd }}</td>
            <td class="r">{{ fmt(p.payAmt) }}</td><td>{{ p.payMethodNm }}</td><td>{{ p.payYmd || '-' }}</td>
            <td><span class="badge" :class="p.paySts">{{ p.payStsNm }}</span></td>
            <td>
              <button v-if="p.paySts === 'PLAN'" class="btn sm go" @click="openPay(p)">지급실행</button>
              <button v-if="p.paySts !== 'CANCEL' && p.paySts !== 'PAID'" class="btn sm" @click="cancel(p)">취소</button>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">지급내역이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="payTarget" class="modal-bg" @click.self="payTarget = null">
      <div class="modal" style="width:440px">
        <div class="modal-head">지급 실행 — {{ payTarget.payNo }} <button class="x" @click="payTarget = null">×</button></div>
        <div class="modal-body">
          <p>협력사 <b>{{ payTarget.vdNm }}</b> · 지급액 <b>{{ fmt(payTarget.payAmt) }}</b> · 방법 {{ payTarget.payMethodNm }}</p>
          <label class="fld">실지급일 <input type="date" v-model="payForm.payYmd" /></label>
          <label class="fld">어음번호(어음결제 시) <input v-model="payForm.noteNo" placeholder="선택" /></label>
          <label class="fld">어음만기일 <input type="date" v-model="payForm.noteDueYmd" /></label>
          <div class="actions"><button class="btn go" @click="doPay">지급 실행</button> <button class="btn" @click="payTarget = null">닫기</button></div>
        </div>
      </div>
    </div>

    <div v-if="showPick" class="modal-bg" @click.self="showPick = false">
      <div class="modal">
        <div class="modal-head">마감확정 정산 선택 <button class="x" @click="showPick = false">×</button></div>
        <div class="modal-body">
          <table><thead><tr><th>정산번호</th><th>협력사</th><th>마감월</th><th class="r">최종정산액</th><th></th></tr></thead>
            <tbody>
              <tr v-for="c in closes" :key="c.id"><td>{{ c.closeNo }}</td><td>{{ c.vdNm }}</td><td>{{ c.closeYm }}</td><td class="r">{{ fmt(c.netAmt || c.totAmt) }}</td><td><button class="btn sm go" @click="fromClose(c)">생성</button></td></tr>
              <tr v-if="!closes.length"><td colspan="5" class="empty">마감확정된 정산이 없습니다.</td></tr>
            </tbody></table>
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
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; margin-right: 4px; } .btn.sm { padding: 4px 9px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.PLAN { background: #eef; color: #556; } .badge.PAID { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 600px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 12px 18px; max-height: 60vh; overflow: auto; }
.fld { display: block; margin: 10px 0; } .fld input { width: 100%; padding: 7px 10px; border: 1px solid #ddd; border-radius: 5px; margin-top: 3px; }
.actions { margin-top: 14px; display: flex; gap: 6px; }
</style>
