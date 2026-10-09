<template>
  <div class="my-favorites-page">
    <div class="page-header">
      <h2>我的收藏</h2>
      <p>管理您心仪的房源</p>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="favorites.length === 0" class="empty">
      暂无收藏的房源，<router-link to="/houses">去逛逛</router-link>
    </div>

    <div v-else class="list-grid">
      <HouseCard v-for="house in favorites" :key="house.id" :house="house">
        <template #actions>
          <div class="actions">
            <router-link :to="`/houses/${house.id}`" class="btn btn-primary">查看详情</router-link>
            <button class="btn btn-light" :disabled="removingIds.has(house.id)" @click="handleRemove(house.id)">
              {{ removingIds.has(house.id) ? '取消中...' : '取消收藏' }}
            </button>
          </div>
        </template>
      </HouseCard>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getMyFavorites, removeFavorite } from '../api';
import HouseCard from '../components/HouseCard.vue';

const loading = ref(false);
const error = ref('');
const favorites = ref([]);
const removingIds = ref(new Set());

async function loadFavorites() {
  loading.value = true;
  error.value = '';
  try {
    const res = await getMyFavorites(1, 100); // 暂不处理分页，直接加载100条
    favorites.value = res.records || [];
  } catch (e) {
    error.value = e.message || '加载失败';
  } finally {
    loading.value = false;
  }
}

async function handleRemove(houseId) {
  if (!confirm('确定要取消收藏吗？')) return;
  if (removingIds.value.has(houseId)) return;
  removingIds.value = new Set(removingIds.value).add(houseId);
  try {
    await removeFavorite(houseId);
    favorites.value = favorites.value.filter(h => h.id !== houseId);
  } catch (e) {
    alert(e.message || '取消失败');
  } finally {
    const nextRemovingIds = new Set(removingIds.value);
    nextRemovingIds.delete(houseId);
    removingIds.value = nextRemovingIds;
  }
}

onMounted(() => {
  loadFavorites();
});
</script>

<style scoped>
.my-favorites-page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  color: var(--ss-text);
}

.page-header {
  margin-bottom: 24px;
  padding: 20px 24px;
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-xl);
  box-shadow: var(--ss-shadow-card);
}
.page-header h2 {
  margin: 0 0 8px;
  color: var(--ss-text);
}
.page-header p {
  margin: 0;
  color: var(--ss-text-secondary);
}

.list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

.btn {
  flex: 1;
  text-align: center;
  padding: 8px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  border: 1px solid transparent;
  touch-action: manipulation;
}

.btn:disabled {
  opacity: 0.58;
  cursor: not-allowed;
}

.btn-primary {
  background: var(--ss-primary);
  color: #fff;
}
.btn-primary:hover {
  background: var(--ss-primary-active);
}

.btn-light {
  background: var(--ss-bg-container);
  color: var(--ss-danger);
  border-color: #ffccc7;
}
.btn-light:hover {
  background: #fff2f0;
}

.empty {
  text-align: center;
  padding: 60px 0;
  color: var(--ss-text-secondary);
  font-size: 16px;
}
.empty a {
  color: var(--ss-primary);
  text-decoration: none;
}

@media (max-width: 640px) {
  .my-favorites-page {
    padding: 24px 16px;
  }
  .actions {
    flex-direction: column;
  }
}

</style>
