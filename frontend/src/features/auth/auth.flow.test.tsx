import { screen } from '@testing-library/react'
import { http } from 'msw'
import { describe, expect, it } from 'vitest'

import { MESSAGES } from '@/shared/messages'
import { seedUser } from '@/test/db'
import { renderApp } from '@/test/render'
import { apiError, server } from '@/test/server'

describe('autenticação', () => {
  it('manda para o login quem tenta abrir os pedidos sem sessão', async () => {
    renderApp({ route: '/pedidos' })

    expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('mostra o erro da API quando a senha está errada', async () => {
    seedUser()
    const { user } = renderApp({ route: '/login' })

    await user.type(screen.getByLabelText('E-mail'), 'op@foody.com')
    await user.type(screen.getByLabelText('Senha'), 'errada')
    await user.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha inválidos')
  })

  it('valida os campos antes de chamar a API', async () => {
    const { user } = renderApp({ route: '/login' })

    await user.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByText(MESSAGES.emailRequired)).toBeInTheDocument()
    expect(screen.getByText(MESSAGES.passwordRequired)).toBeInTheDocument()
  })

  it('entra e leva para a lista de pedidos', async () => {
    seedUser({ name: 'Joana' })
    const { user } = renderApp({ route: '/login' })

    await user.type(screen.getByLabelText('E-mail'), '  OP@foody.com ')
    await user.type(screen.getByLabelText('Senha'), 'secret123')
    await user.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('heading', { name: 'Pedidos' })).toBeInTheDocument()
    expect(screen.getByText('Joana')).toBeInTheDocument()
  })

  it('cria a conta e já entra', async () => {
    const { user } = renderApp({ route: '/cadastro' })

    await user.type(screen.getByLabelText('Nome'), 'Carlos')
    await user.type(screen.getByLabelText('E-mail'), 'carlos@foody.com')
    await user.type(screen.getByLabelText('Senha'), 'secret123')
    await user.click(screen.getByRole('button', { name: 'Criar conta' }))

    expect(await screen.findByRole('heading', { name: 'Pedidos' })).toBeInTheDocument()
    expect(screen.getByText('Carlos')).toBeInTheDocument()
  })

  it('mostra no campo o erro de e-mail já cadastrado', async () => {
    seedUser({ email: 'carlos@foody.com' })
    const { user } = renderApp({ route: '/cadastro' })

    await user.type(screen.getByLabelText('Nome'), 'Carlos')
    await user.type(screen.getByLabelText('E-mail'), 'carlos@foody.com')
    await user.type(screen.getByLabelText('Senha'), 'secret123')
    await user.click(screen.getByRole('button', { name: 'Criar conta' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('E-mail já cadastrado')
  })

  it('leva os erros de campo da API para o formulário', async () => {
    server.use(
      http.post('*/api/auth/register', () =>
        apiError(400, 'Dados inválidos', [{ field: 'email', message: 'E-mail inválido' }]),
      ),
    )
    const { user } = renderApp({ route: '/cadastro' })

    await user.type(screen.getByLabelText('Nome'), 'Carlos')
    await user.type(screen.getByLabelText('E-mail'), 'carlos@foody.com')
    await user.type(screen.getByLabelText('Senha'), 'secret123')
    await user.click(screen.getByRole('button', { name: 'Criar conta' }))

    expect(await screen.findByText('E-mail inválido')).toBeInTheDocument()
    expect(screen.getByLabelText('E-mail')).toHaveAttribute('aria-invalid', 'true')
  })

  it('sai da conta', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos', loggedAs: operator })

    await user.click(await screen.findByRole('button', { name: 'Sair' }))

    expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('volta ao login avisando quando a sessão expira', async () => {
    const operator = seedUser()
    server.use(http.get('*/api/orders', () => apiError(401, 'Autenticação necessária')))
    renderApp({ route: '/pedidos', loggedAs: operator })

    expect(await screen.findByRole('heading', { name: 'Entrar' })).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent(MESSAGES.sessionExpired)
  })

  it('não mostra o login para quem já está logado', async () => {
    const operator = seedUser()
    renderApp({ route: '/login', loggedAs: operator })

    expect(await screen.findByRole('heading', { name: 'Pedidos' })).toBeInTheDocument()
  })
})
