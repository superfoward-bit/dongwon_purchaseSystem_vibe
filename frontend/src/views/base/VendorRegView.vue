<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

interface Vendor { id: number; vdCd?: string; vdNm?: string; bizNo?: string; ceoNm?: string; vdTypNm?: string; indSectNm?: string; purcChrgNm?: string; regSts?: string; regStsNm?: string; vdStsNm?: string; vdSts?: string }
const router = useRouter()
const list = ref<Vendor[]>([])
const keyword = ref('')
const regSts = ref('')
const codes = ref<{ cd: string; cdNmKo: string }[]>([])

async function load() { list.value = (await http.get('/base/vendor/reg', { params: { keyword: keyword.value, regSts: regSts.value } })).data.data }
async function loadCodes() { codes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'VD_REG_STS' } })).data.data }
function open(v: Vendor) { router.push(`/base/vendor?id=${v.id}`) }
onMounted(async () => { await loadCodes(); await load() })
</script>

<template>
  <div class="page">
    <h2>협력사 등록심사</h2>
    <p class="desc">신규 협력사의 등록 신청·심사 진행 현황입니다. 행을 클릭하면 협력사 상세에서 심사요청/결재를 진행할 수 있습니다.</p>
    <div class="panel">
      <div class="panel-head">
        등록심사 목록
        <input class="search" v-model="keyword" placeholder="코드/상호/사업자번호" @keyup.enter="load" />
        <select v-model="regSts" @change="load"><option value="">전체 심사상태</option><option v-for="c in codes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select>
        <button class="btn" @click="load">검색</button>
        <button class="btn primary" @click="router.push('/base/vendor')">+ 신규 협력사 등록</button>
      </div>
      <table>
        <thead><tr><th>코드</th><th>상호</th><th>대표자</th><th>유형</th><th>산업부문</th><th>구매담당</th><th>등록심사</th><th>거래상태</th></tr></thead>
        <tbody>
          <tr v-for="v in list" :key="v.id" class="row" @click="open(v)">
            <td>{{ v.vdCd }}</td><td>{{ v.vdNm }}</td><td>{{ v.ceoNm }}</td><td>{{ v.vdTypNm }}</td>
            <td>{{ v.indSectNm }}</td><td>{{ v.purcChrgNm }}</td>
            <td><span class="badge" :class="v.regSts">{{ v.regStsNm }}</span></td>
            <td><span class="badge" :class="v.vdSts">{{ v.vdStsNm }}</span></td>
          </tr>
          <tr v-if="!list.length"><td colspan="8" class="empty">대상이 없습니다.</td></tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 14px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.search { margin-left: auto; padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; } .panel-head select { padding: 6px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.DRAFT { background: #eef; color: #556; } .badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.APPROVED { background: #e3f6e8; color: #1a7f37; } .badge.REJECTED { background: #f3e3e3; color: #a33; }
.badge.ACTIVE { background: #e3f6e8; color: #1a7f37; } .badge.STOP { background: #f3e3e3; color: #a33; }
</style>
