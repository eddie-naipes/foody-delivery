import { screen, within } from '@testing-library/react'
import { http } from 'msw'
import { describe, expect, it } from 'vitest'

import { MESSAGES } from '@/shared/messages'
import { db, seedOrder, seedUser } from '@/test/db'
import { renderApp } from '@/test/render'
import { apiError, server } from '@/test/server'

describe('lista de pedidos', () => {
  it('convida a criar o primeiro pedido quando a lista está vazia', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos', loggedAs: operator })

    expect(await screen.findByText('Nenhum pedido por aqui ainda')).toBeInTheDocument()
    await user.click(screen.getByRole('link', { name: 'Criar primeiro pedido' }))
    expect(await screen.findByRole('heading', { name: 'Novo pedido' })).toBeInTheDocument()
  })

  it('mostra cada pedido com número, cliente, total e status atual', async () => {
    const operator = seedUser()
    seedOrder(operator, { customerName: 'Maria Silva', status: 'EM_PREPARO' })
    renderApp({ route: '/pedidos', loggedAs: operator })

    const ticket = await screen.findByRole('link', { name: /#0001/ })
    expect(within(ticket).getByText('Maria Silva')).toBeInTheDocument()
    expect(within(ticket).getByText('Em preparo')).toBeInTheDocument()
    expect(within(ticket).getByText(/99,90/)).toBeInTheDocument()
    expect(within(ticket).getByText('Bela Vista')).toBeInTheDocument()
  })

  it('conta e filtra os pedidos por status', async () => {
    const operator = seedUser()
    seedOrder(operator, { customerName: 'Ana', status: 'RECEBIDO' })
    seedOrder(operator, { customerName: 'Bruno', status: 'ENTREGUE' })
    seedOrder(operator, { customerName: 'Caio', status: 'ENTREGUE' })
    const { user } = renderApp({ route: '/pedidos', loggedAs: operator })

    await screen.findByText('Ana')
    expect(screen.getByRole('button', { name: /Todos\s*3/ })).toHaveAttribute('aria-pressed', 'true')

    await user.click(screen.getByRole('button', { name: /Entregue\s*2/ }))

    expect(screen.queryByText('Ana')).not.toBeInTheDocument()
    expect(screen.getByText('Bruno')).toBeInTheDocument()
    expect(screen.getByText('Caio')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /Cancelado\s*0/ }))
    expect(screen.getByText('Nenhum pedido cancelado.')).toBeInTheDocument()
  })

  it('explica quando a lista não carrega', async () => {
    const operator = seedUser()
    server.use(http.get('*/api/orders', () => apiError(500, 'Erro interno do servidor')))
    renderApp({ route: '/pedidos', loggedAs: operator })

    expect(await screen.findByRole('alert')).toHaveTextContent('Erro interno do servidor')
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeInTheDocument()
  })
})

