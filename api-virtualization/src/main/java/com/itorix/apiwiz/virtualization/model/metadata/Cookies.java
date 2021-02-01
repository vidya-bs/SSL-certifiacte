package com.itorix.apiwiz.virtualization.model.metadata;

public class Cookies
{
    private String valueArray;

    private ValueCondition valueCondition;

    private String keyCondition;

    private String value;

    private String key;

    public String getValueArray ()
    {
        return valueArray;
    }

    public void setValueArray (String valueArray)
    {
        this.valueArray = valueArray;
    }

    public ValueCondition getValueCondition ()
    {
        return valueCondition;
    }

    public void setValueCondition (ValueCondition valueCondition)
    {
        this.valueCondition = valueCondition;
    }

    public String getKeyCondition ()
    {
        return keyCondition;
    }

    public void setKeyCondition (String keyCondition)
    {
        this.keyCondition = keyCondition;
    }

    public String getValue ()
    {
        return value;
    }

    public void setValue (String value)
    {
        this.value = value;
    }

    public String getKey ()
    {
        return key;
    }

    public void setKey (String key)
    {
        this.key = key;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [valueArray = "+valueArray+", valueCondition = "+valueCondition+", keyCondition = "+keyCondition+", value = "+value+", key = "+key+"]";
    }
}
		