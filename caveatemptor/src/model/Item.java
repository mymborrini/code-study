package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

/*
This is an example to show that a lot of hibernate annotations does not exist in JPA
*/
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DiscriminatorFormula;

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

  @org.hibernate.annotations.Type(type = "persistence.MonetaryAmountUserType")
  @Column(name = "INITIAL_PRICE")
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

}
