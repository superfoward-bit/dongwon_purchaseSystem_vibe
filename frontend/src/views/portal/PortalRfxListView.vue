<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Rfx { id: number; rfxNo: string; rfxTitle: string; openYmd?: string; closeYmd?: string; sts: string; stsNm?: string }
const router = useRouter()
const list = ref<Rfx[]>([])
async function load() { list.value = (await http.get('/portal/rfx')).data.data }
function open(id: number) { router.push(`/portal/rfx/${id}`) }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>견적 응찰 <small>우리 회사가 초대된 견적</small></h2>
    <div class="panel">
      <table>
        <thead><tr><th>견적번호</th><th>제목</th><th>공고일</th><th>마감일</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="r in list" :key="r.id" class="row" @click="open(r.id)">
            <td>{{ r.rfxNo }}</td><td>{{ r.rfxTitle }}</td><td>{{ r.openYmd }}</td><td>{{ r.closeYmd }}</td>
            <td><span class="badge" :class="r.sts">{{ r.stsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="5" class="empty">응찰할 견적이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; } h2 small { font-size: 13px; color: #999; font-weight: normal; margin-left: 8px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.OPEN { background: #fff3d6; color: #9a6b00; } .badge.SEL { background: #e3f6e8; color: #1a7f37; }
</style>
