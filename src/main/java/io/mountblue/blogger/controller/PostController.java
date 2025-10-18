package io.mountblue.blogger.controller;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public String allPosts(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);

        Page<PostDTO> postsDTOPage = postService.getAllPostsAsDTO(pageable);
        Map<String, Object> filterData = postService.getFilterData();

        model.addAttribute("alltags", filterData.get("tags"));
        model.addAttribute("allauthors", filterData.get("authors"));
        model.addAttribute("allposts", postsDTOPage);
        model.addAttribute("authentication", authentication);

        return "posts";
    }

    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model, Authentication authentication) {
        PostDTO postDTO = postService.getPostDTOById(id);
        boolean canEdit = postService.canUserEditPost(id, authentication);

        model.addAttribute("userComment", new Comment());
        model.addAttribute("editPost", false);
        model.addAttribute("post", postDTO);
        model.addAttribute("comment", new Comment());
        model.addAttribute("isPostAuthorOrAdmin", canEdit);

        return "post-details";
    }

    @GetMapping("/new")
    public String showNewPostForm(Model model, Authentication authentication) {
        Post post = postService.prepareNewPost(authentication);
        Map<String, Boolean> roles = postService.getUserRoles(authentication);

        model.addAttribute("post", post);
        model.addAttribute("isAuthor", roles.get("isAuthor"));
        model.addAttribute("isAdmin", roles.get("isAdmin"));

        return "new-post";
    }

    @PostMapping("/save")
    public String savePost(@ModelAttribute Post post, Authentication authentication) {
        postService.createPost(post, authentication);
        return "redirect:/posts";
    }

    @GetMapping("/update/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model, Authentication authentication) {
        PostDTO postDTO = postService.getPostDTOById(id);

        model.addAttribute("post", postDTO);
        model.addAttribute("editPost", true);
        model.addAttribute("userComment", new Comment());

        return "post-details";
    }

    @PostMapping("/update/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Post post, Authentication authentication) {
        postService.updatePostWithAuthorization(id, post, authentication);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, Authentication authentication) {
        postService.deletePostWithAuthorization(id, authentication);
        return "redirect:/posts";
    }

    @GetMapping("/filter")
    public String filterPosts(Model model,
                              @RequestParam(required = false, defaultValue = "") String keyword,
                              @RequestParam(required = false, defaultValue = "") List<String> tags,
                              @RequestParam(required = false, defaultValue = "") List<String> authors,
                              @RequestParam(required = false, defaultValue = "") @DateTimeFormat LocalDateTime from,
                              @RequestParam(required = false, defaultValue = "") @DateTimeFormat LocalDateTime to,
                              @RequestParam(defaultValue = "desc") String sortDirection,
                              @RequestParam(defaultValue = "createdAt") String sortBy,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PostDTO> postDTOPage = postService.getFilteredPostsAsDTO(
                pageable, keyword, tags, authors, from, to);
        Map<String, Object> filterData = postService.getFilterData();

        model.addAttribute("alltags", filterData.get("tags"));
        model.addAttribute("allauthors", filterData.get("authors"));
        model.addAttribute("allposts", postDTOPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("selectedAuthors", authors);
        model.addAttribute("selectedTags", tags);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sorting", sortDirection);

        return "posts";
    }
}
