import axios from "axios";

const backendHost: string =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";
axios.defaults.baseURL = backendHost;
export default backendHost;
