package de.doridian.xvmanage.node;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

public class LibvirtVM {
	protected String name;

	private long lastCPUTime = 1;
	private long lastCPUTimeTime = System.nanoTime();

	private float cpuResult = 0;

	public float getCpuUsage() {
		final long time = domainInfoTime;

		if(lastCPUTimeTime != time) {
			final long cpuTime = domainInfo.cpuTime;

			cpuResult = ((float)(cpuTime - lastCPUTime)) / ((float)(time - lastCPUTimeTime));

			lastCPUTime = cpuTime;
			lastCPUTimeTime = time;
		}

		return cpuResult * 100.0F;
	}

	public float getRamUsage() {
		return ((float)domainInfo.memory) * 100.0F / ((float)domainInfo.maxMem);
	}

	public int getVcpus() {
		return domainInfo.nrVirtCpu;
	}

	protected static HashMap<String, LibvirtVM> vmStorage;

	private static Connect libvirtConnection;

	private Domain libvirtDomain;

	private long domainInfoTime = 0;
	private DomainInfo domainInfo;

	static {
		vmStorage = new HashMap<String, LibvirtVM>();

		try {
			libvirtConnection = new Connect("qemu:///system");
		} catch (Exception e) {
			e.printStackTrace();
		}

		new Thread() {
			@Override
			public void run() {
				while(true) {
					readVMs();
					try {
						Thread.sleep(60000);
					} catch (Exception e) { }
				}
			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				while(true) {
					for(LibvirtVM vm : vmStorage.values()) {
						vm.refresh();
					}
					try {
						Thread.sleep(5000);
					} catch (Exception e) { }
				}
			}
		}.start();
	}

	public static JSONArray getVMList() {
		JSONArray ret = new JSONArray();
		for(LibvirtVM vm : vmStorage.values()) {
			ret.add(vm.getStatusObject());
		}
		return ret;
	}

	public static String runSystemCommand(final String command) throws IOException {
		StringBuilder ret = new StringBuilder(); String line;

		ret.append(command); ret.append('\n');

		Process proc = Runtime.getRuntime().exec(command);

		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		while((line = reader.readLine()) != null) {
			ret.append(line); ret.append('\n');
		}
		reader.close();
		return ret.toString().trim();
	}

	public static void readVMs() {
		try {
			int[] domains = libvirtConnection.listDomains();

			HashMap<String, LibvirtVM> newVMList = new HashMap<String, LibvirtVM>(vmStorage);

			for(int domain : domains) {
				Domain lvDomain = libvirtConnection.domainLookupByID(domain);
				String name = lvDomain.getName();
				LibvirtVM newVM;
				if(vmStorage.containsKey(name)) {
					newVM = vmStorage.get(name);
				} else {
					newVM = new LibvirtVM();
					newVM.name = name;
				}
				newVM.libvirtDomain = lvDomain;
				newVM.refresh();
				newVMList.put(name, newVM);
			}

			vmStorage = newVMList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}
	}

	private void refresh() {
		try {
			domainInfo = libvirtDomain.getInfo();
			domainInfoTime = System.nanoTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static LibvirtVM getByName(final String vmname) {
		return vmStorage.get(vmname);
	}

	public String getName() {
		return name;
	}

	public boolean isPoweredOn() {
		return domainInfo.state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING;
	}

	public int getVncPort() {
		try {
			String ret = runSystemCommand("/usr/bin/virsh vncdisplay " + name);
			int port = Integer.parseInt(ret.substring(ret.indexOf(':') + 1));
			return port + 5900;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected JSONObject doVNC(boolean ssl) throws IOException {
		final String vncPW = XVMUtils.randomString(10);
		int vncPort;

		try {
			Socket sock = new Socket();
			sock.setTcpNoDelay(true);
			sock.connect(new InetSocketAddress("127.0.0.1", 8888));
			OutputStream outputStream = sock.getOutputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			ssl = false;

			outputStream.write(("{\"daddr\":\"127.0.0.1\", \"dport\":" + getVncPort() + ", \"password\":\"" + vncPW + "\", \"ws\":false, \"tls\":" + (ssl ? "true" : "false") + "}\r\n").getBytes());
			outputStream.flush();

			vncPort = Integer.parseInt(bufferedReader.readLine().trim());
			sock.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}

		JSONObject ret = new JSONObject();
		ret.put("port", vncPort);
		ret.put("password", vncPW);
		return ret;
	}

	public JSONObject getStatusObject() {
		JSONObject thisVM = new JSONObject();
		thisVM.put("name", name);
		thisVM.put("isPoweredOn", isPoweredOn());
		thisVM.put("cpuUsage", getCpuUsage());
		thisVM.put("ramUsage", getRamUsage());
		thisVM.put("vcpus", getVcpus());
		return thisVM;
	}

	public String processCommand(String action) throws LibvirtException {
		if(action.equals("status")) {
			return getStatusObject().toJSONString();
		} else if(action.equals("start")) {
			libvirtDomain.create();
		} else if(action.equals("shutdown")) {
			libvirtDomain.shutdown();
		} else if(action.equals("destroy")) {
			libvirtDomain.destroy();
		} else if(action.equals("reboot")) {
			libvirtDomain.reboot(0);
		} else if(action.equals("reset")) {
			libvirtDomain.destroy();
			libvirtDomain.create();
		}
		JSONObject result = new JSONObject();
		result.put("result", "okay");
		return result.toJSONString();
	}
}
