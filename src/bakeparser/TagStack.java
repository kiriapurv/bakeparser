package bakeparser;

import java.util.Stack;

/**
 * 
 * TagStack is extension of regular stack which provides Tag hierarchy support
 * 
 * @author Apurv Kiri
 *
 */
public class TagStack extends Stack<String>{
	
	private static final long serialVersionUID = 1L;
	private String lastTagName, lastWildCardHierarchy;
	
	public String getHierarchy() {
		String str = String.join(">", this);
		return str;
	}
	
	public String getWildCardHierarchy() {
		if(!this.isEmpty()) {
			lastTagName = this.pop();
			this.push("*");
			lastWildCardHierarchy = getHierarchy();
			return lastWildCardHierarchy;
		}
		return "";
	}
	
	public String restoreWildCard() {
		if(!this.isEmpty() && peek().equals("*")) {
			this.pop();
			this.push(lastTagName);
			return lastWildCardHierarchy;
		}
		return "";
	}

}
