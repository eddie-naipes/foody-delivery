package com.foody.delivery;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FoodyDeliveryApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainStartsApplication() {
		assertThatCode(() -> FoodyDeliveryApplication.main(new String[] {
				"--server.port=0", "--spring.profiles.active=test" }))
			.doesNotThrowAnyException();
	}

}
