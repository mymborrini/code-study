package model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * This class is created to explain the Collection of a component. A component
 * does not have a primary key and its lifecycle depends only on the entity
 * which is referred.
 * 
 * ---------------------------------
 * 
 * SET
 * 
 * We need this class in order to create a Set, so a Table with an item_id
 * referred to the item as the primary key and all the column as the Image
 * property.
 * 
 * We want to create something like this
 * 
 * ITEM | item_id | item_name |
 * 
 * ITEM_IMAGE | ITEM_ID | IMAGE_NAME | FILE_NAME | SIZE_X | SIZE_Y |
 * 
 * There are some controindication of using this approach. First you can load
 * Image instances but they won't have any reference to the owner. Second all
 * the properties must be not null Since they will be all part of the composite
 * key... If any of the property can be null you have to add a primary key to
 * image
 * 
 * ----------------------
 * 
 * COLLECTION
 * 
 * With Collection we allow duplicate entries, so we add the column
 * IMAGE_ITEM_ID as a surrogate key
 * 
 * With this surrogate key implementing equals and hashcode is not required
 * anymore And in this case you can have of course non-null object. For a
 * database point of view this is pretty the same as a standard parent/child
 * entity relationship. An entity relationships supports bidirectional and a way
 * to share reference to the child identity. The price is a more complex
 * lifecycles of object
 * 
 * ---------------------------------
 * 
 * MAP
 * 
 * Another way to switch to a different primary key is a Map. In this case you
 * will have a composite key as ITEM_ID and IMAGE_NAME
 * 
 */

@Embeddable
public class Image {

  @org.hibernate.annotations.Parent
  Item item;

  @Column(name = "IMAGE_NAME", nullable = false)
  private String name;

  @Column(name = "IMAGE_FILENAME", nullable = false)
  private String fileName;

  @Column(name = "SIZE_X", nullable = false)
  private double sizeX;

  @Column(name = "SIZE_Y", nullable = false)
  private double sizeY;

  public String getName() {
    return name;
  }

  public String getFileName() {
    return fileName;
  }

  public double getSizeX() {
    return sizeX;
  }

  public double getSizeY() {
    return sizeY;
  }

  public Item getItem() {
    return item;
  }

  public void setSizeX(double sizeX) {
    this.sizeX = sizeX;
  }

  public void setSizeY(double sizeY) {
    this.sizeY = sizeY;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (o.getClass() != getClass())
      return false;

    Image that = (Image) o;

    if (that.name == null || that.fileName == null)
      return false;
    if (that.name != null && !that.name.equals(name))
      return false;
    if (that.fileName != null && !that.fileName.equals(fileName))
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
    return result;
  }

}
