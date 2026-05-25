@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    // Nur Root-Tags (oberste Ebene)
    public List<Tag> getRootTags() {
        return tagRepository.findByParentTagIsNull();
    }

    // Kinder eines Tags
    public List<Tag> getChildTags(Long parentId) {
        return tagRepository.findByParentTag_TagId(parentId);
    }

    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }

    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public Tag updateTag(Long id, Tag updated) {
        updated.setTag_id(id);
        return tagRepository.save(updated);
    }

    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht gefunden");
        }
        tagRepository.deleteById(id);
    }
}