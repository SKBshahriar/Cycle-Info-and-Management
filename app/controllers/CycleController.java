package controllers;
import io.ebean.Expr;
import models.Cycles;
import models.Users;
import org.mindrot.jbcrypt.BCrypt;
import play.Environment;
import play.api.libs.Crypto;
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
import java.util.ArrayList;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.*;
import java.util.List;


public class CycleController extends Controller {
//    @Inject
//    Test test;

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
        Users user = Users.find.query().where().eq("email",email).findOne();
        if(user != null) {
            String passwordHash = user.password;
            if(BCrypt.checkpw(pass,passwordHash)){
                session("loggedin", "yes");
                session("nid",user.nid);
            }
            else flash("loginerror","password is incorrect");
        }else{
            flash("loginerror","email is incorrect");
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
            Users user = Users.find.byId(session("nid"));
            Cycles cycles =  new Cycles(chassis_number,brand,model,price,currentDate,false,user);
            cycles.save();

            Http.MultipartFormData<File> body = request().body().asMultipartFormData();
            Http.MultipartFormData.FilePart<File> picture = body.getFile("photo");
            if (picture != null) {
                String type = picture.getContentType();
                String[] parts = type.split("/");
                String part1 = parts[0];
                String part2 = parts[1];
                if(part1.equals("image")){
                    File file = picture.getFile();
                    try {
                        String path = environment.getFile("/public/images/cycle_pic/"+chassis_number+".jpg").toPath()+"";

                        Files.move(Paths.get(file.toPath()+""), Paths.get(path));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        ArrayList<Boolean> is_img = new ArrayList<Boolean>();
        for(Cycles cycle: cycles){
            if(Files.exists(Paths.get(environment.getFile("/public/images/cycle_pic/"+cycle.chassis_number+".jpg").toPath()+"")))
                is_img.add(true);
            else is_img.add(false);
        }
        int ofset = 0, cyclesPerPage = 10, numOfPage = 1, totCycles = cycles.size();
        if(totCycles % cyclesPerPage == 0) numOfPage = totCycles / cyclesPerPage;
        else numOfPage = (totCycles / cyclesPerPage) + 1;
        String str = request().getQueryString("page");
        int start = 1, end = 1, page = 1;
        if(str != null){
            page = Integer.parseInt(str);
            ofset = (page - 1) * cyclesPerPage;
            if(page > 2) start = page - 2;
        }
        int limit = ofset + cyclesPerPage;
        if(totCycles - ofset < cyclesPerPage) limit = totCycles;
        if((numOfPage - start) >= 4) end = start + 4;
        else {
            end = numOfPage;
            if(numOfPage >= 5) start = numOfPage - 4;
            else start = 1;
        }
        return ok(myCycles.render(cycles,start,end,ofset,limit,page, is_img));
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
            password = BCrypt.hashpw(password,BCrypt.gensalt());
            String name = form.get("name");
            Users users = new Users(nid,email,district,thana,password,phone,name);
            users.save();
            flash("success","Sign up successful");
            return redirect(routes.CycleController.index());
        }
    }
    public Result showCycleInfo(){
        String loggedin = session("loggedin");
        if(loggedin == null) return redirect(routes.CycleController.index());
        String chassis_number = request().getQueryString("id");
        Cycles cycle = Cycles.find.query().where().eq("chassis_number",chassis_number).findOne();
        if(cycle == null){
            flash("notfound","sorry cycle not found");
            return ok(cycleInfo.render(null));
        }
        else{
            return ok(cycleInfo.render(cycle));
        }
    }

    public Result updateCycle(){
        String loggedin = session("loggedin");
        if(loggedin == null) return redirect(routes.CycleController.index());
        String chassis_number = request().getQueryString("id");
        Cycles cycle = Cycles.find.query().where().eq("chassis_number",chassis_number).findOne();
        if(cycle == null || !cycle.users.nid.equals(session("nid"))){
            flash("notfound","sorry cycle not found");
            return ok(updateCycle.render(null));
        }
        else{
            return ok(updateCycle.render(cycle));
        }
    }

    public Result saveUpdate(){
        DynamicForm form = formFactory.form().bindFromRequest();
        String chassis_number = form.get("chassis_number");
        Cycles cycle = Cycles.find.query().where().eq("chassis_number",chassis_number).findOne();
        Date currentDate = null;
        String brand = form.get("brand");
        String model = form.get("model");
        String date = form.get("date");
        Double price = Double.parseDouble(form.get("price"));
        try {currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(date); }
        catch (ParseException e) { e.printStackTrace(); }
        cycle.brand = brand;
        cycle.model = model;
        cycle.date_of_buy = currentDate;
        cycle.price = price;
        cycle.update();

        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> picture = body.getFile("photo");
        if (picture != null) {
            String type = picture.getContentType();
            String[] parts = type.split("/");
            String part1 = parts[0];
            if(part1.equals("image")){
                File file = picture.getFile();
                try {
                    File tempFile = environment.getFile("/public/images/cycle_pic/"+chassis_number+".jpg");;
                    Files.deleteIfExists(tempFile.toPath());
                    Files.move(Paths.get(file.toPath()+""), Paths.get(tempFile.toPath()+""));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        flash("success","cycle updated successfully");
        return redirect("/editcycle?id="+chassis_number);

    }

    public Result changeOwner(){
        String loggedin = session("loggedin");
        if(loggedin == null) return redirect(routes.CycleController.index());
        String chassis_number = request().getQueryString("id");
        Cycles cycle = Cycles.find.query().where().eq("chassis_number",chassis_number).findOne();
        if(cycle == null || !cycle.users.nid.equals(session("nid"))){
            flash("notfound","sorry cycle not found");
            return ok(ownerChange.render(chassis_number));
        }
        else{
            return ok(ownerChange.render(chassis_number));
        }
    }

    public Result updateOwner(){
        DynamicForm form = formFactory.form().bindFromRequest();
        String email = form.get("email");
        Users newUser = Users.find.query().where().eq("email",email).findOne();
        String chassis_number = form.get("chassis_number");
        if(newUser == null){
            flash("notfound","sorry user not found");
            return ok(ownerChange.render(chassis_number));
        }
        else{
            String password = form.get("pass");
            Users user = Users.find.byId(session("nid"));
            if(BCrypt.checkpw(password,user.password)){
                Cycles cycle = Cycles.find.byId(chassis_number);
                cycle.users = newUser;
                cycle.update();
                flash("success","owner changed successfully");
                return redirect(routes.CycleController.showMyCycles());
            }
            else{
                flash("password","password not matched");
                return ok(ownerChange.render(chassis_number));
            }
        }
    }

    public Result showUserSearchResult(){
        String loggedin = session("loggedin");
        if(loggedin == null) return redirect(routes.CycleController.index());

        String name = request().getQueryString("name");
        List<Users> users = Users.find.query().where().ilike("name","%"+name+"%").findList();
        int totUsers = users.size(), numOfPage = 1, usersPerPage = 10, ofset = 0;
        if(totUsers % usersPerPage == 0) numOfPage = totUsers / usersPerPage;
        else numOfPage = (totUsers / usersPerPage) + 1;
        String str = request().getQueryString("page");
        int start = 1, end = 1, page = 1;
        if(str != null){
            page = Integer.parseInt(str);
            ofset = (page - 1) * usersPerPage;
            if(page > 2) start = page - 2;
        }
        int limit = ofset + usersPerPage;
        if(totUsers - ofset < usersPerPage) limit = totUsers;
        if((numOfPage - start) >= 4) end = start + 4;
        else {
            end = numOfPage;
            if(numOfPage >= 5) start = numOfPage - 4;
            else start = 1;
        }

        return ok(userSearchResult.render(users,start,end,ofset,limit,page,name));
    }
    public Result profile(){
        String id = request().getQueryString("id");
        Users user = Users.find.byId(id);
        if(user == null){
            flash("notfound","sorry user not found");
            return ok(profile.render(null));
        }
        return ok(profile.render(user));

    }

    public Result test(){
        String string = "1234";
        String hash = BCrypt.hashpw(string,BCrypt.gensalt());
        return ok(hash);
    }

}
