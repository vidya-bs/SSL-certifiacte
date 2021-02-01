package com.itorix.apiwiz.virtualization.model.metadata;

public class Headers
{
    private String valueArray;

    private Values[] values;

    private KeyCondition keyCondition;

    private String key;

    public String getValueArray ()
    {
        return valueArray;
    }

    public void setValueArray (String valueArray)
    {
        this.valueArray = valueArray;
    }

    public Values[] getValues ()
    {
        return values;
    }

    public void setValues (Values[] values)
    {
        this.values = values;
    }

    public KeyCondition getKeyCondition ()
    {
        return keyCondition;
    }

    public void setKeyCondition (KeyCondition keyCondition)
    {
        this.keyCondition = keyCondition;
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
        return "ClassPojo [valueArray = "+valueArray+", values = "+values+", keyCondition = "+keyCondition+", key = "+key+"]";
    }
}