package com.vcab.driver.model;

public class DriverRequestReceived {
    String customerUid;
    String pickupLocation;
    String CustomerDestinationLocation;
    String CustomerDestinationAddress;

    public DriverRequestReceived() {
    }

    public DriverRequestReceived(String customerUid, String pickupLocation, String customerDestinationLocation, String CustomerDestinationAddress) {
        this.customerUid = customerUid;
        this.pickupLocation = pickupLocation;
        this.CustomerDestinationLocation = customerDestinationLocation;
        this.CustomerDestinationAddress = CustomerDestinationAddress;
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

    public String getCustomerDestinationAddress() {
        return CustomerDestinationAddress;
    }

    public void setCustomerDestinationAddress(String customerDestinationAddress) {
        CustomerDestinationAddress = customerDestinationAddress;
    }
}