describe('novo pedido', () => {
  async function fillCustomerAndAddress(user: ReturnType<typeof renderApp>['user']) {
    await user.type(await screen.findByLabelText('Nome do cliente'), 'Maria Silva')
    await user.type(screen.getByLabelText('Telefone'), '11999998888')
    await user.type(screen.getByLabelText('Rua'), 'Av. Paulista')
    await user.type(screen.getByLabelText('Número'), '1000')
    await user.type(screen.getByLabelText('Bairro'), 'Bela Vista')
    await user.type(screen.getByLabelText('Cidade'), 'São Paulo')
    await user.type(screen.getByLabelText('CEP'), '01310-100')
  }

  it('aponta os campos obrigatórios sem chamar a API', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: operator })

    await user.click(await screen.findByRole('button', { name: 'Criar pedido' }))

    expect(await screen.findByText(MESSAGES.customerNameRequired)).toBeInTheDocument()
    expect(screen.getByText(MESSAGES.zipCodeInvalid)).toBeInTheDocument()
    expect(screen.getByText(MESSAGES.itemNameRequired)).toBeInTheDocument()
    expect(db.orders).toHaveLength(0)
  })

  it('monta a comanda e o total enquanto o operador digita', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: operator })

    await user.type(await screen.findByLabelText('Item 1'), 'Pizza')
    await user.clear(screen.getByLabelText('Quantidade do item 1'))
    await user.type(screen.getByLabelText('Quantidade do item 1'), '2')
    await user.type(screen.getByLabelText('Preço unitário do item 1'), '45,00')
    await user.click(screen.getByRole('button', { name: 'Adicionar item' }))
    await user.type(screen.getByLabelText('Item 2'), 'Refrigerante')
    await user.type(screen.getByLabelText('Preço unitário do item 2'), '9,90')

    const receipt = screen.getByRole('region', { name: 'Comanda' })
    expect(within(receipt).getByText('Pizza')).toBeInTheDocument()
    expect(within(receipt).getByTestId('receipt-total')).toHaveTextContent('99,90')
  })

  it('deixa fora da comanda o item com valor inválido', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: operator })

    await user.type(await screen.findByLabelText('Item 1'), 'Pizza')
    await user.type(screen.getByLabelText('Preço unitário do item 1'), '45,00')
    await user.click(screen.getByRole('button', { name: 'Adicionar item' }))
    await user.type(screen.getByLabelText('Item 2'), 'Refrigerante')
    // Preço zerado: a máscara aceita os dígitos, mas a regra exige valor maior que zero
    await user.type(screen.getByLabelText('Preço unitário do item 2'), '0,00')

    const receipt = screen.getByRole('region', { name: 'Comanda' })
    expect(within(receipt).queryByText('Refrigerante')).not.toBeInTheDocument()
    expect(within(receipt).getByTestId('receipt-total')).toHaveTextContent('45,00')
  })

  it('não deixa digitar letras em telefone, CEP, quantidade e preço', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: operator })

    await user.type(await screen.findByLabelText('Telefone'), 'tel 11a99999b8888')
    await user.type(screen.getByLabelText('CEP'), '01310abc100')
    await user.clear(screen.getByLabelText('Quantidade do item 1'))
    await user.type(screen.getByLabelText('Quantidade do item 1'), 'x2y')
    await user.type(screen.getByLabelText('Preço unitário do item 1'), 'R$ 45,999')

    expect(screen.getByLabelText('Telefone')).toHaveValue('(11) 99999-8888')
    expect(screen.getByLabelText('CEP')).toHaveValue('01310-100')
    expect(screen.getByLabelText('Quantidade do item 1')).toHaveValue('2')
    expect(screen.getByLabelText('Preço unitário do item 1')).toHaveValue('45,99')
  })

  it('remove um item da lista', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: operator })

    await user.click(await screen.findByRole('button', { name: 'Adicionar item' }))
    expect(screen.getByLabelText('Item 2')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Remover item 2' }))
    expect(screen.queryByLabelText('Item 2')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Remover item 1' })).not.toBeInTheDocument()
  })

  it('cria o pedido e abre a comanda dele', async () => {
    const operator = seedUser()
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: operator })

    await fillCustomerAndAddress(user)
    await user.type(screen.getByLabelText('Item 1'), 'Pizza')
    await user.type(screen.getByLabelText('Preço unitário do item 1'), '45,00')
    await user.click(screen.getByRole('button', { name: 'Criar pedido' }))

    expect(await screen.findByRole('heading', { name: 'Pedido #0001' })).toBeInTheDocument()
    expect(screen.getByRole('status')).toHaveTextContent('Pedido criado')
    expect(db.orders[0]?.deliveryAddress.zipCode).toBe('01310100')
    expect(db.orders[0]?.items[0]).toMatchObject({ name: 'Pizza', quantity: 1, unitPrice: 45 })
  })

  it('leva os erros de campo da API para os campos certos', async () => {
    const operator = seedUser()
    server.use(
      http.post('*/api/orders', () =>
        apiError(400, 'Dados inválidos', [
          { field: 'items[0].quantity', message: 'Quantidade deve ser no mínimo 1' },
          { field: 'deliveryAddress.zipCode', message: 'CEP deve ter 8 dígitos' },
        ]),
      ),
    )
    const { user } = renderApp({ route: '/pedidos/novo', loggedAs: operator })

    await fillCustomerAndAddress(user)
    await user.type(screen.getByLabelText('Item 1'), 'Pizza')
    await user.type(screen.getByLabelText('Preço unitário do item 1'), '45,00')
    await user.click(screen.getByRole('button', { name: 'Criar pedido' }))

    expect(await screen.findByText('Quantidade deve ser no mínimo 1')).toBeInTheDocument()
    expect(screen.getByText('CEP deve ter 8 dígitos')).toBeInTheDocument()
    expect(screen.getByLabelText('CEP')).toHaveAttribute('aria-invalid', 'true')
  })
})

