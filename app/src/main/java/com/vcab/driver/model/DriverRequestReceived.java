package com.vcab.driver.model;

public class DriverRequestReceived {
    String customerToken;
    String pickupLocation;

    public DriverRequestReceived(String customerToken, String pickupLocation) {
        this.customerToken = customerToken;
        this.pickupLocation = pickupLocation;
    }

    public String getCustomerToken() {
        return customerToken;
    }

    public void setCustomerToken(String customerToken) {
        this.customerToken = customerToken;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}
