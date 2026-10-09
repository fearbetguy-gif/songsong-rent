<template>
  <div class="house-list-page">
    <div class="list-header">
      <div class="list-header-row">
        <div>
          <h2>精选房源</h2>
          <p>真实房源，清晰价格，按需挑选</p>
        </div>
        <div class="header-actions">
          <router-link to="/" class="nav-btn">返回首页</router-link>
          <router-link to="/map-search" class="nav-btn primary">地图找房</router-link>
        </div>
      </div>
    </div>

    <p v-if="loading">加载中...</p>
    <p v-else-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div v-else class="list-grid">
      <HouseCard
        v-for="house in housePage.records"
        :key="house.id"
        :house="house"
        :to="`/houses/${house.id}`"
      />
    </div>

    <div v-if="!loading && !errorMessage" class="pager">
      <button :disabled="currentPage <= 1" @click="changePage(currentPage - 1)">上一页</button>
      <span>第 {{ currentPage }} 页</span>
      <button :disabled="isLastPage" @click="changePage(currentPage + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { getHouseList } from "../api";
import HouseCard from "../components/HouseCard.vue";

const loading = ref(false);
const errorMessage = ref("");
const currentPage = ref(1);
const housesPerPage = ref(10);
const housePage = ref({
  total: 0,
  pageNum: 1,
  pageSize: 10,
  records: [],
});
const pageCache = new Map();
let requestSeq = 0;

const isLastPage = computed(() => {
  return currentPage.value * housesPerPage.value >= housePage.value.total;
});

async function fetchHousePage() {
  const pageKey = `${currentPage.value}:${housesPerPage.value}`;
  if (pageCache.has(pageKey)) {
    housePage.value = pageCache.get(pageKey);
    prefetchNextPage();
    return;
  }
  const seq = ++requestSeq;
  loading.value = true;
  errorMessage.value = "";
  try {
    const pageData = await getHouseList(currentPage.value, housesPerPage.value);
    if (seq !== requestSeq) return;
    housePage.value = pageData;
    pageCache.set(pageKey, pageData);
    prefetchNextPage();
  } catch (error) {
    if (seq !== requestSeq) return;
    errorMessage.value = error.message || "加载失败";
  } finally {
    if (seq === requestSeq) loading.value = false;
  }
}

async function prefetchNextPage() {
  if (isLastPage.value) return;
  const nextPage = currentPage.value + 1;
  const pageKey = `${nextPage}:${housesPerPage.value}`;
  if (pageCache.has(pageKey)) return;
  try {
    const pageData = await getHouseList(nextPage, housesPerPage.value);
    pageCache.set(pageKey, pageData);
  } catch (error) {}
}

function changePage(nextPage) {
  currentPage.value = nextPage;
  fetchHousePage();
}

onMounted(() => {
  fetchHousePage();
});
</script>

<style scoped>
.house-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  background: var(--ss-bg-layout);
  color: var(--ss-text);
}

.list-header {
  margin-bottom: 18px;
  padding: 20px 24px;
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
  box-shadow: var(--ss-shadow-card);
}

.list-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.list-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
  color: var(--ss-text);
}

.list-header p {
  margin: 0;
  color: var(--ss-text-secondary);
}

.nav-btn {
  min-height: var(--ss-control-height);
  padding: 7px 16px;
  border: 1px solid var(--ss-border);
  border-radius: var(--ss-radius);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  color: var(--ss-primary);
  background: var(--ss-bg-container);
  transition: all 0.2s;
}

.nav-btn.primary {
  background: var(--ss-primary);
  border-color: var(--ss-primary);
  color: #fff;
}

.nav-btn:hover {
  transform: translateY(-1px);
  border-color: var(--ss-primary-hover);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.14);
}

.list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.house-card {
  display: block;
  padding: 0 0 16px;
  border-radius: var(--ss-radius-lg);
  border: 1px solid var(--ss-border-light);
  background: var(--ss-bg-container);
  color: inherit;
  text-decoration: none;
  box-shadow: var(--ss-shadow-card);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  contain: layout paint;
  content-visibility: auto;
  contain-intrinsic-size: 300px;
  touch-action: manipulation;
}

.house-card:hover {
  transform: translateY(-4px);
  border-color: #91caff;
  box-shadow: var(--ss-shadow);
}

.cover {
  width: 100%;
  height: 170px;
  object-fit: cover;
  border-top-left-radius: var(--ss-radius-lg);
  border-top-right-radius: var(--ss-radius-lg);
}

.house-card h3 {
  margin: 14px 14px 8px;
  font-size: 18px;
  color: var(--ss-text);
}

.price {
  margin: 0 14px 8px;
  color: var(--ss-primary);
  font-size: 22px;
  font-weight: 800;
}

.meta {
  margin: 0 14px 6px;
  color: var(--ss-text-secondary);
}

.pager {
  margin-top: 20px;
  padding: 14px 18px;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
}

.pager button {
  min-height: 32px;
  border: 1px solid var(--ss-border);
  border-radius: var(--ss-radius);
  background: var(--ss-bg-container);
  color: var(--ss-text-secondary);
  cursor: pointer;
  touch-action: manipulation;
}

.pager button:not(:disabled):hover {
  border-color: var(--ss-primary-hover);
  color: var(--ss-primary);
}

.error {
  color: var(--ss-danger);
}

@media (max-width: 720px) {
  .list-header-row {
    align-items: flex-start;
    flex-direction: column;
  }
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  .nav-btn {
    flex: 1;
    text-align: center;
  }
}

@media (prefers-reduced-motion: reduce) {
  .house-card,
  .nav-btn {
    transition: none;
  }
  .house-card:hover,
  .nav-btn:hover {
    transform: none;
  }
}
</style>
