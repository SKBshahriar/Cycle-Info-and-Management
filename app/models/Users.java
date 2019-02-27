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


    public static final Finder<String, Users> find = new Finder<>(Users.class);

    @OneToMany(mappedBy = "users")
    public List<Cycles> cycles = new ArrayList<>();

}
