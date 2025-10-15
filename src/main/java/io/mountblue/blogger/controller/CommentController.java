package io.mountblue.blogger.controller;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.mapper.PostMapper;
import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.service.CommentService;
import io.mountblue.blogger.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private PostMapper postMapper;


    @PostMapping("/add/{id}")
    public String addComment(@ModelAttribute("userComment") Comment comment, @PathVariable("id") Long postId) {
        comment.setId(null);
        commentService.saveComment(comment, postId);
        return "redirect:/posts/" + postId;
    }

    @GetMapping("/update/{id}")
    public String showEditCommentForm(@PathVariable Long id, Model model) {
        Comment comment = commentService.getCommentById(id);
        Post post = postService.getPostById(comment.getPost().getId());
        PostDTO postDTO = postMapper.toDto(post);
        model.addAttribute("editPost", false);
        model.addAttribute("post", postDTO);
        model.addAttribute("editCommentId", id);
        model.addAttribute("comment", comment);
        model.addAttribute("userComment", new Comment());
        return "post-details";
    }

    @PostMapping("/update/{id}")
    public String updateComment(
            @ModelAttribute("updatedComment") Comment comment,
            @PathVariable("id") Long commentId,
            Authentication authentication) {
        Comment existingComment = commentService.getCommentById(commentId);
        String username = authentication.getName();

        boolean isAuthor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));

        if(isAuthor && !existingComment.getPost().getAuthor().equals(username)){
            throw new AccessDeniedException("You can only update comments on your own posts.");
        }

        comment.setPost(existingComment.getPost());
        existingComment.setComment(comment.getComment());
        Comment updatedComment = commentService.updateComment(existingComment, commentId);
        return "redirect:/posts/" + updatedComment.getPost().getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id, Authentication authentication) {
        Long postId = commentService.getPostIdByCommentId(id);
        Post post = postService.getPostById(postId);
        String username = authentication.getName();

        boolean isAuthor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));

        if(isAuthor && !post.getAuthor().equals(username)){
            throw new AccessDeniedException("You can only delete comments on your own posts.");
        }


        commentService.deleteCommentById(id);
        return "redirect:/posts/" + postId;
    }
}
