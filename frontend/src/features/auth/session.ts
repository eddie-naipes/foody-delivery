export interface SessionUser {
  id: number
  name: string
  email: string
}

export interface Session {
  token: string
  user: SessionUser
}

const STORAGE_KEY = 'foody.session'

/** O armazenamento pode estar indisponível (modo privado, bloqueio do navegador): nunca deixa a app quebrar. */
export function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as Session) : null
  } catch {
    return null
  }
}

export function saveSession(session: Session) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } catch {
    // segue só com a sessão em memória
  }
}

export function clearSession() {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    // nada a limpar
  }
}
