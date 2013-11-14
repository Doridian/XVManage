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

		JSONObject result = new JSONObject();
		result.put("host", node.getIp());
		result.put("password", (String)res.get("password"));
		result.put("ssl", ssl);
		result.put("port", ((Number)res.get("port")).intValue());
		return result.toJSONString();
	}
}
