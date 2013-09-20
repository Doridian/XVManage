package de.doridian.xvmanage.navbar;

import de.doridian.xvmanage.models.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class NavbarFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;

		User user = (User)request.getSession().getAttribute("loggedInUser");
		NavbarLink navbarLink = NavbarLink.getForURL(request.getRequestURI());
		if(navbarLink != null && !navbarLink.isAccessible(user)) {
			if(request.getParameter("ajax") != null) {
				response.sendError(403);
				return;
			} else if(user != null && user.isLoggedInUser()) {
				response.sendRedirect("/index.jsp");
				return;
			} else {
				response.sendRedirect("/login.jsp?returnURL=" + request.getRequestURI());
				return;
			}
		}

		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}
}
