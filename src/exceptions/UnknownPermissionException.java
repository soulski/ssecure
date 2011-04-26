package exceptions;

public class UnknownPermissionException extends Exception {
	public String permisionName;
	
	public UnknownPermissionException(String permissionName) {		
		this.permisionName = permissionName;
	}
	
	@Override
	public String toString() {		
		return String.format("Unknow permission name : %s ", permisionName);
	}
}
