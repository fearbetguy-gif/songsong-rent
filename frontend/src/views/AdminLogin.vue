<template>
  <div class="auth-page">
    <header class="auth-header">
      <router-link to="/" class="logo" aria-label="松松租房平台首页">
        <img src="/logo.png" alt="松松租房平台 Logo" class="logo-img" />
        <span class="logo-text">松松租房平台</span>
      </router-link>
      <router-link to="/login" class="text-link">普通用户登录</router-link>
    </header>

    <div class="auth-card">
      <div class="auth-card-inner">
        <p class="eyebrow">后台入口</p>
        <h1 class="title">管理员登录</h1>
        <p class="sub">仅管理员账号可进入后台管理。</p>

        <form class="auth-form" @submit.prevent="onSubmit">
          <label class="auth-field">
            <span class="label">用户名</span>
            <input v-model="username" type="text" required placeholder="请输入管理员用户名" />
          </label>
          <label class="auth-field">
            <span class="label">密码</span>
            <input v-model="password" type="password" required placeholder="请输入密码" />
          </label>
          <button class="auth-submit" type="submit">进入后台</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import "../styles/auth.css";
import { onMounted, ref } from "vue";

const username = ref("");
const password = ref("");

onMounted(() => {
  const adminUserId = localStorage.getItem("adminUserId") || localStorage.getItem("userId");
  const isAdmin = localStorage.getItem("isAdmin");
  if (isAdmin === "1" && adminUserId) {
    window.location.href = "/admin.html";
  }
});

async function onSubmit() {
  const res = await fetch("http://localhost:8080/user/admin-login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: username.value,
      password: password.value,
    }),
  });
  const result = await res.json();
  if (result.code === 200 && result.data?.id != null) {
    localStorage.setItem("userId", String(result.data.id));
    localStorage.setItem("adminUserId", String(result.data.id));
    localStorage.setItem("username", result.data.username || username.value);
    localStorage.setItem("nickname", result.data.nickname || result.data.username || username.value);
    localStorage.setItem("isAdmin", String(result.data.isAdmin || 0));
    if (result.data.token) {
      localStorage.setItem("token", result.data.token);
    }
    window.location.href = "/admin.html";
  } else {
    alert(result.message || "登录失败");
  }
}
</script>
