package io.mountblue.blogger.repository;

import io.mountblue.blogger.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag,Long> {
}
