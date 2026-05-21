@Entity
@Table(name = "Item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long item_id;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @ManyToOne
    @JoinColumn(name = "item_type_id")
    private ItemType itemType;

    @ManyToOne
    @JoinColumn(name = "item_template_id")
    private ItemRepresentationTemplate itemTemplate;

    @ManyToOne
    @JoinColumn(name = "root_item_id")
    private Item rootItem; // selbstreferenzierend

    // Getter & Setter
}