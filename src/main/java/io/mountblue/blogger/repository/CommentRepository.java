package io.mountblue.blogger.repository;

import io.mountblue.blogger.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
