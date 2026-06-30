<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'

interface Attr { attrNm?: string; attrTyp?: string; optionVals?: string; requiredYn?: string; attrVal?: string }
interface Action { action: string; actionNm: string; direction: string; rsnReqYn: string }
interface His { seq: number; fromSts: string; toSts: string; actionNm: string; actorId: string; actionDt: string; rsn?: string }
interface Cate { cateCd: string; cateNm: string }
interface Req {
  id?: number; reqNo?: string; itemNm?: string; itemNmEn?: string; cateCd?: string; cateNm?: string; spec?: string; unitCd?: string;
  itemTyp?: string; taxTyp?: string; reqDesc?: string; sts?: string; stsNm?: string; createdItemCd?: string;
  attrList: Attr[]; actions?: Action[]; history?: His[]
}
const route = useRoute()
const router = useRouter()
const isNew = computed(() => route.params.id === 'new')
const message = ref('')
const req = ref<Req>({ taxTyp: 'TAX', attrList: [] })
const cates = ref<Cate[]>([])
const editable = computed(() => isNew.value || req.value.sts === 'REQ')

async function load() {
  cates.value = (await http.get('/base/category')).data.data
  if (!isNew.value) await reloadById(route.params.id as string)
}
async function reloadById(id: string | number) { const { data } = await http.get(`/base/item-req/${id}`); req.value = { ...data.data, attrList: data.data.attrList || [] } }
async function onCateChange() {
  const c = cates.value.find(x => x.cateCd === req.value.cateCd)
  req.value.cateNm = c ? c.cateNm : ''
  if (!req.value.cateCd) { req.value.attrList = []; return }
  const defs: Attr[] = (await http.get('/base/cate-attr', { params: { cateCd: req.value.cateCd } })).data.data
  // 기존 값 보존하며 분류 속성 정의로 재구성
  const prev: Record<string, string> = {}
  req.value.attrList.forEach(a => { if (a.attrNm) prev[a.attrNm] = a.attrVal || '' })
  req.value.attrList = defs.map(d => ({ ...d, attrVal: prev[d.attrNm!] || '' }))
}
async function save() {
  message.value = ''
  if (!req.value.itemNm) { message.value = '품목명을 입력하세요.'; return }
  const miss = req.value.attrList.find(a => a.requiredYn === 'Y' && !a.attrVal)
  if (miss) { message.value = `필수 속성 누락: ${miss.attrNm}`; return }
  try {
    if (isNew.value) { const { data } = await http.post('/base/item-req', req.value); router.replace(`/base/item-req/${data.data}`); await reloadById(data.data); message.value = '등록요청되었습니다.' }
    else { await http.put(`/base/item-req/${req.value.id}`, req.value); await reloadById(req.value.id!); message.value = '수정되었습니다.' }
  } catch (e: any) { message.value = e.message }
}
async function doAction(a: Action) {
  let reason = ''
  if (a.rsnReqYn === 'Y') { reason = window.prompt(`[${a.actionNm}] 사유`) || ''; if (!reason) return } else if (!confirm(`[${a.actionNm}] 처리하시겠습니까?`)) return
  try { await http.post(`/base/item-req/${req.value.id}/action`, { action: a.action, reason }); await reloadById(req.value.id!); message.value = `[${a.actionNm}] 처리되었습니다.${a.action === 'APPROVE' ? ' (품목 자동생성)' : ''}` }
  catch (e: any) { message.value = e.message }
}
async function remove() { if (!confirm('삭제하시겠습니까?')) return; await http.delete(`/base/item-req/${req.value.id}`); router.push('/base/item-req') }
onMounted(load)
</script>

