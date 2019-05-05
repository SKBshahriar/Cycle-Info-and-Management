package models;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
public class AdPhotos extends Model {

    public AdPhotos(String path, Ads ads){
        this.path = path;
        this.ads = ads;
    }

    @Id
    @GeneratedValue
    public Long id;
    public String path;

    public static final Finder<Long, AdPhotos> find = new Finder<>(AdPhotos.class);

    @ManyToOne(cascade = CascadeType.ALL)
    public Ads ads;
}
