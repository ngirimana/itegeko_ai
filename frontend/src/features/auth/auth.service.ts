"use client";

export type AuthSession = {
  accessToken: string;
  refreshToken?: string;
  expiresAt: number;
  email?: string;
  name?: string;
  roles: string[];
};

const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL || "http://localhost:8081";
const realm = process.env.NEXT_PUBLIC_KEYCLOAK_REALM || "itegeko";
const clientId = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID || "itegeko-frontend";

const storageKeys = {
  session: "itegeko.auth.session",
  codeVerifier: "itegeko.auth.pkce.verifier",
  state: "itegeko.auth.pkce.state"
};

export function getStoredSession(): AuthSession | null {
  const raw = window.localStorage.getItem(storageKeys.session);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthSession;
  } catch {
    window.localStorage.removeItem(storageKeys.session);
    return null;
  }
}

export function clearStoredSession() {
  window.localStorage.removeItem(storageKeys.session);
}

export function isAdmin(session: AuthSession | null): boolean {
  return Boolean(session?.roles.includes("ADMIN"));
}

export function isSessionUsable(session: AuthSession | null): session is AuthSession {
  return Boolean(session && session.expiresAt > Date.now() + 30_000);
}

export async function startLogin() {
  const verifier = randomUrlSafeString(64);
  const state = randomUrlSafeString(32);
  const challenge = await sha256Base64Url(verifier);
  window.localStorage.setItem(storageKeys.codeVerifier, verifier);
  window.localStorage.setItem(storageKeys.state, state);

  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: window.location.origin + window.location.pathname,
    response_type: "code",
    scope: "openid profile email",
    code_challenge: challenge,
    code_challenge_method: "S256",
    state
  });
  window.location.assign(`${keycloakUrl}/realms/${realm}/protocol/openid-connect/auth?${params.toString()}`);
}

export async function completeLoginFromUrl(): Promise<AuthSession | null> {
  const params = new URLSearchParams(window.location.search);
  const code = params.get("code");
  const state = params.get("state");
  if (!code) {
    return null;
  }

  const expectedState = window.localStorage.getItem(storageKeys.state);
  const verifier = window.localStorage.getItem(storageKeys.codeVerifier);
  if (!state || !expectedState || state !== expectedState || !verifier) {
    cleanupPkce();
    throw new Error("Login session could not be verified. Please try signing in again.");
  }

  const tokenResponse = await requestToken({
    grant_type: "authorization_code",
    client_id: clientId,
    code,
    redirect_uri: window.location.origin + window.location.pathname,
    code_verifier: verifier
  });
  cleanupPkce();
  window.history.replaceState({}, document.title, window.location.pathname);
  return storeTokenResponse(tokenResponse);
}

export async function refreshSession(session: AuthSession): Promise<AuthSession | null> {
  if (!session.refreshToken) {
    clearStoredSession();
    return null;
  }
  const tokenResponse = await requestToken({
    grant_type: "refresh_token",
    client_id: clientId,
    refresh_token: session.refreshToken
  });
  return storeTokenResponse(tokenResponse);
}

async function requestToken(values: Record<string, string>): Promise<Record<string, unknown>> {
  const response = await fetch(`${keycloakUrl}/realms/${realm}/protocol/openid-connect/token`, {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams(values)
  });
  const payload = await response.json();
  if (!response.ok) {
    throw new Error(payload?.error_description || payload?.error || "Login failed.");
  }
  return payload;
}

function storeTokenResponse(tokenResponse: Record<string, unknown>): AuthSession {
  const accessToken = String(tokenResponse.access_token || "");
  const refreshToken = tokenResponse.refresh_token ? String(tokenResponse.refresh_token) : undefined;
  const claims = parseJwt(accessToken);
  const roles = Array.isArray(claims.realm_access?.roles) ? claims.realm_access.roles.filter(isString) : [];
  const session: AuthSession = {
    accessToken,
    refreshToken,
    expiresAt: Number(claims.exp || 0) * 1000,
    email: isString(claims.email) ? claims.email : undefined,
    name: isString(claims.name) ? claims.name : undefined,
    roles
  };
  window.localStorage.setItem(storageKeys.session, JSON.stringify(session));
  return session;
}

function parseJwt(token: string): Record<string, any> {
  const [, payload] = token.split(".");
  if (!payload) {
    return {};
  }
  const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
  return JSON.parse(window.atob(padded));
}

function cleanupPkce() {
  window.localStorage.removeItem(storageKeys.codeVerifier);
  window.localStorage.removeItem(storageKeys.state);
}

function isString(value: unknown): value is string {
  return typeof value === "string";
}

function randomUrlSafeString(size: number): string {
  const bytes = new Uint8Array(size);
  window.crypto.getRandomValues(bytes);
  return base64Url(bytes);
}

async function sha256Base64Url(value: string): Promise<string> {
  const bytes = new TextEncoder().encode(value);
  const digest = await window.crypto.subtle.digest("SHA-256", bytes);
  return base64Url(new Uint8Array(digest));
}

function base64Url(bytes: Uint8Array): string {
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return window.btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}
