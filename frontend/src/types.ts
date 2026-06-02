export type AuthResponse = {
  token: string;
  role: string;
  patientCode?: string;
};

export type LoginPayload = {
  username: string;
  password: string;
};

export type RegisterPayload = {
  username: string;
  password: string;
  fullName: string;
  email?: string;
  phone?: string;
  cccd: string;
  dob?: string;
  gender?: string;
  address?: string;
};

export type CreateRecordPayload = {
  patientCode: string;
  diagnosis: string;
  keywords?: string;
  summary?: string;
  securityLevel?: string;
};

export type MetadataRecord = {
  id?: number;
  recordId?: number | null;
  patientCode: string;
  patientName: string;
  keywords: string | null;
  summary: string | null;
  updatedAt: string;
};

export type AuditLog = {
  id: number;
  username: string;
  role: string;
  method: string;
  path: string;
  statusCode: number;
  clientIp: string | null;
  userAgent: string | null;
  createdAt: string;
};

export type SearchReindexResponse = {
  indexed: number;
  status: string;
  message?: string;
};

export type SearchReindexStatus = {
  jobId: string | null;
  status: string;
  processed: number;
  total: number;
  indexed: number;
  running: boolean;
  message?: string | null;
  startedAt?: string | null;
  finishedAt?: string | null;
};

export type SearchRecordPage = {
  items: MetadataRecord[];
  total: number;
  page: number;
  size: number;
};

export type PatientHistoryPage = {
  items: MedicalRecord[];
  total: number;
  page: number;
  size: number;
};

export type DoctorPatientDetail = {
  patientCode: string;
  fullName: string;
  phone: string | null;
  email: string | null;
  dob: string | null;
  gender: string | null;
  address: string | null;
  medicalHistory: string | null;
  records: Array<{
    recordId: number;
    status: string;
    createdAt: string;
    updatedAt: string;
    summary: string | null;
    keywords: string | null;
  }>;
};

export type DoctorPatientBasic = {
  patientCode: string;
  fullName: string | null;
  phone: string | null;
  email: string | null;
  dob: string | null;
  gender: string | null;
  address: string | null;
};

export type AdminUser = {
  accountId: number;
  username: string;
  fullName: string | null;
  roles: string[];
  active: boolean;
  email: string | null;
  phone: string | null;
};

export type AdminRole = {
  name: string;
  description: string | null;
};

export type MasterKeySummary = {
  id: number;
  type: string;
  status: string;
  createdAt: string;
  vaultLocation: string | null;
};

export type MasterKeyBackup = {
  id: number;
  type: string;
  status: string;
  createdAt: string;
  vaultLocation: string | null;
  keyValueEnc: string;
};

export type AdminCreateUserPayload = {
  username: string;
  password: string;
  fullName: string;
  role: string;
  email?: string;
  phone?: string;
};

export type DoctorRecord = {
  recordId: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  summary: string | null;
  keywords: string | null;
  securityLevel?: string | null;
};

export type DoctorRecordDetail = {
  recordId: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  summary: string | null;
  keywords: string | null;
  diagnosis: string | null;
  securityLevel?: string | null;
};

export type PatientRecordDetail = {
  recordId: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  summary: string | null;
  keywords: string | null;
  diagnosis: string | null;
  securityLevel?: string | null;
};

export type PatientProfile = {
  patientCode: string;
  fullName: string | null;
  phone: string | null;
  email: string | null;
  cccd: string | null;
  dob: string | null;
  gender: string | null;
  address: string | null;
};

export type MedicalRecord = {
  id: number;
  status: string;
  createdAt: string;
  updatedAt: string;
  metadata?: MetadataRecord | null;
};

export type UpdateProfilePayload = {
  fullName?: string;
  email?: string;
  phone?: string;
  cccd?: string;
  dob?: string;
  gender?: string;
  address?: string;
};

export type ChangePasswordPayload = {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
};

export type DoctorProfile = {
  username: string;
  fullName: string;
  phone: string | null;
  email: string | null;
  staffCode: string | null;
  department: string | null;
  title: string | null;
  specialty: string | null;
  licenseNumber: string | null;
  facility: string | null;
  shift: string | null;
  employmentStatus: string | null;
  internalPhone: string | null;
  workAddress: string | null;
  gender: string | null;
  dob: string | null;
  avatarUrl: string | null;
  role: string | null;
  accountStatus: string | null;
};

export type DoctorProfileUpdatePayload = {
  fullName?: string;
  phone?: string;
  email?: string;
  gender?: string;
  dob?: string;
  specialty?: string;
  title?: string;
  licenseNumber?: string;
  department?: string;
  facility?: string;
  shift?: string;
  employmentStatus?: string;
  internalPhone?: string;
  workAddress?: string;
  avatarUrl?: string;
};
