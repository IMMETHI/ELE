package com.example.rohitme.contactsdashboard_rohit;

/**
 * Created by rohit.me on 16/11/16.
 */
public class ContactModel implements Comparable<ContactModel>, Cloneable {
    public String name;
    public String phoneNumber;
    public String email;
    public String lastContactTime;
    public long totalTalkTime;
    public String image_uri;

    public ContactModel() {
    }

    @Override
    public int compareTo(ContactModel o) {
        return (int) (o.totalTalkTime - totalTalkTime);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
