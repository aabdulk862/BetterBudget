import {
  Alert,
  Box,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Typography,
} from "@mui/material";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Paper,
} from "@mui/material";
import { useEffect, useState } from "react";
import useStore, { UserInfo } from "../../stores";
import backendHost from "../../backendHost";
import axios from "axios";

interface User {
  userId: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  role: string;
}

export default function () {
  const u: UserInfo = useStore((state: any) => state.user);
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const setSnackbar = useStore((state) => state.setSnackbar);

  const [deleteDialog, setDeleteDialog] = useState(false);
  const [promoteDialog, setPromoteDialog] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User>({
    userId: 0,
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    role: "",
  });

  const fetchUsers = () => {
    setIsLoading(true);
    setError(null);
    fetch(`${backendHost}/users`, {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + u.token,
      },
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("Failed to fetch users");
        }
        return res.json();
      })
      .then((body) => {
        if (!body.hasOwnProperty("error")) {
          body.sort((a: User, b: User) => a.userId - b.userId);
          setUsers(body);
        }
      })
      .catch((err) => {
        const message =
          err instanceof Error ? err.message : "An unexpected error occurred";
        setError(message);
        console.error(err);
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  useEffect(() => {
    fetchUsers();
  }, [u]);

  const handleDeleteUser = (username: string) => {
    // Delete user
    fetch(`${backendHost}/users`, {
      method: "DELETE",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + u.token,
      },
      body: `${username}`,
    }).then(() => {
      // TODO: check failed
      setSnackbar(true, "User deleted successfully!");
      setUsers(users.filter((user) => user.username !== username));
    });
  };

  const handlePromoteUser = (user: User) => {
    let updatedUser = {
      userId: user.userId,
      firstName: user.firstName,
      lastName: user.lastName,
      username: user.username,
      email: user.email,
      role: "ROLE_MANAGER",
    };

    axios
      .patch(`${backendHost}/users/role`, updatedUser, {
        headers: {
          Authorization: `Bearer ${u.token}`,
          "Content-Type": "application/json",
        },
        withCredentials: true,
      })
      .then((res) => {
        console.log(res);
        setUsers(
          users.map((u) => {
            if (u.userId == user.userId) {
              return updatedUser;
            }
            return u;
          }),
        );
        setSnackbar(true, "User promoted successfully!");
      })
      .catch((err) => {
        console.log(err);
      });
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
            <Button color="inherit" size="small" onClick={fetchUsers}>
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
    <Box sx={{ width: "75vw", margin: "auto", paddingTop: "2rem" }}>
      <Typography variant="h4" sx={{ fontWeight: "bold" }} gutterBottom>
        All Users
      </Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ width: "50px" }}>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Username</TableCell>
              <TableCell>Email</TableCell>
              <TableCell sx={{ width: "250px" }}>Role</TableCell>
              <TableCell sx={{ width: "250px" }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((user) => (
              <TableRow key={user.userId}>
                <TableCell sx={{ width: "50px" }}>{user.userId}</TableCell>
                <TableCell>
                  {user.firstName} {user.lastName}
                </TableCell>
                <TableCell>{user.username}</TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell sx={{ width: "250px" }}>
                  {user.role === "ROLE_MANAGER" ? "Manager" : "User"}
                </TableCell>
                <TableCell sx={{ width: "250px" }}>
                  {user.role == "ROLE_EMPLOYEE" ? ( //250px with both buttons
                    <Button
                      variant="contained"
                      color="secondary"
                      sx={{ marginRight: "1rem" }}
                      onClick={() => {
                        setSelectedUser(user);
                        setPromoteDialog(true);
                      }}
                    >
                      Promote
                    </Button>
                  ) : (
                    <></>
                  )}
                  {user.userId != u.userId && (
                    <Button
                      variant="contained"
                      color="secondary"
                      onClick={() => {
                        setSelectedUser(user);
                        setDeleteDialog(true);
                      }}
                    >
                      Delete
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Delete Dialog */}
      <Dialog
        open={deleteDialog}
        onClose={() => {
          setDeleteDialog(false);
        }}
      >
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete user {selectedUser.username}?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              handleDeleteUser(selectedUser.username);
              setDeleteDialog(false);
            }}
          >
            Yes
          </Button>
          <Button
            onClick={() => {
              setDeleteDialog(false);
            }}
          >
            No
          </Button>
        </DialogActions>
      </Dialog>

      {/* Promote Dialog */}
      <Dialog
        open={promoteDialog}
        onClose={() => {
          setPromoteDialog(false);
        }}
      >
        <DialogTitle>Confirm Promotion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to promote user {selectedUser.username} to
            manager?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              handlePromoteUser(selectedUser);
              setPromoteDialog(false);
            }}
          >
            Yes
          </Button>
          <Button
            onClick={() => {
              setPromoteDialog(false);
            }}
          >
            No
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
