package com.vcab.driver.model;

public class DriverRequestReceived {
    String customerUid;
    String pickupLocation;
    String CustomerDestinationLocation;

    public DriverRequestReceived() {
    }

    public DriverRequestReceived(String customerUid, String pickupLocation, String customerDestinationLocation) {
        this.customerUid = customerUid;
        this.pickupLocation = pickupLocation;
        CustomerDestinationLocation = customerDestinationLocation;
    }

    public String getCustomerUid() {
        return customerUid;
    }

    public void setCustomerUid(String customerUid) {
        this.customerUid = customerUid;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getCustomerDestinationLocation() {
        return CustomerDestinationLocation;
    }

    public void setCustomerDestinationLocation(String customerDestinationLocation) {
        CustomerDestinationLocation = customerDestinationLocation;
    }
}
