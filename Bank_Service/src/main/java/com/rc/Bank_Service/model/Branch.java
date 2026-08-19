package com.rc.Bank_Service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "branches")
public class Branch {

    @Id
    @Column(name = "branch_code", length = 4)
    private String branchCode;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "ifsc_code", nullable = false, unique = true, length = 11)
    private String ifscCode;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "pincode", nullable = false, length = 6)
    private String pincode;

    public Branch() {}

    public Branch(String branchCode, String branchName, String ifscCode, String city, String state, String pincode) {
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.ifscCode = ifscCode;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
    }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}
