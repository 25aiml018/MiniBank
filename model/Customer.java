package model;

import model.annotation.Id;

import java.io.Serializable;

// Practical 2, 3, 11, 13: Customer class with encapsulation, nested Address, Cloneable, Serializable, and @Id
public class Customer implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static long customerCounter = 100;

    @Id
    private final String customerId;
    private String name;
    private String email;
    private String mobile;
    private Address address;

    private static synchronized String generateCustomerId() {
        customerCounter++;
        return "CUST" + customerCounter;
    }

    public Customer(String name, String email, String mobile) {
        this.customerId = generateCustomerId();
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public Customer(String name, String email, String mobile, Address address) {
        this(name, email, mobile);
        this.address = address;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    // Practical 3 & 11: Nested public static class Address with Serializable
    public static class Address implements Serializable {

        private static final long serialVersionUID = 1L;

        private String line;
        private String city;
        private String pincode;

        public Address(String line, String city, String pincode) {
            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() {
            return line;
        }

        public String getCity() {
            return city;
        }

        public String getPincode() {
            return pincode;
        }

        @Override
        public String toString() {
            return line + ", " + city + " - " + pincode;
        }
    }

    // Practical 3: clone() method
    @Override
    public Customer clone() {
        try {
            Customer copy = (Customer) super.clone();
            if (this.address != null) {
                copy.address = new Address(this.address.line, this.address.city, this.address.pincode);
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }

    @Override
    public String toString() {
        return "Customer[id=" + customerId + ", name=" + name + ", email=" + email +
                ", mobile=" + mobile + (address != null ? ", address=" + address : "") + "]";
    }
}
