package io.mountblue.blogger.controller;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.mapper.PostMapper;
import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.model.Tag;
import io.mountblue.blogger.model.User;
import io.mountblue.blogger.repository.UserRepository;
import io.mountblue.blogger.service.PostService;
import io.mountblue.blogger.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagService tagService;

    @GetMapping
    public String allPosts(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage = postService.getAllPosts(model, pageable);
        Page<PostDTO> postsDTOPage = postsPage.map(postMapper::toDto);

        List<Tag> tagList = tagService.getAllTags();

        Set<String> authorList = postService.getAllPosts().stream().map(Post::getAuthor).collect(Collectors.toSet());
        System.out.println(authorList);
        model.addAttribute("alltags", tagList);
        model.addAttribute("allauthors", authorList);
        model.addAttribute("allposts", postsDTOPage);

        return "posts";
    }

    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model, Authentication authentication) {
        Post post = postService.getPostById(id);
        PostDTO postDTO = postMapper.toDto(post);

        model.addAttribute("userComment", new Comment());
        model.addAttribute("editPost", false);
        model.addAttribute("post", postDTO);
        model.addAttribute("comment", new Comment());

        boolean isPostAuthor = false;
        boolean isAdmin = false;

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            isPostAuthor = post.getAuthor().equals(username);
            isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        model.addAttribute("isPostAuthorOrAdmin", isPostAuthor || isAdmin);


        return "post-details";
    }

    @GetMapping("/new")
    public String showNewPostForm(Model model, Authentication authentication) {
        Post post = new Post();
        String username = authentication.getName();
        boolean isAuthor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(isAuthor){
            post.setAuthor(username);
        }
        model.addAttribute("post", post);
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("isAdmin", isAdmin);


        model.addAttribute("post", post);

        return "new-post";
    }

    @PostMapping("/save")
    public String savePost(@ModelAttribute Post post, Authentication authentication) {
        String username = authentication.getName();

        boolean isAuthor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAuthor) {
            post.setAuthor(username);
            post.setUser(userRepository.findByName(username));
        }


        if (isAdmin) {
            String givenAuthorName = post.getAuthor();
            if (givenAuthorName != null && !givenAuthorName.isBlank()) {
                User authorUser = userRepository.findByUsername(givenAuthorName);

                if (authorUser != null && authorUser.getRole().name().equals("AUTHOR")) {
                    post.setUser(authorUser);
                } else {
                    post.setUser(null);
                }
            }
        }

            postService.savePost(post);

        return "redirect:/posts";
    }

    @GetMapping("/update/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model, Authentication authentication) {

        Post post = postService.getPostById(id);
        PostDTO postDTO = postMapper.toDto(post);

        model.addAttribute("post", postDTO);
        model.addAttribute("editPost", true);
        model.addAttribute("userComment", new Comment());

        return "post-details";
    }

    // boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a-> a.getAuthority().equals("ROLE_ADMIN"));
    @PostMapping("/update/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Post post, Authentication authentication) {
        Post existingPost = postService.getPostById(id);
        String username = authentication.getName();

        boolean isAuthor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));

        if (isAuthor && !existingPost.getAuthor().equals(username)) {
            throw new AccessDeniedException("You can only edit your own posts.");
        }

        post.setId(existingPost.getId());
        post.setAuthor(existingPost.getAuthor());

        postService.updatePost(post);

        return "redirect:/posts/" + existingPost.getId();
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, Authentication authentication) {
        Post existingPost = postService.getPostById(id);
        String username = authentication.getName();

        boolean isAuthor = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));

        if (isAuthor && !existingPost.getAuthor().equals(username)) {
            throw new AccessDeniedException("You can only edit your own posts.");
        }
        postService.deletePostById(id);
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
        Sort.Direction direction = sortDirection
                .equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Post> posts = postService.getPostsByFilters(pageable, keyword, tags, authors, from, to);


        List<Tag> tagList = tagService.getAllTags();

        Set<String> authorList = postService.getAllPosts().stream().map(Post::getAuthor).collect(Collectors.toSet());

        Page<PostDTO> postDTOPage = posts.map(post -> postMapper.toDto(post));

        model.addAttribute("alltags", tagList);
        model.addAttribute("allauthors", authorList);
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
