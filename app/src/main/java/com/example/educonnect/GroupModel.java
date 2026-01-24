package com.example.educonnect;

public class GroupModel {
    private String id, groupName, description, groupCode, createdBy;

    public GroupModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public String getDescription() { return description; }
    public String getGroupCode() { return groupCode; }
    public String getCreatedBy() { return createdBy; }
}
