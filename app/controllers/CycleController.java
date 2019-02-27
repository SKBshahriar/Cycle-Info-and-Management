package controllers;

import models.Users;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import views.html.cycles.*;

import javax.inject.Inject;
import java.util.List;

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
        List<Users> list = Users.find.query().where().eq("email",email).findList();
        Users user = Users.find.query().where().eq("email",email).eq("password",pass).findOne();
        if(user != null)
            session("loggedin","yes");
        return redirect(routes.CycleController.index());
    }

    public Result logout(){
        session().clear();
        return redirect(routes.CycleController.index());
    }
}
