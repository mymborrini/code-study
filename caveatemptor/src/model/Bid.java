package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import persistence.MonetaryAmount;

// This POJO will be immutable... that means that once created cannot be update
// This will also help avoiding dirty checkings
// The native hibernate Entity annotations extends the jpa entity annotatios with 
// properties

/**
 * AccessType will set the accessor hibernate will use for all the fields If
 * type is "property" the value of the field will be taken by getter method if
 * type is "field" the value of the field will be taken by the field itself
 * AccessType is an annotation which can be set on field, method or class
 */

@Entity
@org.hibernate.annotations.Entity(mutable = false)
@org.hibernate.annotations.AccessType("field")
public class Bid {

  @Id
  private Long id;

  @org.hibernate.annotations.Type(type = "monetary_amount_eur")
  @org.hibernate.annotations.Columns(columns = { @Column(name = "BID_AMOUNT"), @Column(name = "BID_AMOUNT_CUR") })
  private MonetaryAmount bidAmount;

  /**
   * In this case I want to map this situation
   * 
   * | TABLE ITEM |<- | TABLE_BID |
   * 
   * | .......... | |---| item_id |
   * 
   * For this you don't need to map the other side of this assosiaction If you
   * want to get the item of a bid you can call getItem(), if you want to get all
   * bids of an item you can do a query... but of course you will use an ORM to
   * avoid that. Make a one-to-many annotations is really important because tell
   * hibernate to treat the lifecycle of the bid entity in a differenct way and
   * not as a consequence of the main entity like in value-types mapping.
   * 
   * In case you implement a one-to-many association there is a problem. The not
   * null attribute must disappear since at runtime there are two different
   * in-memory representaion of the same foreign key value: the item property of
   * Bid and an element of the bids collection held by an Item. Suppose the
   * application modifies the assosiaction adding a bid. For the method addBid
   * 
   * bid.setItem(item) bids.add(bid)
   * 
   * Hibernate detects two changes to the in-memory persistent instances. From the
   * point of view of the database, only one value has to be updated to reflect
   * this changes: the ITEM_ID column of the bid table;
   * 
   * In other words you have mapped this column twice
   * 
   * The way to solve it is to add the mappedBy in the one to many assosiaction in
   * this case Hibernate understand which end of the link it should not
   * synchronize with the database . So it will ingore the changes made in bids
   * collection. If you call bids.add(bid) no changes are made persistent. Only if
   * you call bid.setItem(item) you have persisted your changes
   * 
   * If you want to make it the opposite way you have to add to the item_id column
   * updatable=false and insertable=false, removing the mappedBy from the
   * one-to-many and in this case.
   * 
   * Since there are two entities it means that, generally speaking, the
   * relationship itself does not influence their lifecycle. So there is another
   * problem how to manage the lifecycle through a relationship?
   * 
   * One way to do it is to use the cascade="save-update" which means that enabled
   * transitive persistence if a particular bid is referenced by a persistence
   * Item.
   * 
   * So add to onetoMany annotation cascadeTyoe.Persist and cascadeType.Merge.
   * It's safe to put it only on one of the link end of the assosiaction. This way
   * the following code is enough
   * 
   * Item newItem = new Item();
   * 
   * Bid newBid = new Bid();
   * 
   * newItem.addBid(newBid);
   * 
   * session.save(newItem);
   * 
   * In this case we say that the object state becomes transistive, cascade means
   * in effect that you share the operations from your base enetity to the
   * associated one
   * 
   * Let's see delation.
   * 
   * If you write this by code, you should write something like
   * 
   * for (Iterator<Bid> it = anItem.getBids().iterator(); it.hasNext()){
   * 
   * Bid bid = it.next(); it.remove(); session.delete(bid);
   * 
   * }
   * 
   * session.delete(anItem);
   * 
   * If you are sure that no other objects holds a reference to these bids, you
   * can make the deletion transitive, simply adding remove to the cascade options
   * 
   * Like this the cide will be like:
   * 
   * session.delete(anItem);
   * 
   * But what happen if even the User entity holds a reference to these bids. If I
   * try to delete it that way, i may have an exception since a foreign key
   * constraint may be violeted. So I first have to delete all the user references
   * and then finally delete the item itself.
   * 
   * Another discussion is about the fact that bid entity has it's own lifecycle
   * so if you remove from the item.getBids().remove(bid). The instance will still
   * exist. This is way different from the point before... You don't want to
   * delete an Item you want to remove the bid from the collection... I want to
   * tell hibernate "If I remove an element from this collection, it will be an
   * entity reference and it's going to be the only reference to that entity
   * instance you can safely delete it".
   * 
   * This option is called cascade orphan delete.
   * 
   * Remember that orphan delete works fine only for one-to-many otherwise there
   * is no conceptual reason to implement it
   */

  @ManyToOne(targetEntity = model.Item.class)
  @JoinColumn(name = "ITEM_ID") // , nullable = false)
  private Item item;

  public void setItem(Item item) {
    this.item = item;
  }

}
