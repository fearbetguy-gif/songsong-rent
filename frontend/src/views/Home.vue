<template>
  <div class="home-page">
    <header class="top-header">
      <router-link to="/" class="logo" aria-label="松松租房平台首页">
        <img src="/logo.png" alt="松松租房平台 Logo" class="logo-img" />
        <span class="logo-text">松松租房平台</span>
      </router-link>
      <nav class="menu">
        <router-link to="/">首页</router-link>
        <a href="#platform-stats">平台数据</a>
        <router-link to="/map-search">地图找房</router-link>
        <router-link to="/houses">精选房源</router-link>
        <router-link v-if="isLogin" to="/landlord">房东工作台</router-link>
        <router-link v-if="isLogin" to="/my-appointments">我的预约</router-link>
        <router-link v-if="isLogin" to="/my-favorites">我的收藏</router-link>
        <a v-if="isLogin" href="#" @click.prevent="logout">退出登录</a>
      </nav>
      <div v-if="isLogin" class="user-box">你好，{{ nickname || username || "用户" }}</div>
      <router-link v-else class="btn btn-secondary" to="/login">立即登录</router-link>
    </header>

    <section class="hero reveal-section">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1>在城市里<br />找到理想好房</h1>
        <p>
          覆盖整租、合租、近地铁与品牌公寓，
          为你提供真实房源、透明价格和省心租住体验。
        </p>
        <router-link class="btn btn-primary" to="/houses">立即找房</router-link>
        <button class="btn btn-secondary admin-entry-btn" type="button" @click="enterAdmin">进入后台管理</button>
      </div>
    </section>

    <section class="recommended reveal-section">
      <h2>推荐房源</h2>
      <div class="card-grid">
        <HouseCard
          v-for="house in houses"
          :key="house.id"
          class="reveal-card"
          :house="house"
          :to="`/houses/${house.id}`"
        />
      </div>
    </section>

    <section class="monitor reveal-section">
      <div class="monitor-head">
        <h2>实时数据面板</h2>
        <p>每 30 秒自动刷新一次，隐藏页面时自动暂停</p>
      </div>
      <div class="monitor-grid">
        <article class="monitor-card">
          <p class="monitor-label">平台在线房源</p>
          <p class="monitor-value">{{ stats.houseCount }}</p>
          <p class="monitor-meta">城市 {{ stats.cityCount }} · 区域 {{ stats.districtCount }}</p>
        </article>
        <article class="monitor-card">
          <p class="monitor-label">平台平均租金</p>
          <p class="monitor-value">￥{{ toMoney(stats.avgRentPrice) }}</p>
          <p class="monitor-meta">来自平台房源统计接口</p>
        </article>
        <article class="monitor-card">
          <p class="monitor-label">推荐房源刷新快照</p>
          <p class="monitor-value">{{ houses.length }} 套</p>
          <p class="monitor-meta">当前首页展示条数</p>
        </article>
        <article class="monitor-card" v-if="isLogin">
          <p class="monitor-label">我的预约（全部状态）</p>
          <p class="monitor-value">{{ userRealtime.appointmentCount }}</p>
          <p class="monitor-meta">待确认 {{ userRealtime.pendingAppointmentCount }}</p>
        </article>
        <article class="monitor-card" v-if="isLogin">
          <p class="monitor-label">我的收藏房源</p>
          <p class="monitor-value">{{ userRealtime.favoriteCount }}</p>
          <p class="monitor-meta">来自收藏分页总数</p>
        </article>
        <article class="monitor-card" v-if="isLogin">
          <p class="monitor-label">我的咨询会话</p>
          <p class="monitor-value">{{ userRealtime.consultSessionCount }}</p>
          <p class="monitor-meta">按已咨询房源维度聚合</p>
        </article>
      </div>
      <p class="monitor-time">最近刷新：{{ monitorUpdatedAt }}</p>
    </section>

    <AboutSection :stats="stats" />
    <ContactSection />
    <SiteFooter />
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import AboutSection from "../components/home/AboutSection.vue";
import ContactSection from "../components/home/ContactSection.vue";
import HouseCard from "../components/HouseCard.vue";
import SiteFooter from "../components/home/SiteFooter.vue";

const router = useRouter();
const houses = ref([]);
const stats = ref({
  houseCount: 0,
  cityCount: 0,
  districtCount: 0,
  avgRentPrice: 0,
});
const isLogin = ref(false);
const username = ref("");
const nickname = ref("");
const monitorUpdatedAt = ref("-");
const userRealtime = ref({
  appointmentCount: 0,
  pendingAppointmentCount: 0,
  favoriteCount: 0,
  consultSessionCount: 0,
});

let revealObserver;
let monitorTimer = null;
let monitorLoading = false;

function initRevealObserver() {
  if (revealObserver) {
    revealObserver.disconnect();
  }
  const elements = document.querySelectorAll(".reveal-section, .reveal-card");
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("is-visible");
          revealObserver.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.16 }
  );

  elements.forEach((el) => revealObserver.observe(el));
}

