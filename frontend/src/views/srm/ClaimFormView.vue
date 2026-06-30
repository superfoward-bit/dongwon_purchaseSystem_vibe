<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import VendorPicker from '@/components/VendorPicker.vue'
import ItemPicker from '@/components/ItemPicker.vue'
import AttachPanel from '@/components/AttachPanel.vue'

interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Clm {
  id?: number; clmNo?: string; vdCd?: string; vdNm?: string; clmTyp?: string; srcTyp?: string; refNo?: string; itemCd?: string; itemNm?: string;
  clmYmd?: string; lotNo?: string; mfgYmd?: string; compTyp?: string; claimQty?: number; clmAmt?: number; recoverAmt?: number; clmDesc?: string; dueYmd?: string; resolveDesc?: string; sts?: string; stsNm?: string; remark?: string;
  actions?: Action[]; history?: His[]
}
const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const clm = ref<Clm>({ clmTyp: 'QUALITY', srcTyp: 'SELF', clmYmd: today(), clmAmt: 0 })
const typeCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const compCodes = ref<{ cd: string; cdNmKo: string }[]>([])
const showVendorPicker = ref(false)
const showItemPicker = ref(false)

function today() { return new Date().toISOString().slice(0, 10) }
const editable = computed(() => isNew.value || (clm.value.sts !== 'CLOSED' && clm.value.sts !== 'CANCEL'))

async function load() {
  typeCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'CLM_TYP' } })).data.data
  compCodes.value = (await http.get('/sys/code/codes', { params: { grpCd: 'CLM_COMP' } })).data.data
  if (!isNew.value) await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) { clm.value = (await http.get(`/srm/claim/${id}`)).data.data }
function onVendorSelect(v: { vdCd: string; vdNm: string }) { clm.value.vdCd = v.vdCd; clm.value.vdNm = v.vdNm; showVendorPicker.value = false }
function onItemSelect(it: any) { clm.value.itemCd = it.itemCd; clm.value.itemNm = it.itemNm; showItemPicker.value = false }
async function save() {
  message.value = ''
  if (!clm.value.vdCd) { message.value = '협력사를 선택하세요.'; return }
  try {
    if (isNew.value) { const { data } = await http.post('/srm/claim', clm.value); router.replace(`/srm/claim/${data.data}`); await reloadById(data.data); message.value = '등록되었습니다.' }
    else { await http.put(`/srm/claim/${clm.value.id}`, clm.value); await reloadById(clm.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/srm/claim/${clm.value.id}/action`, { action: a.action, reason }); await reloadById(clm.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.${a.action === 'START' ? ' (협력사 처리요청 알림 발송)' : ''}` }
  catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/srm/claim/${clm.value.id}`); router.push('/srm/claim') }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>클레임 {{ isNew ? '등록' : '상세' }}
        <span v-if="clm.clmNo" class="prno">{{ clm.clmNo }}</span>
        <span v-if="clm.stsNm" class="badge" :class="clm.sts">{{ clm.stsNm }}</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/srm/claim')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록' : '저장' }}</button>
        <button v-if="!isNew && clm.sts === 'OPEN'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in clm.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">클레임 정보</div>
      <div class="form">
        <label class="w2">협력사 <div class="picker"><input :value="clm.vdNm ? `${clm.vdNm} (${clm.vdCd})` : ''" readonly /><button v-if="editable" class="btn sm" @click="showVendorPicker = true">찾기</button></div></label>
        <label>품목 <div class="picker"><input :value="clm.itemNm" readonly /><button v-if="editable" class="btn sm" @click="showItemPicker = true">찾기</button></div></label>
        <label>클레임유형 <select v-model="clm.clmTyp" :disabled="!editable"><option v-for="c in typeCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>발생일 <input type="date" v-model="clm.clmYmd" :disabled="!editable" /></label>
        <label>처리기한 <input type="date" v-model="clm.dueYmd" :disabled="!editable" /></label>
        <label>LOT번호 <input v-model="clm.lotNo" :disabled="!editable" /></label>
        <label>제조일자 <input type="date" v-model="clm.mfgYmd" :disabled="!editable" /></label>
        <label>클레임수량 <input type="number" v-model.number="clm.claimQty" :disabled="!editable" /></label>
        <label>청구(손해)액 <input type="number" v-model.number="clm.clmAmt" :disabled="!editable" /></label>
        <label>보상방식 <select v-model="clm.compTyp" :disabled="!editable"><option value="">선택</option><option v-for="c in compCodes" :key="c.cd" :value="c.cd">{{ c.cdNmKo }}</option></select></label>
        <label>실회수(보상)금액 <input type="number" v-model.number="clm.recoverAmt" :disabled="!editable" /></label>
        <label>참조(입고/검사) <input v-model="clm.refNo" :disabled="!editable" /></label>
        <label class="w3">클레임 내용 <input v-model="clm.clmDesc" :disabled="!editable" /></label>
        <label class="w3">처리/조치 내용 <input v-model="clm.resolveDesc" :disabled="!editable" /></label>
      </div>
    </div>

    <div v-if="!isNew && clm.id" class="panel">
      <div class="panel-head">첨부파일 (클레임 증빙)</div>
      <div style="padding:8px 12px"><AttachPanel refTyp="CLM" :refId="clm.id" :readonly="!editable" /></div>
    </div>

    <div v-if="clm.history && clm.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in clm.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>

    <VendorPicker v-if="showVendorPicker" @select="onVendorSelect" @close="showVendorPicker = false" />
    <ItemPicker v-if="showItemPicker" @select="onItemSelect" @close="showItemPicker = false" />
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.prno { font-size: 14px; color: #888; font-weight: normal; } .head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 200px; } .form .w2 { flex: 1 1 300px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .picker { display: flex; gap: 6px; } .picker input { flex: 1; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 8px; font-size: 13px; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.OPEN { background: #eef; color: #556; } .badge.PROC { background: #fff3d6; color: #9a6b00; } .badge.RESOLVED { background: #dbeafe; color: #1a4f9c; } .badge.CLOSED { background: #e3f6e8; color: #1a7f37; } .badge.CANCEL { background: #f3e3e3; color: #a33; }
</style>
