package org.apache.tika.parser.ttr;

/**
 * Created by Milee on 3/27/2016.
 */
public class Measurement {
    //Measurement - value,unit,category,subcategory (normalized)

    private String unit;
    private String unit_value;
    private String category;
    private String sub_category;

    /**
     * Set the measurement units
     */
    public void set_unit(String unit) {
        this.unit=unit;
    }
    /**
     * Get the measurement units
     */
    public String get_unit() {
        return unit;
    }

    /**
     * Set the unit value
     */
    public void set_unit_value(String unit_value) {
        this.unit_value=unit_value;
    }

    /**
     * Get the unit value
     */
    public String get_unit_value() {
        return unit_value;
    }

    /**
     * Set the parent category
     */
    public void set_category(String category) {
        this.category=category;
    }
    /**
     * Get the parent category
     */
    public String get_category() {
        return category;
    }

    /**
     * Set the sub category
     */
    public void set_sub_category(String sub_category) {
        this.sub_category=sub_category;
    }
    /**
     * Get the sub category
     */
    public String get_sub_category() {
        return sub_category;
    }

    @Override
    public String toString() {
        String result = get_unit_value() + ":" + get_unit() + ":" + get_category() + ":" + get_sub_category();
        return result;
    }

}
