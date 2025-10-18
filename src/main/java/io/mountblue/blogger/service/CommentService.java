package io.mountblue.blogger.service;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.mapper.PostMapper;
import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private PostMapper postMapper;

    // ============= Basic CRUD Operations =============

    public List<Comment> getCommentsByPostId(Long id) {
        return commentRepository.findByPostId(id);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
    }

    public Comment saveComment(Comment comment, Long postId) {
        comment.setPost(postService.getPostById(postId));
        return commentRepository.save(comment);
    }

    public void addComment(Comment comment, Long postId) {
        comment.setId(null);  // Ensure it's a new comment
        saveComment(comment, postId);
    }

    public Comment updateComment(Comment comment, Long id) {
        Comment existingComment = getCommentById(id);

        existingComment.setName(comment.getName());
        existingComment.setEmail(comment.getEmail());
        existingComment.setComment(comment.getComment());

        return commentRepository.save(existingComment);
    }

    public void deleteCommentById(Long id) {
        commentRepository.deleteById(id);
    }

    // ============= Helper Methods =============

    public Long getPostIdByCommentId(Long id) {
        Comment comment = getCommentById(id);
        return comment.getPost().getId();
    }

    // ============= Authorization Methods =============

    private boolean isAuthor(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void validateAuthorCanModifyComment(Comment comment, Authentication authentication) {
        String username = authentication.getName();

        if (isAuthor(authentication) &&
                !comment.getPost().getAuthor().equals(username)) {
            throw new AccessDeniedException(
                    "You can only modify comments on your own posts.");
        }
    }

    // ============= Business Logic Methods =============

    public Map<String, Object> prepareCommentEdit(Long commentId) {
        Comment comment = getCommentById(commentId);
        Post post = postService.getPostById(comment.getPost().getId());
        PostDTO postDTO = postMapper.toDto(post);

        Map<String, Object> editData = new HashMap<>();
        editData.put("comment", comment);
        editData.put("postDTO", postDTO);

        return editData;
    }

    public Long updateCommentWithAuthorization(
            Long commentId, Comment updatedComment, Authentication authentication) {

        Comment existingComment = getCommentById(commentId);
        validateAuthorCanModifyComment(existingComment, authentication);

        existingComment.setComment(updatedComment.getComment());
        Comment savedComment = updateComment(existingComment, commentId);

        return savedComment.getPost().getId();
    }

    public Long deleteCommentWithAuthorization(Long commentId, Authentication authentication) {
        Comment comment = getCommentById(commentId);
        Long postId = comment.getPost().getId();

        validateAuthorCanModifyComment(comment, authentication);
        deleteCommentById(commentId);

        return postId;
    }
}
