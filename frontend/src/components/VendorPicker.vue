<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

interface Vendor { vdCd: string; vdNm: string; ceoNm?: string; bizNo?: string; vdTypNm?: string }

const emit = defineEmits<{ (e: 'select', v: Vendor): void; (e: 'close'): void }>()

const list = ref<Vendor[]>([])
const keyword = ref('')

async function load() {
  const { data } = await http.get('/base/vendor', { params: { keyword: keyword.value, vdSts: 'ACTIVE' } })
  list.value = data.data
}
function pick(v: Vendor) { emit('select', v) }

onMounted(load)
</script>

<template>
  <div class="modal-bg" @click.self="emit('close')">
    <div class="modal">
      <div class="modal-head">협력사 선택 <button class="x" @click="emit('close')">×</button></div>
      <div class="modal-search">
        <input v-model="keyword" placeholder="코드/상호/사업자번호" @keyup.enter="load" autofocus />
        <button class="btn" @click="load">검색</button>
      </div>
      <div class="modal-body">
        <table>
          <thead><tr><th>코드</th><th>상호</th><th>대표자</th><th>유형</th></tr></thead>
          <tbody>
            <tr v-for="v in list" :key="v.vdCd" class="row" @click="pick(v)">
              <td>{{ v.vdCd }}</td><td>{{ v.vdNm }}</td><td>{{ v.ceoNm }}</td><td>{{ v.vdTypNm }}</td>
            </tr>
            <tr v-if="!list.length"><td colspan="4" class="empty">검색 결과가 없습니다.</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { width: 600px; max-height: 80vh; background: #fff; border-radius: 10px; display: flex; flex-direction: column; overflow: hidden; }
.modal-head { padding: 14px 18px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; }
.x { margin-left: auto; border: none; background: none; font-size: 22px; cursor: pointer; color: #888; }
.modal-search { padding: 12px 18px; display: flex; gap: 8px; }
.modal-search input { flex: 1; padding: 8px 10px; border: 1px solid #ddd; border-radius: 5px; }
.modal-body { overflow: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 8px 14px; border-bottom: 1px solid #f0f0f0; text-align: left; font-size: 14px; } th { background: #fafafa; position: sticky; top: 0; }
.row { cursor: pointer; } .row:hover { background: #eef3fb; }
.empty { text-align: center; color: #aaa; padding: 24px; }
.btn { padding: 7px 14px; border: 1px solid #1a3a6b; background: #1a3a6b; color: #fff; border-radius: 5px; cursor: pointer; }
</style>
