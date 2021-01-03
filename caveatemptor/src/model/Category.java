package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionId;

@Entity
@Table(name = "CATEGORY")
public class Category {

  private Long id;
  private String name;
  private Category parentCategory;
  private Set<Category> childCategories = new HashSet<>();
  private Set<Item> items = new HashSet<>();

  public Category() {
  }

  public String getName() {
    return name;
  }

  public Category getParentCategory() {
    return parentCategory;
  }

  public Set<Category> getChildCategories() {
    return childCategories;
  }

  public Set<Item> getItems() {
    return items;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setParentCategory(Category parentCategory) {
    this.parentCategory = parentCategory;
  }

  public void setItems(Set<Item> items) {
    this.items = items;
  }

  public void addChildCategory(Category childCategory) {
    if (childCategory == null) {
      throw new IllegalArgumentException("Null child category");
    }

    if (childCategory.getParentCategory() != null) {
      childCategory.getParentCategory().getChildCategories().remove(childCategory);
    }

    childCategory.setParentCategory(this);
    childCategories.add(childCategory);

  }

  public void removeChildCategory(Category childCategory) {

    if (childCategory == null) {
      throw new IllegalArgumentException("Null child category");
    }

    if (!childCategory.getParentCategory().equals(this)) {
      throw new IllegalArgumentException("This category does not match the parent Category");
    }

    childCategory.setParentCategory(null);
    childCategories.remove(childCategory);

  }

  /**
   * You can implement A many to many association to map the categories to the
   * items Another way to do it is to map the Associate table as an entity and
   * make two one to many from each side. But let's see with a Join table. So the
   * associate table will be if you use a Set:
   * 
   * | category_id | -> <<PK>> <<FK>>
   * 
   * | item_id | -> <<PK>> <<FK>>
   * 
   * Or you can also use a <bag> using a surrogate primary key for this mapping.
   * In this way an item can be associated more then once to the same Category...
   * this does not seem a useful feature
   * 
   * Of course you can switch to an index collection The primary key of the link
   * table will be a composite key of the category_id and display_position. this
   * mappings guarantees that the position of each Item in a Category is
   * persistent.
   * 
   * | category_id | item_id | display_position |
   * 
   * We have to use an hibernate extension since JPA supports only @OrderBy
   * annotation thats not what we wanted in this case. If you don't add the
   * IndexColum the list is stored with bag semantics so no guaranteed persist
   * order of elements
   * 
   * For a bidirectional many to many it's always better to implemtn addItem
   * method Like in many to one you have to decide which side of the table is used
   * to update the join table. You have to update cascade save-update for both end
   * in this collection in this case since the associated table needs to be update
   * if one part of the other chage.
   * 
   * In this case cascade delete and delete orphan does not make any sense. Since
   * the JoinTable is like a value type for hiberante it does not have a lifecycle
   * indipendent from the entities referred so when you delete the main entity all
   * his value types will be deleted as well
   * 
   */
  @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name = "CATEGORY_ITEM", joinColumns = { @JoinColumn(name = "CATEGORY_ID") }, inverseJoinColumns = {
      @JoinColumn(name = "ITEM_ID") })
  private Set<Item> itemSetManyToMany = new HashSet<>();

  @ManyToMany
  @CollectionId(columns = @Column(name = "CATEGORY_ID_ITEM"), type = @org.hibernate.annotations.Type(type = "long"), generator = "sequence")
  @JoinTable(name = "CATEGORY_ITEM", joinColumns = { @JoinColumn(name = "CATEGORY_ID") }, inverseJoinColumns = {
      @JoinColumn(name = "ITEM_ID") })
  private Collection<Item> itemBagManyToMany = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "CATEGORY_ITEM", joinColumns = { @JoinColumn(name = "CATEGORY_ID") }, inverseJoinColumns = {
      @JoinColumn(name = "ITEM_ID") })
  @org.hibernate.annotations.IndexColumn(name = "DISPLAY_POSITION")
  private List<Item> itemListManyToMany = new ArrayList<>();

  public void addItem(Item item) {
    this.itemSetManyToMany.add(item);
    item.getCategoriesSetManyToMany().add(this);
  }

  public Long getId() {
    return id;
  }

  // In this case remove is essential since the Categorized Item is an Entity
  @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
  private Set<CategorizedItem> categorizedItems = new HashSet<>();

  public Set<CategorizedItem> getCategorizedItems() {
    return categorizedItems;
  }

  @org.hibernate.annotations.CollectionOfElements
  @JoinTable(name = "CATEGORY_ITEM", joinColumns = @JoinColumn(name = "CATEGORY_ID"))
  private Set<CategorizedItemValueType> categorizedItemValueTypes = new HashSet<>();

}