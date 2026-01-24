package com.example.educonnect;

public class MemberModel {
    private String uid;
    private String name;
    private String email;
    private String profileImage;

    public MemberModel() {}

    public MemberModel(String uid, String name, String email, String profileImage) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfileImage() { return profileImage; }
}
