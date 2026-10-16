package com.foody.delivery.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.foody.delivery.dto.response.UserResponse;
import com.foody.delivery.model.User;

class UserMapperTest {

	private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

	@Test
	void toResponseExposesOnlyPublicFields() {
		UserResponse response = mapper.toResponse(new User("Maria", "maria@foody.com", "hash"));

		assertThat(response.name()).isEqualTo("Maria");
		assertThat(response.email()).isEqualTo("maria@foody.com");
	}

}
