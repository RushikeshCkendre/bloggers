package io.mountblue.blogger.dto;



import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class PostDTO {
    private Long id;
    private String title;
    private String excerpt;
    private String content;
    private String author;
    private String tagAssister;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
    private Set<TagDTO> tags;
    private List<CommentDTO> comments;


}
