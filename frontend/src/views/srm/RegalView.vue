<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Regal {
  id?: number; regalNo?: string; regalNm?: string; regalTyp?: string; regalTypNm?: string;
  planYmd?: string; notifyYmd?: string; enforceYmd?: string; targetDesc?: string;
  alarmYn?: string; alarmBeforeDay?: number; sts?: string; stsNm?: string; daysToEnforce?: number; remark?: string
}

const list = ref<Regal[]>([])
const keyword = ref('')
const editing = ref<Regal | null>(null)
const message = ref('')
const TYPES = [{ cd: 'LAW', nm: '법률' }, { cd: 'DECREE', nm: '시행령' }, { cd: 'NOTICE', nm: '고시' }, { cd: 'ETC', nm: '기타' }]

async function load() { list.value = (await http.get('/srm/regal', { params: { keyword: keyword.value } })).data.data }
function addNew() { editing.value = { regalTyp: 'LAW', alarmYn: 'Y', alarmBeforeDay: 30, sts: 'ACTIVE' } }
async function edit(r: Regal) { editing.value = (await http.get(`/srm/regal/${r.id}`)).data.data }
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/srm/regal/${editing.value.id}`, editing.value)
    else await http.post('/srm/regal', editing.value)
    message.value = '저장되었습니다.'; editing.value = null; await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(r: Regal) {
  if (!r.id || !confirm(`[${r.regalNm}] 삭제하시겠습니까?`)) return
  await http.delete(`/srm/regal/${r.id}`); message.value = '삭제되었습니다.'; await load()
}
function dday(r: Regal) {
  if (r.daysToEnforce == null || r.sts !== 'ACTIVE') return ''
  if (r.daysToEnforce < 0) return '시행됨'
  if (r.daysToEnforce === 0) return 'D-DAY'
  return `D-${r.daysToEnforce}`
}
function imminent(r: Regal) { return r.sts === 'ACTIVE' && r.daysToEnforce != null && r.daysToEnforce >= 0 && r.daysToEnforce <= (r.alarmBeforeDay ?? 30) }
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>규제준수</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="panel-head">
        규제/법령
        <input class="search" v-model="keyword" placeholder="법령명 검색" @keyup.enter="load" />
        <button class="btn" @click="load">검색</button>
        <button class="btn primary" @click="addNew">+ 추가</button>
      </div>
      <table>
        <thead><tr><th>번호</th><th>법령명</th><th>구분</th><th>공시일</th><th>시행일</th><th>임박</th><th>알림</th><th>상태</th><th></th></tr></thead>
        <tbody>
          <tr v-for="r in list" :key="r.id" :class="{ warn: imminent(r) }">
            <td>{{ r.regalNo }}</td><td>{{ r.regalNm }}</td><td>{{ r.regalTypNm }}</td>
            <td>{{ r.notifyYmd }}</td><td>{{ r.enforceYmd }}</td>
            <td><span v-if="dday(r)" class="dday" :class="{ red: imminent(r) }">{{ dday(r) }}</span></td>
            <td>{{ r.alarmYn === 'Y' ? `${r.alarmBeforeDay}일전` : '-' }}</td>
            <td><span class="badge" :class="r.sts">{{ r.stsNm }}</span></td>
            <td><button class="link" @click="edit(r)">수정</button><button class="link del" @click="remove(r)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="9" class="empty">등록된 규제가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '규제 수정' : '규제 등록' }}<small v-if="editing.regalNo"> ({{ editing.regalNo }})</small></div>
      <div class="form">
        <label class="w2">법령명 <input v-model="editing.regalNm" /></label>
        <label>구분 <select v-model="editing.regalTyp"><option v-for="t in TYPES" :key="t.cd" :value="t.cd">{{ t.nm }}</option></select></label>
        <label>상태 <select v-model="editing.sts"><option value="ACTIVE">관리중</option><option value="CLOSED">종료</option></select></label>
        <label>계획(예고)일 <input type="date" v-model="editing.planYmd" /></label>
        <label>공시일 <input type="date" v-model="editing.notifyYmd" /></label>
        <label>시행일 <input type="date" v-model="editing.enforceYmd" /></label>
        <label>알림 <select v-model="editing.alarmYn"><option value="Y">사용</option><option value="N">미사용</option></select></label>
        <label>알림(시행 N일전) <input type="number" v-model.number="editing.alarmBeforeDay" style="width:90px" /></label>
        <label class="w3">적용대상/내용 <input v-model="editing.targetDesc" /></label>
        <div class="actions"><button class="btn primary" @click="save">저장</button><button class="btn" @click="editing = null">취소</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 8px; }
.search { margin-left: auto; padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
tr.warn { background: #fff8ec; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.dday { padding: 2px 8px; border-radius: 8px; font-size: 12px; background: #eef; color: #556; font-weight: bold; }
.dday.red { background: #fde2e2; color: #b3261e; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-right: 6px; } .link.del { color: #d33; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; }
.badge.ACTIVE { background: #e3f6e8; color: #1a7f37; } .badge.CLOSED { background: #eee; color: #777; }
.editor { margin-top: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .form .w2 { flex: 1 1 280px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.actions { display: flex; gap: 8px; width: 100%; }
</style>
