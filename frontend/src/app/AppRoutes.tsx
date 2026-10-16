import { Navigate, Route, Routes } from 'react-router'

import { LoginPage } from '@/features/auth/LoginPage'
import { RegisterPage } from '@/features/auth/RegisterPage'
import { ProtectedRoute, PublicOnlyRoute } from '@/features/auth/routes'
import { NewOrderPage } from '@/features/orders/pages/NewOrderPage'
import { OrderDetailPage } from '@/features/orders/pages/OrderDetailPage'
import { OrdersPage } from '@/features/orders/pages/OrdersPage'

import { AppLayout } from './AppLayout'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/pedidos" element={<OrdersPage />} />
          <Route path="/pedidos/novo" element={<NewOrderPage />} />
          <Route path="/pedidos/:id" element={<OrderDetailPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/pedidos" replace />} />
    </Routes>
  )
}
