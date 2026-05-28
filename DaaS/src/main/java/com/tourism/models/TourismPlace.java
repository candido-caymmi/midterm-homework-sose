package com.tourism.models;

public class TourismPlace {

    private String id;
    private String name;
    private String location;
    private String category;
    private String sameAs;

    // Assumed Risk
    private String declaredRiskLevel;

    // Accessibility evidence
    private boolean wheelchairAccessible;
    private boolean stepFreeAccess;
    private boolean accessibleToilets;

    // Sustainability evidence
    private boolean publicTransportAvailable;
    private String visitorPressure;
    private String environmentalSensitivity;
    private String localCommunityImpact;
    private int sustainabilityScore;

    // Data quality
    private String dataSource;
    private String dataLicense;
    private String lastUpdated;

    public TourismPlace() {
    }

    public TourismPlace(
            String id,
            String name,
            String location,
            String category,
            String sameAs,
            String declaredRiskLevel,
            boolean wheelchairAccessible,
            boolean stepFreeAccess,
            boolean accessibleToilets,
            boolean publicTransportAvailable,
            String visitorPressure,
            String environmentalSensitivity,
            String localCommunityImpact,
            int sustainabilityScore,
            String dataSource,
            String dataLicense,
            String lastUpdated) {

        this.id = id;
        this.name = name;
        this.location = location;
        this.category = category;
        this.sameAs = sameAs;
        this.declaredRiskLevel = declaredRiskLevel;

        this.wheelchairAccessible = wheelchairAccessible;
        this.stepFreeAccess = stepFreeAccess;
        this.accessibleToilets = accessibleToilets;

        this.publicTransportAvailable = publicTransportAvailable;
        this.visitorPressure = visitorPressure;
        this.environmentalSensitivity = environmentalSensitivity;
        this.localCommunityImpact = localCommunityImpact;
        this.sustainabilityScore = sustainabilityScore;

        this.dataSource = dataSource;
        this.dataLicense = dataLicense;
        this.lastUpdated = lastUpdated;
    }

    public String getId() {
        return id;
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

    public String getSameAs() {
        return sameAs;
    }

    public String getDeclaredRiskLevel() {
        return declaredRiskLevel;
    }

    public boolean isWheelchairAccessible() {
        return wheelchairAccessible;
    }

    public boolean isStepFreeAccess() {
        return stepFreeAccess;
    }

    public boolean isAccessibleToilets() {
        return accessibleToilets;
    }

    public boolean isPublicTransportAvailable() {
        return publicTransportAvailable;
    }

    public String getVisitorPressure() {
        return visitorPressure;
    }

    public String getEnvironmentalSensitivity() {
        return environmentalSensitivity;
    }

    public String getLocalCommunityImpact() {
        return localCommunityImpact;
    }

    public int getSustainabilityScore() {
        return sustainabilityScore;
    }

    public String getDataSource() {
        return dataSource;
    }

    public String getDataLicense() {
        return dataLicense;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setSameAs(String sameAs) {
        this.sameAs = sameAs;
    }

    public void setDeclaredRiskLevel(String declaredRiskLevel) {
        this.declaredRiskLevel = declaredRiskLevel;
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public void setStepFreeAccess(boolean stepFreeAccess) {
        this.stepFreeAccess = stepFreeAccess;
    }

    public void setAccessibleToilets(boolean accessibleToilets) {
        this.accessibleToilets = accessibleToilets;
    }

    public void setPublicTransportAvailable(boolean publicTransportAvailable) {
        this.publicTransportAvailable = publicTransportAvailable;
    }

    public void setVisitorPressure(String visitorPressure) {
        this.visitorPressure = visitorPressure;
    }

    public void setEnvironmentalSensitivity(String environmentalSensitivity) {
        this.environmentalSensitivity = environmentalSensitivity;
    }

    public void setLocalCommunityImpact(String localCommunityImpact) {
        this.localCommunityImpact = localCommunityImpact;
    }

    public void setSustainabilityScore(int sustainabilityScore) {
        this.sustainabilityScore = sustainabilityScore;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataLicense(String dataLicense) {
        this.dataLicense = dataLicense;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}