%{
	play.templates.TagContext.parent().data.remove("_showNoPermission");
	if(controllers.Secure.hasPermission(_allow)) {
}%
		#{doBody /}
%{
	}
	else {
		play.templates.TagContext.parent().data.put("_showNoPermission", true);
	}
}%