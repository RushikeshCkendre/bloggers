package io.mountblue.blogger.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponseDTO {
    private Long id;
    private String title;
    private String excerpt;
    private String content;
    private String authorName;
    private LocalDateTime publishedAt;
    private List<String> tagNames;
    private List<CommentsResponseDTO> comments;
}
