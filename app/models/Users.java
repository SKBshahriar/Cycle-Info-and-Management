package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Users extends Model {

    @Id
    public String nid;
    @NotNull
    public String email;
    public String district;
    public String thana;
    public String password;
    public String phone;
    public String name;

    public Users(String nid, String email, String district, String thana, String password, String phone, String name) {
        this.nid = nid;
        this.email = email;
        this.district = district;
        this.thana = thana;
        this.password = password;
        this.phone = phone;
        this.name = name;
    }

    public Users(){}

    public static final Finder<String, Users> find = new Finder<>(Users.class);

    @OneToMany(mappedBy = "users",cascade = CascadeType.ALL)
    public List<Cycles> cycles = new ArrayList<>();

    @OneToMany(mappedBy = "users",cascade = CascadeType.ALL)
    public List<Ads> ads = new ArrayList<>();

}
