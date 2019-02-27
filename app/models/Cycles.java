package models;

import java.util.Date;
import javax.validation.constraints.*;
import io.ebean.*;

import play.data.validation.*;
import play.data.format.*;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
public class Cycles extends Model{

    @Id
    public String chassis_number;
    public String brand;
    @NotNull
    public String model;
    public Double price;
    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date date_of_buy = new Date();
    public boolean for_sale;

    public static final Finder<String, Cycles> find = new Finder<>(Cycles.class);

    @ManyToOne()
    public Users users;


}
