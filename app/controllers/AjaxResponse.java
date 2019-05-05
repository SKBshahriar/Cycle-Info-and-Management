package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Cycles;
import models.Users;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

public class AjaxResponse extends Controller {

    public Result test(){
        String chassis_number = request().getQueryString("chassis_number");
        Cycles cycle = Cycles.find.byId(chassis_number);
        return ok(cycle.users.name);
    }

    public Result getSearchSuggestion(){
        ArrayNode result = Json.newArray();
        String text = request().getQueryString("text");
        String like = "%";
        for(int i = 0; i < text.length(); i++){
            like = like + text.charAt(i) + "%";
        }
        List<String> names = new ArrayList<>();
        List<Users> users = Users.find.query().where().ilike("name",like).findList();
        for(int i = 0; i < users.size(); i++){
            if(!names.contains(users.get(i).name)){
                names.add(users.get(i).name);
                result.add(users.get(i).name);
            }
        }
        return ok(result);
    }
}
