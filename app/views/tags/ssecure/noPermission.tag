%{
	if(play.templates.TagContext.parent().data.get("_showNoPermission")) {
		play.templates.TagContext.parent().data.remove("_showNoPermission");
}%
		#{doBody /}
%{
	}	
}%
	