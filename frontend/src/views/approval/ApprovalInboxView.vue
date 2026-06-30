<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Line { id: number; aprvNo: string; docTyp: string; docId: number; docNo?: string; aprvTitle?: string; draftUsrNm?: string; stepNo: number; lineTyp?: string; lineTypNm?: string; finalYn?: string; lineSts: string; lineStsNm?: string; aprvDt?: string; opinion?: string }

const router = useRouter()
const tab = ref<'wait' | 'done'>('wait')
const list = ref<Line[]>([])
const message = ref('')

const DOC_PATH: Record<string, string> = { PR: '/pro/pr', PO: '/pro/po', GR: '/pro/gr', CL: '/pro/cl', CT: '/pro/ct', IP: '/pro/ip/rsv', VD: '/base/vendor?id=', BC: '/pro/bc', PA: '/pro/pa', AV: '/pro/av' }

async function load() {
  list.value = (await http.get('/approval/inbox', { params: { done: tab.value === 'done' } })).data.data
}
function switchTab(t: 'wait' | 'done') { tab.value = t; load() }
function openDoc(l: Line) { const p = DOC_PATH[l.docTyp]; if (p) router.push(p.endsWith('=') ? `${p}${l.docId}` : `${p}/${l.docId}`) }
async function approve(l: Line) {
  const op = window.prompt('승인 의견(선택)') ?? ''
  try { await http.post(`/approval/${l.id}/approve`, { opinion: op }); message.value = '승인되었습니다.'; await load() }
  catch (e: any) { message.value = e.message }
}
async function reject(l: Line) {
  const op = window.prompt('반려 사유(필수)') ?? ''
  if (!op) return
  try { await http.post(`/approval/${l.id}/reject`, { opinion: op }); message.value = '반려되었습니다.'; await load() }
  catch (e: any) { message.value = e.message }
}
async function approveFinal(l: Line) {
  if (!confirm('전결 처리하시겠습니까? 이후 결재단계는 생략됩니다.')) return
  const op = window.prompt('전결 의견(선택)') ?? ''
  try { await http.post(`/approval/${l.id}/final`, { opinion: op }); message.value = '전결 처리되었습니다.'; await load() }
  catch (e: any) { message.value = e.message }
}
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>결재함</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="tabs">
      <button :class="{ on: tab === 'wait' }" @click="switchTab('wait')">결재 대기</button>
      <button :class="{ on: tab === 'done' }" @click="switchTab('done')">처리 완료</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>결재번호</th><th>문서</th><th>제목</th><th>기안자</th><th>단계</th><th>유형</th><th>상태</th><th>처리일</th><th></th></tr></thead>
        <tbody>
          <tr v-for="l in list" :key="l.id">
            <td>{{ l.aprvNo }}</td>
            <td><a class="doc" @click="openDoc(l)">{{ l.docTyp }} {{ l.docNo }}</a></td>
            <td>{{ l.aprvTitle }}</td><td>{{ l.draftUsrNm }}</td><td>{{ l.stepNo }}단계</td>
            <td><span class="ltyp">{{ l.lineTypNm || '결재' }}</span><span v-if="l.finalYn === 'Y'" class="finalbadge">전결권</span></td>
            <td><span class="badge" :class="l.lineSts">{{ l.lineStsNm }}</span></td>
            <td>{{ l.aprvDt }}</td>
            <td v-if="tab === 'wait'">
              <button class="btn go" @click="approve(l)">{{ l.lineTyp === 'AGRE' ? '합의' : '승인' }}</button>
              <button v-if="l.finalYn === 'Y'" class="btn final" @click="approveFinal(l)">전결</button>
              <button class="btn del" @click="reject(l)">반려</button>
            </td>
            <td v-else class="op">{{ l.opinion }}</td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">{{ tab === 'wait' ? '결재 대기 건이 없습니다.' : '처리 내역이 없습니다.' }}</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.tabs { display: flex; gap: 6px; margin-bottom: 12px; }
.tabs button { padding: 8px 18px; border: 1px solid #ddd; background: #fff; border-radius: 6px; cursor: pointer; }
.tabs button.on { background: #1a3a6b; color: #fff; border-color: #1a3a6b; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.doc { color: #1a3a6b; cursor: pointer; text-decoration: underline; }
.op { color: #777; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 5px 12px; border: 1px solid #1a3a6b; background: #fff; border-radius: 5px; cursor: pointer; margin-right: 6px; }
.btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.btn.final { background: #6b3fa0; color: #fff; border-color: #6b3fa0; }
.ltyp { font-size: 12px; color: #555; } .finalbadge { margin-left: 5px; font-size: 11px; background: #efe7f8; color: #6b3fa0; padding: 1px 6px; border-radius: 8px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.WAIT { background: #eee; color: #777; } .badge.ING { background: #fff3d6; color: #9a6b00; }
.badge.APPROVE { background: #e3f6e8; color: #1a7f37; } .badge.REJECT { background: #f3e3e3; color: #a33; }
</style>
