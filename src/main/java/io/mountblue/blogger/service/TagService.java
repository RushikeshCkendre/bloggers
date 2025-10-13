package io.mountblue.blogger.service;

import io.mountblue.blogger.model.Tag;
import io.mountblue.blogger.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags(){
        return tagRepository.findAll();
    }

    public Tag getOrCreateTag(String tagName) {
        String newTag = tagName.trim().toLowerCase();

        if (newTag.isEmpty()) {
            return null;
        }

        Optional<Tag> existingTag = tagRepository.findByName(newTag);
        return existingTag.orElseGet(() -> tagRepository.save(new Tag(newTag)));
    }

    public Set<Tag> getTagSet(String tags) {
        Set<Tag> tagSet = new HashSet<>();

        if (tags != null && !tags.isEmpty()) {
            String[] tagNames = tags.split(",");

            for (String tagName : tagNames) {
                Tag tag = getOrCreateTag(tagName);
                if (tag != null) {
                    tagSet.add(tag);
                }
            }
        }

        return tagSet;
    }

}
