package model;

import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.AttributeOverride;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USER")
public class User {

  @Id
  private Long id;

  private String firstName;
  private String lastName;
  private String username;
  private String password;
  private String email;
  private int ranking;
  private boolean admin;

  /**
   * Here i have to ovveride columns since home Address and billing Address have
   * different column name
   */
  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "street", column = @Column(name = "HOME_STREET")),
      @AttributeOverride(name = "zipcode", column = @Column(name = "HOME_ZIPCODE")),
      @AttributeOverride(name = "city", column = @Column(name = "HOME_CITY")) })
  private Address homeAddress;

  @Embedded
  @AttributeOverrides({ @AttributeOverride(name = "street", column = @Column(name = "BILLING_STREET")),
      @AttributeOverride(name = "zipcode", column = @Column(name = "BILLING_ZIPCODE")),
      @AttributeOverride(name = "city", column = @Column(name = "BILLING_CITY")) })
  private Address billingAddress;

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getEmail() {
    return email;
  }

  public int getRanking() {
    return ranking;
  }

  public boolean isAdmin() {
    return admin;
  }

  public Address getHomeAddress() {
    return homeAddress;
  }

  public Address getBillingAddress() {
    return billingAddress;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setRanking(int ranking) {
    this.ranking = ranking;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public void setHomeAddress(Address homeAddress) {
    this.homeAddress = homeAddress;
  }

  public void setBillingAddress(Address billingAddress) {
    this.billingAddress = billingAddress;
  }

  public User() {
  }

  // Generally an hibernate custom type is better to handle this
  /*
   * public String getName() { return firstName + " " + lastName; }
   * 
   * public void setName(String name) { StringTokenizer t = new
   * StringTokenizer(name); firstName = t.nextToken(); lastName = t.nextToken(); }
   */

}
