<template>
  <section id="platform-stats" class="about-section reveal-section">
    <div class="about-content">
      <p class="eyebrow">平台数据概览</p>
      <h2>来自数据库的实时统计</h2>
      <div class="stats-grid">
        <div class="stat-card">
          <p class="stat-label">在租房源</p>
          <p class="stat-value">{{ stats.houseCount ?? 0 }}</p>
        </div>
        <div class="stat-card">
          <p class="stat-label">覆盖城市</p>
          <p class="stat-value">{{ stats.cityCount ?? 0 }}</p>
        </div>
        <div class="stat-card">
          <p class="stat-label">覆盖区域</p>
          <p class="stat-value">{{ stats.districtCount ?? 0 }}</p>
        </div>
        <div class="stat-card">
          <p class="stat-label">平均租金</p>
          <p class="stat-value">￥{{ formatMoney(stats.avgRentPrice) }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted } from "vue";
const props = defineProps({
  stats: {
    type: Object,
    default: () => ({}),
  },
});
const stats = computed(() => props.stats || {});

let revealObserver;

onMounted(() => {
  const el = document.querySelector(".about-section.reveal-section");
  if (!el) return;
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("is-visible");
          revealObserver.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.18 }
  );
  revealObserver.observe(el);
});

onBeforeUnmount(() => {
  if (revealObserver) {
    revealObserver.disconnect();
  }
});

function formatMoney(value) {
  if (value == null || value === "") return "0.00";
  const num = Number(value);
  if (Number.isNaN(num)) return "0.00";
  return num.toFixed(2);
}
</script>

<style scoped>
.about-section {
  padding: 80px 0;
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.about-section.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.eyebrow {
  margin: 0 0 24px;
  font-size: 13px;
  letter-spacing: 1.8px;
  color: #2563eb;
  font-weight: 700;
}

.about-content h2 {
  margin: 0 0 28px;
  font-size: 48px;
  line-height: 1.12;
  color: #0f172a;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(140px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.stat-label {
  margin: 0 0 8px;
  font-size: 13px;
  color: #64748b;
}

.stat-value {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(140px, 1fr));
  }

  .about-content h2 {
    font-size: 36px;
  }
}
</style>
