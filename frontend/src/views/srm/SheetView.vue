<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import http from '@/api/http'

interface SItem { itemSeq?: number; evalItemNm?: string; evalCls?: string; weight?: number; cateSeq?: number }
interface SCate { cateSeq?: number; cateNm?: string; weight?: number }
interface Sheet { id?: number; sheetCd?: string; sheetNm?: string; evalTyp?: string; description?: string; useCateYn?: string; useYn?: string; items: SItem[]; categories: SCate[] }

const list = ref<Sheet[]>([])
const editing = ref<Sheet | null>(null)
const message = ref('')
const twoLevel = computed(() => editing.value?.useCateYn === 'Y')
const flatSum = computed(() => (editing.value?.items || []).reduce((s, i) => s + (Number(i.weight) || 0), 0))
const cateSum = computed(() => (editing.value?.categories || []).reduce((s, c) => s + (Number(c.weight) || 0), 0))
function itemSumOf(seq: number) { return (editing.value?.items || []).filter(i => i.cateSeq === seq).reduce((s, i) => s + (Number(i.weight) || 0), 0) }

async function load() { list.value = (await http.get('/srm/sheet')).data.data }
function addNew() { editing.value = { sheetCd: '', sheetNm: '', evalTyp: 'REGULAR', useCateYn: 'N', useYn: 'Y', categories: [], items: [{ evalItemNm: '', evalCls: 'QUANT', weight: 0 }] } }
async function edit(s: Sheet) {
  const { data } = await http.get(`/srm/sheet/${s.sheetCd}`)
  editing.value = { ...data.data, items: data.data.items || [], categories: data.data.categories || [] }
}
function toggleCate() {
  if (!editing.value) return
  editing.value.useCateYn = twoLevel.value ? 'N' : 'Y'
  if (editing.value.useCateYn === 'Y' && !editing.value.categories.length) {
    editing.value.categories = [{ cateSeq: 1, cateNm: '', weight: 0 }]
    editing.value.items.forEach(i => { if (!i.cateSeq) i.cateSeq = 1 })
  }
}
function addCate() { const seq = (editing.value!.categories.length) + 1; editing.value!.categories.push({ cateSeq: seq, cateNm: '', weight: 0 }) }
function delCate(i: number) { editing.value!.categories.splice(i, 1); editing.value!.categories.forEach((c, idx) => c.cateSeq = idx + 1) }
function addItem() { editing.value?.items.push({ evalItemNm: '', evalCls: 'QUANT', weight: 0, cateSeq: twoLevel.value ? 1 : undefined }) }
function delItem(i: number) { editing.value?.items.splice(i, 1) }
async function save() {
  if (!editing.value) return
  try {
    if (editing.value.id) await http.put(`/srm/sheet/${editing.value.id}`, editing.value)
    else await http.post('/srm/sheet', editing.value)
    message.value = '저장되었습니다.'; editing.value = null; await load()
  } catch (e: any) { message.value = e.message }
}
async function remove(s: Sheet) {
  if (!s.id || !confirm(`[${s.sheetNm}] 삭제하시겠습니까?`)) return
  await http.delete(`/srm/sheet/${s.id}`, { params: { sheetCd: s.sheetCd } }); message.value = '삭제되었습니다.'; await load()
}
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>평가시트 관리</h2>
    <p v-if="message" class="msg">{{ message }}</p>
    <div class="panel">
      <div class="panel-head">평가시트 <button class="btn" @click="addNew">+ 추가</button></div>
      <table>
        <thead><tr><th>코드</th><th>시트명</th><th>유형</th><th>가중방식</th><th>설명</th><th>사용</th><th></th></tr></thead>
        <tbody>
          <tr v-for="s in list" :key="s.id">
            <td>{{ s.sheetCd }}</td><td>{{ s.sheetNm }}</td><td>{{ s.evalTyp === 'REGULAR' ? '정기' : '등록' }}</td>
            <td><span class="lv" :class="{ two: s.useCateYn === 'Y' }">{{ s.useCateYn === 'Y' ? '2단계(카테고리)' : '단일' }}</span></td>
            <td>{{ s.description }}</td><td>{{ s.useYn }}</td>
            <td><button class="link" @click="edit(s)">수정</button><button class="link del" @click="remove(s)">삭제</button></td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="empty">평가시트가 없습니다.</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="editing" class="editor">
      <div class="panel-head">{{ editing.id ? '시트 수정' : '시트 등록' }}
        <label class="cate-toggle"><input type="checkbox" :checked="twoLevel" @change="toggleCate" /> 2단계 가중(카테고리)</label>
      </div>
      <div class="form">
        <label>시트코드 <input v-model="editing.sheetCd" :disabled="!!editing.id" /></label>
        <label>시트명 <input v-model="editing.sheetNm" /></label>
        <label>유형 <select v-model="editing.evalTyp"><option value="REGULAR">정기평가</option><option value="REGISTER">등록평가</option></select></label>
        <label class="w2">설명 <input v-model="editing.description" /></label>
        <label>사용 <select v-model="editing.useYn"><option value="Y">Y</option><option value="N">N</option></select></label>
      </div>

      <!-- 카테고리 (2단계) -->
      <div v-if="twoLevel" class="catebox">
        <div class="cate-head">카테고리(팩터그룹) <span class="sum" :class="{ bad: cateSum !== 100 }">카테고리 배점합 {{ cateSum }} / 100</span><button class="btn sm" @click="addCate">+ 카테고리</button></div>
        <table class="items">
          <thead><tr><th>순번</th><th>카테고리명</th><th class="r">배점</th><th class="r">항목배점합</th><th></th></tr></thead>
          <tbody>
            <tr v-for="(c, i) in editing.categories" :key="i">
              <td>{{ i + 1 }}</td><td><input v-model="c.cateNm" /></td>
              <td class="r"><input type="number" v-model.number="c.weight" class="num" /></td>
              <td class="r"><span :class="{ bad: itemSumOf(i + 1) !== 100 }">{{ itemSumOf(i + 1) }}/100</span></td>
              <td><button class="link del" @click="delCate(i)">삭제</button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="item-head">평가항목 <span v-if="!twoLevel" class="sum" :class="{ bad: flatSum !== 100 }">배점합 {{ flatSum }} / 100</span></div>
      <table class="items">
        <thead><tr><th>순번</th><th v-if="twoLevel">카테고리</th><th>평가항목</th><th>구분</th><th class="r">배점</th><th></th></tr></thead>
        <tbody>
          <tr v-for="(it, i) in editing.items" :key="i">
            <td>{{ i + 1 }}</td>
            <td v-if="twoLevel"><select v-model.number="it.cateSeq"><option v-for="(c, ci) in editing.categories" :key="ci" :value="ci + 1">{{ c.cateNm || ('카테고리' + (ci + 1)) }}</option></select></td>
            <td><input v-model="it.evalItemNm" /></td>
            <td><select v-model="it.evalCls"><option value="QUANT">정량</option><option value="QUAL">정성</option></select></td>
            <td class="r"><input type="number" v-model.number="it.weight" class="num" /></td>
            <td><button class="link del" @click="delItem(i)">삭제</button></td>
          </tr>
        </tbody>
      </table>
      <div class="actions">
        <button class="btn sm" @click="addItem">+ 항목추가</button>
        <span class="hint" v-if="twoLevel">2단계: 카테고리 배점합 100, 각 카테고리 내 항목 배점합 100</span>
        <span class="spacer" />
        <button class="btn primary" @click="save">저장</button>
        <button class="btn" @click="editing = null">취소</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { font-family: 'Malgun Gothic', sans-serif; } h2 { margin: 0 0 16px; }
