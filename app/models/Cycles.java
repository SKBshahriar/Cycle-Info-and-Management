package models;

import java.util.Date;
import javax.validation.constraints.*;
import io.ebean.*;
import javax.persistence.*;

//@Table(
//        uniqueConstraints=
//        @UniqueConstraint(columnNames={"name", "brand"})
//)

@Entity
public class Cycles extends Model{

    public Cycles(String chassis_number, String brand, String model, Double price, Date date_of_buy, boolean for_sale, Users users) {
        this.chassis_number = chassis_number;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.date_of_buy = date_of_buy;
        this.for_sale = for_sale;
        this.users = users;
    }

    @Id
    public String chassis_number;
    public String brand;
    @NotNull
    public String model;
    public Double price;
    @Temporal(TemporalType.DATE)
    public Date date_of_buy = new Date();
    public boolean for_sale;

    public static final Finder<String, Cycles> find = new Finder<>(Cycles.class);

    @ManyToOne(cascade = CascadeType.ALL)
    public Users users;


}
