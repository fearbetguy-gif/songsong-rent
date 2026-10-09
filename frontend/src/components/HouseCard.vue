<template>
  <component :is="rootComponent" v-bind="rootProps" class="house-card-ui">
    <div class="cover-wrap">
      <img
        v-if="house.coverImage"
        class="cover"
        :src="house.coverImage"
        :alt="house.title || '房源图片'"
        loading="lazy"
        decoding="async"
      />
      <div v-else class="cover cover-empty">暂无图片</div>
      <span v-if="house.district || house.city" class="location-badge">
        {{ house.city || "" }}{{ house.city && house.district ? " · " : "" }}{{ house.district || "" }}
      </span>
    </div>

    <div class="body">
      <h3>{{ house.title || "未命名房源" }}</h3>
      <p class="price">￥{{ house.rentPrice || "-" }} <span>/ 月</span></p>
      <p class="meta">{{ house.roomType || "户型待补充" }} · {{ house.areaSize || "-" }}㎡</p>
      <p class="meta">{{ house.areaName || house.district || "位置待补充" }}</p>
      <div v-if="house.labels?.length" class="tags">
        <span v-for="label in house.labels.slice(0, 3)" :key="label.id" class="tag">{{ label.name }}</span>
      </div>
      <slot name="actions" />
    </div>
  </component>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";

const props = defineProps({
  house: {
    type: Object,
    required: true,
  },
  to: {
    type: [String, Object],
    default: "",
  },
});

const rootComponent = computed(() => (props.to ? RouterLink : "article"));
const rootProps = computed(() => (props.to ? { to: props.to } : {}));
</script>

<style scoped>
.house-card-ui {
  display: block;
  overflow: hidden;
  color: inherit;
  text-decoration: none;
  background: var(--ss-bg-container);
  border: 1px solid var(--ss-border-light);
  border-radius: var(--ss-radius-xl);
  box-shadow: var(--ss-shadow-card);
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  contain: layout paint;
  content-visibility: auto;
  contain-intrinsic-size: 330px;
  touch-action: manipulation;
}

.house-card-ui:hover {
  transform: translateY(-3px);
  border-color: var(--ss-primary-soft-border);
  box-shadow: var(--ss-shadow);
}

.cover-wrap {
  position: relative;
  overflow: hidden;
  aspect-ratio: 16 / 10;
  background: var(--ss-bg-elevated);
}

.cover {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.cover-empty {
  display: grid;
  place-items: center;
  color: var(--ss-text-tertiary);
  font-weight: 700;
  letter-spacing: 0.08em;
}

.location-badge {
  position: absolute;
  left: 12px;
  bottom: 12px;
  max-width: calc(100% - 24px);
  padding: 5px 10px;
  color: #fff;
  background: rgba(15, 23, 42, 0.68);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  backdrop-filter: blur(8px);
}

.body {
  padding: 16px;
}

h3 {
  min-height: 50px;
  margin: 0 0 10px;
  color: var(--ss-text);
  font-size: 18px;
  line-height: 1.38;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price {
  margin: 0 0 8px;
  color: var(--ss-primary);
  font-size: 24px;
  font-weight: 850;
  line-height: 1.1;
}

.price span {
  color: var(--ss-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.meta {
  margin: 0 0 5px;
  color: var(--ss-text-secondary);
  font-size: 14px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.tag {
  padding: 3px 8px;
  color: var(--ss-primary-active);
  background: var(--ss-primary-soft);
  border: 1px solid var(--ss-primary-soft-border);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

@media (prefers-reduced-motion: reduce) {
  .house-card-ui {
    transition: none;
  }

  .house-card-ui:hover {
    transform: none;
  }
}
</style>