async function loadHouses() {
  try {
    const response = await fetch("http://localhost:8080/house/list?pageNum=1&pageSize=6");
    const result = await response.json();
    houses.value = result?.data?.records || [];
  } catch (error) {
    houses.value = [];
  }
  await nextTick();
  initRevealObserver();
}

async function loadStats() {
  try {
    const response = await fetch("http://localhost:8080/house/stats");
    const result = await response.json();
    stats.value = result?.data || stats.value;
  } catch (error) {
    stats.value = {
      houseCount: 0,
      cityCount: 0,
      districtCount: 0,
      avgRentPrice: 0,
    };
  }
}

async function loadUserRealtime() {
  if (!localStorage.getItem("token") || !localStorage.getItem("userId")) {
    userRealtime.value = {
      appointmentCount: 0,
      pendingAppointmentCount: 0,
      favoriteCount: 0,
      consultSessionCount: 0,
    };
    return;
  }
  const token = localStorage.getItem("token");
  const authHeaders = { Authorization: `Bearer ${token}` };
  let appointmentCount = 0;
  let pendingAppointmentCount = 0;
  let favoriteCount = 0;
  let consultSessionCount = 0;

  try {
    const [allAppointmentsRes, pendingAppointmentsRes, favoritesRes] = await Promise.all([
      fetch("http://localhost:8080/appointment/my-list?pageNum=1&pageSize=1", { headers: authHeaders }),
      fetch("http://localhost:8080/appointment/my-list?status=0&pageNum=1&pageSize=1", { headers: authHeaders }),
      fetch("http://localhost:8080/favorite/my-list?pageNum=1&pageSize=1", { headers: authHeaders }),
    ]);
    const [allAppointmentsJson, pendingAppointmentsJson, favoritesJson] = await Promise.all([
      allAppointmentsRes.json(),
      pendingAppointmentsRes.json(),
      favoritesRes.json(),
    ]);
    if (allAppointmentsJson.code === 200) appointmentCount = allAppointmentsJson?.data?.total || 0;
    if (pendingAppointmentsJson.code === 200) pendingAppointmentCount = pendingAppointmentsJson?.data?.total || 0;
    if (favoritesJson.code === 200) favoriteCount = favoritesJson?.data?.total || 0;
  } catch (error) {}

  try {
    const consultSessions = new Set();
    for (const house of houses.value) {
      if (!house?.id) continue;
      const consultRes = await fetch(`http://localhost:8080/consult/house/${house.id}/messages`, {
        headers: authHeaders,
      });
      const consultJson = await consultRes.json();
      if (consultJson.code === 200) {
        const records = consultJson.data || [];
        if (records.length > 0) {
          consultSessions.add(String(house.id));
        }
      }
    }
    consultSessionCount = consultSessions.size;
  } catch (error) {}

  userRealtime.value = {
    appointmentCount,
    pendingAppointmentCount,
    favoriteCount,
    consultSessionCount,
  };
}

function toMoney(value) {
  if (value == null || value === "") return "0.00";
  const num = Number(value);
  if (Number.isNaN(num)) return "0.00";
  return num.toFixed(2);
}

async function refreshRealtimePanel() {
  if (monitorLoading || document.hidden) return;
  monitorLoading = true;
  await loadStats();
  await loadUserRealtime();
  monitorUpdatedAt.value = new Date().toLocaleString("zh-CN", { hour12: false });
  monitorLoading = false;
}

function startRealtimeMonitor() {
  stopRealtimeMonitor();
  monitorTimer = setInterval(() => {
    refreshRealtimePanel();
  }, 30000);
}

function stopRealtimeMonitor() {
  if (monitorTimer) {
    clearInterval(monitorTimer);
    monitorTimer = null;
  }
}

onMounted(() => {
  username.value = localStorage.getItem("username") || "";
  nickname.value = localStorage.getItem("nickname") || "";
  isLogin.value = !!localStorage.getItem("userId") && !!localStorage.getItem("token");
  Promise.all([loadStats(), loadHouses()]).then(() => {
    refreshRealtimePanel();
  });
  startRealtimeMonitor();
  document.addEventListener("visibilitychange", handleVisibilityChange);
});

function handleVisibilityChange() {
  if (document.hidden) {
    stopRealtimeMonitor();
    return;
  }
  refreshRealtimePanel();
  startRealtimeMonitor();
}

function logout() {
  localStorage.removeItem("userId");
  localStorage.removeItem("username");
  localStorage.removeItem("nickname");
  localStorage.removeItem("adminUserId");
  localStorage.removeItem("isAdmin");
  localStorage.removeItem("token");
  isLogin.value = false;
  username.value = "";
  nickname.value = "";
}

