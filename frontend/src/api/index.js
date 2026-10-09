import { useRoute } from "vue-router";

const BASE_URL = "http://localhost:8080";

function getAuthHeaders() {
  const token = localStorage.getItem("token");
  const headers = { "Content-Type": "application/json" };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  } else {
    // 兼容之前逻辑，有些接口可能需要这个，最好全部改用 Authorization
    const userId = localStorage.getItem("userId");
    if (userId) {
      headers["X-User-Id"] = userId;
    }
  }
  return headers;
}

export async function login(username, password) {
  const response = await fetch(`${BASE_URL}/user/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const result = await response.json();
  if (result.code !== 200) {
    throw new Error(result.message || "登录失败");
  }
  
  // 保存 token 和 用户信息
  if (result.data.token) {
    localStorage.setItem("token", result.data.token);
  }
  return result.data;
}

export async function register(username, nickname, password) {
  const response = await fetch(`${BASE_URL}/user/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, nickname, password }),
  });
  const result = await response.json();
  if (result.code !== 200) {
    throw new Error(result.message || "注册失败");
  }
  
  // 保存 token 和 用户信息
  if (result.data.token) {
    localStorage.setItem("token", result.data.token);
  }
  return result.data;
}

async function request(path, options = {}) {
  const headers = getAuthHeaders();
  options.headers = { ...headers, ...options.headers };
  const response = await fetch(`${BASE_URL}${path}`, options);
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  const result = await response.json();
  if (result.code !== 200) {
    throw new Error(result.message || "请求失败");
  }
  return result.data;
}

export function getHouseList(pageNum = 1, pageSize = 10) {
  return request(`/house/list?pageNum=${pageNum}&pageSize=${pageSize}`);
}

export function getCityList() {
  return request("/city/list");
}

export function getDistrictList(cityId) {
  const params = new URLSearchParams();
  if (cityId !== undefined && cityId !== null && cityId !== "") {
    params.set("cityId", String(cityId));
  }
  const query = params.toString();
  return request(`/district/list${query ? `?${query}` : ""}`);
}

export function getLabelList() {
  return request("/label/list");
}

export function getHouseDetail(id) {
  return request(`/house/${id}`);
}

export function addAppointment(payload) {
  const userId = localStorage.getItem("userId");
  if (!userId) {
    return Promise.reject(new Error("请先登录"));
  }
  return request(`/appointment/add`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-User-Id": userId,
    },
    body: JSON.stringify(payload),
  });
}

export function getMyAppointments({ status, pageNum = 1, pageSize = 10 } = {}) {
  const userId = localStorage.getItem("userId");
  if (!userId) {
    return Promise.reject(new Error("请先登录"));
  }
  const params = new URLSearchParams();
  if (status !== undefined && status !== null && status !== "") {
    params.set("status", String(status));
  }
  params.set("pageNum", String(pageNum));
  params.set("pageSize", String(pageSize));
  return request(`/appointment/my-list?${params.toString()}`, {
    headers: { "X-User-Id": userId },
  });
}

export function cancelAppointment(id) {
  const userId = localStorage.getItem("userId");
  if (!userId) {
    return Promise.reject(new Error("请先登录"));
  }
  return request(`/appointment/cancel/${id}`, {
    method: "POST",
    headers: { "X-User-Id": userId },
  });
}

export function addFavorite(houseId) {
  return request(`/favorite/add/${houseId}`, { method: "POST" });
}

export function removeFavorite(houseId) {
  return request(`/favorite/remove/${houseId}`, { method: "POST" });
}

export function checkFavorite(houseId) {
  return request(`/favorite/check/${houseId}`).catch(() => false);
}

export function getMyFavorites(pageNum = 1, pageSize = 10) {
  return request(`/favorite/my-list?pageNum=${pageNum}&pageSize=${pageSize}`);
}

export function getLandlordHouses(pageNum = 1, pageSize = 10) {
  return request(`/landlord/house/list?pageNum=${pageNum}&pageSize=${pageSize}`);
}

export function addLandlordHouse(payload) {
  return request("/landlord/house/add", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateLandlordHouse(payload) {
  return request("/landlord/house/update", {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function removeLandlordHouse(houseId) {
  return request(`/landlord/house/${houseId}`, {
    method: "DELETE",
  });
}

export function getHousePriceHistory(houseId, pageNum = 1, pageSize = 10) {
  return request(`/landlord/house/${houseId}/price-history?pageNum=${pageNum}&pageSize=${pageSize}`);
}

export function getLandlordAppointments({ status, pageNum = 1, pageSize = 10 } = {}) {
  const params = new URLSearchParams();
  if (status !== undefined && status !== null && status !== "") {
    params.set("status", String(status));
  }
  params.set("pageNum", String(pageNum));
  params.set("pageSize", String(pageSize));
  return request(`/landlord/appointment/list?${params.toString()}`);
}

export function confirmLandlordAppointment(appointmentId) {
  return request(`/landlord/appointment/confirm/${appointmentId}`, {
    method: "POST",
  });
}

export function rejectLandlordAppointment(appointmentId, reason) {
  return request(`/landlord/appointment/reject/${appointmentId}`, {
    method: "POST",
    body: JSON.stringify({ reason }),
  });
}

export function finishLandlordAppointment(appointmentId) {
  return request(`/landlord/appointment/finish/${appointmentId}`, {
    method: "POST",
  });
}

export function sendConsultMessage(houseId, content) {
  return request("/consult/send", {
    method: "POST",
    body: JSON.stringify({ houseId, content }),
  });
}

export function getHouseConsultMessages(houseId) {
  return request(`/consult/house/${houseId}/messages`);
}

export function getLandlordConsultSessions() {
  return request("/consult/landlord/sessions");
}

export function getLandlordConsultMessages(houseId, tenantId) {
  const params = new URLSearchParams();
  params.set("houseId", String(houseId));
  params.set("tenantId", String(tenantId));
  return request(`/consult/landlord/messages?${params.toString()}`);
}

export function landlordReplyConsult(houseId, tenantId, content) {
  const params = new URLSearchParams();
  params.set("houseId", String(houseId));
  params.set("tenantId", String(tenantId));
  return request(`/consult/landlord/reply?${params.toString()}`, {
    method: "POST",
    body: JSON.stringify({ reason: content }),
  });
}

export function createLease(payload) {
  return request("/landlord/lease/add", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getLeaseList({ status, pageNum = 1, pageSize = 10 } = {}) {
  const params = new URLSearchParams();
  if (status !== undefined && status !== null && status !== "") {
    params.set("status", String(status));
  }
  params.set("pageNum", String(pageNum));
  params.set("pageSize", String(pageSize));
  return request(`/landlord/lease/list?${params.toString()}`);
}

export function createRentBill(payload) {
  return request("/landlord/rent-bill/add", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getRentBillList({ status, pageNum = 1, pageSize = 10 } = {}) {
  const params = new URLSearchParams();
  if (status !== undefined && status !== null && status !== "") {
    params.set("status", String(status));
  }
  params.set("pageNum", String(pageNum));
  params.set("pageSize", String(pageSize));
  return request(`/landlord/rent-bill/list?${params.toString()}`);
}

export function receiveRentBill(billId, payload) {
  return request(`/landlord/rent-bill/receive/${billId}`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
