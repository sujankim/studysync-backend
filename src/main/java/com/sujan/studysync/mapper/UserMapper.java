package com.sujan.studysync.mapper;

import com.sujan.studysync.dto.response.UserResponse;
import com.sujan.studysync.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// componentModel = "spring" means:
// MapStruct generates a @Component class
// so Spring can @Autowire / inject it
@Mapper(componentModel = "spring")
public interface UserMapper {

    // MapStruct matches fields by NAME automatically:
    // User.id         → UserResponse.id         ✅ same name
    // User.name       → UserResponse.name        ✅ same name
    // User.username   → UserResponse.username    ✅ same name
    // User.email      → UserResponse.email       ✅ same name
    // User.avatarUrl  → UserResponse.avatarUrl   ✅ same name
    // User.bio        → UserResponse.bio         ✅ same name

    // User.role is enum UserRole → need to convert to String
    // User.plan is enum Plan     → need to convert to String
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "plan", expression = "java(user.getPlan().name())")
    UserResponse toResponse(User user);
}
