package com.foody.delivery.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.foody.delivery.repository.OrderRepository;
import com.foody.delivery.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIT {

	private static final String VALID_ORDER = """
			{
			  "customerName": "%s",
			  "customerPhone": "11999998888",
			  "deliveryAddress": {
			    "street": "Av. Paulista", "number": "1000", "complement": "Apto 12",
			    "neighborhood": "Bela Vista", "city": "São Paulo", "zipCode": "01310-100"
			  },
			  "items": [
			    { "name": "Pizza Margherita", "quantity": 2, "unitPrice": 45.00 },
			    { "name": "Refrigerante", "quantity": 1, "unitPrice": 9.90 }
			  ]
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	private String token;

	@BeforeEach
	void setUp() throws Exception {
		orderRepository.deleteAll();
		userRepository.deleteAll();
		String body = mockMvc
			.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Operador", "email": "op@foody.com", "password": "secret123"}
						"""))
			.andReturn()
			.getResponse()
			.getContentAsString();
		token = JsonPath.read(body, "$.token");
	}

	private ResultActions createOrder(String json) throws Exception {
		return mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json));
	}

	private Integer createOrderAndGetId(String customerName) throws Exception {
		String body = createOrder(VALID_ORDER.formatted(customerName)).andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.id");
	}

	private ResultActions patchStatus(Object id, String json) throws Exception {
		return mockMvc.perform(patch("/api/orders/{id}/status", id).header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json));
	}

	@Test
	void createReturns201WithLocationAndRecebido() throws Exception {
		createOrder(VALID_ORDER.formatted("Maria Silva"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", matchesPattern(".*/api/orders/\\d+")))
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.status").value("RECEBIDO"))
			.andExpect(jsonPath("$.customerName").value("Maria Silva"))
			.andExpect(jsonPath("$.deliveryAddress.zipCode").value("01310100"))
			.andExpect(jsonPath("$.items.length()").value(2))
			.andExpect(jsonPath("$.items[0].id").isNumber())
			.andExpect(jsonPath("$.items[0].subtotal").value(90.00))
			.andExpect(jsonPath("$.totalAmount").value(99.90))
			.andExpect(jsonPath("$.createdAt").isNotEmpty())
			.andExpect(jsonPath("$.updatedAt").isNotEmpty());
	}

	@Test
	void createdByIsLoggedUser() throws Exception {
		createOrder(VALID_ORDER.formatted("Maria Silva"))
			.andExpect(jsonPath("$.createdBy.name").value("Operador"))
			.andExpect(jsonPath("$.createdBy.id").isNumber());
	}

	@Test
	void createWithoutItemsReturns400() throws Exception {
		createOrder("""
				{"customerName": "Maria", "customerPhone": "1199",
				 "deliveryAddress": {"street": "Rua", "number": "1", "neighborhood": "Centro",
				                     "city": "SP", "zipCode": "01001000"},
				 "items": []}
				""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("items")));
	}

	@Test
	void createWithInvalidFieldsReturns400() throws Exception {
		createOrder("""
				{"customerName": "", "customerPhone": "1199",
				 "deliveryAddress": {"street": "Rua", "number": "1", "neighborhood": "Centro",
				                     "city": "SP", "zipCode": "123"},
				 "items": [{"name": "Pizza", "quantity": 0, "unitPrice": -1}]}
				""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("customerName")))
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("deliveryAddress.zipCode")))
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("items[0].quantity")))
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("items[0].unitPrice")));
	}

	@Test
	void createWithoutAddressReturns400() throws Exception {
		createOrder("""
				{"customerName": "Maria", "customerPhone": "1199",
				 "items": [{"name": "Pizza", "quantity": 1, "unitPrice": 10}]}
				""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("deliveryAddress")));
	}

	@Test
	void listReturnsOrdersMostRecentFirst() throws Exception {
		createOrderAndGetId("Primeiro");
		createOrderAndGetId("Segundo");

		mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].customerName").value("Segundo"))
			.andExpect(jsonPath("$[1].customerName").value("Primeiro"));
	}

	@Test
	void getByIdReturnsOrder() throws Exception {
		Integer id = createOrderAndGetId("Maria Silva");

		mockMvc.perform(get("/api/orders/{id}", id).header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.customerName").value("Maria Silva"));
	}

	@Test
	void getByIdMissingReturns404() throws Exception {
		mockMvc.perform(get("/api/orders/{id}", 999999).header("Authorization", "Bearer " + token))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Pedido 999999 não encontrado"));
	}

	@Test
	void patchStatusValidReturns200() throws Exception {
		Integer id = createOrderAndGetId("Maria Silva");

		patchStatus(id, """
				{"status": "EM_PREPARO"}
				""")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("EM_PREPARO"));

		mockMvc.perform(get("/api/orders/{id}", id).header("Authorization", "Bearer " + token))
			.andExpect(jsonPath("$.status").value("EM_PREPARO"));
	}

	@Test
	void patchStatusInvalidTransitionReturns422() throws Exception {
		Integer id = createOrderAndGetId("Maria Silva");

		patchStatus(id, """
				{"status": "ENTREGUE"}
				""")
			.andExpect(status().is(422))
			.andExpect(jsonPath("$.message").value("Transição de RECEBIDO para ENTREGUE não permitida"));
	}

	@Test
	void patchStatusOfMissingOrderReturns404() throws Exception {
		patchStatus(999999, """
				{"status": "EM_PREPARO"}
				""")
			.andExpect(status().isNotFound());
	}

	@Test
	void patchWithUnknownStatusReturns400() throws Exception {
		Integer id = createOrderAndGetId("Maria Silva");

		patchStatus(id, """
				{"status": "PRONTO"}
				""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Corpo da requisição inválido"));
	}

	@Test
	void patchWithNullStatusReturns400() throws Exception {
		Integer id = createOrderAndGetId("Maria Silva");

		patchStatus(id, "{}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[*].field", hasItem("status")));
	}

	@Test
	void getByNonNumericIdReturns400() throws Exception {
		mockMvc.perform(get("/api/orders/abc").header("Authorization", "Bearer " + token))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Parâmetro inválido: id"));
	}

	@Test
	void unknownRouteReturns404() throws Exception {
		mockMvc.perform(get("/api/unknown").header("Authorization", "Bearer " + token))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Recurso não encontrado"));
	}

	@Test
	void unsupportedMethodReturns405() throws Exception {
		mockMvc.perform(delete("/api/orders/1").header("Authorization", "Bearer " + token))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.message").value("Método não permitido"));
	}

	@Test
	void validationErrorBodyHasStandardFormat() throws Exception {
		createOrder("{}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.error").value("Bad Request"))
			.andExpect(jsonPath("$.message").value("Dados inválidos"))
			.andExpect(jsonPath("$.fieldErrors[?(@.field == 'items')].message")
				.value(hasItem("O pedido deve ter ao menos um item")))
			.andExpect(jsonPath("$.timestamp").isNotEmpty());
	}

	@Test
	void ordersWithoutTokenReturns401() throws Exception {
		mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
			.content(VALID_ORDER.formatted("Maria")))
			.andExpect(status().isUnauthorized());
	}

}
