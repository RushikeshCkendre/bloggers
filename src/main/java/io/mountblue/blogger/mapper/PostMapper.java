package io.mountblue.blogger.mapper;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TagMapper.class, CommentMapper.class})
public interface PostMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "excerpt", target = "excerpt")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "publishedAt", target = "publishedAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "tags", target = "tags")
    @Mapping(source = "tagAssister", target = "tagAssister")
    @Mapping(source = "comments", target = "comments")

    PostDTO toDto(Post post);
    List<PostDTO> toDTOList(List<Post> posts);

}
