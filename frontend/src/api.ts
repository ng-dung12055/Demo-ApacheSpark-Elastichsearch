import type {
  AdminCreateUserPayload,
  AdminRole,
  AdminUser,
  AuditLog,
  AuthResponse,
  ChangePasswordPayload,
  CreateRecordPayload,
  DoctorPatientBasic,
  DoctorProfile,
  DoctorProfileUpdatePayload,
  DoctorPatientDetail,
  DoctorRecord,
  DoctorRecordDetail,
  LoginPayload,
  MasterKeyBackup,
  MasterKeySummary,
  MedicalRecord,
  MetadataRecord,
  PatientHistoryPage,
  PatientRecordDetail,
  PatientProfile,
  RegisterPayload,
  SearchReindexResponse,
  SearchReindexStatus,
  SearchRecordPage,
  UpdateProfilePayload
} from "./types";

const DEFAULT_HTTP_API = "http://localhost:8081";
const DEFAULT_HTTPS_API = "https://localhost:8083";
const isHttps =
  typeof window !== "undefined" && window.location.protocol === "https:";
const API_BASE =
  import.meta.env.VITE_API_BASE_URL || (isHttps ? DEFAULT_HTTPS_API : DEFAULT_HTTP_API);

const getToken = () => {
  const raw = localStorage.getItem("token");
  if (!raw) {
    return null;
  }
  const trimmed = raw.trim();
  if (trimmed.startsWith('"') && trimmed.endsWith('"')) {
    try {
      return JSON.parse(trimmed);
    } catch {
      return trimmed.replace(/^"+|"+$/g, "");
    }
  }
  return trimmed.replace(/^Bearer\s+/i, "");
};

