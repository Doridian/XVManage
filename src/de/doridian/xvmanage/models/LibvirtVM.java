package de.doridian.xvmanage.models;

import de.doridian.xvmanage.XVMAPI;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class LibvirtVM {
	protected String name;

	static {
		VMNode.loadThisClass();
	}

	protected boolean isPoweredOn;

	public float getCpuUsage() {
		return cpuUsage;
	}

	public float getRamUsage() {
		return ramUsage;
	}

	public int getVcpus() {
		return vcpus;
	}

	protected float cpuUsage;
	protected float ramUsage;
	protected int vcpus;

	protected VMNode node;

	protected boolean canBeRunRemoved = false;

	public VMNode getNode() {
		return node;
	}

	public LibvirtVM(VMNode vmNode, JSONObject vmStatus) {
		receivedVMStatus(vmNode, vmStatus);
		setName((String)vmStatus.get("name"));
	}

	public void receivedVMStatus(VMNode vmNode, JSONObject vmStatus) {
		node = vmNode;
		cpuUsage = ((Number)vmStatus.get("cpuUsage")).floatValue();
		ramUsage = ((Number)vmStatus.get("ramUsage")).floatValue();
		vcpus = ((Number)vmStatus.get("vcpus")).intValue();
		isPoweredOn = (Boolean)vmStatus.get("isPoweredOn");
	}

	protected final static HashMap<String, LibvirtVM> vmStorage = new HashMap<String, LibvirtVM>();

	public static LibvirtVM getByName(final String vmname) {
		return vmStorage.get(vmname);
	}

	public String getName() {
		return name;
	}

	private void setName(final String name) {
		if(this.name != null && !this.name.isEmpty())
			vmStorage.remove(this.name);

		this.name = name;

		if(this.name != null && !this.name.isEmpty())
			vmStorage.put(this.name, this);
	}

	public boolean isPoweredOn() {
		return isPoweredOn;
	}

	public String processCommand(String command) throws IOException {
		JSONObject res = new JSONObject();
		res.put("target", "vm");
		res.put("action", command);
		res.put("vm", getName());
		return (String)XVMAPI.apiCall(node, res).get("result");
	}

	public String doVNC(boolean ssl) throws IOException {
		JSONObject res = new JSONObject();
		res.put("target", "vm");
		res.put("action", "vnc");
		res.put("ssl", ssl);
		res.put("vm", getName());

		res = XVMAPI.apiCall(node, res);

		String vncPW = (String)res.get("password");
		int vncPort = ((Number)res.get("port")).intValue();

		return
				"<!DOCTYPE html><html><head>" +
						"<title>VNC: " + getName() + "</title>" +
						"<script src='/static/vnc_include_path.js' type='text/javascript'></script>" +
						"<script src='/static/vnc/util.js' type='text/javascript'></script>" +
						"<script src='/static/vnc/ui.js' type='text/javascript'></script>" +
						"<link rel='stylesheet' href='/static/vnc/base.css' title='plain'>" +
						"</head><body style='margin: 0px; overflow: hidden;'>" +
						"<div id='noVNC_screen'>" +
						"<div id='noVNC_status_bar' class='noVNC_status_bar' style='margin-top: 0px;'>" +
						"<table border=0 width='100%'><tr>" +
						"<td><div id='noVNC_status'>Loading</div></td>" +
						"<td width='1%'><div id='noVNC_buttons'>" +
						"<input type=button value='Send CtrlAltDel' id='sendCtrlAltDelButton'>" +
						"</div></td>" +
						"</tr></table>" +
						"</div>" +
						"<canvas id='noVNC_canvas' width='1024px' height='768px'>" +
						"Canvas not supported." +
						"</canvas>" +
						"</div>" +
						"<script type='text/javascript'>var VNC_HOST = '" + node.getIp() + "'; var VNC_PASSWORD = '" + vncPW + "'; var VNC_PORT = " + vncPort + ";</script>" +
						"<script src='/static/vncapp.js' type='text/javascript'></script>" +
						"</body></html>";
	}
}
