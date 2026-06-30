<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Req { id: number; reqNo: string; itemNm?: string; cateNm?: string; spec?: string; sts: string; stsNm?: string; createdItemCd?: string }
const router = useRouter()
const list = ref<Req[]>([])
const keyword = ref('')
const sts = ref('')
const codes = ref<{ cd: string; cdNmKo: string }[]>([])
async function load() { list.value = (await http.get('/base/item-req', { params: { keyword: keyword.value, sts: sts.value } })).data.data }
async function loadCodes() { codes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'IR_STS' } })).data.data }
function open(id: number | 'new') { router.push(`/base/item-req/${id}`) }
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <h2>품목 등록요청</h2>
    <div class="toolbar">
      <input v-model="keyword" placeholder="요청번호/품목명" @keyup.enter="load" />
      <select v-model="sts" @change="load"><option value="">전체 상태</option><option v-for="c in codes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="open('new')">+ 등록요청</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>요청번호</th><th>품목명</th><th>분류</th><th>규격</th><th>상태</th><th>생성품목</th></tr></thead>
        <tbody>
          <tr v-for="r in list" :key="r.id" class="row" @click="open(r.id)">
            <td>{{ r.reqNo }}</td><td>{{ r.itemNm }}</td><td>{{ r.cateNm }}</td><td>{{ r.spec }}</td>
            <td><span class="badge" :class="r.sts">{{ r.stsNm }}</span></td><td>{{ r.createdItemCd }}</td>
          </tr>
          <tr v-if="!list.length"><td colspan="6" class="empty">등록요청이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; } th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; } .empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.APPROVED { background: #e3f6e8; color: #1a7f37; } .badge.REJECTED { background: #f3e3e3; color: #a33; } .badge.CANCEL { background: #eee; color: #777; }
</style>
