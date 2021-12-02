package com.vcab.driver.model;

public class DriverRequestReceived {
    String customerUid;
    String pickupLocation;

    public DriverRequestReceived(String customerUid, String pickupLocation) {
        this.customerUid = customerUid;
        this.pickupLocation = pickupLocation;
    }

    public String getCustomerUid() {
        return customerUid;
    }

    public void getCustomerUid(String customerUid) {
        this.customerUid = customerUid;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}
