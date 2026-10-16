/**
 * Textos de erro e validação do front-end em um só lugar. Mensagens vindas da API
 * são exibidas como chegam; estas cobrem o que o navegador valida ou detecta sozinho.
 */
export const MESSAGES = {
  networkError: 'Não foi possível falar com o servidor. Verifique a conexão e tente de novo.',
  unexpectedError: 'Algo deu errado do nosso lado. Tente de novo em instantes.',
  sessionExpired: 'Sua sessão expirou. Entre de novo para continuar.',
  orderChangedElsewhere: 'Este pedido foi atualizado por outra pessoa. A tela já mostra a etapa atual; confira antes de continuar.',

  nameRequired: 'Informe seu nome',
  nameTooLong: 'Use no máximo 100 caracteres',
  emailRequired: 'Informe o e-mail',
  emailInvalid: 'E-mail inválido',
  passwordRequired: 'Informe a senha',
  passwordTooShort: 'A senha precisa ter pelo menos 6 caracteres',
  passwordTooLong: 'Senha muito longa (máximo de 72 bytes)',

  customerNameRequired: 'Informe o nome do cliente',
  customerPhoneRequired: 'Informe o telefone do cliente',
  streetRequired: 'Informe a rua',
  numberRequired: 'Informe o número',
  neighborhoodRequired: 'Informe o bairro',
  cityRequired: 'Informe a cidade',
  zipCodeInvalid: 'CEP deve ter 8 dígitos (ex.: 01310-100)',
  itemsRequired: 'Adicione ao menos um item',
  itemNameRequired: 'Informe o item',
  quantityInvalid: 'Mínimo 1',
  unitPriceInvalid: 'Preço maior que zero',
  unitPriceDecimals: 'Use no máximo 2 casas decimais',
} as const
