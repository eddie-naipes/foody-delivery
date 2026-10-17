package com.foody.delivery.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class OrderRequestNormalizationTest {

	@Test
	void addressRequestTrimsFieldsAndRemovesZipCodeHyphen() {
		AddressRequest address = new AddressRequest(" Av. Paulista ", " 1000 ", " Apto 12 ", " Bela Vista ",
				" São Paulo ", " 01310-100 ");

		assertThat(address.street()).isEqualTo("Av. Paulista");
		assertThat(address.number()).isEqualTo("1000");
		assertThat(address.complement()).isEqualTo("Apto 12");
		assertThat(address.neighborhood()).isEqualTo("Bela Vista");
		assertThat(address.city()).isEqualTo("São Paulo");
		assertThat(address.zipCode()).isEqualTo("01310100");
	}

	@Test
	void addressRequestKeepsMisplacedHyphenForValidation() {
		assertThat(new AddressRequest("Rua", "1", null, "Centro", "SP", "0131-0100").zipCode()).isEqualTo("0131-0100");
	}

	@Test
	void addressRequestTurnsBlankComplementIntoNull() {
		assertThat(new AddressRequest("Rua", "1", "   ", "Centro", "SP", "01001000").complement()).isNull();
	}

	@Test
	void addressRequestKeepsNullsForValidation() {
		AddressRequest address = new AddressRequest(null, null, null, null, null, null);

		assertThat(address.street()).isNull();
		assertThat(address.complement()).isNull();
		assertThat(address.zipCode()).isNull();
	}

	@Test
	void createOrderRequestTrimsCustomerData() {
		CreateOrderRequest request = new CreateOrderRequest(" Maria Silva ", " 11999998888 ", null, null);

		assertThat(request.customerName()).isEqualTo("Maria Silva");
		assertThat(request.customerPhone()).isEqualTo("11999998888");
	}

	@Test
	void createOrderRequestKeepsOnlyPhoneDigits() {
		assertThat(new CreateOrderRequest("Maria", " (11) 99999-8888 ", null, null).customerPhone())
			.isEqualTo("11999998888");
	}

	@Test
	void createOrderRequestKeepsPhoneWithLettersForValidation() {
		assertThat(new CreateOrderRequest("Maria", "11 9999 abcd", null, null).customerPhone()).isEqualTo("11 9999 abcd");
		assertThat(new CreateOrderRequest("Maria", null, null, null).customerPhone()).isNull();
	}

	@Test
	void orderItemRequestTrimsName() {
		OrderItemRequest item = new OrderItemRequest(" Pizza ", 1, BigDecimal.TEN);

		assertThat(item.name()).isEqualTo("Pizza");
		assertThat(item.quantity()).isEqualTo(1);
		assertThat(item.unitPrice()).isEqualByComparingTo("10");
	}

}
