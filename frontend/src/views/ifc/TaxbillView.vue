<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface TItem { lineNo: number; itemNm?: string; spec?: string; qty?: number; supplyAmt?: number; vatAmt?: number }
interface Bill { id: number; billNo: string; closeNo?: string; vdNm?: string; billYmd?: string; writeYmd?: string; issueYmd?: string; supplyAmt?: number; vatAmt?: number; totAmt?: number; billSts: string; billStsNm?: string; billTyp?: string; ntsStatus?: string; ntsApprovalNo?: string; supBizNo?: string; supNm?: string; supCeo?: string; buyBizNo?: string; buyNm?: string; buyCeo?: string; billKind?: string; items?: TItem[] }
interface Close { id: number; closeNo: string; vdNm?: string; closeYm?: string; totAmt?: number }
const list = ref<Bill[]>([])
const keyword = ref('')
const message = ref('')
const showPick = ref(false)
const closes = ref<Close[]>([])
const detail = ref<Bill | null>(null)
async function openDetail(b: Bill) { detail.value = (await http.get(`/ifc/tax/${b.id}`)).data.data }

async function load() { list.value = (await http.get('/ifc/tax', { params: { keyword: keyword.value } })).data.data }
async function openPick() { closes.value = (await http.get('/pro/cl', { params: { sts: 'CFM' } })).data.data; showPick.value = true }
async function fromClose(c: Close) {
  try { await http.post('/ifc/tax/from-close', { closeId: c.id }); showPick.value = false; message.value = `${c.closeNo} 세금계산서 생성`; await load() }
  catch (e: any) { message.value = e.message }
}
async function issue(b: Bill) { try { await http.post(`/ifc/tax/${b.id}/issue`, {}); message.value = `${b.billNo} 발행·국세청 전송 완료`; await load() } catch (e: any) { message.value = e.message } }
async function cancel(b: Bill) { if (!confirm('취소하시겠습니까?')) return; await http.post(`/ifc/tax/${b.id}/cancel`, {}); await load() }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>전자세금계산서</h2>
    <p class="desc">마감확정된 정산을 기준으로 전자세금계산서를 발행하고 국세청(NTS)에 전송(시뮬)합니다.</p>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="toolbar">
      <input v-model="keyword" placeholder="계산서번호/협력사/정산번호" @keyup.enter="load" />
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="openPick">+ 정산에서 발행</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>계산서번호</th><th>정산</th><th>협력사</th><th>작성일</th><th class="r">공급가</th><th class="r">세액</th><th class="r">합계</th><th>상태</th><th></th></tr></thead>
        <tbody>
          <tr v-for="b in list" :key="b.id">
            <td>{{ b.billNo }}</td><td>{{ b.closeNo }}</td><td>{{ b.vdNm }}</td><td>{{ b.billYmd }}</td>
            <td class="r">{{ fmt(b.supplyAmt) }}</td><td class="r">{{ fmt(b.vatAmt) }}</td><td class="r">{{ fmt(b.totAmt) }}</td>
            <td><span class="badge" :class="b.billSts">{{ b.billStsNm }}</span></td>
            <td><button class="btn sm" @click="openDetail(b)">상세</button>
                <button v-if="b.billSts === 'DRAFT'" class="btn sm go" @click="issue(b)">발행</button>
                <button v-if="b.billSts !== 'CANCEL' && b.billSts !== 'SENT'" class="btn sm" @click="cancel(b)">취소</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">세금계산서가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="detail" class="modal-bg" @click.self="detail = null">
      <div class="modal" style="width:680px">
        <div class="modal-head">세금계산서 {{ detail.billNo }}
          <span style="font-weight:normal;font-size:12px;color:#888;margin-left:8px">{{ detail.billTyp === 'TAX' ? '과세' : detail.billTyp === 'ZERO' ? '영세율' : '면세' }} · {{ detail.billKind === 'MODIFY' ? '수정' : '일반' }}</span>
          <button class="x" @click="detail = null">×</button>
        </div>
        <div class="modal-body">
          <div class="legal">
            <div class="party">
              <div class="ph">공급자</div>
              <div>{{ detail.supNm }} ({{ detail.supBizNo }})</div>
              <div>대표: {{ detail.supCeo }}</div>
            </div>
            <div class="party">
              <div class="ph">공급받는자(자사)</div>
              <div>{{ detail.buyNm }} ({{ detail.buyBizNo }})</div>
              <div>대표: {{ detail.buyCeo }}</div>
            </div>
          </div>
          <div class="meta">
            <span>작성일 {{ detail.writeYmd || '-' }}</span>
            <span>발행일 {{ detail.issueYmd || '-' }}</span>
            <span>국세청 승인번호 <b>{{ detail.ntsApprovalNo || '(발행 전)' }}</b></span>
          </div>
          <table>
            <thead><tr><th>품목</th><th>규격</th><th class="r">수량</th><th class="r">공급가</th><th class="r">세액</th></tr></thead>
            <tbody>
              <tr v-for="it in detail.items" :key="it.lineNo">
                <td>{{ it.itemNm }}</td><td>{{ it.spec }}</td><td class="r">{{ fmt(it.qty) }}</td><td class="r">{{ fmt(it.supplyAmt) }}</td><td class="r">{{ fmt(it.vatAmt) }}</td>
              </tr>
              <tr v-if="!detail.items || !detail.items.length"><td colspan="5" class="empty">품목명세 없음</td></tr>
            </tbody>
            <tfoot><tr><td colspan="3" class="r"><b>합계</b></td><td class="r"><b>{{ fmt(detail.supplyAmt) }}</b></td><td class="r"><b>{{ fmt(detail.vatAmt) }}</b></td></tr></tfoot>
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
              <tr v-for="c in closes" :key="c.id"><td>{{ c.closeNo }}</td><td>{{ c.vdNm }}</td><td>{{ c.closeYm }}</td><td class="r">{{ fmt(c.totAmt) }}</td><td><button class="btn sm go" @click="fromClose(c)">발행</button></td></tr>
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
.badge.DRAFT { background: #eef; color: #556; } .badge.ISSUED { background: #fff3d6; color: #9a6b00; } .badge.SENT { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 600px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 12px 18px; max-height: 60vh; overflow: auto; }
.legal { display: flex; gap: 12px; margin-bottom: 10px; } .party { flex: 1; border: 1px solid #eee; border-radius: 6px; padding: 10px 12px; font-size: 13px; } .party .ph { font-weight: bold; color: #1a3a6b; margin-bottom: 4px; }
.meta { display: flex; gap: 16px; font-size: 13px; color: #555; margin-bottom: 10px; } .meta b { color: #1a7f37; } tfoot td { border-top: 2px solid #ddd; }
</style>
