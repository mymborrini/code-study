package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 * with this table_per_class a query generated for this class works like this
 * 
 * select billing_details_id, owner, number, exp_month, exp_year, account,
 * bankname , swift, clazz_
 * 
 * from (
 * 
 * select billing_details_id, owner, number, exp_month, exp_year, null as
 * account, null as bankname, null as swift, 1 as clazz_ from credit_card
 * 
 * union
 * 
 * select billing_details_id, owner, null as number, null as exp_month, null as
 * exp_year, account, bankname, swift, 2 as clazz_ from bank_account
 * 
 * )
 */

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BillingDetails {

  @Id
  @GeneratedValue
  @Column(name = "BILLING_DETAILS_ID")
  private Long id = null;

  @Column(name = "OWNER", nullable = false)
  private String owner;

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  @ManyToOne
  private User user;

}
