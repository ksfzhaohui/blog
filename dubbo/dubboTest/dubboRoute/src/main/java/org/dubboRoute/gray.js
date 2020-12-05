function gray_rule(invokers, context) {
	var tag = context.getAttachment("tag");
	
	var result = new java.util.ArrayList(invokers.size());
	if(tag == "gray"){
		for (i = 0; i < invokers.size(); i ++) {
			if (invokers.get(i).getUrl().getPort()==20881) {
				result.add(invokers.get(i));
			}
		}
	} else {
		for (i = 0; i < invokers.size(); i ++) {
			if (invokers.get(i).getUrl().getPort()==20882) {
				result.add(invokers.get(i));
			}
		}
	}
	return result;
} (invokers,context)