import React from "react";
import { Alert, Box, Button } from "@mui/material";
import { Link } from "react-router-dom";

interface Props {
  children: React.ReactNode;
}

interface State {
  hasError: boolean;
}

class ErrorBoundary extends React.Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(): State {
    return { hasError: true };
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    console.error("ErrorBoundary caught an error:", error, info);
  }

  handleRetry = () => {
    this.setState({ hasError: false });
  };

  render() {
    if (this.state.hasError) {
      return (
        <Box
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          minHeight="50vh"
          gap={2}
          p={3}
        >
          <Alert severity="error" sx={{ maxWidth: 500, width: "100%" }}>
            Something went wrong. Please try again or return to the home page.
          </Alert>
          <Box display="flex" gap={2}>
            <Button variant="contained" onClick={this.handleRetry}>
              Retry
            </Button>
            <Button variant="outlined" component={Link} to="/">
              Go Home
            </Button>
          </Box>
        </Box>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
