package io.mountblue.blogger.repository;

import io.mountblue.blogger.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    @EntityGraph(attributePaths = {"tags", "comments"})
    Optional<Post> findById(Long id);


    @EntityGraph(attributePaths = {"tags", "comments"})
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags LEFT JOIN FETCH p.comments")
    Page<Post> findAllWithCommentsAndTags(Pageable pageable);

    List<Post> findAll();

    Page<Post> findDistinctByAuthorContainingOrTitleContainingOrContentContainingOrTags_NameContaining(
            Pageable pageable ,String keyword, String keyword1, String keyword2, String keyword3);


    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "OR (:tags IS NULL OR t.name IN :tags) " +
            "OR (:authors IS NULL OR p.author IN :authors)")
    Page<Post> getPostsBySearchAndOrTagsAndOrAuthors(
            Pageable pageable,
            @RequestParam String keyword,
            @RequestParam List<String> tags,
            @RequestParam List<String> authors);


}
