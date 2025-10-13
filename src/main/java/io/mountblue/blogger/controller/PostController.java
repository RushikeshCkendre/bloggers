package io.mountblue.blogger.controller;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.mapper.PostMapper;
import io.mountblue.blogger.model.Comment;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.model.Tag;
import io.mountblue.blogger.service.PostService;
import io.mountblue.blogger.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private PostMapper postMapper;

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

        List<String> authorList = postService.getAllPosts().stream().map(Post::getAuthor).toList();

        model.addAttribute("alltags", tagList);
        model.addAttribute("allauthors", authorList);
        model.addAttribute("allposts", postsDTOPage);

        return "posts";
    }

    @GetMapping("/{id}")
    public String getPost(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id);

        PostDTO postDTO = postMapper.toDto(post);

        model.addAttribute("userComment", new Comment());
        model.addAttribute("editPost", false);
        model.addAttribute("post", postDTO);
        model.addAttribute("comment", new Comment());

        return "post-details";
    }

    @GetMapping("/new")
    public String showNewPostForm(Model model) {
        Post post = new Post();
        model.addAttribute("post", post);

        return "new-post";
    }

    @GetMapping("/search")
    public String getPostsByKeyword(Model model,
                                    @RequestParam String keyword,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getPostsByKeyword(pageable, keyword, keyword, keyword, keyword);

        Page<PostDTO> postDTOPage = posts.map(post -> postMapper.toDto(post));

        List<Tag> tagList = tagService.getAllTags();

        List<String> authorList = postService.getAllPosts().stream().map(Post::getAuthor).toList();

        model.addAttribute("alltags", tagList);
        model.addAttribute("allauthors", authorList);
        model.addAttribute("allposts", postDTOPage);
        model.addAttribute("keyword", keyword);
        return "posts";
    }

    @PostMapping("/save")
    public String savePost(@ModelAttribute Post post) {
        postService.savePost(post);

        return "redirect:/posts";
    }

    @GetMapping("/update/{id}")
    public String showEditPostForm(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id);
        PostDTO postDTO = postMapper.toDto(post);
        System.out.println(postDTO);

        model.addAttribute("post", postDTO);
        model.addAttribute("editPost", true);
        model.addAttribute("userComment", new Comment());

        return "post-details";
    }


    @PostMapping("/update/{id}")
    public String updatePost(@ModelAttribute Post post) {
        Post updatedPost = postService.updatePost(post);
        return "redirect:/posts/" + updatedPost.getId();
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePostById(id);
        return "posts";
    }

    @GetMapping("/filter")
    public String filterPostsBySearchAndOrTagsAndOrAuthors(Model model,
                                                           @RequestParam String keyword,
                                                           @RequestParam(defaultValue = "") List<String> tags,
                                                           @RequestParam(defaultValue = "") List<String> authors,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getPostsBySearchAndOrTagsAndOrAuthors(pageable, keyword, tags, authors);


        List<Tag> tagList = tagService.getAllTags();

        List<String> authorList = postService.getAllPosts().stream().map(Post::getAuthor).toList();

        Page<PostDTO> postDTOPage = posts.map(post -> postMapper.toDto(post));


        model.addAttribute("alltags", tagList);
        model.addAttribute("allauthors", authorList);
        model.addAttribute("allposts", postDTOPage);
        model.addAttribute("keyword", keyword);
        return "posts";
    }

}
