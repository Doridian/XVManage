package de.doridian.xvmanage.navbar;

import de.doridian.xvmanage.models.User;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class NavbarLink {
	private static final Map<String, NavbarLink> NAVBAR_LINK_MAP = new HashMap<String, NavbarLink>();

	public static final NavbarLink[] NAVBAR_LINKS = {
			new NavbarLink("Index", "/index.jsp", -1, 9999),

			//new NavbarLink("Register", "/register.jsp", -1, -1),
			new NavbarLink("Login", "/login.jsp", -1, -1),

			new NavbarLink("VMs", "/vms.jsp", 0, 9999),
			new NavbarLink("Account", "/account.jsp", 0, 9999),
			new NavbarLink("Logout", "/logout.jsp", 0, 9999),

			new NavbarLink(null, "/AdminRun.do", 1000, 9999),
			new NavbarLink("Admin", "/admin.jsp", 1000, 9999),
	};

	public static NavbarLink getForURL(String page) {
		if(page == null)
			return null;
		if(page.equals("/"))
			return NAVBAR_LINK_MAP.get("/index.jsp");
		return NAVBAR_LINK_MAP.get(page);
	}

	private final String title;
	private final String page;
	private final int minLevel;
	private final int maxLevel;

	public NavbarLink(String title, String page, int minLevel, int maxLevel) {
		this.title = title;
		this.page = page;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;

		NAVBAR_LINK_MAP.put(page, this);
	}

	public boolean isAccessible(User currentUser) {
		if(minLevel < 0 && maxLevel >= 9999) return true;
		final int level = currentUser == null ? -1 : currentUser.getLevel();
		return (level >= minLevel) && (level <= maxLevel);
	}

	public boolean isVisible(User currentUser) {
		return this.title != null && isAccessible(currentUser);
	}

	public boolean isActive(HttpServletRequest request) {
		return request.getRequestURI().equals(page) || (page.equals("/index.jsp") && request.getRequestURI().equals("/"));
	}

	public String getHTML(HttpServletRequest request) {
		if(isActive(request)) {
			return "<li class=\"active\"><a href=\""+page+"\">"+title+"</a></li>";
		} else {
			return "<li><a href=\""+page+"\">"+title+"</a></li>";
		}
	}
}
