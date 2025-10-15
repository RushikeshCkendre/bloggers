package io.mountblue.blogger.service;


import io.mountblue.blogger.mapper.PostMapper;
import io.mountblue.blogger.model.Post;
import io.mountblue.blogger.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private PostMapper postMapper;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow();
    }


    public Page<Post> getAllPosts(Model model, Pageable pageable) {
        return postRepository.findAllWithCommentsAndTags(pageable);
    }

    public List<Post> getAllPosts(){
        return postRepository.findAll();
    }

    public void savePost(Post post) {
        post.setExcerpt(setNewExcerpts(post.getContent()));
        post.setTags(tagService.getTagSet(post.getTagAssister()));

        if (post.isPublished()) {
            post.setPublishedAt(LocalDateTime.now());
        }

        postRepository.save(post);
    }

    // title excerpt content tags

    public Post updatePost(Post newPost) {
        Post oldPost = getPostById(newPost.getId());

        oldPost.setTitle(newPost.getTitle());
        oldPost.setContent(newPost.getContent());
        oldPost.setPublished(newPost.isPublished());
        oldPost.setExcerpt(setNewExcerpts(newPost.getContent()));

        oldPost.setTags(tagService.getTagSet(newPost.getTagAssister()));

        return postRepository.save(oldPost);
    }

    private String setNewExcerpts(String content) {
        if (content.length() >= 200) {
            return content.substring(0, 200).trim() + "....";
        } else {
            return content.trim() + "....";
        }
    }

    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    public Page<Post> getPostsByKeyword(Pageable pageable, String keyword,
                                        String keyword1, String keyword2, String keyword3) {
    return  postRepository
            .findDistinctByAuthorContainingOrTitleContainingOrContentContainingOrTags_NameContaining(
                    pageable,keyword,keyword,keyword,keyword);
    }

    public Page<Post> getPostsBySearchAndOrTagsAndOrAuthors(
            Pageable pageable, String keyword, List<String> tags, List<String> authors, LocalDateTime from, LocalDateTime to) {
         if (tags != null && tags.isEmpty()) {
            tags = null;
        }
        if (authors != null && authors.isEmpty()) {
            authors = null;
        }

            return postRepository.getPostsBySearchAndOrTagsAndOrAuthors(pageable, keyword, tags, authors, from, to);
    }


}






