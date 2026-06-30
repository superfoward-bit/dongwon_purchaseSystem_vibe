<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Appr { id: number; aprvNo: string; docTyp: string; docId: number; docNo?: string; aprvTitle?: string; curStepNo?: number; aprvSts: string; aprvStsNm?: string; draftDt?: string }

const router = useRouter()
const list = ref<Appr[]>([])
const DOC_PATH: Record<string, string> = { PR: '/pro/pr', PO: '/pro/po', GR: '/pro/gr', CL: '/pro/cl', CT: '/pro/ct', IP: '/pro/ip/rsv', VD: '/base/vendor?id=', BC: '/pro/bc', PA: '/pro/pa', AV: '/pro/av' }

async function load() { list.value = (await http.get('/approval/draft')).data.data }
function openDoc(a: Appr) { const p = DOC_PATH[a.docTyp]; if (p) router.push(p.endsWith('=') ? `${p}${a.docId}` : `${p}/${a.docId}`) }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>상신함</h2>
    <div class="panel">
      <table>
        <thead><tr><th>결재번호</th><th>문서</th><th>제목</th><th>현재단계</th><th>상태</th><th>기안일</th></tr></thead>
        <tbody>
          <tr v-for="a in list" :key="a.id" class="row" @click="openDoc(a)">
            <td>{{ a.aprvNo }}</td><td>{{ a.docTyp }} {{ a.docNo }}</td><td>{{ a.aprvTitle }}</td>
            <td>{{ a.curStepNo }}단계</td><td><span class="badge" :class="a.aprvSts">{{ a.aprvStsNm }}</span></td><td>{{ a.draftDt }}</td>
          </tr>
          <tr v-if="!list.length"><td colspan="6" class="empty">상신한 문서가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.ING { background: #fff3d6; color: #9a6b00; } .badge.DONE { background: #e3f6e8; color: #1a7f37; }
.badge.REJECT { background: #f3e3e3; color: #a33; } .badge.RECALL { background: #eee; color: #777; }
</style>
