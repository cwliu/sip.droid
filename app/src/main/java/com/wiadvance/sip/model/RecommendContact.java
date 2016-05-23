package com.wiadvance.sip.model;

import java.util.List;

public class RecommendContact {

    private String name;
    private Double percent;
    private List<String> phoneList;

    public RecommendContact(String name, Double percent, List<String> phoneList) {
        this.name = name;
        this.percent = percent;
        this.phoneList = phoneList;
    }

    public List<String> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(List<String> phoneList) {
        this.phoneList = phoneList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }
}