describe('detalhe do pedido', () => {
  it('avança o pedido pelas etapas até a entrega', async () => {
    const operator = seedUser()
    seedOrder(operator)
    const { user } = renderApp({ route: '/pedidos/1', loggedAs: operator })

    await user.click(await screen.findByRole('button', { name: 'Iniciar preparo' }))
    expect(await screen.findByRole('status')).toHaveTextContent('Preparo iniciado')

    await user.click(screen.getByRole('button', { name: 'Enviar para entrega' }))
    await user.click(await screen.findByRole('button', { name: 'Marcar como entregue' }))

    expect(await screen.findByText('Pedido marcado como entregue')).toBeInTheDocument()
    expect(db.orders[0]?.status).toBe('ENTREGUE')
    expect(screen.queryByRole('button', { name: 'Cancelar pedido' })).not.toBeInTheDocument()
  })

  it('pede confirmação antes de cancelar', async () => {
    const operator = seedUser()
    seedOrder(operator)
    const { user } = renderApp({ route: '/pedidos/1', loggedAs: operator })

    await user.click(await screen.findByRole('button', { name: 'Cancelar pedido' }))
    await user.click(screen.getByRole('button', { name: 'Manter pedido' }))
    expect(db.orders[0]?.status).toBe('RECEBIDO')

    await user.click(screen.getByRole('button', { name: 'Cancelar pedido' }))
    await user.click(screen.getByRole('button', { name: 'Confirmar cancelamento' }))

    expect(await screen.findByText('Pedido cancelado')).toBeInTheDocument()
    expect(db.orders[0]?.status).toBe('CANCELADO')
    // O trilho já diz que foi cancelado: sem uma terceira mensagem repetindo o mesmo
    expect(screen.getByText('Este pedido foi cancelado.')).toBeInTheDocument()
    expect(screen.queryByText(/Não há mais etapas/)).not.toBeInTheDocument()
  })

  it('recarrega o pedido quando outra pessoa já mudou o status', async () => {
    const operator = seedUser()
    const order = seedOrder(operator, { status: 'EM_PREPARO' })
    const { user } = renderApp({ route: '/pedidos/1', loggedAs: operator })
    await screen.findByRole('button', { name: 'Enviar para entrega' })

    // Outro operador avança o pedido enquanto esta tela está aberta
    order.status = 'SAIU_PARA_ENTREGA'
    await user.click(screen.getByRole('button', { name: 'Enviar para entrega' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(MESSAGES.orderChangedElsewhere)
    expect(await screen.findByRole('button', { name: 'Marcar como entregue' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Pedido #0001' }).parentElement).toHaveTextContent('Saiu para entrega')
  })

  it('avisa quando o pedido não existe', async () => {
    const operator = seedUser()
    renderApp({ route: '/pedidos/999', loggedAs: operator })

    expect(await screen.findByRole('alert')).toHaveTextContent('Pedido 999 não encontrado')
    expect(screen.getByRole('link', { name: 'Voltar para pedidos' })).toBeInTheDocument()
  })
})
