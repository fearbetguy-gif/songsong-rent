<template>
  <div class="house-detail-page">
    <p v-if="loading">加载中...</p>
    <p v-else-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div v-else-if="detail?.house">
      <div class="head">
        <h2>{{ detail.house.title }}</h2>
        <p class="price">￥{{ detail.house.rentPrice }} / 月</p>
      </div>

      <div class="actions">
        <button class="btn btn-primary" type="button" @click="openAppointment">预约看房</button>
        <button class="btn btn-light" type="button" @click="toggleFavorite">
          {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏房源' }}
        </button>
      </div>

      <div v-if="detail.labels && detail.labels.length > 0" class="label-list">
        <span v-for="label in detail.labels" :key="label.id" class="label-chip">
          {{ label.name }}
        </span>
      </div>

      <div class="meta-grid">
        <p>城市：{{ detail.house.city || "-" }}</p>
        <p>区域：{{ detail.house.district || "-" }}</p>
        <p>户型：{{ detail.house.roomType || "-" }}</p>
        <p>面积：{{ detail.house.areaSize || "-" }} ㎡</p>
        <p>地址：{{ detail.house.address || "-" }}</p>
      </div>

      <p class="desc">描述：{{ detail.house.description || "暂无描述" }}</p>

      <h3>房源图片</h3>
      <div class="images">
        <img
          v-for="img in detail.images"
          :key="img.id"
          :src="img.imageUrl"
          alt="房源图片"
          loading="lazy"
          decoding="async"
        />
      </div>

      <div class="consult-panel">
        <h3>在线咨询房东</h3>
        <p class="consult-sub">登录后可与房东即时沟通，消息每 5 秒自动刷新。</p>
        <p v-if="consultError" class="error">{{ consultError }}</p>
        <div class="consult-box">
          <div class="consult-messages">
            <p v-if="consultLoading">咨询加载中...</p>
            <p v-else-if="consultMessages.length === 0" class="empty-msg">暂无咨询记录，发起第一条消息吧。</p>
            <div
              v-for="msg in consultMessages"
              :key="msg.id"
              class="msg"
              :class="{ mine: Number(msg.fromUserId) === Number(myUserId) }"
            >
              <div class="msg-meta">
                <span>{{ Number(msg.fromUserId) === Number(myUserId) ? "我" : (msg.fromNickname || msg.fromUsername || "房东") }}</span>
                <span>{{ formatConsultTime(msg.createTime) }}</span>
              </div>
              <div class="msg-content">{{ msg.content }}</div>
            </div>
          </div>
          <div class="consult-input">
            <textarea v-model="consultText" rows="3" placeholder="输入你想咨询的问题，例如：是否可月付、可否养宠物..." />
            <button class="btn btn-primary" :disabled="consultSubmitting" @click="sendConsult">
              {{ consultSubmitting ? "发送中..." : "发送咨询" }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showAppointmentModal" class="modal-mask" @click.self="closeAppointment">
      <div class="modal">
        <h3 class="modal-title">预约看房</h3>
        <p class="modal-sub">请填写预约信息，管理员确认后会更新状态。</p>

        <div class="form">
          <label class="field">
            <span>看房人姓名</span>
            <input v-model="appointment.viewerName" placeholder="请输入姓名" />
          </label>
          <label class="field">
            <span>手机号</span>
            <input v-model="appointment.phone" placeholder="请输入手机号" />
          </label>
          <label class="field">
            <span>预约时间</span>
            <input v-model="appointment.appointmentTime" type="datetime-local" />
          </label>
          <label class="field">
            <span>备注（可选）</span>
            <input v-model="appointment.remark" placeholder="例如：周末更方便" />
          </label>
        </div>

        <div class="modal-actions">
          <button class="btn btn-light" type="button" @click="closeAppointment">取消</button>
          <button class="btn btn-primary" type="button" :disabled="submitLoading" @click="submitAppointment">
            {{ submitLoading ? "提交中..." : "提交预约" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import {
  addAppointment,
  getHouseDetail,
  addFavorite,
  removeFavorite,
  checkFavorite,
  getHouseConsultMessages,
  sendConsultMessage,
} from "../api";

const route = useRoute();
const loading = ref(false);
const errorMessage = ref("");
const detail = ref(null);
const isFavorited = ref(false);
const myUserId = ref(localStorage.getItem("userId") || "");

const showAppointmentModal = ref(false);
const submitLoading = ref(false);
const appointment = ref({
  viewerName: "",
  phone: "",
  appointmentTime: "",
  remark: "",
});

const consultMessages = ref([]);
const consultLoading = ref(false);
const consultSubmitting = ref(false);
const consultText = ref("");
const consultError = ref("");
let consultTimer = null;

async function loadDetail(id) {
  loading.value = true;
  errorMessage.value = "";
  try {
    detail.value = await getHouseDetail(id);
    if (localStorage.getItem("token")) {
      isFavorited.value = await checkFavorite(id);
    }
  } catch (error) {
    errorMessage.value = error.message || "加载失败";
  } finally {
    loading.value = false;
  }
}

async function loadConsultMessages() {
  if (!detail.value?.house?.id) return;
  if (!localStorage.getItem("token")) return;
  if (consultLoading.value || document.hidden) return;
  consultLoading.value = true;
  consultError.value = "";
  try {
    consultMessages.value = await getHouseConsultMessages(detail.value.house.id);
  } catch (error) {
    consultError.value = error.message || "咨询消息加载失败";
  } finally {
    consultLoading.value = false;
  }
}

async function sendConsult() {
  if (!localStorage.getItem("token")) {
    alert("请先登录后再咨询");
    return;
  }
  const content = (consultText.value || "").trim();
  if (!content) {
    alert("请输入咨询内容");
    return;
  }
  consultSubmitting.value = true;
  try {
    await sendConsultMessage(Number(detail.value.house.id), content);
    consultText.value = "";
    await loadConsultMessages();
  } catch (error) {
    alert(error.message || "发送失败");
  } finally {
    consultSubmitting.value = false;
  }
}

function formatConsultTime(value) {
  if (!value) return "-";
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0] = value;
    return `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")} ${String(hh).padStart(2, "0")}:${String(mm).padStart(2, "0")}`;
  }
  return String(value).replace("T", " ").slice(0, 16);
}

function startConsultPolling() {
  stopConsultPolling();
  if (!localStorage.getItem("token")) return;
  consultTimer = setInterval(() => {
    loadConsultMessages();
  }, 10000);
}

function stopConsultPolling() {
  if (consultTimer) {
    clearInterval(consultTimer);
    consultTimer = null;
  }
}

async function toggleFavorite() {
  if (!localStorage.getItem("token")) {
    alert("请先登录后再收藏");
    return;
  }
  try {
    if (isFavorited.value) {
      await removeFavorite(detail.value.house.id);
      isFavorited.value = false;
    } else {
      await addFavorite(detail.value.house.id);
      isFavorited.value = true;
    }
  } catch (e) {
    alert(e.message || "操作失败");
  }
}

function openAppointment() {
  const userId = localStorage.getItem("userId");
  const token = localStorage.getItem("token");
  if (!userId || !token) {
    alert("请先登录后再预约");
    return;
  }
  appointment.value = {
    viewerName: localStorage.getItem("nickname") || localStorage.getItem("username") || "",
    phone: "",
    appointmentTime: "",
    remark: "",
  };
  showAppointmentModal.value = true;
}

function closeAppointment() {
  showAppointmentModal.value = false;
}

function handleVisibilityChange() {
  if (document.hidden) {
    stopConsultPolling();
    return;
  }
  if (detail.value?.house?.id && localStorage.getItem("token")) {
    loadConsultMessages();
    startConsultPolling();
  }
}

async function submitAppointment() {
  if (!detail.value?.house?.id) return;
  const viewerName = (appointment.value.viewerName || "").trim();
  const phone = (appointment.value.phone || "").trim();
  let appointmentTime = appointment.value.appointmentTime;
  if (!viewerName || !phone || !appointmentTime) {
    alert("请填写完整：姓名/手机号/预约时间");
    return;
  }
  // datetime-local 一般是 "YYYY-MM-DDTHH:mm"，后端 LocalDateTime 默认解析更稳妥的是带秒
  if (typeof appointmentTime === "string" && appointmentTime.length === 16) {
    appointmentTime = `${appointmentTime}:00`;
  }
  submitLoading.value = true;
  try {
    await addAppointment({
      houseId: Number(detail.value.house.id),
      viewerName,
      phone,
      appointmentTime,
      remark: (appointment.value.remark || "").trim(),
    });
    alert("预约提交成功（待管理员确认）");
    showAppointmentModal.value = false;
  } catch (error) {
    alert(error.message || "预约失败");
  } finally {
    submitLoading.value = false;
  }
}

watch(
  () => route.params.id,
  async (id) => {
    if (id) {
      await loadDetail(id);
      await loadConsultMessages();
      startConsultPolling();
    }
  }
);

onMounted(async () => {
  if (route.params.id) {
    await loadDetail(route.params.id);
    await loadConsultMessages();
    startConsultPolling();
  }
  window.addEventListener("beforeunload", stopConsultPolling);
  document.addEventListener("visibilitychange", handleVisibilityChange);
});

onUnmounted(() => {
  window.removeEventListener("beforeunload", stopConsultPolling);
  document.removeEventListener("visibilitychange", handleVisibilityChange);
  stopConsultPolling();
});
</script>

<style scoped>
.house-detail-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  background: var(--ss-bg-layout);
  color: var(--ss-text);
}

.house-detail-page > div[aria-busy],
.house-detail-page > div[v-else-if],
.house-detail-page > div {
  border-radius: var(--ss-radius-lg);
}

.actions {
  margin: 12px 0 6px;
  display: flex;
  gap: 10px;
}

.label-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 14px 0 4px;
}

