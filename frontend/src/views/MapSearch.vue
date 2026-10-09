<template>
  <div class="map-search-page">
    <header class="page-header">
      <h2>地图找房</h2>
      <p class="subtitle">在地图上直观探索房源，寻找您的理想家园</p>
    </header>
    

    <div class="map-container" v-show="!mapError">
      <div id="amap-container" class="amap-wrapper"></div>
      
      <div class="house-panel" v-if="selectedHouse">
        <button class="close-btn" @click="selectedHouse = null">&times;</button>
        <img v-if="selectedHouse.coverImage" :src="selectedHouse.coverImage" alt="cover" class="panel-cover" />
        <div class="panel-info">
          <h3>{{ selectedHouse.title }}</h3>
          <p class="price">￥{{ selectedHouse.rentPrice }} / 月</p>
          <p class="meta">{{ selectedHouse.district }} {{ selectedHouse.areaName || '' }}</p>
          <p class="meta">{{ selectedHouse.roomType || '-' }} · {{ selectedHouse.areaSize || '-' }} ㎡</p>
          <router-link :to="`/houses/${selectedHouse.id}`" class="btn btn-primary detail-btn">查看详情</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import AMapLoader from '@amap/amap-jsapi-loader';

const map = ref(null);
const houses = ref([]);
const selectedHouse = ref(null);
const mapError = ref(false);

onMounted(async () => {
  // 加载房源数据
  await loadHouses();
  // 初始化地图
  initMap();
});

onUnmounted(() => {
  if (map.value) {
    map.value.destroy();
  }
});

async function loadHouses() {
  try {
    const res = await fetch("http://localhost:8080/house/list?pageNum=1&pageSize=100");
    const result = await res.json();
    if (result.code === 200) {
      houses.value = result.data.records || [];
    }
  } catch (e) {
    console.error("加载房源失败", e);
  }
}

function initMap() {
  window._AMapSecurityConfig = {
    securityJsCode: "6ae25edb7c898e55df51a62ecdd2f6db", 
  };
  
  AMapLoader.load({
    key: "1c1493758b72b2a5e810cbec967ea482", 
    version: "2.0",
    plugins: ['AMap.Marker'],
  }).then((AMap) => {
    map.value = new AMap.Map("amap-container", {
      zoom: 11,
      center: [116.397428, 39.90923], // 默认北京天安门
    });

    const markers = [];

    // 遍历房源，添加标记
    houses.value.forEach(house => {
      // 模拟经纬度：如果数据库里没有经纬度，就在中心点附近随机散布以便演示
      let lng = house.longitude;
      let lat = house.latitude;
      
      if (!lng || !lat) {
        lng = 116.397428 + (Math.random() - 0.5) * 0.2;
        lat = 39.90923 + (Math.random() - 0.5) * 0.2;
      }

      const marker = new AMap.Marker({
        position: [lng, lat],
        title: house.title,
      });

      marker.on('click', () => {
        selectedHouse.value = house;
        map.value.setCenter([lng, lat]);
        map.value.setZoom(14);
      });

      markers.push(marker);
      map.value.add(marker);
    });

    // 如果有房源，自动调整视野包含所有 marker
    if (markers.length > 0) {
      map.value.setFitView(markers);
    }
  }).catch(e => {
    console.error("加载地图失败", e);
    if (String(e).includes("KEY") || String(e).includes("SCODE")) {
      mapError.value = true;
    }
  });
}
</script>

<style scoped>
.map-search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 28px;
  margin: 0 0 8px;
  color: #0f172a;
}
.subtitle {
  color: #64748b;
  margin: 0;
}

.map-error-alert {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  border-radius: 12px;
  padding: 24px;
  color: #991b1b;
  margin-bottom: 24px;
  line-height: 1.6;
}

.map-error-alert h3 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #991b1b;
}

.map-error-alert ol {
  margin: 12px 0 0;
  padding-left: 20px;
}

.map-error-alert a {
  color: #1d4ed8;
  font-weight: bold;
}

.map-container {
  position: relative;
  width: 100%;
  height: 600px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
  border: 1px solid #e2e8f0;
}

.amap-wrapper {
  width: 100%;
  height: 100%;
}

.house-panel {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 320px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 100;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { transform: translateX(20px); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

.close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border: none;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2;
}
.close-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

.panel-cover {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.panel-info {
  padding: 20px;
}

.panel-info h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #0f172a;
}

.panel-info .price {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 12px;
}

.panel-info .meta {
  color: #64748b;
  font-size: 14px;
  margin: 0 0 8px;
}

.detail-btn {
  width: 100%;
  margin-top: 16px;
}

.btn {
  border: 1px solid transparent;
  padding: 10px 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  display: block;
}
.btn-primary {
  background: #2563eb;
  color: #fff;
}
.btn-primary:hover {
  background: #1d4ed8;
}
</style>
