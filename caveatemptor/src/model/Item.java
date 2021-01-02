package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
/*
This is an example to show that a lot of hibernate annotations does not exist in JPA
*/
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.DiscriminatorFormula;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;

import persistence.MonetaryAmount;

// This name is use in HQL if there is a conflict between java classes maybe with the
// same name but in different packages
@Entity(name = "AuctionItem")
@Table(name = "ITEM")
@BatchSize(size = 10)
@DiscriminatorFormula("case when ITEM_IS_SPECIAL is not null then A else B")
public class Item {

  private String name;

  /**
   * Hibernate provide a serialization fallback for property which is
   * serializable, of course in database the byte stream is saved as a sequence of
   * bit in a varbinary but when is loaded hibernate deserialize it. This may be
   * useful only for temporary data since "Data lives longer then application"
   */

  // Description can be a really large value. So i choose to map it to a clob
  // column in db
  // There is no need to use java.sql.Clob
  /**
   * In this case as in the case below Hibernate will render all the value
   * immediatly and not onDemand this could be an issue in performance
   */
  @Lob
  @Column(name = "ITEM_DESCRIPTION")
  private String description;

  // The same is true even for image, there is no need to use java.sql.Blob
  @Lob
  @Column(name = "ITEM_IMAGE")
  private byte[] image;

  @org.hibernate.annotations.Type(type = "persistence.MonetatyAmountCompositeUserType")
  @org.hibernate.annotations.Columns(columns = {
      @Column(name = "INITIAL_PRICE", columnDefinition = "number(10,2) default '1'"),
      @Column(name = "INITIAL_PRICE_CURRENCY", length = 2) })
  private MonetaryAmount initialPrice;

  /**
   * Don't know which of this two method is correct todo
   */
  @AttributeOverrides({
      @AttributeOverride(name = "amount", column = @Column(name = "RESERVE_PRICE_AMOUNT", columnDefinition = "number(10,2) default '1'")) })
  private MonetaryAmount reservePrice;

  @Temporal(TemporalType.TIMESTAMP) // It could be Time or Date
  @Column(name = "START DATE", nullable = false, updatable = false)
  private Date startDate;
  private Date endDate;

