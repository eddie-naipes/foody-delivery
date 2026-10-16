import { describe, expect, it } from 'vitest'

import { loginSchema, registerSchema } from './schemas'

describe('cadastro', () => {
  const valid = { name: 'Operador', email: 'op@foody.com', password: 'secret123' }

  it('aceita dados válidos', () => {
    expect(registerSchema.safeParse(valid).success).toBe(true)
  })

  it('exige senha de 6 a 72 bytes, como o back-end', () => {
    expect(registerSchema.safeParse({ ...valid, password: '12345' }).success).toBe(false)
    expect(registerSchema.safeParse({ ...valid, password: 'a'.repeat(72) }).success).toBe(true)
    // 37 caracteres acentuados = 74 bytes: passa no limite de caracteres, mas não no do BCrypt
    expect(registerSchema.safeParse({ ...valid, password: 'á'.repeat(37) }).success).toBe(false)
  })

  it('rejeita e-mail inválido e nome vazio', () => {
    const result = registerSchema.safeParse({ name: ' ', email: 'nao-e-email', password: 'secret123' })
    expect(result.success).toBe(false)
    expect(result.error?.issues.map((i) => i.path[0])).toEqual(['name', 'email'])
  })
})

describe('login', () => {
  it('aceita e-mail com espaços, que a API normaliza', () => {
    expect(loginSchema.safeParse({ email: '  OP@foody.com ', password: 'x' }).success).toBe(true)
  })

  it('exige senha', () => {
    expect(loginSchema.safeParse({ email: 'op@foody.com', password: '' }).success).toBe(false)
  })
})
