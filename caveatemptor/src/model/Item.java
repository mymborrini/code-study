package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

import javax.persistence.Column;
/*
This is an example to show that a lot of hibernate annotations does not exist in JPA
*/
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DiscriminatorFormula;

// This name is use in HQL if there is a conflict between java classes maybe with the
// same name but in different packages
@Entity(name = "AuctionItem")
@Table(name = "ITEM")
@BatchSize(size = 10)
@DiscriminatorFormula("case when ITEM_IS_SPECIAL is not null then A else B")
public class Item {

  private String name;
  private String description;

  /**
   * This field is settled by default to 1 during insertion in this case you have
   * to trigger a select after an insertion or update in order to get this value
   * but you have to keep the column updatable and insertable
   * 
   */
  @Column(columnDefinition = "number(10,2) default '1'")
  @org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.INSERT)
  private BigDecimal initialPrice;
  private BigDecimal reservePrice;
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
  public BigDecimal getInitialPrice() {
    return initialPrice;
  }

  public BigDecimal getReservePrice() {
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

  public void setInitialPrice(BigDecimal initialPrice) {
    this.initialPrice = initialPrice;
  }

  public void setReservePrice(BigDecimal reservePrice) {
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