const authHeaders = (): Record<string, string> => {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const jsonHeaders = (): Record<string, string> => ({
  "Content-Type": "application/json"
});

async function toErrorMessage(res: Response) {
  try {
    const data = await res.json();
    if (data && typeof data.message === "string") {
      return data.message;
    }
  } catch {
    // ignore
  }
  try {
    const text = await res.text();
    if (text) {
      return text;
    }
  } catch {
    // ignore
  }
  return `${res.status} ${res.statusText}`;
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: "POST",
    headers: jsonHeaders(),
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const res = await fetch(`${API_BASE}/api/auth/register`, {
    method: "POST",
    headers: jsonHeaders(),
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function searchByPatientCode(patientCode: string): Promise<MetadataRecord[]> {
  const url = new URL(`${API_BASE}/api/doctor/search`);
  url.searchParams.set("patientCode", patientCode);
  const res = await fetch(url.toString(), {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function searchByKeyword(
  keyword: string,
  limit = 2000
): Promise<MetadataRecord[]> {
  const url = new URL(`${API_BASE}/api/doctor/search/keyword`);
  url.searchParams.set("keyword", keyword);
  url.searchParams.set("page", "0");
  url.searchParams.set("size", String(limit));
  const res = await fetch(url.toString(), {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  const data = (await res.json()) as SearchRecordPage;
  return data.items ?? [];
}

export async function searchRecords(params: {
  keyword?: string;
  patientCode?: string;
  recordId?: number;
  page?: number;
  size?: number;
}): Promise<SearchRecordPage> {
  const url = new URL(`${API_BASE}/api/doctor/search/keyword`);
  if (params.keyword) {
    url.searchParams.set("keyword", params.keyword);
  }
  if (params.patientCode) {
    url.searchParams.set("patientCode", params.patientCode);
  }
  if (params.recordId !== undefined) {
    url.searchParams.set("recordId", String(params.recordId));
  }
  url.searchParams.set("page", String(params.page ?? 0));
  url.searchParams.set("size", String(params.size ?? 50));
  const res = await fetch(url.toString(), {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getDoctorPatientDetail(patientCode: string): Promise<DoctorPatientDetail> {
  const res = await fetch(`${API_BASE}/api/doctor/patient/${patientCode}`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getDoctorPatientBasic(patientCode: string): Promise<DoctorPatientBasic> {
  const res = await fetch(`${API_BASE}/api/doctor/patient/${patientCode}/basic`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getDoctorRecord(recordId: number): Promise<DoctorRecord> {
  const res = await fetch(`${API_BASE}/api/doctor/record/${recordId}`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getDoctorRecordDetail(recordId: number): Promise<DoctorRecordDetail> {
  const res = await fetch(`${API_BASE}/api/doctor/record/${recordId}/detail`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function updateDoctorRecord(
  recordId: number,
  payload: {
    diagnosis?: string;
    keywords?: string;
    summary?: string;
    status?: string;
    securityLevel?: string;
  }
): Promise<void> {
  const res = await fetch(`${API_BASE}/api/doctor/record/${recordId}`, {
    method: "PUT",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function deleteDoctorRecord(recordId: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/doctor/record/${recordId}`, {
    method: "DELETE",
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function createRecord(payload: CreateRecordPayload): Promise<void> {
  const res = await fetch(`${API_BASE}/api/doctor/record`, {
    method: "POST",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function getDebugMe(): Promise<{ username: string; authorities: string[] }> {
  const res = await fetch(`${API_BASE}/api/debug/me`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getDoctorProfile(): Promise<DoctorProfile> {
  const res = await fetch(`${API_BASE}/api/doctor/profile`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function updateDoctorProfile(
  payload: DoctorProfileUpdatePayload
): Promise<DoctorProfile> {
  const res = await fetch(`${API_BASE}/api/doctor/profile`, {
    method: "PUT",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getPatientProfile(): Promise<PatientProfile> {
  const res = await fetch(`${API_BASE}/api/patient/profile`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function updatePatientProfile(payload: UpdateProfilePayload): Promise<PatientProfile> {
  const res = await fetch(`${API_BASE}/api/patient/profile`, {
    method: "PUT",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getPatientHistory(): Promise<MedicalRecord[]> {
  const res = await fetch(`${API_BASE}/api/patient/history`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getPatientHistoryPage(
  page = 0,
  size = 50,
  status?: string
): Promise<PatientHistoryPage> {
  const url = new URL(`${API_BASE}/api/patient/history/page`);
  url.searchParams.set("page", String(page));
  url.searchParams.set("size", String(size));
  if (status) {
    url.searchParams.set("status", status);
  }
  const res = await fetch(url.toString(), {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getPatientRecordDetail(recordId: number): Promise<PatientRecordDetail> {
  const res = await fetch(`${API_BASE}/api/patient/record/${recordId}/detail`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function changePatientPassword(payload: ChangePasswordPayload): Promise<void> {
  const res = await fetch(`${API_BASE}/api/patient/password`, {
    method: "PUT",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function changeDoctorPassword(payload: ChangePasswordPayload): Promise<void> {
  const res = await fetch(`${API_BASE}/api/doctor/password`, {
    method: "PUT",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function getAdminUsers(): Promise<AdminUser[]> {
  const res = await fetch(`${API_BASE}/api/admin/users`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getAdminRoles(): Promise<AdminRole[]> {
  const res = await fetch(`${API_BASE}/api/admin/roles`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function createAdminUser(payload: AdminCreateUserPayload): Promise<void> {
  const res = await fetch(`${API_BASE}/api/admin/users`, {
    method: "POST",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify(payload)
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function updateAdminUserRole(accountId: number, role: string): Promise<void> {
  const res = await fetch(`${API_BASE}/api/admin/users/${accountId}/role`, {
    method: "PUT",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify({ role })
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function setAdminUserActive(accountId: number, active: boolean): Promise<void> {
  const url = new URL(`${API_BASE}/api/admin/users/${accountId}/lock`);
  url.searchParams.set("active", String(active));
  const res = await fetch(url.toString(), {
    method: "PUT",
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function deleteAdminUser(accountId: number): Promise<void> {
  const res = await fetch(`${API_BASE}/api/admin/users/${accountId}`, {
    method: "DELETE",
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function getMasterKeys(): Promise<MasterKeySummary[]> {
  const res = await fetch(`${API_BASE}/api/admin/master-key`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function createMasterKey(storageLocation: string): Promise<void> {
  const res = await fetch(`${API_BASE}/api/admin/master-key`, {
    method: "POST",
    headers: { ...jsonHeaders(), ...authHeaders() },
    body: JSON.stringify({ storageLocation })
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
}

export async function backupMasterKeys(): Promise<MasterKeyBackup[]> {
  const res = await fetch(`${API_BASE}/api/admin/master-key/backup`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getAuditLogs(limit = 50): Promise<AuditLog[]> {
  const url = new URL(`${API_BASE}/api/admin/audit-logs`);
  url.searchParams.set("limit", String(limit));
  const res = await fetch(url.toString(), {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function reindexSearch(): Promise<SearchReindexResponse> {
  const res = await fetch(`${API_BASE}/api/admin/search/reindex`, {
    method: "POST",
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}

export async function getReindexStatus(): Promise<SearchReindexStatus> {
  const res = await fetch(`${API_BASE}/api/admin/search/reindex/status`, {
    headers: authHeaders()
  });
  if (!res.ok) {
    throw new Error(await toErrorMessage(res));
  }
  return res.json();
}
