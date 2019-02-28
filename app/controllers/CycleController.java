package controllers;

import models.Cycles;
import models.Users;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.cycles.*;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.*;


public class CycleController extends Controller {
    @Inject
    FormFactory formFactory;
    public Result index(){
        String loggedin = session("loggedin");
        if(loggedin != null) return ok(home.render());
        else return ok(login.render());
    }

    public Result login(){
        DynamicForm form = formFactory.form().bindFromRequest();
        String email = form.get("email");
        String pass = form.get("pass");
        Users user = Users.find.query().where().eq("email",email).eq("password",pass).findOne();
        if(user != null) {
            session("loggedin", "yes");
            session("nid",user.nid);
        }
        return redirect(routes.CycleController.index());
    }

    public Result logout(){
        session().clear();
        return redirect(routes.CycleController.index());
    }

    public Result addCycle(){
        String loggedin = session("loggedin");
        if(loggedin != null) return ok(addCycle.render());
        else return redirect(routes.CycleController.index());
    }

    public Result saveCycle(){
        DynamicForm form = formFactory.form().bindFromRequest();
        String chassis_number = form.get("chassis_number");
        Cycles cycle = Cycles.find.query().where().eq("chassis_number",chassis_number).findOne();
        if(cycle != null){
            flash("duplicate","This chassis number already exist");
            return ok(addCycle.render());
        }
        else{
            Date currentDate = null;
            String brand = form.get("brand");
            String model = form.get("model");
            String date = form.get("date");
            Double price = Double.parseDouble(form.get("price"));
            try {currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(date); }
            catch (ParseException e) { e.printStackTrace(); }
            Users user = new Users();
            user.nid = session("nid");
            Cycles cycles =  new Cycles(chassis_number,brand,model,price,currentDate,false,user);
            cycles.save();

            Http.MultipartFormData<File> body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart<File> picture = body.getFile("photo");
            if (picture != null) {
//                String fileName = picture.getFilename();
//                String contentType = picture.getContentType();
                File file = picture.getFile();
//                Files.deleteIfExists(file.toPath());
                try {
                    Path temp = Files.move(Paths.get(file.toPath()+""), Paths.get("E:/IDE Workspaces/intelij/Cycle_Info_and_Management/public/images/cycle_pic/"+chassis_number+".jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            flash("success","Cycle added successfully");
            return redirect(routes.CycleController.addCycle());
        }
    }
}
