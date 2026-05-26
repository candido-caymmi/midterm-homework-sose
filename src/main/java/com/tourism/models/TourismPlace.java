package com.tourism.models;

public class TourismPlace {

    private String name;
    private String location;
    private String category;
    private String sustainability;
    private boolean accessible;
    private String riskLevel;
    private String sameAs;

    public TourismPlace() {
    }

    public TourismPlace(
            String name,
            String location,
            String category,
            String sustainability,
            boolean accessible,
            String riskLevel,
            String sameAs) {

        this.name = name;
        this.location = location;
        this.category = category;
        this.sustainability = sustainability;
        this.accessible = accessible;
        this.riskLevel = riskLevel;
        this.sameAs = sameAs;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getCategory() {
        return category;
    }

    public String getSustainability() {
        return sustainability;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getSameAs() {
        return sameAs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSustainability(String sustainability) {
        this.sustainability = sustainability;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public void setSameAs(String sameAs) {
        this.sameAs = sameAs;
    }
}