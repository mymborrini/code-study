package model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This class is created in order to explain how to add columns to a many to
 * many associate entity and to show how to different rpresent this kind of
 * associations. In this case I want to add the columns ADDED_BY_USER and
 * ADDED_ON
 */
@Entity
@Table(name = "CATEGORIZED_ITEM")
public class CategorizedItem {

  /**
   * Hibernate access composite id fields directly you don't need getter and
   * setter in this nested class
   */
  @Embeddable
  public static class Id implements Serializable {

    public static final long serialVersionUID = 2L;

    @Column(name = "CATEOGORY_ID")
    private Long categoryId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    public Id() {
    }

    public Id(Long categoryId, Long itemId) {
      this.categoryId = categoryId;
      this.itemId = itemId;
    }

    public boolean equals(Object o) {
      if (o != null && o instanceof Id) {
        Id that = (Id) o;
        return this.categoryId.equals(that.categoryId) && this.itemId.equals(that.itemId);
      } else {
        return false;
      }
    }

    public int hashCode() {
      return categoryId.hashCode() + itemId.hashCode();
    }
  }

  @EmbeddedId
  private Id id = new Id();

  @Column(name = "ADDED_BY_USER")
  private String username;

  @Column(name = "ADDED_ON")
  private Date dateAdded = new Date();

  @ManyToOne
  @JoinColumn(name = "ITEM_ID", insertable = false, updatable = false)
  private Item item;

  @ManyToOne
  @JoinColumn(name = "CATEGORY_ID", insertable = false, updatable = false)
  private Category category;

  public CategorizedItem() {
  }

  public CategorizedItem(String username, Category category, Item item) {
    this.username = username;
    this.category = category;
    this.item = item;

    // set identifier values
    this.id.categoryId = category.getId();
    this.id.itemId = item.getId();

    // Guarantee referential integrity
    category.getCategorizedItems().add(this);
    item.getCategorizedItems().add(this);

  }

  public Id getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public Date getDateAdded() {
    return dateAdded;
  }

  public Item getItem() {
    return item;
  }

  public Category getCategory() {
    return category;
  }

}
