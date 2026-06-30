<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Line { lineNo: number; drCr: string; acctCd?: string; acctNm?: string; amt?: number; remark?: string }
interface Slip { id: number; slipNo: string; slipTypNm?: string; srcNo?: string; vdNm?: string; postingYmd?: string; drAcct?: string; crAcct?: string; amt?: number; slipSts: string; slipStsNm?: string; fiscalYm?: string; erpDocNo?: string; lines?: Line[] }
interface Close { id: number; closeNo: string; vdNm?: string; closeYm?: string; totAmt?: number }
const list = ref<Slip[]>([])
const keyword = ref('')
const message = ref('')
const showPick = ref(false)
const closes = ref<Close[]>([])
const detail = ref<Slip | null>(null)

async function openDetail(s: Slip) { detail.value = (await http.get(`/ifc/slip/${s.id}`)).data.data }
function sumDr() { return (detail.value?.lines || []).filter(l => l.drCr === 'D').reduce((a, l) => a + (l.amt || 0), 0) }
function sumCr() { return (detail.value?.lines || []).filter(l => l.drCr === 'C').reduce((a, l) => a + (l.amt || 0), 0) }

async function load() { list.value = (await http.get('/ifc/slip', { params: { keyword: keyword.value } })).data.data }
async function openPick() { closes.value = (await http.get('/pro/cl', { params: { sts: 'CFM' } })).data.data; showPick.value = true }
async function fromClose(c: Close) {
  try { await http.post('/ifc/slip/from-close', { closeId: c.id }); showPick.value = false; message.value = `${c.closeNo} 매입전표 생성`; await load() }
  catch (e: any) { message.value = e.message }
}
async function post(s: Slip) { try { await http.post(`/ifc/slip/${s.id}/post`, {}); message.value = `${s.slipNo} 전기 완료`; await load() } catch (e: any) { message.value = e.message } }
async function send(s: Slip) { try { await http.post(`/ifc/slip/${s.id}/send`, {}); message.value = `${s.slipNo} SAP 전송 완료`; await load() } catch (e: any) { message.value = e.message } }
async function cancel(s: Slip) { if (!confirm('취소하시겠습니까?')) return; await http.post(`/ifc/slip/${s.id}/cancel`, {}); await load() }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>회계전표 (매입전표)</h2>
    <p class="desc">마감확정된 정산을 기준으로 매입전표(차변 매입 / 대변 외상매입금)를 생성하여 전기 후 SAP에 전송(시뮬)합니다.</p>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="toolbar">
      <input v-model="keyword" placeholder="전표번호/협력사/정산번호" @keyup.enter="load" />
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="openPick">+ 정산에서 생성</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>전표번호</th><th>유형</th><th>정산</th><th>협력사</th><th>전기일</th><th>차변</th><th>대변</th><th class="r">금액</th><th>상태</th><th></th></tr></thead>
        <tbody>
          <tr v-for="s in list" :key="s.id">
            <td>{{ s.slipNo }}</td><td>{{ s.slipTypNm }}</td><td>{{ s.srcNo }}</td><td>{{ s.vdNm }}</td><td>{{ s.postingYmd }}</td>
            <td>{{ s.drAcct }}</td><td>{{ s.crAcct }}</td><td class="r">{{ fmt(s.amt) }}</td>
            <td><span class="badge" :class="s.slipSts">{{ s.slipStsNm }}</span></td>
            <td>
              <button class="btn sm" @click="openDetail(s)">명세</button>
              <button v-if="s.slipSts === 'DRAFT'" class="btn sm go" @click="post(s)">전기</button>
              <button v-if="s.slipSts === 'POSTED'" class="btn sm go" @click="send(s)">SAP전송</button>
              <button v-if="s.slipSts !== 'CANCEL' && s.slipSts !== 'SENT'" class="btn sm" @click="cancel(s)">취소</button>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="10" class="empty">회계전표가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="detail" class="modal-bg" @click.self="detail = null">
      <div class="modal">
        <div class="modal-head">전표 명세 — {{ detail.slipNo }}
          <span style="font-weight:normal;font-size:12px;color:#888;margin-left:10px">회계기간 {{ detail.fiscalYm || '-' }} · ERP전표 {{ detail.erpDocNo || '-' }}</span>
          <button class="x" @click="detail = null">×</button>
        </div>
        <div class="modal-body">
          <table>
            <thead><tr><th>차/대</th><th>계정코드</th><th>계정과목</th><th class="r">금액</th><th>적요</th></tr></thead>
            <tbody>
              <tr v-for="l in detail.lines" :key="l.lineNo">
                <td><span class="badge" :class="l.drCr === 'D' ? 'POSTED' : 'SENT'">{{ l.drCr === 'D' ? '차변' : '대변' }}</span></td>
                <td>{{ l.acctCd }}</td><td>{{ l.acctNm }}</td><td class="r">{{ fmt(l.amt) }}</td><td>{{ l.remark }}</td>
              </tr>
            </tbody>
            <tfoot><tr><td colspan="3" class="r"><b>차변 {{ fmt(sumDr()) }} / 대변 {{ fmt(sumCr()) }}</b></td><td class="r"><b :class="{ bad: sumDr() !== sumCr() }">{{ sumDr() === sumCr() ? '대차일치' : '불일치' }}</b></td><td></td></tr></tfoot>
          </table>
        </div>
      </div>
    </div>

    <div v-if="showPick" class="modal-bg" @click.self="showPick = false">
      <div class="modal">
        <div class="modal-head">마감확정 정산 선택 <button class="x" @click="showPick = false">×</button></div>
        <div class="modal-body">
          <table><thead><tr><th>정산번호</th><th>협력사</th><th>마감월</th><th class="r">금액</th><th></th></tr></thead>
            <tbody>
              <tr v-for="c in closes" :key="c.id"><td>{{ c.closeNo }}</td><td>{{ c.vdNm }}</td><td>{{ c.closeYm }}</td><td class="r">{{ fmt(c.totAmt) }}</td><td><button class="btn sm go" @click="fromClose(c)">생성</button></td></tr>
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
.badge.DRAFT { background: #eef; color: #556; } .badge.POSTED { background: #fff3d6; color: #9a6b00; } .badge.SENT { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.bad { color: #c0392b; } tfoot td { padding: 9px 12px; border-top: 2px solid #ddd; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 600px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 12px 18px; max-height: 60vh; overflow: auto; }
</style>