.msg { color: #1a7f37; background: #eafaef; padding: 8px 12px; border-radius: 6px; }
.panel { background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.panel-head { padding: 12px 16px; border-bottom: 1px solid #eee; font-weight: bold; display: flex; align-items: center; gap: 10px; }
.panel-head .btn { margin-left: auto; }
.cate-toggle { margin-left: auto; font-weight: normal; font-size: 13px; display: flex; align-items: center; gap: 6px; color: #1a3a6b; }
.lv { font-size: 12px; color: #777; } .lv.two { color: #6b3fa0; font-weight: bold; }
.sum { color: #1a7f37; font-weight: normal; font-size: 13px; } .sum.bad, .bad { color: #c0392b; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; } th.r, td.r { text-align: right; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.editor { margin-top: 16px; background: #fff; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.form { padding: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: flex-end; }
.form label { display: flex; flex-direction: column; font-size: 13px; color: #555; gap: 4px; } .form .w2 { flex: 1 1 280px; }
.form input, .form select { padding: 8px; border: 1px solid #ddd; border-radius: 5px; }
.catebox { border-top: 1px dashed #eee; padding: 8px 0; } .cate-head, .item-head { padding: 8px 16px; font-weight: bold; display: flex; align-items: center; gap: 10px; } .cate-head .btn { margin-left: 6px; } .cate-head .sum { margin-left: auto; }
.items td input, .items td select { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 100%; box-sizing: border-box; }
.items .num { text-align: right; width: 90px; }
.actions { padding: 12px 16px; display: flex; gap: 8px; align-items: center; } .spacer { flex: 1; } .hint { color: #999; font-size: 12px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; }
.btn.sm { padding: 4px 10px; font-size: 13px; } .btn.primary { background: #1a3a6b; color: #fff; }
.link { border: none; background: none; color: #1a3a6b; cursor: pointer; margin-right: 6px; } .link.del { color: #d33; }
</style>
