<template>
  <footer class="site-footer reveal-section">
    <div class="footer-inner">
      <div class="col brand-col">
        <h3>松松租房平台</h3>
      </div>

      <div class="col">
        <h4>快捷入口</h4>
        <a href="#">找整租</a>
        <a href="#">找合租</a>
        <a href="#">租金地图</a>
      </div>

      <div class="col">
        <h4>热门城市</h4>
        <p>北京</p>
        <p>上海</p>
        <p>深圳</p>
      </div>

      <div class="col">
        <h4>联系我们</h4>
        <p>400-800-1234</p>
        <p>service@songsongrent.com</p>
        <p class="social">微信 &nbsp; 微博 &nbsp; 抖音</p>
      </div>
    </div>
    <div class="copyright">© 2026 松松租房平台</div>
  </footer>
</template>

<script setup>
import { onBeforeUnmount, onMounted } from "vue";

let revealObserver;

onMounted(() => {
  const el = document.querySelector(".site-footer.reveal-section");
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
    { threshold: 0.15 }
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
.site-footer {
  margin-top: 40px;
  background:
    linear-gradient(140deg, #0f4fc5 0%, #1e63d5 45%, #2f85f0 100%);
  color: #ffffff;
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  border-top: 1px solid rgba(255, 255, 255, 0.24);
  opacity: 0;
  transform: translateY(18px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.site-footer.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr 1fr;
  gap: 44px;
}

.col h4,
.col h3 {
  margin: 0 0 16px;
  font-size: 24px;
  color: #ffffff;
  font-weight: 700;
}

.col h4 {
  font-size: 22px;
}

.col a,
.col p {
  display: block;
  margin: 0 0 10px;
  color: rgba(239, 246, 255, 0.88);
  text-decoration: none;
  font-size: 16px;
  line-height: 1.7;
}

.col a:hover {
  color: #dbeafe;
  transform: translateX(2px);
}

.social {
  letter-spacing: 2px;
  font-weight: 600;
}

.copyright {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 28px;
  color: rgba(239, 246, 255, 0.82);
  font-size: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.22);
  padding-top: 18px;
}

@media (max-width: 900px) {
  .footer-inner {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .footer-inner {
    grid-template-columns: 1fr;
  }
}
</style>
