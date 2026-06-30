<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Seg { id?: number; segCd?: string; segNm?: string; description?: string; defaultSheetCd?: string; defaultSheetNm?: string; sortNo?: number; useYn?: string; vendorCnt?: number }
interface Sheet { sheetCd: string; sheetNm: string }
const list = ref<Seg[]>([])
const sheets = ref<Sheet[]>([])
const message = ref('')
const showForm = ref(false)
const form = ref<Seg>(blank())

function blank(): Seg { return { sortNo: 1, useYn: 'Y' } }
async function load() { list.value = (await http.get('/srm/segment')).data.data }
async function loadSheets() { sheets.value = (await http.get('/srm/sheet')).data.data }
function openNew() { form.value = blank(); showForm.value = true }
function openEdit(s: Seg) { form.value = { ...s }; showForm.value = true }
async function save() {
  message.value = ''
  if (!form.value.segCd || !form.value.segNm) { message.value = '코드와 명칭을 입력하세요.'; return }
  try {
    if (form.value.id) await http.put(`/srm/segment/${form.value.id}`, form.value)
    else await http.post('/srm/segment', form.value)
    showForm.value = false; await load(); message.value = '저장되었습니다.'
  } catch (e: any) { message.value = e.message }
}
async function remove(s: Seg) {
  if (!confirm(`[${s.segNm}] 삭제하시겠습니까?`)) return
  if ((s.vendorCnt ?? 0) > 0 && !confirm('소속 협력사가 있습니다. 그래도 삭제할까요?')) return
  await http.delete(`/srm/segment/${s.id}`); await load()
}
onMounted(async () => { await loadSheets(); await load() })
</script>

<template>
  <div class="page">
    <div class="head"><h2>협력사 세그먼트</h2><button class="btn primary" @click="openNew">+ 신규</button></div>
    <p v-if="message" class="msg">{{ message }}</p>
    <p class="desc">협력사를 세그먼트(전략/일반/한시 등)로 분류하고, 세그먼트별 기본 평가시트·등급행렬을 다르게 적용합니다.</p>

    <div class="panel">
      <table>
        <thead><tr><th>코드</th><th>세그먼트</th><th>설명</th><th>기본 평가시트</th><th class="r">소속 협력사</th><th>사용</th><th></th></tr></thead>
        <tbody>
          <tr v-for="s in list" :key="s.id">
            <td>{{ s.segCd }}</td><td><b>{{ s.segNm }}</b></td><td class="dim">{{ s.description }}</td>
            <td>{{ s.defaultSheetNm || '-' }}</td><td class="r">{{ s.vendorCnt }}개</td>
            <td><span class="badge" :class="s.useYn === 'Y' ? 'on' : 'off'">{{ s.useYn === 'Y' ? '사용' : '중지' }}</span></td>
            <td class="rgt"><button class="link" @click="openEdit(s)">수정</button><button class="link del" @click="remove(s)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="empty">세그먼트가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="showForm" class="modal-bg" @click.self="showForm = false">
      <div class="modal">
        <div class="modal-head">세그먼트 {{ form.id ? '수정' : '등록' }} <button class="x" @click="showForm = false">×</button></div>
        <div class="modal-body">
          <label>코드 <input v-model="form.segCd" :disabled="!!form.id" placeholder="STRAT 등" /></label>
          <label>명칭 <input v-model="form.segNm" /></label>
          <label class="w2">설명 <input v-model="form.description" /></label>
          <label>기본 평가시트 <select v-model="form.defaultSheetCd"><option value="">선택</option><option v-for="sh in sheets" :key="sh.sheetCd" :value="sh.sheetCd">{{ sh.sheetNm }}</option></select></label>
          <label>정렬 <input type="number" v-model.number="form.sortNo" style="width:70px" /></label>
          <label>사용 <select v-model="form.useYn"><option value="Y">사용</option><option value="N">중지</option></select></label>
        </div>
        <div class="modal-foot"><button class="btn primary" @click="save">저장</button><button class="btn" @click="showForm = false">취소</button></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 10px; } .head h2 { margin: 0; } .head .primary { margin-left: auto; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; } .desc { color: #888; font-size: 13px; margin: 0 0 12px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 9px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; } td.rgt { text-align: right; }
.dim { color: #888; } .empty { text-align: center; color: #aaa; padding: 22px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-left: 6px; } .link.del { color: #d33; }
.badge { padding: 2px 9px; border-radius: 10px; font-size: 12px; } .badge.on { background: #e3f6e8; color: #1a7f37; } .badge.off { background: #eee; color: #888; }
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 460px; background: #fff; border-radius: 10px; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; } .x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-body { padding: 16px 18px; display: flex; flex-wrap: wrap; gap: 12px; } .modal-body label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 45%; } .modal-body .w2 { flex: 1 1 100%; }
.modal-body input, .modal-body select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.modal-foot { padding: 12px 18px; border-top: 1px solid #eee; display: flex; gap: 8px; justify-content: flex-end; }
</style>
