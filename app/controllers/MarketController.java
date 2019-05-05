package controllers;

import models.AdPhotos;
import models.Ads;
import models.Cycles;
import models.Users;
import play.Environment;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cycles.*;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MarketController extends Controller {
    @Inject
    Environment environment;
    @Inject
    FormFactory formFactory;
    public Result postAd(){
        String id = request().getQueryString("id");
        Cycles cycle = Cycles.find.byId(id);
        if(cycle == null || !cycle.users.nid.equals(session("nid"))){
            flash("notfound","sorry cycle not found");
            return ok(postAd.render(null));
        }
        return ok(postAd.render(cycle));
    }

    public Result saveAd() {
        DynamicForm form = formFactory.form().bindFromRequest();
        String phone = form.get("phone");
        String district = form.get("district");
        String thana = form.get("thana");
        double price = Double.parseDouble(form.get("price"));
        String description= form.get("description");
        String chassis_number = form.get("chassis_number");
        String user_nid = form.get("user");
        Users user = Users.find.byId(user_nid);
        Ads ad = new Ads(description,price,district,thana,phone,chassis_number,user);
        ad.save();
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        List<Http.MultipartFormData.FilePart<File>> pictures = body.getFiles();
        for (int i = 0; i < pictures.size(); i++) {
            String type = pictures.get(i).getContentType();
            String[] parts = type.split("/");
            String part1 = parts[0];
            if (part1.equals("image")) {
                File picture = pictures.get(i).getFile();
                String path = environment.getFile("/public/images/adimages/"+ad.id+"_"+i+".jpg").toPath()+"";
                AdPhotos adPhoto = new AdPhotos("/images/adimages/"+ad.id+"_"+i+".jpg",ad);
                adPhoto.save();
                try {
                    Files.move(Paths.get(picture.toPath()+""), Paths.get(path));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        flash("adsuccess","Your ad has been successfully posted");
        return redirect("/cycle?id="+chassis_number);
    }

    public Result showMyAds(){
        List<Ads> ads = Ads.find.query().where().eq("users_nid",session("nid")).findList();

        int ofset = 0, adsPerPage = 10, numOfPage = 1, totAds = ads.size();
        if(totAds % adsPerPage == 0) numOfPage = totAds / adsPerPage;
        else numOfPage = (totAds / adsPerPage) + 1;
        String str = request().getQueryString("page");
        int start = 1, end = 1, page = 1;
        if(str != null){
            page = Integer.parseInt(str);
            ofset = (page - 1) * adsPerPage;
            if(page > 2) start = page - 2;
        }
        int limit = ofset + adsPerPage;
        if(totAds - ofset < adsPerPage) limit = totAds;
        if((numOfPage - start) >= 4) end = start + 4;
        else {
            end = numOfPage;
            if(numOfPage >= 5) start = numOfPage - 4;
            else start = 1;
        }
        List<Cycles> cycles = new ArrayList<>();
        for(int i = 0; i < totAds; i++){
            String chassis_number = ads.get(i).chassis_number;
            Cycles cycle = Cycles.find.byId(chassis_number);
            cycles.add(cycle);
        }

        return ok(myAds.render(ads,cycles,start,end,ofset,limit,page));
    }

}
