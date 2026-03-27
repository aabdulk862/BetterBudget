import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Navbar } from "./Components/Navbar/Navbar";
import { Login } from "./Components/Auth/Login";
import { Register } from "./Components/Auth/Register";
import { DetailedEnvelope } from "./Components/DetailedEnvelope/DetailedEnvelope";
import { Personalize } from "./Components/Auth/Personalize";
import { AddMoney } from "./Components/AddMoney/AddMoney";
import { EnvelopeList } from "./Components/Envelopes/EnvelopeList";
import { CreateEnvelope } from "./Components/CreateEnvelope/CreateEnvelope";
import { useEffect } from "react";
import SeeUsers from "./Components/SeeUsers/SeeUsers";
import useStore from "./stores";
import { Alert, Snackbar } from "@mui/material";
import { AllTransactions } from "./Components/Transactions/AllTransactions";
import ErrorBoundary from "./Components/ErrorBoundary/ErrorBoundary";

function App() {
  const setUser = useStore((state) => state.setUser);
  const snackbar = useStore((state) => state.snackbar);
  const setSnackbar = useStore((state) => state.setSnackbar);
  // Login on page refresh
  useEffect(() => {
    const token = localStorage.getItem("gooderBudgetToken");

    if (token) {
      // Parse stored user information (if any)
      const userInfo = JSON.parse(
        localStorage.getItem("gooderBudgetUser") || "{}",
      );

      if (userInfo && userInfo.token) {
        setUser({
          loggedIn: true,
          ...userInfo,
        });
      }
    }
  }, [setUser]);

  const handleCloseSnackbar = () => {
    setSnackbar(false, "");
  };
  return (
    <>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route
            path="/"
            element={
              <ErrorBoundary>
                <Login />
              </ErrorBoundary>
            }
          />
          <Route
            path="/register"
            element={
              <ErrorBoundary>
                <Register />
              </ErrorBoundary>
            }
          />
          <Route
            path="/personalize"
            element={
              <ErrorBoundary>
                <Personalize />
              </ErrorBoundary>
            }
          />
          <Route
            path="/new_envelope"
            element={
              <ErrorBoundary>
                <CreateEnvelope />
              </ErrorBoundary>
            }
          />
          <Route
            path="/envelopes"
            element={
              <ErrorBoundary>
                <EnvelopeList />
              </ErrorBoundary>
            }
          />
          <Route
            path="/envelope/:id"
            element={
              <ErrorBoundary>
                <DetailedEnvelope />
              </ErrorBoundary>
            }
          />
          <Route
            path="/transactions"
            element={
              <ErrorBoundary>
                <AllTransactions />
              </ErrorBoundary>
            }
          />
          <Route
            path="/add"
            element={
              <ErrorBoundary>
                <AddMoney />
              </ErrorBoundary>
            }
          />
          <Route
            path="/users"
            element={
              <ErrorBoundary>
                <SeeUsers />
              </ErrorBoundary>
            }
          />
        </Routes>
      </BrowserRouter>
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity="success"
          sx={{ width: "100%" }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
}

export default App;
