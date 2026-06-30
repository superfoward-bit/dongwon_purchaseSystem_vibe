<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
import { useAuthStore } from '@/stores/auth'

interface Stat { code: string; name: string; cnt: number }
interface Month { ym: string; amt: number }
interface Doc { id: number; docNo: string; title?: string; vdNm?: string; amt?: number; stsNm?: string; regDt?: string }

const router = useRouter()
const auth = useAuthStore()
const d = ref<any>(null)

async function load() { d.value = (await http.get('/dashboard')).data.data }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
function ymLabel(ym: string) { return `${ym.slice(2, 4)}.${ym.slice(4, 6)}` }

const maxMonthAmt = computed(() => Math.max(1, ...(d.value?.poMonthly || []).map((m: Month) => m.amt)))
function gradeColor(code: string) { return ({ A: '#0f7a35', B: '#1a4f9c', C: '#9a6b00', D: '#b3261e' } as any)[code] || '#999' }

onMounted(load)
</script>

<template>
  <div class="page" v-if="d">
    <h2>대시보드 <small>{{ auth.usrNm }} 님 환영합니다</small></h2>

    <!-- 카드 -->
    <div class="cards">
      <div class="card c1" @click="router.push('/pro/pr')">
        <div class="lbl">진행중 구매요청</div><div class="val">{{ d.cards.prOpen }}<span>건</span></div>
      </div>
      <div class="card c2" @click="router.push('/pro/po')">
        <div class="lbl">발주완료</div><div class="val">{{ d.cards.poDone }}<span>건</span></div>
      </div>
      <div class="card c3" @click="router.push('/pro/gr')">
        <div class="lbl">입고확정</div><div class="val">{{ d.cards.grConfirmed }}<span>건</span></div>
      </div>
      <div class="card c4" @click="router.push('/pro/rx')">
        <div class="lbl">공고중 견적</div><div class="val">{{ d.cards.rfxOpen }}<span>건</span></div>
      </div>
      <div class="card c5">
        <div class="lbl">이번달 발주금액</div><div class="val sm">{{ fmt(d.cards.poAmtThisMonth) }}<span>원</span></div>
      </div>
    </div>

    <div class="row2">
      <!-- 월별 발주금액 -->
      <div class="panel">
        <div class="panel-head">월별 발주금액 (최근 6개월)</div>
        <div class="chart">
          <div v-for="m in d.poMonthly" :key="m.ym" class="bar-col">
            <div class="bar" :style="{ height: (m.amt / maxMonthAmt * 140 + 2) + 'px' }" :title="fmt(m.amt)"></div>
            <div class="bar-lbl">{{ ymLabel(m.ym) }}</div>
          </div>
          <div v-if="!d.poMonthly.length" class="empty">발주 데이터가 없습니다.</div>
        </div>
      </div>

      <!-- 협력사 등급분포 -->
      <div class="panel">
        <div class="panel-head">협력사 등급분포</div>
        <div class="grades">
          <div v-for="g in d.vendorByGrade" :key="g.code" class="grade-row">
            <span class="g-name" :style="{ color: gradeColor(g.code) }">{{ g.name }}</span>
            <div class="g-bar-bg"><div class="g-bar" :style="{ width: (g.cnt * 28 + 10) + 'px', background: gradeColor(g.code) }">{{ g.cnt }}</div></div>
          </div>
        </div>
      </div>
    </div>

    <div class="row3">
      <div class="panel" v-for="(grp, key) in { '구매요청': d.prByStatus, '발주': d.poByStatus, '입고': d.grByStatus }" :key="key">
        <div class="panel-head">{{ key }} 상태분포</div>
        <div class="chips">
          <div v-for="s in grp" :key="s.code" class="chip"><span>{{ s.name }}</span><b>{{ s.cnt }}</b></div>
          <div v-if="!grp.length" class="empty">없음</div>
        </div>
      </div>
    </div>

    <div class="row2">
      <div class="panel">
        <div class="panel-head">최근 발주</div>
        <table>
          <thead><tr><th>발주번호</th><th>협력사</th><th class="r">금액</th><th>상태</th></tr></thead>
          <tbody>
            <tr v-for="p in d.recentPo" :key="p.id" class="row" @click="router.push(`/pro/po/${p.id}`)">
              <td>{{ p.docNo }}</td><td>{{ p.vdNm }}</td><td class="r">{{ fmt(p.amt) }}</td><td>{{ p.stsNm }}</td>
            </tr>
            <tr v-if="!d.recentPo.length"><td colspan="4" class="empty">없음</td></tr>
          </tbody>
        </table>
      </div>
      <div class="panel">
        <div class="panel-head">최근 입고</div>
        <table>
          <thead><tr><th>입고번호</th><th>발주번호</th><th class="r">금액</th><th>상태</th></tr></thead>
          <tbody>
            <tr v-for="g in d.recentGr" :key="g.id" class="row" @click="router.push(`/pro/gr/${g.id}`)">
              <td>{{ g.docNo }}</td><td>{{ g.title }}</td><td class="r">{{ fmt(g.amt) }}</td><td>{{ g.stsNm }}</td>
            </tr>
            <tr v-if="!d.recentGr.length"><td colspan="4" class="empty">없음</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
h2 { margin: 0 0 16px; } h2 small { font-size: 14px; color: #999; font-weight: normal; margin-left: 8px; }
.cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 16px; }
.card { background: #fff; border-radius: 10px; padding: 18px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); cursor: pointer; border-left: 4px solid #ccc; }
.card:hover { box-shadow: 0 3px 10px rgba(0,0,0,0.1); }
.card .lbl { color: #888; font-size: 13px; }
.card .val { font-size: 30px; font-weight: bold; color: #222; margin-top: 6px; }
.card .val.sm { font-size: 22px; } .card .val span { font-size: 13px; color: #999; margin-left: 3px; font-weight: normal; }
.card.c1 { border-color: #4a7bd0; } .card.c2 { border-color: #1a7f37; } .card.c3 { border-color: #d08a1a; }
.card.c4 { border-color: #9a4fd0; } .card.c5 { border-color: #1a3a6b; }
.row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.row3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; }
.chart { display: flex; align-items: flex-end; gap: 18px; height: 190px; padding: 16px 20px; }
.bar-col { display: flex; flex-direction: column; align-items: center; justify-content: flex-end; }
.bar { width: 38px; background: linear-gradient(#4a7bd0, #1a3a6b); border-radius: 4px 4px 0 0; transition: height .3s; }
.bar-lbl { margin-top: 6px; font-size: 12px; color: #888; }
.grades { padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.grade-row { display: flex; align-items: center; gap: 12px; }
.g-name { width: 80px; font-weight: bold; font-size: 14px; }
.g-bar-bg { flex: 1; }
.g-bar { height: 24px; border-radius: 4px; color: #fff; font-size: 13px; display: flex; align-items: center; justify-content: flex-end; padding-right: 8px; min-width: 22px; }
.chips { padding: 14px 16px; display: flex; flex-wrap: wrap; gap: 8px; }
.chip { background: #f2f5fa; border-radius: 16px; padding: 6px 12px; font-size: 13px; display: flex; gap: 8px; align-items: center; }
.chip b { color: #1a3a6b; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 14px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
th.r, td.r { text-align: right; }
.row { cursor: pointer; } .row:hover { background: #f7f9fc; }
.empty { text-align: center; color: #aaa; padding: 18px; }
</style>
