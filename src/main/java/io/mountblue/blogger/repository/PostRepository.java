package io.mountblue.blogger.repository;

import io.mountblue.blogger.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
