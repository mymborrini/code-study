package model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * You can mixing inheritance strategies for example mix single table and table
 * for sub-class for a particular subclass. At runtime Hibernate executes an
 * outer join to fetch billing details and all subclass instances
 * polymorphically
 * 
 * select billing_details_id, billing_details_type, owner, cc.cc_number,
 * cc.cc_exp_month, ..., ba_account, ba_bankname from billing_detatils left
 * outer join credit_card cc on billing_details_ud = cc.credit_card_id
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "DISCRIMINATOR_TYPE")
public abstract class BillingDetails {

  @Id
  @GeneratedValue
  @Column(name = "BILLING_DETAILS_ID")
  private Long id;

  @Column(name = "DISCRIMINATOR_TYPE")
  private String type;

  @Column(name = "OWNER", nullable = false)
  private String owner;

  public Long getId() {
    return id;
  }

  public String getOwner() {
    return owner;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

}