function enterAdmin() {
  const adminUserId = localStorage.getItem("adminUserId") || localStorage.getItem("userId");
  const isAdmin = localStorage.getItem("isAdmin");
  if (isAdmin === "1" && adminUserId) {
    window.location.href = "/admin.html";
    return;
  }
  router.push("/admin-login");
}

onBeforeUnmount(() => {
  if (revealObserver) {
    revealObserver.disconnect();
  }
  stopRealtimeMonitor();
  document.removeEventListener("visibilitychange", handleVisibilityChange);
});
</script>

<style scoped>
.home-page {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 24px 64px;
  background: var(--ss-bg-layout);
  color: var(--ss-text);
}

.top-header {
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 0 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--ss-border-light);
}

.hero,
.recommended,
.monitor {
  padding: 32px 0;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 700;
  color: var(--ss-text);
}

.logo-img {
  height: 44px;
  width: auto;
  display: block;
  object-fit: contain;
}

.logo-text {
  line-height: 1;
}

.menu {
  display: flex;
  gap: 22px;
}

.menu a {
  color: var(--ss-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.menu a.router-link-active {
  color: var(--ss-primary);
}

.menu a:hover {
  color: var(--ss-primary);
}

.btn {
  min-height: var(--ss-control-height);
  border: 1px solid transparent;
  padding: 7px 16px;
  border-radius: var(--ss-radius);
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease, transform 0.3s ease, box-shadow 0.3s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  box-sizing: border-box;
}

.btn-primary {
  background: var(--ss-primary);
  color: #ffffff;
  box-shadow: 0 2px 0 rgba(5, 145, 255, 0.1);
}

.btn-primary:hover {
  background: var(--ss-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
}

.btn-secondary {
  background: var(--ss-bg-container);
  border-color: var(--ss-border);
  color: var(--ss-primary);
}

.btn-secondary:hover {
  border-color: var(--ss-primary-hover);
  transform: translateY(-1px);
}

.admin-entry-btn {
  margin-left: 12px;
}

.user-box {
  color: var(--ss-primary);
  font-size: 14px;
  font-weight: 600;
}

.reveal-section {
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.reveal-section.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.hero {
  position: relative;
  min-height: 360px;
  border-radius: var(--ss-radius-lg);
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(0, 0, 0, 0.62) 0%, rgba(0, 0, 0, 0.28) 52%, rgba(0, 0, 0, 0.08) 100%),
    url("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=1800&q=80") center / cover;
  box-shadow: var(--ss-shadow-card);
}

.hero::before {
  content: "";
  position: absolute;
  background:
    linear-gradient(90deg, rgba(22, 119, 255, 0.18) 0%, rgba(22, 119, 255, 0) 55%);
  inset: 0;
  pointer-events: none;
}

.hero-overlay {
  display: none;
}

.hero-content {
  position: absolute;
  top: 50%;
  left: 48px;
  transform: translateY(-50%);
  max-width: 480px;
  color: #ffffff;
}

.hero-content h1 {
  margin: 0 0 18px;
  font-size: 44px;
  line-height: 1.12;
  font-weight: 700;
}

.hero-content p {
  margin: 0 0 24px;
  font-size: 16px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.92);
}

.recommended h2 {
  margin: 0 0 20px;
  font-size: 28px;
  line-height: 1.2;
  color: var(--ss-text);
}

.monitor-head {
  margin-bottom: 20px;
}

.monitor-head h2 {
  margin: 0 0 8px;
  font-size: 28px;
  line-height: 1.2;
  color: var(--ss-text);
}

.monitor-head p {
  margin: 0;
  color: var(--ss-text-secondary);
  font-size: 15px;
}

.monitor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.monitor-card {
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
  box-shadow: var(--ss-shadow-card);
  padding: 18px;
}

.monitor-label {
  margin: 0 0 10px;
  font-size: 14px;
  color: var(--ss-text-secondary);
}

.monitor-value {
  margin: 0 0 8px;
  font-size: 32px;
  line-height: 1;
  font-weight: 800;
  color: var(--ss-primary);
}

.monitor-meta {
  margin: 0;
  color: var(--ss-text-secondary);
  font-size: 14px;
}

.monitor-time {
  margin: 14px 0 0;
  color: var(--ss-text-tertiary);
  font-size: 13px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
}

.reveal-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  opacity: 0;
  transform: translateY(18px);
}

.reveal-card:hover {
  transform: translateY(-3px);
}

.reveal-card.is-visible {
  opacity: 1;
  transform: translateY(0);
}

@media (max-width: 768px) {
  .top-header {
    height: auto;
    gap: 12px;
    flex-wrap: wrap;
    padding: 20px 8px;
  }

  .menu {
    gap: 16px;
  }

  .hero {
    min-height: 400px;
  }

  .hero-content {
    left: 24px;
    right: 24px;
  }

  .hero-content h1 {
    font-size: 40px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .btn,
  .reveal-card,
  .reveal-section {
    transition: none;
  }

  .btn:hover,
  .reveal-card:hover {
    transform: none;
  }
}
</style>
