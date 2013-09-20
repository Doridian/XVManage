package de.doridian.xvmanage.forms;

import de.doridian.xvmanage.models.User;

public class LoginForm extends BaseForm {
	protected String username;
	protected String password;

	protected String returnURL;

	protected User user = null;

	public User getCurrentUser() {
		return user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getReturnURL() {
		return returnURL;
	}

	public void setReturnURL(String returnURL) {
		this.returnURL = returnURL;
	}

	public boolean validate() {
		boolean isOK = true;

		if(username.isEmpty()) {
			errors.put("username", "Username may not be empty");
			isOK = false;
		}

		if(password.isEmpty()) {
			errors.put("password", "Password may not be empty");
			isOK = false;
		}

		if(!isOK) return false;

		user = User.getByName(username);

		if(user == null || !user.checkPassword(password)) {
			errors.put("username", "Invalid username or password");
			return false;
		}

		return true;
	}
}
