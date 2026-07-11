import "@mdi/font/css/materialdesignicons.css";
import "vuetify/styles";
import { createVuetify } from "vuetify";

export default createVuetify({
  components: {},
  theme: {
    defaultTheme: "light",
    themes: {
      light: {
        dark: false,
        colors: {
          background: "#FAFAFA",
          surface: "#FFFFFF",
          primary: "#81BA24",
          "primary-dark": "#4F8A00",
          "primary-light": "#B4ED59",
          secondary: "#03DAC6",
          "secondary-darken-1": "#018786",
          "dark-gray": "#414958",
          black: "#1F242E",
          white: "#FFFFFF",
          error: "#e60000",
          info: "#2196F3",
          success: "#4CAF50",
          warning: "#FB8C00",
        },
      },
      dark: {
        dark: true,
        colors: {
          background: "#1e1e1e",
          surface: "#252526",
          primary: "#81BA24",
          "primary-dark": "#4F8A00",
          "primary-light": "#B4ED59",
          secondary: "#03DAC6",
          "secondary-darken-1": "#018786",
          "dark-gray": "#414958",
          black: "#1F242E",
          white: "#FFFFFF",
          error: "#e60000",
          info: "#2196F3",
          success: "#4CAF50",
          warning: "#FB8C00",
        },
      },
    },
  },
});
