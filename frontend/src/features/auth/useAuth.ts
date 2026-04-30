"use client";

import { useCallback, useEffect, useState } from "react";
import {
  type AuthSession,
  clearStoredSession,
  completeLoginFromUrl,
  getStoredSession,
  isSessionUsable,
  refreshSession,
  startLogin
} from "./auth.service";

export function useAuth() {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;
    async function initialize() {
      setLoading(true);
      setError("");
      try {
        const completed = await completeLoginFromUrl();
        const stored = completed ?? getStoredSession();
        if (mounted) {
          setSession(stored);
        }
      } catch (caught) {
        if (mounted) {
          setError(caught instanceof Error ? caught.message : "Login failed.");
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }
    initialize();
    return () => {
      mounted = false;
    };
  }, []);

  const login = useCallback(() => startLogin(), []);

  const logout = useCallback(() => {
    clearStoredSession();
    setSession(null);
  }, []);

  const getAccessToken = useCallback(async () => {
    const current = session ?? getStoredSession();
    if (!current) {
      return null;
    }
    if (isSessionUsable(current)) {
      return current.accessToken;
    }
    const refreshed = await refreshSession(current);
    setSession(refreshed);
    return refreshed?.accessToken ?? null;
  }, [session]);

  return {
    session,
    loading,
    error,
    login,
    logout,
    getAccessToken
  };
}
