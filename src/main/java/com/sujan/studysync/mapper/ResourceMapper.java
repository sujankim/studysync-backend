package com.sujan.studysync.mapper;

import com.sujan.studysync.dto.response.ResourceResponse;
import com.sujan.studysync.model.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ResourceMapper {

    // Resource fields:                 ResourceResponse fields:
    // resource.id          →           id           ✅
    // resource.title       →           title        ✅
    // resource.description →           description  ✅
    // resource.type(enum)  →           type(String) ← needs conversion
    // resource.url         →           url          ✅
    // resource.fileSize    →           fileSize     ✅
    // resource.originalFileName →      originalFileName ✅
    // resource.room.id     →           roomId       ← nested
    // resource.uploadedBy  →           uploadedBy   ← UserMapper handles
    // resource.createdAt   →           createdAt    ✅

    @Mapping(target = "type",
            expression = "java(resource.getType().name())")
    @Mapping(target = "roomId",
            source = "room.id")
    ResourceResponse toResponse(Resource resource);
}
