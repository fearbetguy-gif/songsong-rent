<template>
  <div class="appointment-page">
    <header class="page-header">
      <h2>我的预约</h2>
      <div class="filters">
        <select v-model="selectedStatus" @change="reloadAppointments">
          <option value="">全部状态</option>
          <option value="0">待确认</option>
          <option value="1">已确认</option>
          <option value="2">已拒绝</option>
          <option value="3">已完成</option>
          <option value="4">已取消</option>
        </select>
        <button class="btn" @click="reloadAppointments">刷新</button>
      </div>
    </header>

    <p v-if="loading">加载中...</p>
    <p v-else-if="errorMessage" class="error">{{ errorMessage }}</p>

    <div v-else class="list">
      <div v-if="appointmentPage.records.length === 0" class="empty">暂无预约记录</div>

      <div v-for="appointment in appointmentPage.records" :key="appointment.id" class="card">
        <div class="card-main">
          <div class="title">{{ appointment.houseTitle || `房源#${appointment.houseId}` }}</div>
          <div class="meta">
            <span class="tag" :class="statusClass(appointment.status)">{{ statusText(appointment.status) }}</span>
            <span>预约时间：{{ formatAppointmentTime(appointment.appointmentTime) }}</span>
          </div>
          <div class="meta">看房人：{{ appointment.viewerName }} · 手机：{{ appointment.phone }}</div>
          <div v-if="appointment.rejectReason" class="meta reject">拒绝原因：{{ appointment.rejectReason }}</div>
          <div v-if="appointment.remark" class="meta">备注：{{ appointment.remark }}</div>
        </div>
        <div class="actions">
          <button
            v-if="appointment.status === 0 || appointment.status === 1"
            class="btn btn-danger"
            @click="cancelMyAppointment(appointment.id)"
          >
            取消预约
          </button>
        </div>
      </div>
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
import { cancelAppointment, getMyAppointments } from "../api";

const APPOINTMENT_STATUS_TEXT = {
  0: "待确认",
  1: "已确认",
  2: "已拒绝",
  3: "已完成",
  4: "已取消",
};

const loading = ref(false);
const errorMessage = ref("");
const selectedStatus = ref("");
const currentPage = ref(1);
const appointmentsPerPage = ref(10);
const appointmentPage = ref({
  total: 0,
  pageNum: 1,
  pageSize: 10,
  records: [],
});

const isLastPage = computed(() => {
  return currentPage.value * appointmentsPerPage.value >= appointmentPage.value.total;
});

function statusText(statusCode) {
  return APPOINTMENT_STATUS_TEXT[statusCode] || "-";
}

function statusClass(statusCode) {
  if (statusCode === 0) return "tag-pending";
  if (statusCode === 1) return "tag-ok";
  if (statusCode === 2) return "tag-reject";
  if (statusCode === 3) return "tag-done";
  return "tag-cancel";
}

function formatAppointmentTime(timeValue) {
  if (!timeValue) return "-";
  if (Array.isArray(timeValue)) {
    const [year, month, day, hour = 0, minute = 0] = timeValue;
    return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")} ${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
  }
  return String(timeValue).replace("T", " ").slice(0, 16);
}

async function fetchMyAppointments() {
  loading.value = true;
  errorMessage.value = "";
  try {
    appointmentPage.value = await getMyAppointments({
      status: selectedStatus.value === "" ? null : Number(selectedStatus.value),
      pageNum: currentPage.value,
      pageSize: appointmentsPerPage.value,
    });
  } catch (error) {
    errorMessage.value = error.message || "加载失败";
  } finally {
    loading.value = false;
  }
}

function changePage(nextPage) {
  currentPage.value = nextPage;
  fetchMyAppointments();
}

function reloadAppointments() {
  currentPage.value = 1;
  fetchMyAppointments();
}

async function cancelMyAppointment(appointmentId) {
  if (!confirm("确定要取消该预约吗？")) return;
  try {
    await cancelAppointment(appointmentId);
    reloadAppointments();
  } catch (error) {
    alert(error.message || "取消失败");
  }
}

onMounted(() => {
  fetchMyAppointments();
});
</script>

<style scoped>
.appointment-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  background: #f7f9fc;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #0f172a;
}

.filters {
  display: flex;
  gap: 10px;
  align-items: center;
}

select {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
}

.btn {
  border: 1px solid rgba(37, 99, 235, 0.2);
  background: #fff;
  color: #1d4ed8;
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  font-weight: 700;
}

.btn-danger {
  border-color: rgba(220, 38, 38, 0.22);
  color: #dc2626;
}

.list {
  display: grid;
  gap: 12px;
}

.card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  padding: 14px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 8px;
}

.meta {
  color: #475569;
  font-size: 14px;
  margin-bottom: 6px;
}

.reject {
  color: #b91c1c;
}

.actions {
  display: flex;
  align-items: center;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  margin-right: 10px;
  border: 1px solid transparent;
}

.tag-pending {
  background: #fff7ed;
  border-color: rgba(251, 146, 60, 0.35);
  color: #c2410c;
}

.tag-ok {
  background: #ecfdf5;
  border-color: rgba(16, 185, 129, 0.25);
  color: #047857;
}

.tag-reject {
  background: #fef2f2;
  border-color: rgba(239, 68, 68, 0.25);
  color: #b91c1c;
}

.tag-done {
  background: #eff6ff;
  border-color: rgba(59, 130, 246, 0.25);
  color: #1d4ed8;
}

.tag-cancel {
  background: #f1f5f9;
  border-color: rgba(100, 116, 139, 0.3);
  color: #334155;
}

.pager {
  margin-top: 18px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.error {
  color: #dc2626;
}

.empty {
  color: #64748b;
  padding: 24px 10px;
}
</style>
