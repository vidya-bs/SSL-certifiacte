package com.itorix.apiwiz.design.studio.model;
public class SwaggerTeamPermissionVO
{
    private String name;

    private String[] roles;

    private String type;

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String[] getRoles ()
    {
        return roles;
    }

    public void setRoles (String[] roles)
    {
        this.roles = roles;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [name = "+name+", roles = "+roles+", type = "+type+"]";
    }
}