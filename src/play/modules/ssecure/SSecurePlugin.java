package play.modules.ssecure;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Router;
import play.mvc.Router.Route;
import play.mvc.results.Error;
import play.mvc.results.Result;
import play.vfs.VirtualFile;

public class SSecurePlugin extends PlayPlugin {
	private static final String FOLDER_PERMISSION = Play.configuration.getProperty("ssecure.permission.folder", "permission");
	
	public static Map<String, String[]> secureRoutes = new HashMap<String, String[]>();
	public static Map<String, Class> permissionClasses = new HashMap<String, Class>();
	
	@Override
	public void onApplicationStart() {
		List<Class> allPermissionClasses = readAllPermissionClass();
		for (Class permissionClass : allPermissionClasses) {
			PermissionName permissionName = (PermissionName) permissionClass.getAnnotation(PermissionName.class);
			String name = permissionName != null ? permissionName.name() : permissionClass.getName();
			permissionClasses.put(name, permissionClass);
		}
	}
	
	public List<Class> readAllPermissionClass() {
		List<VirtualFile> permissionFiles = new ArrayList<VirtualFile>();
		String filterRelativePath = File.separator + "app" + File.separator + FOLDER_PERMISSION;
		VirtualFile filterFolder = VirtualFile.fromRelativePath(filterRelativePath);
		if ((filterFolder.exists()) && (filterFolder.isDirectory())) {
			permissionFiles = filterFolder.list();
		}
		
		List<Class> allPermissionClass = new ArrayList<Class>();
		for (VirtualFile filterFile : permissionFiles) {
			String filename = filterFile.getName();
			String classname = FOLDER_PERMISSION + "."
					+ filename.substring(0, filename.lastIndexOf("."));
			Class filterClass = Play.classloader.getClassIgnoreCase(classname);
			allPermissionClass.add(filterClass);
		}
		return allPermissionClass;
	}
	
	@Override
	public void onRoutesLoaded() {
		for(Route route : Router.routes) {
			if(isSecureRoute(route.path)) {				
				String actualPath = route.path.substring(0, route.path.lastIndexOf(":"));
				String permissionsText = route.path.substring(route.path.lastIndexOf(":") + 1);
				permissionsText = permissionsText.trim();
				permissionsText = permissionsText.endsWith(",") ? permissionsText.substring(0, permissionsText.length() - 1) : permissionsText;
				route.path = actualPath;
				route.compute();
				secureRoutes.put(actualPath, permissionsText.split(","));							
			}
		}		
	}
	
	@Override
	public void onRequestRouting(Route route) {	
		if(secureRoutes.containsKey(route.path)) {
			String[] permissionList = secureRoutes.get(route.path);
			for(String permissionText : permissionList) {
				Class permissionClass = permissionClasses.get(permissionText);
				
				// cannot find permission name
				if(permissionClass == null) {
					Logger.error("There is no permission name : %s", new Object[] { permissionText });					
					throw new Error(500, "No permission to access.");
				}
				
				try {
					Method method = permissionClass.getMethod("hasPermission");
					Boolean result = (Boolean) method.invoke(null);
					if(!result.booleanValue()) {
						throw new Error(500, "No permission to access.");
					}
				} catch (Result result) {
					throw result;
				} catch (Exception e) {
					Logger.error(e, "Fail to run permission name : %s", new Object[] { permissionClass.getName() });
				}
			}
		}
	}
	
	public boolean isSecureRoute(String routePath) {
		return routePath.matches(".*:((?:(?:\\s*\\w*\\s*)(?:,|$))+)");
	}
	
}
