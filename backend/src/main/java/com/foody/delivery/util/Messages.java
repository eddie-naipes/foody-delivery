package com.foody.delivery.util;

/**
 * Todos os textos devolvidos pela API em um só lugar. As constantes são usadas pelas
 * anotações de validação, pelas exceções e pelos testes, para que mensagem e
 * asserção nunca divirjam. Mensagens com {@code %s}/{@code %d} são preenchidas com
 * {@link String#formatted}.
 */
public final class Messages {

	// --- Validação: usuário ---
	public static final String NAME_REQUIRED = "Nome é obrigatório";
	public static final String NAME_MAX_LENGTH = "Nome deve ter no máximo 100 caracteres";
	public static final String EMAIL_REQUIRED = "E-mail é obrigatório";
	public static final String EMAIL_INVALID = "E-mail inválido";
	public static final String PASSWORD_REQUIRED = "Senha é obrigatória";
	public static final String PASSWORD_MIN_LENGTH = "Senha deve ter no mínimo 6 caracteres";
	public static final String PASSWORD_TOO_LONG = "Senha muito longa (máximo de 72 bytes)";

	// --- Validação: pedido ---
	public static final String CUSTOMER_NAME_REQUIRED = "Nome do cliente é obrigatório";
	public static final String CUSTOMER_PHONE_REQUIRED = "Telefone do cliente é obrigatório";
	public static final String CUSTOMER_PHONE_INVALID = "Telefone com DDD: 10 ou 11 dígitos";
	public static final String DELIVERY_ADDRESS_REQUIRED = "Endereço de entrega é obrigatório";
	public static final String ITEMS_REQUIRED = "O pedido deve ter ao menos um item";
	public static final String ITEM_REQUIRED = "Item não pode ser nulo";
	public static final String ITEM_NAME_REQUIRED = "Nome do item é obrigatório";
	public static final String QUANTITY_REQUIRED = "Quantidade é obrigatória";
	public static final String QUANTITY_MIN = "Quantidade deve ser no mínimo 1";
	public static final String UNIT_PRICE_REQUIRED = "Preço unitário é obrigatório";
	public static final String UNIT_PRICE_POSITIVE = "Preço unitário deve ser maior que zero";
	public static final String UNIT_PRICE_DECIMALS = "Preço unitário deve ter no máximo 2 casas decimais";
	public static final String STATUS_REQUIRED = "Status é obrigatório";

	// --- Validação: paginação ---
	public static final String PAGE_MIN = "Página deve ser 0 ou maior";
	public static final String PAGE_SIZE_RANGE = "Tamanho da página deve ser entre 1 e 100";

	// --- Validação: endereço ---
	public static final String STREET_REQUIRED = "Logradouro é obrigatório";
	public static final String NUMBER_REQUIRED = "Número é obrigatório";
	public static final String NEIGHBORHOOD_REQUIRED = "Bairro é obrigatório";
	public static final String CITY_REQUIRED = "Cidade é obrigatória";
	public static final String ZIP_CODE_REQUIRED = "CEP é obrigatório";
	public static final String ZIP_CODE_INVALID = "CEP deve ter 8 dígitos";

	// --- Erros da API ---
	public static final String VALIDATION_FAILED = "Dados inválidos";
	public static final String MALFORMED_REQUEST = "Corpo da requisição inválido";
	public static final String INVALID_ENUM_VALUE = "Valor inválido para '%s'. Valores aceitos: %s";
	public static final String INVALID_PARAMETER = "Parâmetro inválido: %s";
	public static final String AUTHENTICATION_REQUIRED = "Autenticação necessária";
	public static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos";
	public static final String USER_NOT_FOUND = "Usuário não encontrado";
	public static final String EMAIL_ALREADY_IN_USE = "E-mail já cadastrado";
	public static final String ORDER_NOT_FOUND = "Pedido %d não encontrado";
	public static final String INVALID_STATUS_TRANSITION = "Transição de %s para %s não permitida";
	public static final String RESOURCE_NOT_FOUND = "Recurso não encontrado";
	public static final String METHOD_NOT_ALLOWED = "Método não permitido";
	public static final String BAD_REQUEST = "Requisição inválida";
	public static final String INTERNAL_ERROR = "Erro interno do servidor";

	private Messages() {
	}

}
