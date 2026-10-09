<template>
  <div class="landlord-page">
    <header class="page-header">
      <div>
        <h1>房东工作台</h1>
        <p>管理你发布的房源和租客预约</p>
      </div>
      <div class="header-actions">
        <router-link class="btn btn-light" to="/">返回首页</router-link>
        <button class="btn" @click="logout">退出登录</button>
      </div>
    </header>

    <section class="panel">
      <div class="panel-head">
        <h2>房源管理</h2>
        <button class="btn" @click="openCreate">发布房源</button>
      </div>

      <p v-if="houseLoading">房源加载中...</p>
      <p v-else-if="houseError" class="error">{{ houseError }}</p>
      <div v-else>
        <div v-if="housePage.records.length === 0" class="empty">你还没有发布房源</div>
        <table v-else>
          <thead>
            <tr>
              <th>ID</th>
              <th>标题</th>
              <th>标签</th>
              <th>租金</th>
              <th>区域</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="house in housePage.records" :key="house.id">
              <td>{{ house.id }}</td>
              <td>{{ house.title }}</td>
              <td>
                <span v-if="!house.labels || house.labels.length === 0">-</span>
                <template v-else>
                  <span v-for="label in house.labels" :key="label.id" class="tag">{{ label.name }}</span>
                </template>
              </td>
              <td>￥{{ house.rentPrice }}</td>
              <td>{{ house.district || "-" }}</td>
              <td>{{ houseStatusText(house.status) }}</td>
              <td class="row-actions">
                <button class="mini" @click="openEdit(house)">编辑</button>
                <button class="mini" @click="openPriceHistory(house)">价格历史</button>
                <button class="mini danger" @click="removeHouse(house)">下架</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="pager">
          <button :disabled="housePageNum <= 1" @click="changeHousePage(housePageNum - 1)">上一页</button>
          <span>第 {{ housePageNum }} 页</span>
          <button :disabled="isHouseLastPage" @click="changeHousePage(housePageNum + 1)">下一页</button>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>租约管理</h2>
        <button class="btn" @click="openLeaseCreate">新建租约</button>
      </div>
      <p v-if="leaseError" class="error">{{ leaseError }}</p>
      <table v-if="leasePage.records.length > 0">
        <thead>
          <tr>
            <th>ID</th>
            <th>房源</th>
            <th>租客</th>
            <th>月租</th>
            <th>租期</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="lease in leasePage.records" :key="lease.id">
            <td>{{ lease.id }}</td>
            <td>{{ lease.houseTitle || `房源#${lease.houseId}` }}</td>
            <td>{{ lease.tenantNickname || lease.tenantUsername || `租客#${lease.tenantId}` }}</td>
            <td>￥{{ lease.monthlyRent }}</td>
            <td>{{ lease.startDate }} ~ {{ lease.endDate }}</td>
            <td>{{ leaseStatusText(lease.status) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无租约记录</div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>租金管理</h2>
        <button class="btn" @click="openBillCreate">新增账单</button>
      </div>
      <p v-if="billError" class="error">{{ billError }}</p>
      <table v-if="billPage.records.length > 0">
        <thead>
          <tr>
            <th>ID</th>
            <th>租约</th>
            <th>房源</th>
            <th>租客</th>
            <th>账期</th>
            <th>金额</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="bill in billPage.records" :key="bill.id">
            <td>{{ bill.id }}</td>
            <td>#{{ bill.leaseId }}</td>
            <td>{{ bill.houseTitle || `房源#${bill.houseId}` }}</td>
            <td>{{ bill.tenantNickname || bill.tenantUsername || `租客#${bill.tenantId}` }}</td>
            <td>{{ bill.billingMonth }}</td>
            <td>￥{{ bill.amount }}</td>
            <td>{{ billStatusText(bill.status) }}</td>
            <td>
              <button v-if="bill.status !== 1" class="mini" @click="markReceived(bill.id)">标记收款</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无账单记录</div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>在线咨询</h2>
        <button class="btn btn-light" @click="reloadConsult">刷新</button>
      </div>

      <p v-if="consultError" class="error">{{ consultError }}</p>
      <div class="consult-layout">
        <aside class="session-list">
          <p v-if="consultLoading">会话加载中...</p>
          <div
            v-for="session in consultSessions"
            :key="`${session.houseId}-${session.tenantId}`"
            class="session-item"
            :class="{ active: isCurrentSession(session) }"
            @click="selectSession(session)"
          >
            <div class="session-title">{{ session.houseTitle || `房源#${session.houseId}` }}</div>
            <div class="session-sub">{{ session.tenantNickname || session.tenantUsername || `租客#${session.tenantId}` }}</div>
            <div class="session-last">{{ session.lastMessage || "-" }}</div>
          </div>
          <p v-if="!consultLoading && consultSessions.length === 0" class="empty">暂无咨询会话</p>
        </aside>

        <div class="chat-panel">
          <p v-if="!currentSession">请选择一个会话查看聊天记录</p>
          <template v-else>
            <div class="chat-head">
              <strong>{{ currentSession.houseTitle || `房源#${currentSession.houseId}` }}</strong>
              <span>租客：{{ currentSession.tenantNickname || currentSession.tenantUsername || currentSession.tenantId }}</span>
            </div>
            <div class="chat-messages">
              <p v-if="messageLoading">消息加载中...</p>
              <p v-else-if="consultMessages.length === 0" class="empty">暂无消息</p>
              <div
                v-for="msg in consultMessages"
                :key="msg.id"
                class="chat-msg"
                :class="{ mine: Number(msg.fromUserId) === Number(myUserId) }"
              >
                <div class="chat-meta">
                  <span>{{ Number(msg.fromUserId) === Number(myUserId) ? "我" : (msg.fromNickname || msg.fromUsername || "租客") }}</span>
                  <span>{{ formatAppointmentTime(msg.createTime) }}</span>
                </div>
                <div class="chat-text">{{ msg.content }}</div>
              </div>
            </div>
            <div class="chat-input">
              <textarea v-model="replyText" rows="3" placeholder="回复租客咨询..." />
              <button class="btn" :disabled="replySubmitting" @click="sendReply">
                {{ replySubmitting ? "发送中..." : "发送回复" }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>预约管理</h2>
        <div class="filters">
          <select v-model="appointmentStatus" @change="reloadAppointments">
            <option value="">全部状态</option>
            <option value="0">待确认</option>
            <option value="1">已确认</option>
            <option value="2">已拒绝</option>
            <option value="3">已完成</option>
            <option value="4">已取消</option>
          </select>
          <button class="btn btn-light" @click="reloadAppointments">刷新</button>
        </div>
      </div>

      <p v-if="appointmentLoading">预约加载中...</p>
      <p v-else-if="appointmentError" class="error">{{ appointmentError }}</p>
      <div v-else>
        <div v-if="appointmentPage.records.length === 0" class="empty">暂无预约记录</div>
        <table v-else>
          <thead>
            <tr>
              <th>ID</th>
              <th>房源</th>
              <th>看房人</th>
              <th>预约时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="appointment in appointmentPage.records" :key="appointment.id">
              <td>{{ appointment.id }}</td>
              <td>{{ appointment.houseTitle || `房源#${appointment.houseId}` }}</td>
              <td>{{ appointment.viewerName }} / {{ appointment.phone }}</td>
              <td>{{ formatAppointmentTime(appointment.appointmentTime) }}</td>
              <td>{{ appointmentStatusText(appointment.status) }}</td>
              <td class="row-actions">
                <button
                  v-if="appointment.status === 0"
                  class="mini"
                  @click="confirmAppointment(appointment.id)"
                >确认</button>
                <button
                  v-if="appointment.status === 0"
                  class="mini danger"
                  @click="rejectAppointment(appointment.id)"
                >拒绝</button>
                <button
                  v-if="appointment.status === 1"
                  class="mini"
                  @click="finishAppointment(appointment.id)"
                >完成</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="pager">
          <button :disabled="appointmentPageNum <= 1" @click="changeAppointmentPage(appointmentPageNum - 1)">上一页</button>
          <span>第 {{ appointmentPageNum }} 页</span>
          <button :disabled="isAppointmentLastPage" @click="changeAppointmentPage(appointmentPageNum + 1)">下一页</button>
        </div>
      </div>
    </section>

    <div v-if="showHouseForm" class="modal-mask" @click.self="closeForm">
      <div class="modal">
        <h3>{{ formMode === "create" ? "发布房源" : "编辑房源" }}</h3>
        <label>
          标题
          <input v-model="houseForm.title" type="text" placeholder="如：地铁口精装一居" />
        </label>
        <label>
          月租金
          <input v-model.number="houseForm.rentPrice" type="number" min="1" placeholder="如：3500" />
        </label>
        <label>
          城市
          <select
            v-model="houseForm.cityId"
            :disabled="formMode === 'edit'"
            @change="onCityChange"
          >
            <option :value="null">请选择城市</option>
            <option v-for="city in cityOptions" :key="city.id" :value="city.id">
              {{ city.name }}
            </option>
          </select>
        </label>
        <label>
          区域
          <select v-model="houseForm.district" :disabled="formMode === 'edit'">
            <option :value="null">请选择区域</option>
            <option v-for="district in districtOptions" :key="district.id" :value="district.id">
              {{ district.name }}
            </option>
          </select>
          <small v-if="formMode === 'edit'" class="field-tip">编辑模式不支持修改区域</small>
        </label>
        <label>
          房源标签
          <div class="checkbox-group">
            <span v-if="labelOptions.length === 0" class="field-tip">暂无可选标签，请先在标签管理中添加</span>
            <label v-for="label in labelOptions" :key="label.id" class="checkbox-item">
              <input v-model="houseForm.labelIds" type="checkbox" :value="label.id" />
              {{ label.name }}
            </label>
          </div>
        </label>
        <label>
          描述
          <textarea v-model="houseForm.description" rows="4" placeholder="补充交通、装修、押付方式等"></textarea>
        </label>
        <label>
          纬度
          <input v-model.number="houseForm.latitude" type="number" step="0.000001" placeholder="可选" />
        </label>
        <label>
          经度
          <input v-model.number="houseForm.longitude" type="number" step="0.000001" placeholder="可选" />
        </label>

        <p v-if="formError" class="error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-light" @click="closeForm">取消</button>
          <button class="btn" @click="submitHouseForm">保存</button>
        </div>
      </div>
    </div>

    <div v-if="showPriceHistory" class="modal-mask" @click.self="closePriceHistory">
      <div class="modal history-modal">
        <div class="modal-title-row">
          <h3>房源价格历史</h3>
          <button class="mini" @click="closePriceHistory">关闭</button>
        </div>
        <div class="history-house">
          <strong>{{ priceHistoryHouse?.title || "-" }}</strong>
          <span>当前租金：￥{{ priceHistoryHouse?.rentPrice || "-" }}</span>
        </div>
        <p v-if="priceHistoryLoading">价格记录加载中...</p>
        <p v-else-if="priceHistoryError" class="error">{{ priceHistoryError }}</p>
        <div v-else>
          <div v-if="priceHistoryPage.records.length === 0" class="empty">暂无价格变更记录</div>
          <table v-else>
            <thead>
              <tr>
                <th>原租金</th>
                <th>新租金</th>
                <th>操作人</th>
                <th>生效时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in priceHistoryPage.records" :key="record.id">
                <td>￥{{ record.oldPrice }}</td>
                <td>￥{{ record.newPrice }}</td>
                <td>#{{ record.operatorId || "-" }}</td>
                <td>{{ formatAppointmentTime(record.effectiveTime || record.createTime) }}</td>
              </tr>
            </tbody>
          </table>
          <div class="pager" v-if="priceHistoryPage.total > priceHistoryPageSize">
            <button :disabled="priceHistoryPageNum <= 1" @click="changePriceHistoryPage(priceHistoryPageNum - 1)">上一页</button>
            <span>第 {{ priceHistoryPageNum }} 页</span>
            <button :disabled="isPriceHistoryLastPage" @click="changePriceHistoryPage(priceHistoryPageNum + 1)">下一页</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showLeaseForm" class="modal-mask" @click.self="closeLeaseForm">
      <div class="modal">
        <h3>新建租约</h3>
        <label>
          房源
          <select v-model="leaseForm.houseId">
            <option :value="null">请选择房源</option>
            <option v-for="house in leaseHouseOptions" :key="house.id" :value="house.id">
              #{{ house.id }} {{ house.title }}
            </option>
          </select>
        </label>
        <label>
          租客
          <select v-model="leaseForm.tenantId">
            <option :value="null">请选择租客</option>
            <option v-for="tenant in leaseTenantOptions" :key="tenant.id" :value="tenant.id">
              #{{ tenant.id }} {{ tenant.name }}
            </option>
          </select>
        </label>
        <label>月租金<input v-model.number="leaseForm.monthlyRent" type="number" min="1" /></label>
        <label>开始日期<input v-model="leaseForm.startDate" type="date" /></label>
        <label>结束日期<input v-model="leaseForm.endDate" type="date" /></label>
        <label>备注<textarea v-model="leaseForm.remark" rows="3" /></label>
        <p v-if="leaseFormError" class="error">{{ leaseFormError }}</p>
        <div class="modal-actions">
          <button class="btn btn-light" @click="closeLeaseForm">取消</button>
          <button class="btn" @click="submitLeaseForm">保存</button>
        </div>
      </div>
    </div>

    <div v-if="showBillForm" class="modal-mask" @click.self="closeBillForm">
      <div class="modal">
        <h3>新增账单</h3>
        <label>
          租约
          <select v-model="billForm.leaseId">
            <option :value="null">请选择租约</option>
            <option v-for="lease in billLeaseOptions" :key="lease.id" :value="lease.id">
              #{{ lease.id }} {{ lease.houseTitle || `房源#${lease.houseId}` }} / {{ lease.tenantNickname || lease.tenantUsername || `租客#${lease.tenantId}` }}
            </option>
          </select>
        </label>
        <label>账期（YYYY-MM）<input v-model="billForm.billingMonth" type="text" placeholder="2026-04" /></label>
        <label>金额<input v-model.number="billForm.amount" type="number" min="1" /></label>
        <label>应付日期<input v-model="billForm.dueDate" type="date" /></label>
        <label>备注<textarea v-model="billForm.remark" rows="3" /></label>
        <p v-if="billFormError" class="error">{{ billFormError }}</p>
        <div class="modal-actions">
          <button class="btn btn-light" @click="closeBillForm">取消</button>
          <button class="btn" @click="submitBillForm">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import {
  addLandlordHouse,
  confirmLandlordAppointment,
  finishLandlordAppointment,
  getLandlordAppointments,
  getLandlordHouses,
  rejectLandlordAppointment,
  removeLandlordHouse,
  updateLandlordHouse,
  getLandlordConsultSessions,
  getLandlordConsultMessages,
  landlordReplyConsult,
  createLease,
  getLeaseList,
  createRentBill,
  getCityList,
  getDistrictList,
  getHousePriceHistory,
  getLabelList,
  getRentBillList,
  receiveRentBill,
} from "../api";

const router = useRouter();

const houseLoading = ref(false);
const houseError = ref("");
const housePageNum = ref(1);
const housePageSize = ref(10);
const housePage = ref({ total: 0, pageNum: 1, pageSize: 10, records: [] });

const appointmentLoading = ref(false);
const appointmentError = ref("");
const appointmentStatus = ref("");
const appointmentPageNum = ref(1);
const appointmentPageSize = ref(10);
const appointmentPage = ref({ total: 0, pageNum: 1, pageSize: 10, records: [] });

const showHouseForm = ref(false);
const formMode = ref("create");
const editingHouseId = ref(null);
const formError = ref("");
const houseForm = ref({
  title: "",
  rentPrice: null,
  cityId: null,
  district: null,
  description: "",
  latitude: null,
  longitude: null,
  labelIds: [],
});
const cityOptions = ref([]);
const districtOptions = ref([]);
const labelOptions = ref([]);
const myUserId = ref(localStorage.getItem("userId") || "");
const consultLoading = ref(false);
const consultError = ref("");
const consultSessions = ref([]);
const currentSession = ref(null);
const consultMessages = ref([]);
const messageLoading = ref(false);
const replySubmitting = ref(false);
const replyText = ref("");
let consultTimer = null;
const leasePage = ref({ total: 0, pageNum: 1, pageSize: 10, records: [] });
const leaseError = ref("");
const billPage = ref({ total: 0, pageNum: 1, pageSize: 10, records: [] });
const billError = ref("");
const showPriceHistory = ref(false);
const priceHistoryHouse = ref(null);
const priceHistoryLoading = ref(false);
const priceHistoryError = ref("");
const priceHistoryPageNum = ref(1);
const priceHistoryPageSize = ref(10);
const priceHistoryPage = ref({ total: 0, pageNum: 1, pageSize: 10, records: [] });

const showLeaseForm = ref(false);
const showBillForm = ref(false);
const leaseFormError = ref("");
const billFormError = ref("");
const leaseForm = ref({
  houseId: null,
  tenantId: null,
  monthlyRent: null,
  startDate: "",
  endDate: "",
  remark: "",
});
const billForm = ref({
  leaseId: null,
  billingMonth: "",
  amount: null,
  dueDate: "",
  remark: "",
});

const isHouseLastPage = computed(() => housePageNum.value * housePageSize.value >= housePage.value.total);
const isAppointmentLastPage = computed(() => {
  return appointmentPageNum.value * appointmentPageSize.value >= appointmentPage.value.total;
});
const isPriceHistoryLastPage = computed(() => {
  return priceHistoryPageNum.value * priceHistoryPageSize.value >= priceHistoryPage.value.total;
});
const leaseHouseOptions = computed(() => {
  const records = housePage.value?.records || [];
  return records.filter((item) => item && Number(item.status) === 1);
});
const leaseTenantOptions = computed(() => {
  const map = new Map();
  const fromAppointments = appointmentPage.value?.records || [];
  for (const item of fromAppointments) {
    if (!item || !item.userId) continue;
    map.set(Number(item.userId), {
      id: Number(item.userId),
      name: item.nickname || item.username || `租客#${item.userId}`,
    });
  }
  for (const session of consultSessions.value || []) {
    if (!session || !session.tenantId) continue;
    map.set(Number(session.tenantId), {
      id: Number(session.tenantId),
      name: session.tenantNickname || session.tenantUsername || `租客#${session.tenantId}`,
    });
  }
  return Array.from(map.values());
});
const billLeaseOptions = computed(() => leasePage.value?.records || []);

function guardLogin() {
  if (!localStorage.getItem("token") || !localStorage.getItem("userId")) {
    router.push("/login");
    return false;
  }
  return true;
}

function houseStatusText(status) {
  if (status === 1) return "已上架";
  if (status === 0) return "已下架";
  return "-";
}

function appointmentStatusText(status) {
  const map = {
    0: "待确认",
    1: "已确认",
    2: "已拒绝",
    3: "已完成",
    4: "已取消",
  };
  return map[status] || "-";
}

function leaseStatusText(status) {
  const map = { 0: "待生效", 1: "履约中", 2: "已结束", 3: "已解约" };
  return map[status] || "-";
}

function billStatusText(status) {
  const map = { 0: "待支付", 1: "已支付", 2: "已逾期" };
  return map[status] || "-";
}

function formatAppointmentTime(value) {
  if (!value) return "-";
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0] = value;
    return `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")} ${String(hh).padStart(2, "0")}:${String(mm).padStart(2, "0")}`;
  }
  return String(value).replace("T", " ").slice(0, 16);
}

async function loadHouses() {
  houseLoading.value = true;
  houseError.value = "";
  try {
    housePage.value = await getLandlordHouses(housePageNum.value, housePageSize.value);
  } catch (error) {
    houseError.value = error.message || "房源加载失败";
  } finally {
    houseLoading.value = false;
  }
}

async function loadCityOptions() {
  try {
    cityOptions.value = await getCityList();
  } catch (error) {
    cityOptions.value = [];
    formError.value = error.message || "城市列表加载失败";
  }
}

async function loadLabelOptions() {
  try {
    labelOptions.value = await getLabelList();
  } catch (error) {
    labelOptions.value = [];
    formError.value = error.message || "标签列表加载失败";
  }
}

async function onCityChange() {
  houseForm.value.district = null;
  if (!houseForm.value.cityId) {
    districtOptions.value = [];
    return;
  }
  try {
    districtOptions.value = await getDistrictList(Number(houseForm.value.cityId));
  } catch (error) {
    districtOptions.value = [];
    formError.value = error.message || "区域列表加载失败";
  }
}

async function loadAppointments() {
  appointmentLoading.value = true;
  appointmentError.value = "";
  try {
    appointmentPage.value = await getLandlordAppointments({
      status: appointmentStatus.value === "" ? null : Number(appointmentStatus.value),
      pageNum: appointmentPageNum.value,
      pageSize: appointmentPageSize.value,
    });
  } catch (error) {
    appointmentError.value = error.message || "预约加载失败";
  } finally {
    appointmentLoading.value = false;
  }
}

async function loadConsultSessions() {
  consultLoading.value = true;
  consultError.value = "";
  try {
    consultSessions.value = await getLandlordConsultSessions();
    if (!currentSession.value && consultSessions.value.length > 0) {
      currentSession.value = consultSessions.value[0];
      await loadCurrentMessages();
    }
  } catch (error) {
    consultError.value = error.message || "咨询会话加载失败";
  } finally {
    consultLoading.value = false;
  }
}

async function loadLeases() {
  leaseError.value = "";
  try {
    leasePage.value = await getLeaseList({ pageNum: 1, pageSize: 20 });
  } catch (error) {
    leaseError.value = error.message || "租约加载失败";
  }
}

async function loadBills() {
  billError.value = "";
  try {
    billPage.value = await getRentBillList({ pageNum: 1, pageSize: 20 });
  } catch (error) {
    billError.value = error.message || "账单加载失败";
  }
}

async function loadPriceHistory() {
  if (!priceHistoryHouse.value?.id) return;
  priceHistoryLoading.value = true;
  priceHistoryError.value = "";
  try {
    priceHistoryPage.value = await getHousePriceHistory(
      priceHistoryHouse.value.id,
      priceHistoryPageNum.value,
      priceHistoryPageSize.value
    );
  } catch (error) {
    priceHistoryError.value = error.message || "价格历史加载失败";
  } finally {
    priceHistoryLoading.value = false;
  }
}

async function loadCurrentMessages() {
  if (!currentSession.value) return;
  messageLoading.value = true;
  consultError.value = "";
  try {
    consultMessages.value = await getLandlordConsultMessages(
      currentSession.value.houseId,
      currentSession.value.tenantId
    );
  } catch (error) {
    consultError.value = error.message || "咨询消息加载失败";
  } finally {
    messageLoading.value = false;
  }
}

function isCurrentSession(session) {
  if (!currentSession.value) return false;
  return (
    Number(currentSession.value.houseId) === Number(session.houseId) &&
    Number(currentSession.value.tenantId) === Number(session.tenantId)
  );
}

async function selectSession(session) {
  currentSession.value = session;
  await loadCurrentMessages();
}

async function sendReply() {
  if (!currentSession.value) return;
  const content = (replyText.value || "").trim();
  if (!content) {
    alert("请输入回复内容");
    return;
  }
  replySubmitting.value = true;
  try {
    await landlordReplyConsult(currentSession.value.houseId, currentSession.value.tenantId, content);
    replyText.value = "";
    await Promise.all([loadCurrentMessages(), loadConsultSessions()]);
  } catch (error) {
    alert(error.message || "发送失败");
  } finally {
    replySubmitting.value = false;
  }
}

async function reloadConsult() {
  await Promise.all([loadConsultSessions(), loadCurrentMessages()]);
}

function openLeaseCreate() {
  leaseFormError.value = "";
  leaseForm.value = {
    houseId: null,
    tenantId: null,
    monthlyRent: null,
    startDate: "",
    endDate: "",
    remark: "",
  };
  showLeaseForm.value = true;
}

function closeLeaseForm() {
  showLeaseForm.value = false;
}

async function submitLeaseForm() {
  leaseFormError.value = "";
  if (!leaseForm.value.houseId || !leaseForm.value.tenantId || !leaseForm.value.monthlyRent || !leaseForm.value.startDate || !leaseForm.value.endDate) {
    leaseFormError.value = "请完整填写租约信息";
    return;
  }
  try {
    await createLease({
      ...leaseForm.value,
      houseId: Number(leaseForm.value.houseId),
      tenantId: Number(leaseForm.value.tenantId),
    });
    closeLeaseForm();
    await loadLeases();
  } catch (error) {
    leaseFormError.value = error.message || "租约创建失败";
  }
}

function openBillCreate() {
  billFormError.value = "";
  billForm.value = {
    leaseId: null,
    billingMonth: "",
    amount: null,
    dueDate: "",
    remark: "",
  };
  showBillForm.value = true;
}

function closeBillForm() {
  showBillForm.value = false;
}

async function submitBillForm() {
  billFormError.value = "";
  if (!billForm.value.leaseId || !billForm.value.billingMonth || !billForm.value.amount || !billForm.value.dueDate) {
    billFormError.value = "请完整填写账单信息";
    return;
  }
  try {
    await createRentBill({
      ...billForm.value,
      leaseId: Number(billForm.value.leaseId),
    });
    closeBillForm();
    await loadBills();
  } catch (error) {
    billFormError.value = error.message || "账单创建失败";
  }
}

async function markReceived(billId) {
  const paymentMethod = prompt("请输入收款方式（如：微信/支付宝/银行转账）：");
  if (!paymentMethod || !paymentMethod.trim()) return;
  try {
    await receiveRentBill(billId, { paymentMethod: paymentMethod.trim(), remark: "" });
    await loadBills();
  } catch (error) {
    alert(error.message || "收款状态更新失败");
  }
}

function changeHousePage(nextPage) {
  housePageNum.value = nextPage;
  loadHouses();
}

function changeAppointmentPage(nextPage) {
  appointmentPageNum.value = nextPage;
  loadAppointments();
}

function changePriceHistoryPage(nextPage) {
  priceHistoryPageNum.value = nextPage;
  loadPriceHistory();
}

function reloadAppointments() {
  appointmentPageNum.value = 1;
  loadAppointments();
}

function resetForm() {
  formError.value = "";
  houseForm.value = {
    title: "",
    rentPrice: null,
    cityId: null,
    district: null,
    description: "",
    latitude: null,
    longitude: null,
    labelIds: [],
  };
  districtOptions.value = [];
}

function openCreate() {
  formMode.value = "create";
  editingHouseId.value = null;
  resetForm();
  showHouseForm.value = true;
}

function openEdit(house) {
  formMode.value = "edit";
  editingHouseId.value = house.id;
  formError.value = "";
  houseForm.value = {
    title: house.title || "",
    rentPrice: house.rentPrice != null ? Number(house.rentPrice) : null,
    cityId: null,
    district: null,
    description: house.description || "",
    latitude: null,
    longitude: null,
    labelIds: (house.labels || []).map((label) => Number(label.id)),
  };
  districtOptions.value = [];
  showHouseForm.value = true;
}

function closeForm() {
  showHouseForm.value = false;
}

async function openPriceHistory(house) {
  priceHistoryHouse.value = house;
  priceHistoryPageNum.value = 1;
  priceHistoryPage.value = { total: 0, pageNum: 1, pageSize: priceHistoryPageSize.value, records: [] };
  showPriceHistory.value = true;
  await loadPriceHistory();
}

function closePriceHistory() {
  showPriceHistory.value = false;
}

async function submitHouseForm() {
  formError.value = "";
  if (!houseForm.value.title || !houseForm.value.rentPrice) {
    formError.value = "标题和租金不能为空";
    return;
  }

  try {
    if (formMode.value === "create") {
      if (!houseForm.value.district) {
        formError.value = "请选择房源所在区域";
        return;
      }
      await addLandlordHouse({
        title: houseForm.value.title,
        rentPrice: houseForm.value.rentPrice,
        district: Number(houseForm.value.district),
        description: houseForm.value.description,
        latitude: houseForm.value.latitude,
        longitude: houseForm.value.longitude,
        labelIds: houseForm.value.labelIds.map((id) => Number(id)),
      });
    } else {
      await updateLandlordHouse({
        id: editingHouseId.value,
        title: houseForm.value.title,
        rentPrice: houseForm.value.rentPrice,
        description: houseForm.value.description,
        latitude: houseForm.value.latitude,
        longitude: houseForm.value.longitude,
        labelIds: houseForm.value.labelIds.map((id) => Number(id)),
      });
    }
    closeForm();
    await loadHouses();
  } catch (error) {
    formError.value = error.message || "保存失败";
  }
}

async function removeHouse(house) {
  if (!confirm(`确定下架房源「${house.title}」吗？`)) return;
  try {
    await removeLandlordHouse(house.id);
    await loadHouses();
  } catch (error) {
    alert(error.message || "下架失败");
  }
}

async function confirmAppointment(appointmentId) {
  try {
    await confirmLandlordAppointment(appointmentId);
    await loadAppointments();
  } catch (error) {
    alert(error.message || "确认失败");
  }
}

async function rejectAppointment(appointmentId) {
  const reason = prompt("请输入拒绝原因：");
  if (!reason || !reason.trim()) return;
  try {
    await rejectLandlordAppointment(appointmentId, reason.trim());
    await loadAppointments();
  } catch (error) {
    alert(error.message || "拒绝失败");
  }
}

async function finishAppointment(appointmentId) {
  try {
    await finishLandlordAppointment(appointmentId);
    await loadAppointments();
  } catch (error) {
    alert(error.message || "操作失败");
  }
}

function logout() {
  stopConsultPolling();
  localStorage.removeItem("userId");
  localStorage.removeItem("adminUserId");
  localStorage.removeItem("username");
  localStorage.removeItem("nickname");
  localStorage.removeItem("isAdmin");
  localStorage.removeItem("token");
  router.push("/login");
}

function startConsultPolling() {
  stopConsultPolling();
  consultTimer = setInterval(async () => {
    await loadConsultSessions();
    await loadCurrentMessages();
  }, 5000);
}

function stopConsultPolling() {
  if (consultTimer) {
    clearInterval(consultTimer);
    consultTimer = null;
  }
}

onMounted(async () => {
  if (!guardLogin()) return;
  await Promise.all([loadHouses(), loadAppointments(), loadConsultSessions(), loadLeases(), loadBills(), loadCityOptions(), loadLabelOptions()]);
  startConsultPolling();
});

watch(
  () => leaseForm.value.houseId,
  (houseId) => {
    if (!houseId) return;
    const selectedHouse = (housePage.value?.records || []).find((item) => Number(item.id) === Number(houseId));
    if (selectedHouse?.rentPrice && !leaseForm.value.monthlyRent) {
      leaseForm.value.monthlyRent = Number(selectedHouse.rentPrice);
    }
  }
);

onUnmounted(() => {
  stopConsultPolling();
});
</script>

<style scoped>
.landlord-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 22px 80px;
  background: var(--ss-bg-layout);
  color: var(--ss-text);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.page-header p {
  margin: 6px 0 0;
  color: var(--ss-text-secondary);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.panel {
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
  padding: 16px;
  box-shadow: var(--ss-shadow-card);
  margin-bottom: 18px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.panel-head h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn {
  min-height: var(--ss-control-height);
  border: 1px solid var(--ss-primary);
  background: var(--ss-primary);
  color: #fff;
  border-radius: var(--ss-radius);
  padding: 7px 14px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-light {
  background: var(--ss-bg-container);
  border-color: var(--ss-border);
  color: var(--ss-primary);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  border-bottom: 1px solid var(--ss-border-light);
  padding: 10px 8px;
  text-align: left;
  font-size: 14px;
  color: var(--ss-text-secondary);
}

th {
  color: var(--ss-text);
  background: #fafafa;
}

.row-actions {
  display: flex;
  gap: 8px;
}

.mini {
  border: 1px solid var(--ss-border);
  border-radius: var(--ss-radius);
  padding: 4px 8px;
  background: var(--ss-bg-container);
  color: var(--ss-primary);
  cursor: pointer;
}

.mini.danger {
  border-color: #ffccc7;
  color: var(--ss-danger);
}

.pager {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.empty {
  color: var(--ss-text-tertiary);
  padding: 16px 8px;
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
  z-index: 1000;
}

.modal {
  width: min(560px, 92vw);
  background: var(--ss-bg-container);
  border-radius: var(--ss-radius-lg);
  padding: 18px;
  box-shadow: var(--ss-shadow);
  display: grid;
  gap: 10px;
}

.modal h3 {
  margin: 0 0 4px;
}

.history-modal {
  width: min(760px, 94vw);
}

.modal-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.history-house {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius);
  background: #fafafa;
  color: var(--ss-text-secondary);
  font-size: 14px;
}

.history-house strong {
  color: var(--ss-text);
}

.modal label {
  display: grid;
  gap: 6px;
  font-size: 14px;
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid var(--ss-border);
  border-radius: var(--ss-radius);
  padding: 8px 10px;
  font-size: 14px;
}

.tag {
  display: inline-block;
  margin: 2px 4px 2px 0;
  padding: 2px 8px;
  border: 1px solid #91caff;
  border-radius: 4px;
  background: #e6f4ff;
  color: var(--ss-primary-active);
  font-size: 12px;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.checkbox-item {
  width: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: 1px solid var(--ss-border);
  border-radius: var(--ss-radius);
  background: #fafafa;
}

.checkbox-item input {
  width: auto;
}

.field-tip {
  color: var(--ss-text-tertiary);
  font-size: 12px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 6px;
}

.consult-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 12px;
}

.session-list {
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
  padding: 8px;
  max-height: 420px;
  overflow: auto;
}

.session-item {
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius);
  padding: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  background: var(--ss-bg-container);
}

.session-item.active {
  border-color: #91caff;
  background: #e6f4ff;
}

.session-title {
  font-weight: 700;
  color: var(--ss-text);
}

.session-sub {
  font-size: 12px;
  color: var(--ss-text-secondary);
  margin: 4px 0;
}

.session-last {
  font-size: 12px;
  color: var(--ss-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-panel {
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-lg);
  padding: 10px;
  display: grid;
  gap: 10px;
}

.chat-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 14px;
  color: var(--ss-text-secondary);
}

.chat-messages {
  max-height: 300px;
  overflow: auto;
  display: grid;
  gap: 8px;
}

.chat-msg {
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius);
  padding: 8px;
  background: #fafafa;
}

.chat-msg.mine {
  border-color: #91caff;
  background: #e6f4ff;
}

.chat-meta {
  display: flex;
  justify-content: space-between;
  color: var(--ss-text-tertiary);
  font-size: 12px;
  margin-bottom: 4px;
}

.chat-text {
  white-space: pre-wrap;
  font-size: 14px;
  color: var(--ss-text);
}

.chat-input {
  display: grid;
  gap: 8px;
}
</style>
