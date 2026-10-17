import '@testing-library/jest-dom/vitest'

import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'

import { resetDb } from './db'
import { server } from './server'

// O jsdom não implementa rolagem; o mock permite verificar que a página volta ao topo
window.scrollTo = vi.fn() as unknown as typeof window.scrollTo

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))

afterEach(() => {
  cleanup()
  server.resetHandlers()
  resetDb()
  localStorage.clear()
})

afterAll(() => server.close())
