package play.modules.ssecure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionName {
	public abstract String name();
}
