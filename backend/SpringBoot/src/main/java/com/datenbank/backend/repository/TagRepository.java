@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    // Alle Tags die einen bestimmten Parent haben
    List<Tag> findByParentTag_TagId(Long parentTagId);

    // Alle Root-Tags (ohne Parent)
    List<Tag> findByParentTagIsNull();
}