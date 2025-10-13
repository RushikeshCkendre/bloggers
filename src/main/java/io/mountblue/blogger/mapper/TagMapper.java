package io.mountblue.blogger.mapper;

import io.mountblue.blogger.dto.TagDTO;
import io.mountblue.blogger.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;


@Mapper(componentModel = "spring")
public interface TagMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    TagDTO toDto(Tag tag);
    Set<TagDTO> toDtoSet(Set<Tag> tagSet);
}