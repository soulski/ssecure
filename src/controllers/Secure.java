package controllers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import exceptions.UnknownPermissionException;
import play.Play;
import play.modules.ssecure.SSecurePlugin;
import play.mvc.*;
import play.mvc.Http.Cookie;
import play.data.validation.*;
import play.libs.*;
import play.utils.*;

public class Secure extends Controller {
    
    public static boolean hasPermission(String permissionString) throws UnknownPermissionException, Exception {		
		boolean allow = true;
		String[] permissions = permissionString.split(",");
		for(String permissionName : permissions){
			Class permissionClass = SSecurePlugin.permissionClasses.get(permissionName);		
			if(permissionClass == null) {
				throw new UnknownPermissionException("There is no permission name : " + permissionName);
			}
			
			Method method = permissionClass.getMethod("hasPermission");
			Boolean result = (Boolean) method.invoke(null);
			if(!result.booleanValue()) {
				allow = false;
				break;
			}
		}
		
		return allow;
	}
    
}
