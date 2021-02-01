package com.itorix.apiwiz.virtualization.model.metadata;

public class Values
{
    private ValueCondition valueCondition;

    private String value;

    public ValueCondition getValueCondition ()
    {
        return valueCondition;
    }

    public void setValueCondition (ValueCondition valueCondition)
    {
        this.valueCondition = valueCondition;
    }

    public String getValue ()
    {
        return value;
    }

    public void setValue (String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [valueCondition = "+valueCondition+", value = "+value+"]";
    }
}