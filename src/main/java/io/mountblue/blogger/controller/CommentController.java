package io.mountblue.blogger.controller;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.mapper.PostMapper;
import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.service.CommentService;
import io.mountblue.blogger.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String updateComment(@ModelAttribute("updatedComment") Comment comment, @PathVariable("id") Long commentId) {
        Comment updatedComment = commentService.updateComment(comment, commentId);
        return "redirect:/posts/" + updatedComment.getPost().getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id) {
        Long postId = commentService.getPostIdByCommentId(id);
        commentService.deleteCommentById(id);
        return "redirect:/posts/" + postId;
    }
}
