<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface If { id: number; ifNo: string; ifTyp: string; ifTypNm?: string; ifNm?: string; direction?: string; sysCd?: string; refNo?: string; payload?: string; ifStatus: string; ifStatusNm?: string; sentDt?: string; resultMsg?: string; retryCnt?: number; regDt?: string }
const list = ref<If[]>([])
const ifTyp = ref('')
const ifStatus = ref('')
const keyword = ref('')
const typCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const stsCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const message = ref('')

async function load() { list.value = (await http.get('/ifc', { params: { ifTyp: ifTyp.value, ifStatus: ifStatus.value, keyword: keyword.value } })).data.data }
async function loadCodes() {
  typCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'IF_TYP' } })).data.data
  stsCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'IF_STATUS' } })).data.data
}
async function resend(i: If) {
  try { await http.post(`/ifc/${i.id}/resend`, {}); message.value = `${i.ifNo} 재전송 완료`; await load() } catch (e: any) { message.value = e.message }
}
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <h2>인터페이스 모니터링</h2>
    <p class="desc">SAP·EAI(INDIGO)·국세청(NTS) 등 대외 연계 송수신 이력입니다. (현재 환경은 시뮬레이션 연계) 실패 건은 재전송할 수 있습니다.</p>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="toolbar">
      <input v-model="keyword" placeholder="연계번호/참조번호" @keyup.enter="load" />
      <select v-model="ifTyp" @change="load"><option value="">전체 유형</option><option v-for="c in typCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
      <select v-model="ifStatus" @change="load"><option value="">전체 상태</option><option v-for="c in stsCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
      <button class="btn" @click="load">조회</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>연계번호</th><th>유형</th><th>시스템</th><th>참조</th><th>내용</th><th>상태</th><th>전송시각</th><th>재시도</th><th></th></tr></thead>
        <tbody>
          <tr v-for="i in list" :key="i.id">
            <td>{{ i.ifNo }}</td><td>{{ i.ifTypNm }}</td><td><span class="sys">{{ i.sysCd }}</span></td><td>{{ i.refNo }}</td>
            <td class="pl">{{ i.payload }}</td>
            <td><span class="badge" :class="i.ifStatus">{{ i.ifStatusNm }}</span><div v-if="i.resultMsg" class="rm">{{ i.resultMsg }}</div></td>
            <td class="dim">{{ i.sentDt }}</td><td class="r">{{ i.retryCnt }}</td>
            <td><button v-if="i.ifStatus === 'FAIL'" class="btn sm" @click="resend(i)">재전송</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">연계 이력이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 13px; vertical-align: top; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.sys { font-size: 12px; background: #eef; color: #445; padding: 2px 8px; border-radius: 8px; }
.pl { color: #555; max-width: 260px; } .dim { color: #aaa; } .rm { color: #888; font-size: 12px; margin-top: 2px; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 9px; font-size: 13px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.READY { background: #eef; color: #556; } .badge.SUCCESS { background: #e3f6e8; color: #1a7f37; } .badge.FAIL { background: #f3e3e3; color: #a33; }
</style>
