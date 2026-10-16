import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'

import { AppProviders } from '@/app/AppProviders'
import { AppRoutes } from '@/app/AppRoutes'
import { saveSession } from '@/features/auth/session'

import { tokenFor, type TestUser } from './db'

interface Options {
  route?: string
  /** Usuário já logado (sessão gravada no localStorage antes de montar a aplicação). */
  loggedAs?: TestUser
}

/** Monta a aplicação inteira numa rota, como o usuário a veria. */
export function renderApp({ route = '/', loggedAs }: Options = {}) {
  if (loggedAs) {
    saveSession({
      token: tokenFor(loggedAs),
      user: { id: loggedAs.id, name: loggedAs.name, email: loggedAs.email },
    })
  }
  const user = userEvent.setup()
  const view = render(
    <MemoryRouter initialEntries={[route]}>
      <AppProviders>
        <AppRoutes />
      </AppProviders>
    </MemoryRouter>,
  )
  return { user, ...view }
}
