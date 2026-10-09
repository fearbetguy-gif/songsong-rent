import { createPinia, defineStore } from "pinia";

const pinia = createPinia();

export default pinia;

export const useAppStore = defineStore("app", {
  state: () => ({
    appName: "租房平台",
  }),
});
