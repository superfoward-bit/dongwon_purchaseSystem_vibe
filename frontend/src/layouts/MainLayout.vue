<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute, RouterView } from 'vue-router'
import http from '@/api/http'
import { useAuthStore } from '@/stores/auth'

interface Menu {
  menuId: string
  menuNm: string
  url?: string
  icon?: string
  children: Menu[]
}

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const menus = ref<Menu[]>([])
const expanded = ref<Set<string>>(new Set())

onMounted(async () => {
  try {
    const { data } = await http.get('/menu/my')
    menus.value = data.data
    expandActive()
  } catch {
    menus.value = []
  }
})

/** 현재 경로가 속한 상위 메뉴를 자동으로 펼침 */
function expandActive() {
  for (const m of menus.value) {
    if (m.children?.some(c => isActive(c.url))) expanded.value.add(m.menuId)
  }
  expanded.value = new Set(expanded.value)
}
watch(() => route.path, expandActive)

function toggle(id: string) {
  const s = new Set(expanded.value)
  s.has(id) ? s.delete(id) : s.add(id)
  expanded.value = s
}
function isActive(url?: string) {
  return !!url && (route.path === url || route.path.startsWith(url + '/'))
}
function go(url?: string) {
  if (url) router.push(url)
}
function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">구매시스템</div>
      <nav>
        <div v-for="m in menus" :key="m.menuId" class="menu-grp">
          <button class="grp-title" :class="{ open: expanded.has(m.menuId) }" @click="toggle(m.menuId)">
            <span>{{ m.menuNm }}</span>
            <span class="chev">▸</span>
          </button>
          <div v-show="expanded.has(m.menuId)" class="sub">
            <a
              v-for="c in m.children"
              :key="c.menuId"
              class="menu-item"
              :class="{ active: isActive(c.url) }"
              @click="go(c.url)"
            >{{ c.menuNm }}</a>
          </div>
        </div>
      </nav>
    </aside>
    <div class="main">
      <header class="topbar">
        <span class="spacer" />
        <span class="user">{{ auth.usrNm }} 님</span>
        <button class="logout" @click="logout">로그아웃</button>
      </header>
      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout { display: flex; height: 100vh; font-family: 'Malgun Gothic', sans-serif; }
.sidebar { width: 220px; background: #1a3a6b; color: #fff; flex-shrink: 0; display: flex; flex-direction: column; }
.logo { padding: 20px; font-size: 18px; font-weight: bold; border-bottom: 1px solid #2c4f85; flex-shrink: 0; }
nav { flex: 1; overflow-y: auto; padding-bottom: 16px; }
nav::-webkit-scrollbar { width: 8px; }
nav::-webkit-scrollbar-thumb { background: #2c4f85; border-radius: 4px; }
nav::-webkit-scrollbar-track { background: transparent; }
.menu-grp { border-bottom: 1px solid rgba(255,255,255,0.05); }
.grp-title { width: 100%; box-sizing: border-box; display: flex; align-items: center; justify-content: space-between;
  padding: 12px 18px; font-size: 13px; font-weight: 600; color: #cdd9ee; background: none; border: none; cursor: pointer; text-align: left; }
.grp-title:hover { background: #234276; color: #fff; }
.grp-title .chev { font-size: 11px; color: #7e9bca; transition: transform 0.15s; }
.grp-title.open { color: #fff; }
.grp-title.open .chev { transform: rotate(90deg); color: #cdd9ee; }
.sub { background: #16335f; padding: 4px 0; }
.menu-item { display: block; padding: 8px 18px 8px 30px; color: #b9c8e2; cursor: pointer; font-size: 13.5px; border-left: 3px solid transparent; }
.menu-item:hover { background: #234276; color: #fff; }
.menu-item.active { color: #fff; background: #2c4f85; border-left-color: #6ea8ff; font-weight: 600; }
.main { flex: 1; display: flex; flex-direction: column; background: #f0f2f5; }
.topbar { height: 56px; background: #fff; display: flex; align-items: center; padding: 0 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.spacer { flex: 1; }
.user { margin-right: 16px; font-size: 14px; }
.logout { padding: 6px 14px; border: 1px solid #ccc; background: #fff; border-radius: 5px; cursor: pointer; }
.content { flex: 1; padding: 24px; overflow: auto; }
</style>
