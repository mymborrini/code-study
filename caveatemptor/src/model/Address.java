package model;

import javax.persistence.Column;

public class Address {

  @Column(name = "ADDRESS_STREET", nullable = false)
  private String street;

  @Column(name = "ADDRESS_ZIPCODE", nullable = false)
  private String zipcode;

  @Column(name = "ADDRESS_CITY", nullable = false)
  private String city;

  // This is useful to get the user as a backpoint if you have an address
  @org.hibernate.annotations.Parent
  private User user;

  public Address() {
  }

  public String getStreet() {
    return street;
  }

  public String getZipcode() {
    return zipcode;
  }

  public String getCity() {
    return city;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  public void setCity(String city) {
    this.city = city;
  }

}
