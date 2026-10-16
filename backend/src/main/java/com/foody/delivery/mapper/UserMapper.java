package com.foody.delivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.foody.delivery.dto.response.UserResponse;
import com.foody.delivery.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

	UserResponse toResponse(User user);

}
