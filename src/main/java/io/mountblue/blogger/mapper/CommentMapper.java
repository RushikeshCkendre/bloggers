package io.mountblue.blogger.mapper;

import io.mountblue.blogger.dto.CommentDTO;
import io.mountblue.blogger.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "comment", target = "comment")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")

    CommentDTO toDto(Comment comment);
}