.label-chip {
  display: inline-flex;
  align-items: center;
  border: 1px solid #91caff;
  background: #e6f4ff;
  color: var(--ss-primary-active);
  border-radius: 4px;
  padding: 5px 12px;
  font-size: 13px;
  font-weight: 700;
}

.btn {
  min-height: var(--ss-control-height);
  border: 1px solid var(--ss-border);
  background: var(--ss-bg-container);
  color: var(--ss-primary);
  border-radius: var(--ss-radius);
  padding: 7px 16px;
  cursor: pointer;
  font-weight: 800;
}

.btn-primary {
  background: var(--ss-primary);
  color: #fff;
  border-color: transparent;
}

.btn-light {
  background: #fff;
  color: var(--ss-primary);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.head {
  margin-bottom: 12px;
  padding: 24px;
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
  box-shadow: var(--ss-shadow-card);
}

.head h2 {
  margin: 0 0 10px;
  color: var(--ss-text);
}

.price {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: var(--ss-primary);
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(220px, 1fr));
  gap: 10px 16px;
  margin: 18px 0 20px;
  padding: 20px;
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
  box-shadow: var(--ss-shadow-card);
}

.meta-grid p {
  margin: 0;
  color: var(--ss-text-secondary);
}

.desc {
  margin: 0 0 20px;
  padding: 20px;
  color: var(--ss-text-secondary);
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
}

