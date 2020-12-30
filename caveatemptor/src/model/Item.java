package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

/*
This is an example to show that a lot of hibernate annotations does not exist in JPA
*/
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DiscriminatorFormula;

@Entity
@Table(name = "ITEM")
@BatchSize(size = 10)
@DiscriminatorFormula("case when ITEM_IS_SPECIAL is not null then A else B")
public class Item {

  private String name;
  private String description;
  private BigDecimal initialPrice;
  private BigDecimal reservePrice;
  private Date startDate;
  private Date endDate;

  private Set<Category> categories = new HashSet<>();

  public Item() {
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getInitialPrice() {
    return initialPrice;
  }

  public BigDecimal getReservePrice() {
    return reservePrice;
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