<template>
  <div class="page">
    <div class="head">
      <h2>품목 등록요청 {{ isNew ? '' : '상세' }}
        <span v-if="req.reqNo" class="prno">{{ req.reqNo }}</span>
        <span v-if="req.stsNm" class="badge" :class="req.sts">{{ req.stsNm }}</span>
        <span v-if="req.createdItemCd" class="created">→ {{ req.createdItemCd }} 생성됨</span>
      </h2>
      <div class="head-actions">
        <button class="btn" @click="router.push('/base/item-req')">목록</button>
        <button v-if="editable" class="btn primary" @click="save">{{ isNew ? '등록요청' : '저장' }}</button>
        <button v-if="!isNew && req.sts === 'REQ'" class="btn del" @click="remove">삭제</button>
        <button v-for="a in req.actions" :key="a.action" class="btn" :class="a.direction === 'BWD' ? 'warn' : (a.direction === 'END' ? 'del' : 'go')" @click="doAction(a)">{{ a.actionNm }}</button>
      </div>
    </div>
    <p v-if="message" class="msg">{{ message }}</p>

    <div class="panel">
      <div class="panel-head">품목 정보</div>
      <div class="form">
        <label class="w2">품목명 <input v-model="req.itemNm" :disabled="!editable" /></label>
        <label>영문명 <input v-model="req.itemNmEn" :disabled="!editable" /></label>
        <label>분류 <select v-model="req.cateCd" :disabled="!editable" @change="onCateChange"><option value="">선택</option><option v-for="c in cates" :key="c.cateCd" :value="c.cateCd">{{ c.cateNm }}</option></select></label>
        <label>규격 <input v-model="req.spec" :disabled="!editable" /></label>
        <label>단위 <input v-model="req.unitCd" :disabled="!editable" /></label>
        <label>품목유형 <input v-model="req.itemTyp" :disabled="!editable" placeholder="RAW/SUB/GOODS/SVC" /></label>
        <label class="w3">요청사유 <input v-model="req.reqDesc" :disabled="!editable" /></label>
      </div>
    </div>

    <div v-if="req.attrList.length" class="panel">
      <div class="panel-head">분류 속성 ({{ req.cateNm }})</div>
      <div class="form">
        <label v-for="(a, i) in req.attrList" :key="i">
          {{ a.attrNm }} <em v-if="a.requiredYn === 'Y'" class="req">*</em>
          <select v-if="a.attrTyp === 'SELECT'" v-model="a.attrVal" :disabled="!editable"><option value="">선택</option><option v-for="o in (a.optionVals||'').split(',')" :key="o" :value="o.trim()">{{ o.trim() }}</option></select>
          <input v-else :type="a.attrTyp === 'NUM' ? 'number' : (a.attrTyp === 'DATE' ? 'date' : 'text')" v-model="a.attrVal" :disabled="!editable" />
        </label>
      </div>
    </div>

    <div v-if="req.history && req.history.length" class="panel">
      <div class="panel-head">진행이력</div>
      <table>
        <thead><tr><th>순번</th><th>처리</th><th>상태변화</th><th>처리자</th><th>일시</th><th>사유</th></tr></thead>
        <tbody><tr v-for="h in req.history" :key="h.seq"><td>{{ h.seq }}</td><td>{{ h.actionNm }}</td><td>{{ h.fromSts }} → {{ h.toSts }}</td><td>{{ h.actorId }}</td><td>{{ h.actionDt }}</td><td>{{ h.rsn }}</td></tr></tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; }
.head { display: flex; align-items: center; margin-bottom: 12px; } .head h2 { margin: 0; display: flex; align-items: center; gap: 10px; }
.prno { font-size: 14px; color: #888; font-weight: normal; } .created { font-size: 13px; color: #1a7f37; } .head-actions { margin-left: auto; display: flex; gap: 8px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); margin-bottom: 16px; }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; } .form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; flex: 1 1 200px; } .form .w2 { flex: 1 1 300px; } .form .w3 { flex: 1 1 100%; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; } .req { color: #c0392b; font-style: normal; }
table { width: 100%; border-collapse: collapse; } th, td { padding: 8px 10px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.primary { background: #1a3a6b; color: #fff; } .btn.go { background: #1a7f37; color: #fff; border-color: #1a7f37; } .btn.warn { background: #fff3d6; color: #9a6b00; border-color: #e3c878; } .btn.del { background: #fff; color: #c0392b; border-color: #e2b4ae; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 13px; }
.badge.REQ { background: #fff3d6; color: #9a6b00; } .badge.APPROVED { background: #e3f6e8; color: #1a7f37; } .badge.REJECTED { background: #f3e3e3; color: #a33; } .badge.CANCEL { background: #eee; color: #777; }
</style>
