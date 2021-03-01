package hsbc.library.model;

import org.springframework.hateoas.RepresentationModel;

public class User extends RepresentationModel<User> {

    private String userid;

    private String name;
    private String address;
    private String borrowedBook1;
    private String borrowedBook2;

    public User() {
    }

    public User(String id, String name, String address) {
        this.userid = id;
        this.name = name;
        this.address = address;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBorrowedBook1() {
        return borrowedBook1;
    }

    public void setBorrowedBook1(String borrowedBook1) {
        this.borrowedBook1 = borrowedBook1;
    }

    public String getBorrowedBook2() {
        return borrowedBook2;
    }

    public void setBorrowedBook2(String borrowedBook2) {
        this.borrowedBook2 = borrowedBook2;
    }

    @Override
    public String toString() {
        return "User{" + "name='" + name + '\'' + ", address='" + address + '\'' + '}';
    }
}
