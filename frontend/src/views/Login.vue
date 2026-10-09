<template>
  <div class="auth-page">
    <header class="auth-header">
      <router-link to="/" class="logo" aria-label="松松租房平台首页">
        <img src="/logo.png" alt="松松租房平台 Logo" class="logo-img" />
        <span class="logo-text">松松租房平台</span>
      </router-link>
      <router-link to="/register" class="text-link">还没有账号？去注册</router-link>
    </header>

    <div class="auth-card">
      <div class="auth-card-inner">
        <p class="eyebrow">欢迎回来</p>
        <h1 class="title">登录账号</h1>
        <p class="sub">使用用户名和密码登录。</p>

        <form class="auth-form" @submit.prevent="onSubmit">
          <label class="auth-field">
            <span class="label">用户名</span>
            <input v-model="username" type="text" required placeholder="请输入用户名" />
          </label>
          <label class="auth-field">
            <span class="label">密码</span>
            <input v-model="password" type="password" required placeholder="请输入密码" />
          </label>
          <button class="auth-submit" type="submit">登录</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import "../styles/auth.css";
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const username = ref("");
const password = ref("");

async function onSubmit() {
  const res = await fetch("http://localhost:8080/user/login", {
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
    localStorage.setItem("isAdmin", String(result.data.isAdmin || 0));
    localStorage.setItem("username", result.data.username || username.value);
    localStorage.setItem("nickname", result.data.nickname || result.data.username || username.value);
    if (result.data.token) {
      localStorage.setItem("token", result.data.token);
    }
    router.push("/");
  } else {
    alert(result.message || "登录失败");
  }
}
</script>
