package de.doridian.xvmanage.models;

import de.doridian.xvmanage.XVMUtils;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public class User implements Serializable {
	protected int level = -1;
	protected String name;
	protected byte[] password;
	protected HashSet<String> machines = new HashSet<String>();

	public static final long serialVersionUID = 1L;

	private final static HashMap<String, User> userStorage;
	static {
		HashMap<String, User> tmp = null;

		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(new File(XVMUtils.XVMANAGE_STORAGE_ROOT, "users.db")));
			tmp = (HashMap<String, User>)objectInputStream.readObject();
			objectInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(tmp == null) {
			userStorage = new HashMap<String, User>();
			User admin = new User();
			admin.setLevel(1000);
			admin.setPassword("admin");
			admin.setName("admin");
			admin.save();
		} else {
			userStorage = tmp;
		}
	}

	public Set<LibvirtVM> getMachines() {
		if(level >= 1000)
			return new HashSet<LibvirtVM>(LibvirtVM.vmStorage.values());

		HashSet<LibvirtVM> ret = new HashSet<LibvirtVM>();

		for(String vmStr : machines) {
			LibvirtVM vm = LibvirtVM.getByName(vmStr);
			if(vm != null)
				ret.add(vm);
		}

		return ret;
	}

	public void setCanManage(final LibvirtVM vm, final boolean can) {
		final String vmN = vm.getName();
		if(can) {
			if(!machines.contains(vmN)) machines.add(vmN);
		} else {
			if(machines.contains(vmN)) machines.remove(vmN);
		}
	}

	public boolean canManage(final LibvirtVM vm) {
		return canManage(vm.getName());
	}

	public boolean canManage(final String name) {
		return level >= 1000 || machines.contains(name);
	}

	public static User getByName(final String username) {
		return userStorage.get(username.toLowerCase());
	}

	public boolean isLoggedInUser() {
		return getLevel() >= 0;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(final int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		if(this.name != null && !this.name.isEmpty())
			userStorage.remove(this.name.toLowerCase());

		this.name = name;

		if(this.name != null && !this.name.isEmpty())
			userStorage.put(this.name.toLowerCase(), this);
	}

	public void save() {
		saveAll();
	}

	public static void saveAll() {
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(XVMUtils.XVMANAGE_STORAGE_ROOT, "users.db")));
			objectOutputStream.writeObject(userStorage);
			objectOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPassword(final String password) {
		this.password = _hashPassword(password);
	}

	public boolean checkPassword(final String password) {
		return Arrays.equals(this.password, _hashPassword(password));
	}

	protected final byte[] _hashPassword(final String password) {
		try {
			return _hashPassword(password.getBytes());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected final byte[] _hashPassword(final byte[] password) {
		try {
			return MessageDigest.getInstance("SHA-1").digest(password);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
