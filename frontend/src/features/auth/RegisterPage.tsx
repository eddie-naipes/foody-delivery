import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'

import { applyApiFieldErrors } from '@/shared/api/fieldErrors'
import { toApiError } from '@/shared/api/errors'
import { Alert } from '@/shared/ui/Alert'
import { Button } from '@/shared/ui/Button'
import { TextField } from '@/shared/ui/TextField'

import { AuthLayout } from './AuthLayout'
import { registerSchema, type RegisterValues } from './schemas'
import { useAuth } from './useAuth'

export function RegisterPage() {
  const { register } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { name: '', email: '', password: '' },
  })
  const { errors, isSubmitting } = form.formState

  const onSubmit = form.handleSubmit(async (values) => {
    setError(null)
    try {
      await register(values)
    } catch (cause) {
      const apiError = toApiError(cause)
      if (!applyApiFieldErrors(apiError, form.setError)) setError(apiError.message)
    }
  })

  return (
    <AuthLayout
      title="Criar conta"
      subtitle="Você já entra no sistema assim que terminar."
      footer={
        <>
          Já tem conta?{' '}
          <Link to="/login" className="font-bold text-brand underline-offset-4 hover:underline">
            Entrar
          </Link>
        </>
      }
    >
      <form noValidate onSubmit={onSubmit} className="flex flex-col gap-4">
        {error ? <Alert tone="error">{error}</Alert> : null}
        <TextField label="Nome" autoComplete="name" maxLength={100} error={errors.name?.message} {...form.register('name')} />
        <TextField
          label="E-mail"
          type="email"
          autoComplete="email"
          maxLength={254}
          error={errors.email?.message}
          {...form.register('email')}
        />
        <TextField
          label="Senha"
          type="password"
          autoComplete="new-password"
          hint="Pelo menos 6 caracteres."
          error={errors.password?.message}
          {...form.register('password')}
        />
        <Button type="submit" loading={isSubmitting} className="mt-2 w-full">
          Criar conta
        </Button>
      </form>
    </AuthLayout>
  )
}
