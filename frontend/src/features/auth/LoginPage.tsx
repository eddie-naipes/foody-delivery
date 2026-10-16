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
import { loginSchema, type LoginValues } from './schemas'
import { useAuth } from './useAuth'

export function LoginPage() {
  const { login, notice } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const form = useForm<LoginValues>({ resolver: zodResolver(loginSchema), defaultValues: { email: '', password: '' } })
  const { errors, isSubmitting } = form.formState

  const onSubmit = form.handleSubmit(async (values) => {
    setError(null)
    try {
      await login(values)
    } catch (cause) {
      const apiError = toApiError(cause)
      if (!applyApiFieldErrors(apiError, form.setError)) setError(apiError.message)
    }
  })

  return (
    <AuthLayout
      title="Entrar"
      subtitle="Use o e-mail e a senha da sua conta de operador."
      footer={
        <>
          Ainda não tem conta?{' '}
          <Link to="/cadastro" className="font-semibold text-ink underline underline-offset-4">
            Criar conta
          </Link>
        </>
      }
    >
      <form noValidate onSubmit={onSubmit} className="flex flex-col gap-4">
        {notice && !error ? <Alert tone="info">{notice}</Alert> : null}
        {error ? <Alert tone="error">{error}</Alert> : null}
        <TextField
          label="E-mail"
          type="email"
          autoComplete="email"
          error={errors.email?.message}
          {...form.register('email')}
        />
        <TextField
          label="Senha"
          type="password"
          autoComplete="current-password"
          error={errors.password?.message}
          {...form.register('password')}
        />
        <Button type="submit" loading={isSubmitting} className="mt-2 w-full">
          Entrar
        </Button>
      </form>
    </AuthLayout>
  )
}
