import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import type { Role } from '../types/api';

export function ProtectedRoute() {
  const { session } = useAuth(); const location = useLocation();
  return session ? <Outlet /> : <Navigate to="/login" replace state={{ from: location.pathname }} />;
}
export function RoleRoute({ role }: { role: Role }) {
  const { session } = useAuth();
  if (!session) return <Navigate to="/login" replace />;
  return session.role === role ? <Outlet /> : <Navigate to={session.role === 'PACIENTE' ? '/paciente' : '/medico'} replace />;
}
