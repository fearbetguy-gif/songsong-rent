<template>
  <div class="auth-page">
    <header class="auth-header">
      <router-link to="/" class="logo" aria-label="松松租房平台首页">
        <img src="/logo.png" alt="松松租房平台 Logo" class="logo-img" />
        <span class="logo-text">松松租房平台</span>
      </router-link>
      <router-link to="/login" class="text-link">已有账号？去登录</router-link>
    </header>

    <div class="auth-card">
      <div class="auth-card-inner">
        <p class="eyebrow">欢迎加入</p>
        <h1 class="title">注册账号</h1>
        <p class="sub">创建账号后即可使用平台服务。</p>

        <form class="auth-form" @submit.prevent="onSubmit">
          <label class="auth-field">
            <span class="label">用户名</span>
            <input v-model="username" type="text" required placeholder="请输入用户名" />
          </label>
          <label class="auth-field">
            <span class="label">昵称</span>
            <input v-model="nickname" type="text" required placeholder="请输入昵称" />
          </label>
          <label class="auth-field">
            <span class="label">密码</span>
            <input v-model="password" type="password" required placeholder="请输入密码" />
          </label>
          <button class="auth-submit" type="submit">注册</button>
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
const nickname = ref("");
const password = ref("");

async function onSubmit() {
  const res = await fetch("http://localhost:8080/user/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: username.value,
      nickname: nickname.value,
      password: password.value,
    }),
  });
  const result = await res.json();
  if (result.code === 200 && result.data?.id != null) {
    localStorage.setItem("userId", String(result.data.id));
    localStorage.setItem("username", result.data.username || username.value);
    localStorage.setItem("nickname", result.data.nickname || nickname.value);
    localStorage.setItem("isAdmin", String(result.data.isAdmin || 0));
    if (result.data.token) {
      localStorage.setItem("token", result.data.token);
    }
    router.push("/");
  } else {
    alert(result.message || "注册失败");
  }
}
</script>
