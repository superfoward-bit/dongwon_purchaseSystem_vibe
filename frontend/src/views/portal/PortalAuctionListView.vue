<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Auc { id: number; auctionNo: string; auctionTitle: string; startPrc?: number; startYmd?: string; endYmd?: string; sts: string; stsNm?: string }
const router = useRouter()
const list = ref<Auc[]>([])
async function load() { list.value = (await http.get('/portal/au')).data.data }
function open(id: number) { router.push(`/portal/au/${id}`) }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>역경매 입찰 <small>우리 회사가 참여한 역경매</small></h2>
    <div class="panel">
      <table>
        <thead><tr><th>경매번호</th><th>제목</th><th class="r">시작가</th><th>시작</th><th>종료</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="a in list" :key="a.id" class="row" @click="open(a.id)">
            <td>{{ a.auctionNo }}</td><td>{{ a.auctionTitle }}</td><td class="r">{{ fmt(a.startPrc) }}</td>
            <td>{{ a.startYmd }}</td><td>{{ a.endYmd }}</td><td><span class="badge" :class="a.sts">{{ a.stsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="6" class="empty">참여한 역경매가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; } h2 small { font-size: 13px; color: #999; font-weight: normal; margin-left: 8px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; } .badge.OPEN { background: #fff3d6; color: #9a6b00; } .badge.AWARD { background: #e3f6e8; color: #1a7f37; }
</style>
