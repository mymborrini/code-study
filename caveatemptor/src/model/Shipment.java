package model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

/**
 * Lets discuss another topic let's say we want to create a shipment service
 * which is indipendent from the item... So let's say that the shipment has an
 * optional one-to-one association to an Item. In a database I will map this
 * with an extra table ITEM SHIPMENT like this
 * 
 * | item_id | -> <<FK>> <<Unique>>
 * 
 * | shipment_id | -> <<PK>> <<FK>>
 * 
 * You can create this with a @JoinTable annotation
 * 
 * Another way to create it is using a SecondaryTable annotation, with this you
 * can share some properties with a secondary table by specifiyng the table name
 * in the JoinColumn annotation.
 * 
 * Moving property from a secondary table can be really useful
 */

@Entity
@Table(name = "SHIPMENT")
@SecondaryTable(name = "ITEM_SHIPMENT")
public class Shipment {

  @Id
  private Long id;

  @OneToOne
  // @JoinTable(name = "ITEM_SHIPMENT", joinColumns = @JoinColumn(name =
  // "SHIPMENT_ID"), inverseJoinColumns = @JoinColumn(name = "ITEM_ID"))
  @JoinColumn(table = "ITEM_SHIPMENT", name = "ITEM_ID")
  private Item acution;

}
