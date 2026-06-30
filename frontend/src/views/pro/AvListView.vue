<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Av { id: number; advNo: string; vdNm?: string; advAmt?: number; balance?: number; repaidAmt?: number; payYmd?: string; sts: string; stsNm?: string }
const router = useRouter()
const list = ref<Av[]>([])
const keyword = ref('')
const sts = ref('')
const codes = ref<{ cd: string; cdNmKo: string }[]>([])
async function load() { list.value = (await http.get('/pro/av', { params: { keyword: keyword.value, sts: sts.value } })).data.data }
async function loadCodes() { codes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'AV_STS' } })).data.data }
function open(id: number | 'new') { router.push(`/pro/av/${id}`) }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <h2>선급금</h2>
    <div class="toolbar">
      <input v-model="keyword" placeholder="선급금번호/협력사" @keyup.enter="load" />
      <select v-model="sts" @change="load"><option value="">전체 상태</option><option v-for="c in codes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
      <button class="btn" @click="load">검색</button>
      <button class="btn primary" @click="open('new')">+ 신규 선급금</button>
    </div>
    <div class="panel">
      <table>
        <thead><tr><th>선급금번호</th><th>협력사</th><th class="r">선급금액</th><th class="r">반제액</th><th class="r">잔액</th><th>지급예정</th><th>상태</th></tr></thead>
        <tbody>
          <tr v-for="a in list" :key="a.id" class="row" @click="open(a.id)">
            <td>{{ a.advNo }}</td><td>{{ a.vdNm }}</td>
            <td class="r">{{ fmt(a.advAmt) }}</td><td class="r">{{ fmt(a.repaidAmt) }}</td>
            <td class="r"><b :class="{ zero: (a.balance||0)===0 }">{{ fmt(a.balance) }}</b></td>
            <td>{{ a.payYmd }}</td>
            <td><span class="badge" :class="a.sts">{{ a.stsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="empty">선급금이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; } .toolbar input, .toolbar select { padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; } .toolbar .primary { margin-left: auto; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; } .zero { color: #1a7f37; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.TMP { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.CFM { background: #dbeafe; color: #1a4f9c; } .badge.CLOSED { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
</style>
