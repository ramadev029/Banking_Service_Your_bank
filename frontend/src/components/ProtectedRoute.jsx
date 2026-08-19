import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'

/**
 * OWASP A01:2021 & A07:2021 – Security Guard & Session Invalidation Engine.
 * Enforces session storage checks and automatically invalidates sessions that have
 * navigated away or expired due to inactivity.
 */
export default function ProtectedRoute({ children, requireAuth = true }) {
  const location = useLocation()

  const checkActiveSession = () => {
    const activeSession = sessionStorage.getItem('yourbank_active_session')
    const lastActiveTime = sessionStorage.getItem('yourbank_last_active_time')

    if (!activeSession) return false

    // OWASP Inactivity Guard: Invalidate session if inactive/navigated away for > 120 seconds
    if (lastActiveTime) {
      const elapsed = Date.now() - parseInt(lastActiveTime, 10)
      if (elapsed > 120000) {
        sessionStorage.removeItem('yourbank_active_session')
        sessionStorage.removeItem('yourbank_last_active_time')
        return false
      }
    }

    // Refresh last active timestamp on valid activity
    sessionStorage.setItem('yourbank_last_active_time', Date.now().toString())
    return true
  }

  const isAuthenticated = checkActiveSession()

  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (!requireAuth && isAuthenticated) {
    return <Navigate to="/dashboard/overview" replace />
  }

  return children
}
