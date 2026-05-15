package com.sujan.studysync.mapper;

import com.sujan.studysync.dto.response.MessageResponse;
import com.sujan.studysync.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
// "uses = {UserMapper.class}" → when mapping sender (User → UserResponse),
// MapStruct uses our existing UserMapper automatically
public interface MessageMapper {

    // Message fields:              MessageResponse fields:
    // message.id           →       id           ✅ same
    // message.content      →       content      ✅ same
    // message.room.id      →       roomId       ← nested mapping
    // message.sender       →       sender       ← UserMapper handles this
    // message.isEdited     →       isEdited     ✅ same
    // message.createdAt    →       createdAt    ✅ same

    @Mapping(target = "roomId", source = "room.id")
    MessageResponse toResponse(Message message);
}