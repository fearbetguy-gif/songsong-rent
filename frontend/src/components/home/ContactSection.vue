<template>
  <section class="contact-section reveal-section">
    <h2>还在犹豫选哪套房？<br />让我们来帮你</h2>

    <form class="contact-form" @submit.prevent>
      <input type="email" placeholder="请输入你的邮箱" />
      <button class="btn btn-primary" type="submit">发送</button>
    </form>
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted } from "vue";

let revealObserver;

onMounted(() => {
  const el = document.querySelector(".contact-section.reveal-section");
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
    { threshold: 0.2 }
  );
  revealObserver.observe(el);
});

onBeforeUnmount(() => {
  if (revealObserver) {
    revealObserver.disconnect();
  }
});
</script>

<style scoped>
.contact-section {
  padding: 80px 0;
  text-align: center;
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.contact-section.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.contact-section h2 {
  margin: 0 0 32px;
  font-size: 46px;
  line-height: 1.25;
  color: #0f172a;
}

.contact-form {
  width: min(620px, 100%);
  margin: 0 auto;
  background: #ffffff;
  border-radius: 20px;
  padding: 7px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
  display: flex;
  align-items: center;
}

.contact-form input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  padding: 14px 20px;
  font-size: 17px;
  color: #334155;
}

.contact-form input::placeholder {
  color: #9ca3af;
}

.btn {
  border: 1px solid transparent;
  border-radius: 999px;
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease, transform 0.3s ease, box-shadow 0.3s ease;
}

.btn-primary {
  background: #2563eb;
  color: #ffffff;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.22);
}

.btn-primary:hover {
  background: #1d4ed8;
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(29, 78, 216, 0.3);
}

@media (max-width: 768px) {
  .contact-section h2 {
    font-size: 34px;
  }

  .contact-form {
    border-radius: 20px;
    flex-direction: column;
    gap: 10px;
  }

  .contact-form .btn,
  .contact-form input {
    width: 100%;
    border-radius: 999px;
  }
}
</style>
