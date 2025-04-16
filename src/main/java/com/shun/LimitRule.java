package com.shun;


public class LimitRule {
    private String instrument;
    private VariationType variationType;
    private ValidationScenarioType validationScenarioType;
    private float variationLimit;

    // Constructors
    public LimitRule() {
    }

    public LimitRule(String instrument, VariationType variationType,
                     ValidationScenarioType validationScenarioType, float variationLimit) {
        this.instrument = instrument;
        this.variationType = variationType;
        this.validationScenarioType = validationScenarioType;
        this.variationLimit = variationLimit;
    }

    // Getters and Setters
    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public VariationType getVariationType() {
        return variationType;
    }

    public void setVariationType(VariationType variationType) {
        this.variationType = variationType;
    }

    public ValidationScenarioType getValidationScenarioType() {
        return validationScenarioType;
    }

    public void setValidationScenarioType(ValidationScenarioType validationScenarioType) {
        this.validationScenarioType = validationScenarioType;
    }

    public float getVariationLimit() {
        return variationLimit;
    }

    public void setVariationLimit(float variationLimit) {
        this.variationLimit = variationLimit;
    }

    @Override
    public String toString() {
        return "VariationRule { " +
                "instrument='" + instrument + '\'' +
                ", variationType=" + variationType +
                ", validationScenarioType=" + validationScenarioType +
                ", variationLimit=" + variationLimit +
                " }";
    }
}