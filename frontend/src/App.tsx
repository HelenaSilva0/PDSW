import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute, RoleRoute } from './auth/Routes';
import { AppLayout } from './layout/AppLayout';
import { LoginPage, RegisterPage } from './pages/AuthPages';
import { PrivacyPage, TermsPage } from './pages/LegalPages';
import { ChatPage } from './pages/ChatPage';
import { DoctorDashboardPage, DoctorPatientLayout } from './pages/DoctorPages';
import { ExamsPage } from './pages/ExamsPage';
import { HistoryPage } from './pages/HistoryPage';
import { HomePage } from './pages/HomePage';
import { PatientPage } from './pages/PatientPage';

export function App() {
  return <Routes><Route element={<AppLayout />}><Route index element={<HomePage />} /><Route path="login" element={<LoginPage />} /><Route path="cadastro" element={<RegisterPage />} /><Route path="termos" element={<TermsPage />} /><Route path="privacidade" element={<PrivacyPage />} /><Route element={<ProtectedRoute />}><Route element={<RoleRoute role="PACIENTE" />}><Route path="paciente" element={<PatientPage />} /><Route path="paciente/historico-familiar" element={<HistoryPage />} /><Route path="paciente/exames" element={<ExamsPage />} /><Route path="paciente/chat" element={<ChatPage />} /></Route><Route element={<RoleRoute role="MEDICO" />}><Route path="medico" element={<DoctorDashboardPage />} /><Route path="medico/chat" element={<ChatPage />} /><Route path="medico/paciente/:pacienteId" element={<DoctorPatientLayout />} /></Route></Route><Route path="*" element={<Navigate to="/" replace />} /></Route></Routes>;
}
