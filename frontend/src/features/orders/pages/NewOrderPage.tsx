import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useFieldArray, useForm, useWatch, type Control } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'

import { toApiError } from '@/shared/api/errors'
import { applyApiFieldErrors } from '@/shared/api/fieldErrors'
import { parseMoney } from '@/shared/lib/format'
import { Alert } from '@/shared/ui/Alert'
import { Button } from '@/shared/ui/Button'
import { TextField } from '@/shared/ui/TextField'

import { addressLines } from '../address'
import { Receipt, type ReceiptLine } from '../components/Receipt'
import { useCreateOrder } from '../hooks'
import { emptyItem, emptyOrderForm, orderFormSchema, toCreateOrderInput, type OrderFormValues } from '../schemas'

export function NewOrderPage() {
  const navigate = useNavigate()
  const createOrder = useCreateOrder()
  const [error, setError] = useState<string | null>(null)

  const form = useForm<OrderFormValues>({ resolver: zodResolver(orderFormSchema), defaultValues: emptyOrderForm() })
  const items = useFieldArray({ control: form.control, name: 'items' })
  const { errors } = form.formState

  const onSubmit = form.handleSubmit(async (values) => {
    setError(null)
    try {
      const order = await createOrder.mutateAsync(toCreateOrderInput(values))
      navigate(`/pedidos/${order.id}`, { state: { notice: 'Pedido criado' } })
    } catch (cause) {
      const apiError = toApiError(cause)
      if (!applyApiFieldErrors(apiError, form.setError)) setError(apiError.message)
    }
  })

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Link to="/pedidos" className="text-[15px] text-muted underline-offset-4 hover:underline">
          Voltar para pedidos
        </Link>
        <h1 className="mt-2 text-[36px] leading-tight font-bold tracking-tight">Novo pedido</h1>
      </div>

      <div className="grid items-start gap-8 lg:grid-cols-[minmax(0,1fr)_380px]">
        <form noValidate onSubmit={onSubmit} className="flex flex-col gap-8">
          {error ? <Alert tone="error">{error}</Alert> : null}

          <fieldset className="flex flex-col gap-4">
            <legend className="mb-3 text-[18px] font-bold">Cliente</legend>
            <div className="grid gap-4 sm:grid-cols-2">
              <TextField
                label="Nome do cliente"
                autoComplete="off"
                error={errors.customerName?.message}
                {...form.register('customerName')}
              />
              <TextField
                label="Telefone"
                type="tel"
                inputMode="tel"
                autoComplete="off"
                error={errors.customerPhone?.message}
                {...form.register('customerPhone')}
              />
            </div>
          </fieldset>

          <fieldset className="flex flex-col gap-4">
            <legend className="mb-3 text-[18px] font-bold">Endereço de entrega</legend>
            <div className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_120px]">
              <TextField label="Rua" error={errors.deliveryAddress?.street?.message} {...form.register('deliveryAddress.street')} />
              <TextField label="Número" error={errors.deliveryAddress?.number?.message} {...form.register('deliveryAddress.number')} />
            </div>
            <TextField
              label="Complemento"
              hint="Opcional. Apartamento, bloco, ponto de referência."
              {...form.register('deliveryAddress.complement')}
            />
            <div className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_140px]">
              <TextField
                label="Bairro"
                error={errors.deliveryAddress?.neighborhood?.message}
                {...form.register('deliveryAddress.neighborhood')}
              />
              <TextField label="Cidade" error={errors.deliveryAddress?.city?.message} {...form.register('deliveryAddress.city')} />
              <TextField
                label="CEP"
                inputMode="numeric"
                placeholder="00000-000"
                maxLength={9}
                error={errors.deliveryAddress?.zipCode?.message}
                {...form.register('deliveryAddress.zipCode')}
              />
            </div>
          </fieldset>

          <fieldset className="flex flex-col gap-4">
            <legend className="mb-3 text-[18px] font-bold">Itens</legend>
            {(errors.items?.root?.message ?? errors.items?.message) ? (
              <Alert tone="error">{errors.items?.root?.message ?? errors.items?.message}</Alert>
            ) : null}

            <ol className="flex flex-col gap-4">
              {items.fields.map((field, index) => {
                const position = index + 1
                const itemErrors = errors.items?.[index]
                return (
                  <li key={field.id} className="flex flex-col gap-2">
                    <div className="grid grid-cols-[minmax(0,1fr)_80px_120px] items-start gap-3">
                      <TextField
                        label={`Item ${position}`}
                        placeholder="Ex.: Pizza margherita"
                        error={itemErrors?.name?.message}
                        {...form.register(`items.${index}.name`)}
                      />
                      <TextField
                        label="Qtd."
                        aria-label={`Quantidade do item ${position}`}
                        inputMode="numeric"
                        error={itemErrors?.quantity?.message}
                        {...form.register(`items.${index}.quantity`)}
                      />
                      <TextField
                        label="Preço"
                        aria-label={`Preço unitário do item ${position}`}
                        inputMode="decimal"
                        placeholder="0,00"
                        error={itemErrors?.unitPrice?.message}
                        {...form.register(`items.${index}.unitPrice`)}
                      />
                    </div>
                    {itemErrors?.message ? <p className="text-[13px] text-danger">{itemErrors.message}</p> : null}
                    {items.fields.length > 1 ? (
                      <Button
                        variant="ghost"
                        className="h-8 self-start px-2 text-[13px] text-muted"
                        aria-label={`Remover item ${position}`}
                        onClick={() => items.remove(index)}
                      >
                        Remover
                      </Button>
                    ) : null}
                  </li>
                )
              })}
            </ol>
            <Button variant="secondary" className="self-start" onClick={() => items.append(emptyItem())}>
              Adicionar item
            </Button>
          </fieldset>

          <div className="flex flex-wrap gap-3 border-t border-ink/10 pt-6">
            <Button type="submit" loading={form.formState.isSubmitting}>
              Criar pedido
            </Button>
            <Link to="/pedidos" className="inline-flex h-10 items-center px-2 text-[15px] text-muted hover:text-ink">
              Descartar
            </Link>
          </div>
        </form>

        <aside className="lg:sticky lg:top-6">
          <LiveReceipt control={form.control} />
        </aside>
      </div>
    </div>
  )
}

/** Comanda que se monta enquanto o operador digita. Itens incompletos ainda não entram. */
function LiveReceipt({ control }: { control: Control<OrderFormValues> }) {
  const values = useWatch({ control }) as OrderFormValues

  const lines: ReceiptLine[] = (values.items ?? []).flatMap((item, index) => {
    const quantity = Number(item.quantity)
    const unitPrice = parseMoney(item.unitPrice ?? '')
    const valid = item.name?.trim() && Number.isInteger(quantity) && quantity > 0 && unitPrice > 0
    return valid ? [{ key: index, name: item.name.trim(), quantity, unitPrice }] : []
  })
  const total = lines.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0)

  return (
    <Receipt
      customerName={values.customerName?.trim() ?? ''}
      customerPhone={values.customerPhone?.trim()}
      addressLines={addressLines(values.deliveryAddress ?? {})}
      lines={lines}
      total={total}
      animateLines
    />
  )
}
