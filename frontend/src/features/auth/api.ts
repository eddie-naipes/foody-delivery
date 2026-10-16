import { http } from '@/shared/api/http'

import type { LoginValues, RegisterValues } from './schemas'
import type { Session } from './session'

export const authApi = {
  login: (values: LoginValues) => http.post<Session>('/auth/login', values).then((response) => response.data),
  register: (values: RegisterValues) =>
    http.post<Session>('/auth/register', values).then((response) => response.data),
}
