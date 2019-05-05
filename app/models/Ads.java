package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Ads extends Model {


    public Ads(String description, double price, String district, String thana, String phone, String chassis_number, Users users) {
        this.description = description;
        this.price = price;
        this.district = district;
        this.thana = thana;
        this.phone = phone;
        this.chassis_number = chassis_number;
        this.users = users;
    }

    @Id
    @GeneratedValue
    public Long id;
    public String description;
    public double price;
    public String district;
    public String thana;
    public String phone;
    public String chassis_number;

    public static final Finder<Long,Ads> find = new Finder<>(Ads.class);

    @ManyToOne(cascade = CascadeType.ALL)
    public Users users;

    @OneToMany(mappedBy = "ads",cascade = CascadeType.ALL)
    public List<AdPhotos> adPhotos = new ArrayList<>();


}
