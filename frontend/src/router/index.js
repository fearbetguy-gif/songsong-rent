import { createRouter, createWebHistory } from "vue-router";
import Home from "../views/Home.vue";
import HouseList from "../views/HouseList.vue";
import HouseDetail from "../views/HouseDetail.vue";
import Register from "../views/Register.vue";
import Login from "../views/Login.vue";
import AdminLogin from "../views/AdminLogin.vue";
import MyAppointments from "../views/MyAppointments.vue";
import MapSearch from "../views/MapSearch.vue";
import MyFavorites from "../views/MyFavorites.vue";
import LandlordDashboard from "../views/LandlordDashboard.vue";

const routes = [
  { path: "/", name: "Home", component: Home },
  { path: "/houses", name: "HouseList", component: HouseList },
  { path: "/map-search", name: "MapSearch", component: MapSearch },
  { path: "/houses/:id", name: "HouseDetail", component: HouseDetail },
  { path: "/login", name: "Login", component: Login },
  { path: "/register", name: "Register", component: Register },
  { path: "/admin-login", name: "AdminLogin", component: AdminLogin },
  { path: "/landlord", name: "LandlordDashboard", component: LandlordDashboard },
  { path: "/my-appointments", name: "MyAppointments", component: MyAppointments },
  { path: "/my-favorites", name: "MyFavorites", component: MyFavorites },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
