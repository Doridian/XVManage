package de.doridian.xvmanage.forms;

import de.doridian.xvmanage.models.User;

public class AccountForm extends BaseForm {
	protected String oldPassword;

	protected String password;
	protected String passwordConfirm;

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public boolean validate(final User loggedInUser) {
		if(oldPassword == null || oldPassword.isEmpty()) {
			errors.put("oldpassword", "Old password may not be empty");
			return false;
		}

		if(!loggedInUser.checkPassword(oldPassword)) {
			errors.put("oldpassword", "Old password is wrong");
			return false;
		}

		if(password != null && !password.isEmpty() && !password.equals(passwordConfirm)) {
			errors.put("passwordConfirm", "Password confirmation does not match password");
			return false;
		}

		return true;
	}
}
