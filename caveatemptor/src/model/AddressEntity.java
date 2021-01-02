package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;

/**
 * Right now we can discuss about making Adress a real entity and not just a
 * component this is useful if we want to relate it to another class different
 * from User... Shipment for example. In this case Adress and User have a one to
 * one association
 * 
 * User 1->1 Address ...<-0 Shipment
 * 
 * This is the case let's name this one-to-one relationship shipping adress. A
 * one-to-one entity needs to share the same primary key.
 * 
 * So lets start adding a foreign identifier a user. This foreign key constraint
 * means that a user has to exist for a particular shipping address. So when an
 * Address is saved the primary key is taken from the user property. To create a
 * unidirectional one to one association is to map with onetoone the shipping
 * address in user and the specify with PrimaryKeyJoinColumn that the primary
 * key should be shared.
 * 
 * Now if I map like this since Jpa does not like an entity with no Id I have to
 * add an id with this strategy
 * 
 * If you need a different primary key from Address and User but you still want
 * to create a sort of one-to-one association just create a simple ManytoOne and
 * setting to unique in this case two users cannot share an address like in a
 * onetoone relationship.
 * 
 * But what if you want the last association bidirectional... in this case you
 * map with a one-to-one but instead of use the primarykeyjoincolumn annotation
 * just use a joinColumn annotation. And in Address entity use a one to one with
 * mappedBy options
 * 
 */
@Entity
@Table(name = "ADDRESS")
public class AddressEntity {

  @Id
  @GeneratedValue(generator = "myForeignGenerator")
  @org.hibernate.annotations.GenericGenerator(name = "myForeignGenerator", strategy = "foreign", parameters = @Parameter(name = "property", value = "user"))
  @Column(name = "ADDRESS_ID")
  private Long id;

  /*
   * @ManyToOne private User user;
   */

  @OneToOne(mappedBy = "shippingAddress")
  private User user;

  public AddressEntity() {

  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

}
