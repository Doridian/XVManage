package de.doridian.xvmanage.servlets;

import de.doridian.xvmanage.models.LibvirtVM;
import de.doridian.xvmanage.models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;

@WebServlet("/ManageVM.do")
public class ManageVM extends HttpServlet {
	private static final HashSet<String> validActions;
	static {
		validActions = new HashSet<String>();

		validActions.add("start");
		validActions.add("shutdown");
		validActions.add("destroy");
		validActions.add("reboot");

		validActions.add("vnc");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String action = req.getParameter("action").toLowerCase();
		if(!validActions.contains(action)) {
			resp.sendError(500);
			return;
		}

		final User user = (User)req.getSession().getAttribute("loggedInUser");
		final String vmStr = req.getParameter("vm");

		if(!user.canManage(vmStr)) {
			resp.sendError(403);
			return;
		}

		final LibvirtVM vm = LibvirtVM.getByName(vmStr);
		if(vm == null) {
			resp.sendError(404);
			return;
		}

		if(action.equals("vnc")) {
			resp.getWriter().write(vm.doVNC(true/*req.isSecure()*/));
			return;
		}

		resp.getWriter().println(vm.processCommand(action));
	}
}
