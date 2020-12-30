package model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CATEGORY")
public class Category {

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

}