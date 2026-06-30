<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '@/api/http'

const props = defineProps<{ refTyp: string; refId: number; readonly?: boolean }>()
interface Attach { id: number; fileNm: string; fileSize: number; contentTyp?: string; regId?: string; regDt?: string }
const list = ref<Attach[]>([])
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const message = ref('')

async function load() {
  list.value = (await http.get('/attach', { params: { refTyp: props.refTyp, refId: props.refId } })).data.data
}
function pick() { fileInput.value?.click() }
async function onFile(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (!files || !files.length) return
  uploading.value = true; message.value = ''
  try {
    for (const f of Array.from(files)) {
      const fd = new FormData()
      fd.append('file', f)
      await http.post(`/attach?refTyp=${props.refTyp}&refId=${props.refId}`, fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    }
    await load(); message.value = '업로드되었습니다.'
  } catch (err: any) { message.value = err.message }
  finally { uploading.value = false; if (fileInput.value) fileInput.value.value = '' }
}
async function download(a: Attach) {
  const res = await http.get(`/attach/${a.id}/download`, { responseType: 'blob' })
  const url = URL.createObjectURL(res.data)
  const link = document.createElement('a')
  link.href = url; link.download = a.fileNm; link.click()
  URL.revokeObjectURL(url)
}
async function remove(a: Attach) {
  if (!confirm('첨부파일을 삭제하시겠습니까?')) return
  await http.delete(`/attach/${a.id}`); await load()
}
function fmtSize(n: number) { return n < 1024 ? `${n} B` : n < 1048576 ? `${(n / 1024).toFixed(1)} KB` : `${(n / 1048576).toFixed(1)} MB` }
onMounted(load)
defineExpose({ reload: load })
</script>

<template>
  <div class="attach">
    <div class="ahead">
      <span class="atitle">첨부파일 ({{ list.length }})</span>
      <button v-if="!readonly" class="btn sm" :disabled="uploading" @click="pick">{{ uploading ? '업로드중...' : '+ 파일 추가' }}</button>
      <input ref="fileInput" type="file" multiple style="display:none" @change="onFile" />
      <span v-if="message" class="amsg">{{ message }}</span>
    </div>
    <ul class="alist">
      <li v-for="a in list" :key="a.id">
        <a class="fn" @click="download(a)">📎 {{ a.fileNm }}</a>
        <span class="sz">{{ fmtSize(a.fileSize) }}</span>
        <span class="reg">{{ a.regId }} · {{ a.regDt }}</span>
        <button v-if="!readonly" class="link del" @click="remove(a)">삭제</button>
      </li>
      <li v-if="!list.length" class="empty">첨부파일이 없습니다.</li>
    </ul>
  </div>
</template>

<style scoped>
.attach { border: 1px solid #eee; border-radius: 8px; }
.ahead { display: flex; align-items: center; gap: 10px; padding: 10px 14px; border-bottom: 1px solid #f0f0f0; }
.atitle { font-weight: bold; font-size: 14px; }
.amsg { color: #1a7f37; font-size: 12px; }
.alist { list-style: none; margin: 0; padding: 6px 14px; }
.alist li { display: flex; align-items: center; gap: 12px; padding: 6px 0; font-size: 14px; }
.fn { color: #1a3a6b; cursor: pointer; text-decoration: underline; }
.sz { color: #888; font-size: 12px; } .reg { color: #aaa; font-size: 12px; margin-left: auto; }
.empty { color: #aaa; justify-content: center; }
.btn { padding: 6px 12px; border: 1px solid #1a3a6b; background: #fff; color: #1a3a6b; border-radius: 5px; cursor: pointer; } .btn.sm { padding: 4px 10px; font-size: 13px; }
.link.del { border: none; background: none; color: #d33; cursor: pointer; font-size: 13px; }
</style>
