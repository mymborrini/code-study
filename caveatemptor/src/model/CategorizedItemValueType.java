package model;

import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * In this class i represents the same as CategorizedItem, but with a Value
 * type... not with an entity with this I can simplify the code drastically.
 * There is only a change in database schema, the primary key now is composite
 * of all this column. So no coulumn can be nullable false... and this is a
 * strong limitation to this method. The advantage of a collection of components
 * is the implicity lifecycle of all entities. To break the link remove with
 * from the collection, no extra cascade options are required. Another downside
 * of this approch is that I cannot navigate from Item to categorizedItem, since
 * we put this collection into Category... You cannot put this into Item too
 * since this is no more an Entity and its lifecycle depends on category entity
 * only. The upside of this approch is that can be do a ternary association in
 * this case with a few lines of code and metadata... a lot of less then an
 * Entity
 */
@Embeddable
public class CategorizedItemValueType {

  @org.hibernate.annotations.Parent
  private Category category;

  @ManyToOne
  @JoinColumn(name = "USER_ID", nullable = false, updatable = false)
  private User user;

  @Temporal(TemporalType.TIMESTAMP)
  @JoinColumn(name = "ADDED_ON", nullable = false, updatable = false)
  private Date dateAdded;

  @ManyToOne
  @JoinColumn(name = "ITEM_ID", nullable = false, updatable = false)
  private Item item;

  public CategorizedItemValueType(User user, Item item, Category category) {
    this.user = user;
    this.item = item;
    this.category = category;
  }

  public User getUser() {
    return user;
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

  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o.getClass() != getClass())
      return false;

    CategorizedItemValueType that = (CategorizedItemValueType) o;

    if (!this.item.equals(that.item) || !this.category.equals(that.category))
      return false;
    return true;

  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((item != null) ? item.hashCode() : 0);
    result = prime * result + ((category != null) ? category.hashCode() : 0);
    return result;
  }

}