  /**
   * This field is settled by trigger in database Generation time could be always
   * or insert, with this property hibernate will make a refresh (SELECT )
   * immediatly after the insert or update in order to get the value
   */
  @Column(updatable = false, insertable = false)
  @org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.ALWAYS)
  private Date lastModified;

  @Transient
  private BigDecimal totalIncludingTax;

  private Set<Category> categories = new HashSet<>();

  public Item() {
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  /**
   * With Set the order is not preserved In this case I have a different Table
   * named for example item_image which as 2 columns (a composite key of this 2
   * columns) once for the item_id as the primary key and one for filename of the
   * image
   * 
   * | item_id | filename |
   * 
   * If you use generic collections hibernate can automatically detect the type of
   * the element if you use generic collection like this one. Otherwise you have
   * to specify it.
   * 
   * In emmbed it's pretty the same you can also override some column if you want
   * but all the columns must be not-nullable
   */
  @org.hibernate.annotations.CollectionOfElements(targetElement = java.lang.String.class)
  @JoinTable(name = "ITEM_IMAGE", joinColumns = @JoinColumn(name = "ITEM_ID"))
  @Column(name = "FILENAME", nullable = false)
  private Set<String> imageSet = new HashSet();

  @org.hibernate.annotations.CollectionOfElements(targetElement = java.lang.String.class)
  @JoinTable(name = "ITEM_IMAGE", joinColumns = @JoinColumn(name = "ITEM_ID"))
  @AttributeOverride(name = "element.name", column = @Column(name = "IMAGE_NAME", length = 255, nullable = false))
  private Set<Image> imagesSetEmbeed = new HashSet<>();

  /**
   * With Collection is like a <bag> the order is not preserved indeed With a Set
   * of images you cannot have duplicate images, if you want duplicate images it's
   * better to use a collection and implement it with new Arraylist In this case I
   * should have a table with 3 columns
   * 
   * | Item_image_id | item_id | filename |
   * 
   * In this case the primary key is a single column the item_image_id while the
   * other two columns are not null
   * 
   * With Embeed images you have to specify a CollectionId Annotation
   */
  // private Collection images = new ArrayList();

  @org.hibernate.annotations.CollectionOfElements(targetElement = java.lang.String.class)
  @JoinTable(name = "ITEM_IMAGE", joinColumns = @JoinColumn(name = "ITEM_ID"))
  @org.hibernate.annotations.CollectionId(columns = @Column(name = "ITEM_IMAGE_ID"), type = @org.hibernate.annotations.Type(type = "long"), generator = "sequence")
  private Collection<Image> imageCollectionEmbedeed = new ArrayList();

  /**
   * With List you preserve the order of the item inserted. In this case I have to
   * add an index column in order to persist the position of the element in the
   * collection So like in a Set the primary key is a composite key of the item_id
   * primary key and the filename, plus you have an additional column position
   * 
   * | item_id | position | filename |
   * 
   * In indexColumn base means the starter of the count. If you forget to specify
   * the index column this list will be treated as a a normal bag collection
   */
  @org.hibernate.annotations.CollectionOfElements
  @JoinTable(name = "ITEM_IMAGE", joinColumns = @JoinColumn(name = "ITEM_ID"))
  @org.hibernate.annotations.IndexColumn(name = "POSITION", base = 1)
  @Column(name = "filename")
  private List<String> imageList = new ArrayList();

  /**
   * Now suppose that the images for an item hava a user-supplied names in
   * addition to the filename; One way to this is with a Map. In this case I will
   * have a table with 3 columns one for containing the key of the map
   * 
   * | item_id | image_name | filename |
   * 
   * In this case i have a composite primary key with image_id and image_name This
   * map is of course unsorted If the key of the map are not a simple key
   */
  @org.hibernate.annotations.CollectionOfElements
  @JoinTable(name = "ITEM_IMAGE", joinColumns = @JoinColumn(name = "ITEM_ID"))
  @MapKeyColumn(name = "IMAGE_NAME")
  @Column(name = "FILE_NAME")
  private Map<String, String> imageMap = new HashMap();

  /**
   * In hibernate sorted and ordered have two different meanings sorted is a
   * sorted in memory using a Java comparator. An orderd collection is orderd at
   * the sql level using a SQL query with order_by cause. With Sorted Map you
   * still have three columns as in map
   * 
   * | item_id | image_name | filename |
   * 
   * But you are telling hibernate to sort the image accoriding to the compareTo()
   * method in Java.lang.String if you use natural if you need a different
   * comparator you can specify as a class which implements java.util.Comparator
   * in the sort attribute
   */
  // @org.hibernate.annotations.Sort(comparator = package.Class.class)
  // @org.hibernate.annotations.Sort(type = SortType.NATURAL)
  // private SortedMap images = new TreeMap();

  /**
   * You can also sort a Set with the same method as well in this case you still
   * have two columns but with the annotation of sorted too that works exactly
   * like it works in SortedMap
   * 
   * | item_id | filename |
   * 
   */
  @JoinTable(name = "ITEM_IMAGE", joinColumns = @JoinColumn(name = "ITEM_ID"))
  @Column(name = "FILE_NAME")
  @org.hibernate.annotations.Sort(type = SortType.NATURAL)
  private SortedSet imageSortedSet = new TreeSet();

  /*
   * Hibenrate will consider the following two annotations as the same so it's
   * better to chooese one of the two
   */
  // @Basic(optional = false) // This annotation will set that this method cannot
  // return null at java level
  @Column(nullable = false) // This annotation is responsable for NOT NULL constraint in datbase
  public MonetaryAmount getInitialPrice() {
    return initialPrice;
  }

  public MonetaryAmount getReservePrice() {
    return reservePrice;
  }

  @org.hibernate.annotations.Formula("TOTAL + TAX_RATE * TOTAL")
  public BigDecimal getTotalIncludingTax() {
    return totalIncludingTax;
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public Set<Category> getCategories() {
    return categories;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setInitialPrice(MonetaryAmount initialPrice) {
    this.initialPrice = initialPrice;
  }

  public void setReservePrice(MonetaryAmount reservePrice) {
    this.reservePrice = reservePrice;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public void addCategory(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Category is null");
    }

    category.getItems().add(this);
    categories.add(category);

  }

  @OneToMany(mappedBy = "item", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
  @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
  private Set<Bid> bids = new HashSet<>();

  public void setBids(Set<Bid> bids) {
    this.bids = bids;
  }

  public Set<Bid> getBids() {
    return bids;
  }

  public void addBid(Bid bid) {
    bid.setItem(this);

    // This is useless as explained in mappedBy in Bid.class
    // bids.add(bid);
  }

}
