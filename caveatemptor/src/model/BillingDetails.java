package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * Type join represents inheritance as relational foreign key association. All
 * class has it's own table even the abstract class. The query will be something
 * like this for the superclass (so with left join)
 * 
 * select bd.billing_details_id, bd.owner, cc.number, cc.exp_month....,
 * ba.account,... case when cc.credit_Card_id is not null then 1 case when
 * ba.bank_account_id is not null then 2 case when bd.billing_details_id is not
 * null then 0 end as clazz_
 * 
 * from billing_details as bd left join credit_card cc on bd.billing_details_id
 * = cc.credit_card_id left join bank_account ba on ba.bank_account_id =
 * bd.billing_details_id
 * 
 * 
 * The query will be something like this for the subclass (with inner join on
 * the super class)
 * 
 * select bd.billing_details_id, bd.owner, cc.number, cc.exp_month....,
 * 
 * from credit_card cc inner join billing_details bd on bd.billing_details_id =
 * cc.credit_card_id
 * 
 * One disadvantage of this system is that performance can be unaccetable for
 * complex class hierarchies
 * 
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BillingDetails {

  @Id
  @GeneratedValue
  @Column(name = "BILLING_DETAILS_ID")
  private Long id;

  @Column(name = "OWNER", nullable = false)
  private String owner;

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

}
