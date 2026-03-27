import { useState, useEffect } from "react";
import useStore, { UserInfo } from "../../stores";
import { EnvelopeListCard } from "./EnvelopeListCard";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import AttachMoneyIcon from "@mui/icons-material/AttachMoney";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  IconButton,
  Tooltip,
} from "@mui/material";
import { useNavigate } from "react-router-dom";
import backendHost from "../../backendHost";

interface UserData {
  userId: number;
  username: string;
  email: string;
  role: string;
  firstName: string;
  lastName: string;
}

interface Envelope {
  envelopeId: number;
  user_id: number;
  envelopeDescription: string;
  balance: number;
  maxLimit: number;
  user: UserData;
}

export const EnvelopeList = () => {
  const user: UserInfo = useStore((state: any) => state.user);
  const [envelopeList, setEnvelopeList] = useState<Envelope[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.token) {
      loadEnvelopeList();
    }
  }, [user?.token]);

  const loadEnvelopeList = async () => {
    setIsLoading(true);
    setError(null);
    try {
      let requestString = "";
      if (user.role === "ROLE_MANAGER") {
        requestString = `${backendHost}/envelopes`;
      } else {
        requestString = `${backendHost}/envelopes?userId=${user.userId}`;
      }
      const response = await fetch(requestString, {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${user.token}`,
        },
      });
      if (!response.ok) {
        throw new Error("Failed to fetch envelopes");
      }
      const data = await response.json();
      // Paginated response has a content array; plain list is used for userId filter
      const envelopes = Array.isArray(data) ? data : (data.content ?? []);
      setEnvelopeList(envelopes);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "An unexpected error occurred";
      setError(message);
      console.error("Error fetching envelopes:", err);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "200px",
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "200px",
          padding: 2,
        }}
      >
        <Alert
          severity="error"
          action={
            <Button color="inherit" size="small" onClick={loadEnvelopeList}>
              Retry
            </Button>
          }
        >
          {error}
        </Alert>
      </Box>
    );
  }

  return (
    <div className="envelope-container">
      <div className="envelope-title-group">
        <p className="envelope-title">
          {user.role === "ROLE_MANAGER" ? "All Envelopes" : "My Envelopes"}
        </p>
        <Tooltip title="Add new envelope" placement="bottom" arrow>
          <IconButton
            aria-label="add"
            size="large"
            onClick={() => navigate("/new_envelope")}
          >
            <AddCircleOutlineIcon fontSize="inherit" />
          </IconButton>
        </Tooltip>
        <Tooltip title="Add Money" placement="bottom" arrow>
          <IconButton
            aria-label="addtransact"
            size="large"
            onClick={() => navigate("/add")}
          >
            <AttachMoneyIcon fontSize="inherit" />
          </IconButton>
        </Tooltip>
      </div>
      <hr className="envelope-hr" />
      {/* No envelopes, prompts to create one */}
      {envelopeList.length === 0 && (
        <div>
          <p className="envelope-subtitle">You have no envelopes...</p>
          <br />
          <p className="envelope-subtitle-sub">
            Create a new envelope by clicking the + above!
          </p>
        </div>
      )}
      {/* Shows if there are envelopes within the budget */}
      {envelopeList.some((env) => env.balance >= env.maxLimit / 2) && (
        <div className="envelope-row-group">
          <p className="envelope-subtitle">Within Budget</p>
          <div className="envelope-row">
            {envelopeList.map((env) => {
              console.log(env.balance, env.maxLimit);
              // Within Budget - more than half balance remaining
              if (env.balance >= env.maxLimit / 2) {
                return (
                  <EnvelopeListCard
                    key={env.envelopeId}
                    colorClass={"envelope-header-good"}
                    envelope={env}
                    onClick={() => console.log(env.envelopeId)}
                  />
                );
              }
            })}
          </div>
        </div>
      )}
      {/* Shows if there are envelopes nearly used */}
      {envelopeList.some(
        (env) => env.balance > 0 && env.balance < env.maxLimit / 2,
      ) && (
        <div className="envelope-row-group">
          <p className="envelope-subtitle">Nearly Used</p>
          <div className="envelope-row">
            {envelopeList.map((env) => {
              // Nearly Used - less than half, but still non zero balance
              if (env.balance > 0 && env.balance < env.maxLimit / 2) {
                return (
                  <EnvelopeListCard
                    key={env.envelopeId}
                    colorClass={"envelope-header-warning"}
                    envelope={env}
                    onClick={() => console.log(env.envelopeId)}
                  />
                );
              }
            })}
          </div>
        </div>
      )}
      {/* Shows if there are envelopes over budget */}
      {envelopeList.some((env) => env.balance <= 0) && (
        <div className="envelope-row-group">
          <p className="envelope-subtitle">Over Budget</p>
          <div className="envelope-row">
            {envelopeList.map((env) => {
              // Over Budget - balance is zero or negative
              if (env.balance <= 0) {
                return (
                  <EnvelopeListCard
                    key={env.envelopeId}
                    colorClass={"envelope-header-danger"}
                    envelope={env}
                    onClick={() => console.log(env.envelopeId)}
                  />
                );
              }
            })}
          </div>
        </div>
      )}
    </div>
  );
};
