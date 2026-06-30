<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import http from '@/api/http'

const route = useRoute()
const po = ref<any>(null)
async function load() { po.value = (await http.get(`/pro/po/${route.params.id}`)).data.data }
function fmt(n?: number) { return (n ?? 0).toLocaleString() }
function print() { window.print() }
onMounted(load)
</script>

<template>
  <div v-if="po" class="sheet">
    <div class="noprint bar"><button class="btn" @click="print">🖨 인쇄</button><button class="btn" @click="$router.back()">닫기</button></div>
    <h1>발 주 서</h1>
    <div class="meta">
      <div><span>발주번호</span><b>{{ po.poNo }}</b></div>
      <div><span>발주일</span><b>{{ po.poYmd }}</b></div>
      <div><span>발주유형</span><b>{{ po.poTypNm }} / {{ po.purcTypNm }}</b></div>
    </div>
    <table class="info">
      <tr><th>공급자(협력사)</th><td>{{ po.vdNm }} ({{ po.vdCd }})</td><th>납기일</th><td>{{ po.dlvYmd || '-' }}</td></tr>
      <tr><th>건명</th><td colspan="3">{{ po.poTitle }}</td></tr>
      <tr><th>납품조건</th><td>{{ po.dlvCond || '-' }}</td><th>결제조건</th><td>{{ po.payCond || '-' }}</td></tr>
      <tr v-if="po.performBondYn==='Y' || po.maintBondYn==='Y'">
        <th>보증</th><td colspan="3">
          <span v-if="po.performBondYn==='Y'">계약이행보증 {{ po.performBondRate }}%</span>
          <span v-if="po.maintBondYn==='Y'"> · 하자유지보증 {{ po.maintBondRate }}%</span>
        </td>
      </tr>
    </table>

    <table class="items">
      <thead><tr><th>#</th><th>품목</th><th>규격</th><th>단위</th><th class="r">수량</th><th class="r">단가</th><th class="r">공급가</th><th class="r">부가세</th><th class="r">금액</th></tr></thead>
      <tbody>
        <tr v-for="(it, i) in po.items" :key="i">
          <td>{{ i + 1 }}</td><td>{{ it.itemNm }}</td><td>{{ it.spec }}</td><td>{{ it.unitCd }}</td>
          <td class="r">{{ fmt(it.qty) }}</td><td class="r">{{ fmt(it.prc) }}</td>
          <td class="r">{{ fmt(it.suplAmt) }}</td><td class="r">{{ fmt(it.vatAmt) }}</td><td class="r">{{ fmt(it.amt) }}</td>
        </tr>
      </tbody>
      <tfoot><tr><td colspan="6" class="r">합계</td><td class="r">{{ fmt(po.suplAmt) }}</td><td class="r">{{ fmt(po.vatAmt) }}</td><td class="r"><b>{{ fmt(po.totAmt) }}</b></td></tr></tfoot>
    </table>

    <table v-if="po.payPlanYn==='Y' && po.payPlans && po.payPlans.length" class="items">
      <caption>■ 분할결제(선금/기성/잔금) 계획</caption>
      <thead><tr><th>회차</th><th>유형</th><th>명칭</th><th class="r">비율</th><th class="r">금액</th><th>지급예정일</th></tr></thead>
      <tbody>
        <tr v-for="(p, i) in po.payPlans" :key="i">
          <td>{{ i + 1 }}</td><td>{{ p.payTypNm }}</td><td>{{ p.payNm }}</td>
          <td class="r">{{ p.rate }}%</td><td class="r">{{ fmt(p.amt) }}</td><td>{{ p.planYmd }}</td>
        </tr>
      </tbody>
    </table>

    <p class="foot">상기와 같이 발주합니다.</p>
    <div class="sign"><div>발주: ____________</div><div>공급자 확인: ____________</div></div>
  </div>
</template>

<style scoped>
.sheet { font-family: 'Malgun Gothic', serif; max-width: 900px; margin: 0 auto; padding: 24px; color: #222; background: #fff; }
h1 { text-align: center; letter-spacing: 12px; margin: 8px 0 20px; }
.bar { display: flex; gap: 8px; justify-content: flex-end; margin-bottom: 10px; }
.btn { padding: 6px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.meta { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; } .meta span { color: #777; margin-right: 6px; }
table { width: 100%; border-collapse: collapse; margin-bottom: 16px; font-size: 14px; }
.info th, .info td { border: 1px solid #999; padding: 8px 10px; text-align: left; } .info th { background: #f3f3f3; width: 130px; }
.items th, .items td { border: 1px solid #999; padding: 7px 9px; } .items th { background: #f3f3f3; text-align: center; }
.items .r, table .r { text-align: right; } .items caption { text-align: left; font-weight: bold; margin-bottom: 6px; }
.items tfoot td { background: #fafafa; font-weight: bold; }
.foot { text-align: center; margin: 24px 0 16px; font-size: 15px; }
.sign { display: flex; justify-content: space-around; margin-top: 30px; }
@media print { .noprint { display: none; } .sheet { padding: 0; } }
</style>
