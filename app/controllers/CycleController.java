package controllers;

import io.ebean.Expr;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.*;
import java.util.List;


public class CycleController extends Controller {
    @Inject
    FormFactory formFactory;
    @Inject
    Environment environment;
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
//                String contentType = picture.getContentType()
//                Files.deleteIfExists(file.toPath());
                File file = picture.getFile();
                try {
                    String path = environment.getFile("/public/images/cycle_pic/"+chassis_number+".jpg").toPath()+"";
                    Files.move(Paths.get(file.toPath()+""), Paths.get(path));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            flash("success","Cycle added successfully");
            return redirect(routes.CycleController.addCycle());
        }
    }

    public Result showMyCycles(){
        String loggedin = session("loggedin");
        if(loggedin == null) return redirect(routes.CycleController.index());
        List<Cycles> cycles = Cycles.find.query().where().eq("users_nid",session("nid")).findList();
        int ofset = 0, cyclesPerPage = 5, numOfPage = 1, totCycles = cycles.size();
        if(totCycles % cyclesPerPage == 0) numOfPage = totCycles / cyclesPerPage;
        else numOfPage = (totCycles / cyclesPerPage) + 1;
        String str = request().getQueryString("page");
        int start = 1, end = 1, page = 1;
        if(str != null){
            page = Integer.parseInt(str);
            ofset = (page - 1) * 5;
            if(page > 2) start = page - 2;
        }
        int limit = ofset + 5;
        if(totCycles - ofset < 5) limit = totCycles;
        if((numOfPage - start) >= 4) end = start + 4;
        else {
            end = numOfPage;
            if(numOfPage >= 5) start = numOfPage - 4;
            else start = 1;
        }
        return ok(myCycles.render(cycles,start,end,ofset,limit,page));
    }

    public Result signup(){
        String loggedin = session("loggedin");
        if(loggedin != null) return redirect(routes.CycleController.index());
        return ok(signup.render());
    }

    public Result signupAuthenticate(){
        DynamicForm form = formFactory.form().bindFromRequest();
        String nid = form.get("nid");
        String email = form.get("email");
        Users user = Users.find.query().where().or(Expr.eq("nid",nid),Expr.eq("email",email)).findOne();
        if(user != null){
            flash("duplicate","This nid or email already exist");
            return ok(signup.render());
        }
        else {
            String district = form.get("district");
            String thana = form.get("thana");
            String phone = form.get("phone");
            String password = form.get("pass");
            String name = form.get("name");
            Users users = new Users(nid,email,district,thana,password,phone,name);
            users.save();
            flash("success","Sign up successful");
            return redirect(routes.CycleController.index());
        }
    }
}
