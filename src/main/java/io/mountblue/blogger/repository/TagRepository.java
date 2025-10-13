package io.mountblue.blogger.repository;

import io.mountblue.blogger.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag,Long> {

    Tag save(Tag tag);

    Optional<Tag> findById(Long id);

    Optional<Tag> findByName(String name);

    List<Tag> findAll();

}
