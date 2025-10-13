package io.mountblue.blogger.service;

import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostService postService;

    public List<Comment> getCommentsByPostId(Long id){
        return commentRepository.findByPostId(id);
    }

    public Comment saveComment(Comment comment, Long id){
        comment.setPost(postService.getPostById(id));
        return commentRepository.save(comment);
    }

    public Comment updateComment(Comment comment,Long id){
        Comment existingComment = commentRepository.getById(id);

        existingComment.setName(comment.getName());
        existingComment.setEmail(comment.getEmail());
        existingComment.setComment(comment.getComment());

        return commentRepository.save(existingComment);
    }

    public Long getPostIdByCommentId(Long id) {

        Comment comment = commentRepository.getById(id);
        return comment.getPost().getId();
    }

    public void deleteCommentById(Long id) {
       commentRepository.deleteById(id);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.getById(id);
    }
}
