package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

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

}
