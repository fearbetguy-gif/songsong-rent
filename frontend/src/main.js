import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import pinia from "./stores";
import "./styles/design-system.css";

createApp(App).use(router).use(pinia).mount("#app");
