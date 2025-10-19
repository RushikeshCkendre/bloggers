package io.mountblue.blogger.service;

import io.mountblue.blogger.dto.PostDTO;
import io.mountblue.blogger.mapper.PostMapper;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.model.Tag;
import io.mountblue.blogger.model.User;
import io.mountblue.blogger.repository.PostRepository;
import io.mountblue.blogger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserRepository userRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow();
    }

    public Page<PostDTO> getAllPostsAsDTO(Pageable pageable) {
        Page<Post> posts = postRepository.findAllWithCommentsAndTags(pageable);
        return posts.map(postMapper::toDto);
    }

    public void savePost(Post post) {
        post.setExcerpt(setExcerpts(post.getContent()));
        post.setTags(tagService.getTagSet(post.getTagAssister()));

        if (post.isPublished()) {
            post.setPublishedAt(LocalDateTime.now());
        }

        postRepository.save(post);
    }

    public Post updatePost(Post post) {
        Post existingPost = getPostById(post.getId());

        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        existingPost.setPublished(post.isPublished());
        existingPost.setExcerpt(setExcerpts(post.getContent()));
        existingPost.setTags(tagService.getTagSet(post.getTagAssister()));

        return postRepository.save(existingPost);
    }

    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }


    public PostDTO getPostDTOById(Long id) {
        Post post = getPostById(id);
        return postMapper.toDto(post);
    }



    public Page<PostDTO> getFilteredPostsAsDTO(
            Pageable pageable, String keyword, List<String> tags,
            List<String> authors, LocalDateTime from, LocalDateTime to) {
        Page<Post> posts = getPostsByFilters(pageable, keyword, tags, authors, from, to);
        return posts.map(postMapper::toDto);
    }


    public Page<Post> getPostsByFilters(
            Pageable pageable, String keyword, List<String> tags,
            List<String> authors, LocalDateTime from, LocalDateTime to) {
        if (tags != null && tags.isEmpty()) {
            tags = null;
        }
        if (authors != null && authors.isEmpty()) {
            authors = null;
        }

        return postRepository.getPostsBySearchAndOrTagsAndOrAuthors(
                pageable, keyword, tags, authors, from, to);
    }

    public Map<String, Object> getFilterData() {
        List<Tag> tags = tagService.getAllTags();
        Set<String> authors = postRepository.findAll().stream()
                .map(Post::getAuthor)
                .collect(Collectors.toSet());

        Map<String, Object> filterData = new HashMap<>();
        filterData.put("tags", tags);
        filterData.put("authors", authors);

        return filterData;
    }


    public boolean isAuthor(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_AUTHOR"));
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public Map<String, Boolean> getUserRoles(Authentication authentication) {
        Map<String, Boolean> roles = new HashMap<>();
        roles.put("isAuthor", isAuthor(authentication));
        roles.put("isAdmin", isAdmin(authentication));
        return roles;
    }

    public boolean canUserEditPost(Long postId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Post post = getPostById(postId);
        String username = authentication.getName();
        boolean isPostAuthor = post.getAuthor().equals(username);

        return isPostAuthor || isAdmin(authentication);
    }

    public void validateUserCanEditPost(Post post, Authentication authentication) {
        String username = authentication.getName();

        if (isAuthor(authentication) && !post.getAuthor().equals(username)) {
            throw new AccessDeniedException("You can only edit your own posts.");
        }
    }


    public Post prepareNewPost(Authentication authentication) {
        Post post = new Post();
        String username = authentication.getName();

        if (isAuthor(authentication)) {
            post.setAuthor(username);
            post.setUser(userRepository.findByName(username));
        }

        return post;
    }

    public void createPost(Post post, Authentication authentication) {
        assignAuthorToPost(post, authentication);
        savePost(post);
    }

    public void updatePostWithAuthorization(Long id, Post post, Authentication authentication) {
        Post existingPost = getPostById(id);
        validateUserCanEditPost(existingPost, authentication);

        post.setId(existingPost.getId());
        post.setAuthor(existingPost.getAuthor());
        updatePost(post);
    }

    public void deletePostWithAuthorization(Long id, Authentication authentication) {
        Post existingPost = getPostById(id);
        validateUserCanEditPost(existingPost, authentication);
        deletePostById(id);
    }

    private void assignAuthorToPost(Post post, Authentication authentication) {
        String username = authentication.getName();

        if (isAuthor(authentication)) {
            User currentUser = userRepository.findByName(username);
            post.setAuthor(currentUser.getName());
            post.setUser(currentUser);
        } else if (isAdmin(authentication)) {
            String givenAuthorName = post.getAuthor();
            if (givenAuthorName != null && !givenAuthorName.isBlank()) {
                User authorUser = userRepository.findByName(givenAuthorName);

                if (authorUser != null && authorUser.getRole().name().equals("AUTHOR")) {
                    post.setUser(authorUser);
                    post.setAuthor(authorUser.getName());
                } else {
                    post.setUser(null);
                }
            }
        }
    }


    private String setExcerpts(String content) {
        if (content.length() >= 200) {
            return content.substring(0, 200).trim() + "....";
        } else {
            return content.trim() + "....";
        }
    }
}
