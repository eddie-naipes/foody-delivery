import { z } from 'zod'

import { MESSAGES } from '@/shared/messages'

const email = z.string().trim().min(1, MESSAGES.emailRequired).pipe(z.email(MESSAGES.emailInvalid))

/** O BCrypt do back-end aceita até 72 bytes; acentos ocupam 2 bytes em UTF-8. */
const utf8Bytes = (value: string) => new TextEncoder().encode(value).length

export const loginSchema = z.object({
  email,
  password: z.string().min(1, MESSAGES.passwordRequired),
})

export const registerSchema = z.object({
  name: z.string().trim().min(1, MESSAGES.nameRequired).max(100, MESSAGES.nameTooLong),
  email,
  password: z
    .string()
    .min(6, MESSAGES.passwordTooShort)
    .refine((value) => utf8Bytes(value) <= 72, MESSAGES.passwordTooLong),
})

export type LoginValues = z.infer<typeof loginSchema>
export type RegisterValues = z.infer<typeof registerSchema>
