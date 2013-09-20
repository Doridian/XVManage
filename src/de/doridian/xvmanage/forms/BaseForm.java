package de.doridian.xvmanage.forms;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseForm {
	protected final HashMap<String, String> errors = new HashMap<String, String>();

	public boolean isSubmit(HttpServletRequest request) {
		return request.getMethod().equalsIgnoreCase("post");
	}

	public String getErrorFor(String field) {
		return errors.get(field);
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public String getErrorHTML() {
		StringBuilder errorMsgB = new StringBuilder();
		errorMsgB.append("<div class=\"alert alert-error\"><ul style=\"margin-bottom: 0;\">");
		Map<String, String> errors = getErrors();
		for(String error : errors.values()) {
			errorMsgB.append("<li>");
			errorMsgB.append(error);
			errorMsgB.append("</li>");
		}
		errorMsgB.append("</ul></div>");
		return errorMsgB.toString();
	}
}
