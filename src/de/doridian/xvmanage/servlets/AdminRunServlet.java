package de.doridian.xvmanage.servlets;

import de.doridian.xvmanage.models.LibvirtVM;
import de.doridian.xvmanage.models.User;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/AdminRun.do")
public class AdminRunServlet extends HttpServlet {
	public static class XVMJSClass {
		public User getUserByName(String name) {
			return User.getByName(name);
		}

		public LibvirtVM getVMByName(String name) {
			return LibvirtVM.getByName(name);
		}

		public User createUser() {
			return new User();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			final Context cx = Context.enter();
			final Scriptable scope = cx.initStandardObjects();
			Object wrappedUtils = Context.javaToJS(new XVMJSClass(), scope);
			ScriptableObject.putProperty(scope, "XVM", wrappedUtils);
			resp.getWriter().write((String)Context.jsToJava(cx.evaluateString(scope, req.getParameter("code"), "ADMINCODE", 0, null), String.class));
		} catch (Exception e) {
			e.printStackTrace(resp.getWriter());
		}
	}
}
