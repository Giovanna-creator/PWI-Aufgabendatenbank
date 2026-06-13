import { createApp } from "vue";
import { createPinia } from "pinia";

// Components
import App from "./App.vue";

// Composables
import router from "./router";

// Plugins
import { registerPlugins } from "./plugins";
import vuetify from "./plugins/vuetify";

// API adapter selection
// - npm run dev          → AdbApiService (real HTTP, expects backend at /api)
// - npm run dev:dummy    → DevAdbApiService (logs requests, uses seed dummy data)
import { setApiAdapter } from "@/stores/exerciseStore";
import adbApi from "@/feature/aufgabendatenbank/adbApi.service";
import devAdbApi from "@/feature/aufgabendatenbank/dev-adb-api.service";
const adapter = import.meta.env.VITE_ADB_API_MODE === "dummy" ? devAdbApi : adbApi;
setApiAdapter(adapter);

const pinia = createPinia();
const app = createApp(App);
registerPlugins();

app.use(router);
app.use(vuetify);
app.use(pinia);
app.mount("#app");
