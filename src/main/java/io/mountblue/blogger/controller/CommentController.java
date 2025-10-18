package io.mountblue.blogger.controller;

import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/add/{id}")
    public String addComment(
            @ModelAttribute("userComment") Comment comment,
            @PathVariable("id") Long postId) {

        commentService.addComment(comment, postId);
        return "redirect:/posts/" + postId;
    }

    @GetMapping("/update/{id}")
    public String showEditCommentForm(@PathVariable Long id, Model model) {
        Map<String, Object> editData = commentService.prepareCommentEdit(id);

        model.addAttribute("editPost", false);
        model.addAttribute("post", editData.get("postDTO"));
        model.addAttribute("editCommentId", id);
        model.addAttribute("comment", editData.get("comment"));
        model.addAttribute("userComment", new Comment());

        return "post-details";
    }

    @PostMapping("/update/{id}")
    public String updateComment(
            @ModelAttribute("updatedComment") Comment comment,
            @PathVariable("id") Long commentId,
            Authentication authentication) {

        Long postId = commentService.updateCommentWithAuthorization(
                commentId, comment, authentication);

        return "redirect:/posts/" + postId;
    }

    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id, Authentication authentication) {
        Long postId = commentService.deleteCommentWithAuthorization(id, authentication);
        return "redirect:/posts/" + postId;
    }
}
