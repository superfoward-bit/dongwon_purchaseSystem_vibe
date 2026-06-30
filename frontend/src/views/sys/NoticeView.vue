<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Notice { id: number; notiTyp: string; notiTypNm?: string; toAddr?: string; toUsrId?: string; title?: string; content?: string; refTyp?: string; refId?: number; eventCd?: string; sendSts: string; sendStsNm?: string; sentDt?: string; errMsg?: string; regDt?: string }
const list = ref<Notice[]>([])
const notiTyp = ref('')
const sendSts = ref('')

async function load() {
  list.value = (await http.get('/sys/notice', { params: { notiTyp: notiTyp.value, sendSts: sendSts.value } })).data.data
}
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>알림 발송이력</h2>
    <p class="desc">결재 요청/완료/반려 등 이벤트 발생 시 자동 발송된 메일/SMS 이력입니다. (현재 환경은 시뮬레이션 발송)</p>
    <div class="toolbar">
      <select v-model="notiTyp" @change="load"><option value="">전체 유형</option><option value="EMAIL">이메일</option><option value="SMS">SMS</option></select>
      <select v-model="sendSts" @change="load"><option value="">전체 상태</option><option value="READY">발송대기</option><option value="SENT">발송완료</option><option value="FAIL">발송실패</option></select>
      <button class="btn" @click="load">새로고침</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>유형</th><th>수신</th><th>제목</th><th>이벤트</th><th>참조문서</th><th>상태</th><th>발송시각</th></tr></thead>
        <tbody>
          <tr v-for="n in list" :key="n.id">
            <td><span class="ntyp">{{ n.notiTypNm }}</span></td>
            <td>{{ n.toUsrId }} <span v-if="n.toAddr" class="dim">{{ n.toAddr }}</span></td>
            <td class="ttl">{{ n.title }}<div class="ct">{{ n.content }}</div></td>
            <td>{{ n.eventCd }}</td>
            <td>{{ n.refTyp }} <span class="dim">{{ n.refId }}</span></td>
            <td><span class="badge" :class="n.sendSts">{{ n.sendStsNm }}</span><div v-if="n.errMsg" class="err">{{ n.errMsg }}</div></td>
            <td>{{ n.sentDt || n.regDt }}</td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="empty">발송 이력이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 14px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; vertical-align: top; } th { background: #fafafa; }
.ntyp { font-size: 12px; background: #eef; color: #445; padding: 2px 8px; border-radius: 8px; }
.dim { color: #aaa; font-size: 12px; } .ttl .ct { color: #888; font-size: 12px; margin-top: 2px; }
.err { color: #c0392b; font-size: 12px; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.badge { padding: 2px 9px; border-radius: 10px; font-size: 12px; }
.badge.READY { background: #eef; color: #556; } .badge.SENT { background: #e3f6e8; color: #1a7f37; } .badge.FAIL { background: #f3e3e3; color: #a33; }
</style>