.images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.images img {
  width: 100%;
  height: 170px;
  object-fit: cover;
  border-radius: var(--ss-radius);
  border: 1px solid var(--ss-border-light);
}

.consult-panel {
  margin-top: 24px;
  background: var(--ss-bg-container);
  border-radius: var(--ss-radius-lg);
  border: 1px solid var(--ss-border-light);
  box-shadow: var(--ss-shadow-card);
  padding: 14px;
}

.consult-sub {
  margin-top: 4px;
  color: var(--ss-text-secondary);
  font-size: 14px;
}

.consult-box {
  margin-top: 10px;
  display: grid;
  gap: 10px;
}

.consult-messages {
  max-height: 320px;
  overflow: auto;
  display: grid;
  gap: 8px;
  padding-right: 4px;
}

.msg {
  border: 1px solid var(--ss-border-light);
  background: #fafafa;
  border-radius: var(--ss-radius);
  padding: 8px 10px;
}

.msg.mine {
  border-color: #91caff;
  background: #e6f4ff;
}

.msg-meta {
  display: flex;
  justify-content: space-between;
  color: var(--ss-text-tertiary);
  font-size: 12px;
  margin-bottom: 5px;
}

.msg-content {
  color: var(--ss-text);
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.consult-input {
  display: grid;
  gap: 8px;
}

.consult-input textarea {
  border: 1px solid var(--ss-border);
  border-radius: var(--ss-radius);
  padding: 10px;
  resize: vertical;
}

.empty-msg {
  color: var(--ss-text-tertiary);
  font-size: 14px;
}

.error {
  color: var(--ss-danger);
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
}

.modal {
  width: min(520px, 100%);
  background: var(--ss-bg-container);
  border-radius: var(--ss-radius-lg);
  padding: 18px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.2);
}

.modal-title {
  margin: 0 0 6px;
  font-size: 20px;
}

.modal-sub {
  margin: 0 0 12px;
  color: var(--ss-text-secondary);
  font-size: 14px;
}

.form {
  display: grid;
  gap: 10px;
}

.field {
  display: grid;
  gap: 6px;
  font-size: 14px;
  color: var(--ss-text-secondary);
}

.field input {
  padding: 10px 12px;
  border: 1px solid var(--ss-border);
  border-radius: var(--ss-radius);
  outline: none;
}

.modal-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
