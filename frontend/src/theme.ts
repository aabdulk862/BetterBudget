import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    primary: {
      main: "#23A455",
    },
    secondary: {
      main: "#8B4DFE",
    },
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily: '"Roboto", sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        contained: {
          backgroundColor: "#8B4DFE",
          color: "#ffffff",
          borderRadius: "30px",
          fontWeight: "bold",
          boxShadow: "0px 2px 5px rgba(0, 0, 0, 0.2)",
          transition:
            "background-color 0.3s ease-in-out, color 0.3s ease-in-out",
          "&:hover": {
            backgroundColor: "#7a3de8",
          },
        },
        root: {
          "&:focus-visible": {
            outline: "2px solid #8B4DFE",
            outlineOffset: "2px",
          },
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          "&:focus-visible": {
            outline: "2px solid #8B4DFE",
            outlineOffset: "2px",
          },
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          "& .MuiInputBase-root:focus-within": {
            outline: "2px solid #8B4DFE",
            outlineOffset: "2px",
          },
        },
      },
    },
  },
});

export default theme;
