package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * 
 * A lot of time you cannot add a column for discriminator especially in legacy
 * so you can use a formula insteda. The disadventage of this solution is that
 * denormalized schema can become a major burden in the long run. So this method
 * has better performace then TABLE_PER_CLASS but it can be really difficult to
 * maintein Starting from the fact that you cannot have not nullable columns in
 * your domain. In this case since the table is One the query will be
 * 
 * select billing_details_id, billing_details_type, owner, cc_number,
 * cc_exp_month, ..., ba_account, ba_bankname
 * 
 * from billing_details
 * 
 * where billing_details.billing_details_type = 'CC'
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// @DiscriminatorColumn(name = "BILLING_DETAILS_TYPE", discriminatorType =
// DiscriminatorType.STRING)
@org.hibernate.annotations.DiscriminatorFormula("case when CC_NUMBER is not null then 'CC' else 'BA' end")
public abstract class BillingDetails {

  @Id
  @GeneratedValue
  @Column(name = "BILLING_DETAILS_ID")
  private Long id;

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
