import { screen } from '@testing-library/react'
import axe from 'axe-core'
import { describe, expect, it } from 'vitest'

import { MESSAGES } from '@/shared/messages'

import { seedOrder, seedUser } from './db'
import { renderApp } from './render'

/**
 * Verificação automática de acessibilidade (axe-core, regras WCAG 2.1 A/AA) em cada tela,
 * nos estados que importam. Contraste de cor não é calculável no jsdom: ele é medido no
 * navegador de verdade, com o Lighthouse (ver README).
 */
async function expectNoViolations() {
  const results = await axe.run(document.body, {
    runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'best-practice'] },
    rules: { 'color-contrast': { enabled: false } },
  })
  const summary = results.violations.map(
    (violation) => `${violation.id}: ${violation.help} (${violation.nodes.map((node) => node.target.join(' ')).join(', ')})`,
  )
  expect(summary).toEqual([])
  // Garante que a página foi de fato analisada (e não um DOM vazio)
  expect(results.passes.length).toBeGreaterThan(10)
}

describe('acessibilidade', () => {
  it('login', async () => {
    renderApp({ route: '/login' })
    await screen.findByRole('heading', { name: 'Entrar' })
    await expectNoViolations()
  })

  it('login com erros nos campos', async () => {
    const { user } = renderApp({ route: '/login' })
    await user.click(await screen.findByRole('button', { name: 'Entrar' }))
    await screen.findByText(MESSAGES.emailRequired)
    await expectNoViolations()
  })

  it('cadastro', async () => {
    renderApp({ route: '/cadastro' })
    await screen.findByRole('heading', { name: 'Criar conta' })
    await expectNoViolations()
  })

  it('lista vazia', async () => {
    renderApp({ route: '/pedidos', loggedAs: seedUser() })
    await screen.findByText('Nenhum pedido por aqui ainda')
    await expectNoViolations()
  })

  it('lista com filtros e paginação', async () => {
    const operator = seedUser()
    for (let i = 1; i <= 13; i++) seedOrder(operator, { status: i % 2 ? 'RECEBIDO' : 'EM_PREPARO' })
    renderApp({ route: '/pedidos', loggedAs: operator })
    await screen.findByRole('navigation', { name: 'Paginação' })
    await expectNoViolations()
  })

  it('novo pedido com erros de validação', async () => {
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: seedUser() })
    await user.click(await screen.findByRole('button', { name: 'Criar pedido' }))
    await screen.findByText(MESSAGES.customerNameRequired)
    await expectNoViolations()
  })

  it('detalhe com histórico e confirmação de cancelamento', async () => {
    const operator = seedUser()
    seedOrder(operator, { status: 'EM_PREPARO' })
    const { user } = renderApp({ route: '/pedidos/1', loggedAs: operator })
    await user.click(await screen.findByRole('button', { name: 'Cancelar pedido' }))
    await screen.findByRole('button', { name: 'Confirmar cancelamento' })
    await expectNoViolations()
  })
})
