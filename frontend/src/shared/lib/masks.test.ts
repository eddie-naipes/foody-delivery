import { describe, expect, it } from 'vitest'

import { maskDigits, maskMoney, maskPhone, maskZipCode } from './masks'

describe('máscaras de digitação', () => {
  it('telefone: só dígitos, com DDD, formatado enquanto digita', () => {
    expect(maskPhone('abc')).toBe('')
    expect(maskPhone('1')).toBe('(1')
    expect(maskPhone('11')).toBe('(11')
    expect(maskPhone('119')).toBe('(11) 9')
    expect(maskPhone('1133334444')).toBe('(11) 3333-4444')
    expect(maskPhone('11999998888')).toBe('(11) 99999-8888')
    expect(maskPhone('(11) 99999-8888 ramal 2')).toBe('(11) 99999-8888')
    expect(maskPhone('11a9b9c9d9e9f8g8h8i8')).toBe('(11) 99999-8888')
  })

  it('CEP: só dígitos, com hífen depois do quinto', () => {
    expect(maskZipCode('01310')).toBe('01310')
    expect(maskZipCode('013101')).toBe('01310-1')
    expect(maskZipCode('01310abc100')).toBe('01310-100')
    expect(maskZipCode('0131-0100')).toBe('01310-100')
    expect(maskZipCode('013101009999')).toBe('01310-100')
  })

  it('quantidade: só dígitos e com limite de tamanho', () => {
    expect(maskDigits('a2b', 3)).toBe('2')
    expect(maskDigits('12345', 3)).toBe('123')
    expect(maskDigits('-1', 3)).toBe('1')
  })

  it('preço: dígitos e uma vírgula, com no máximo 2 casas', () => {
    expect(maskMoney('R$ 45,90')).toBe('45,90')
    expect(maskMoney('45.9')).toBe('45,9')
    expect(maskMoney('45,999')).toBe('45,99')
    expect(maskMoney('4,5,6')).toBe('4,56')
    expect(maskMoney('abc')).toBe('')
    expect(maskMoney(',5')).toBe('0,5')
  })
})
