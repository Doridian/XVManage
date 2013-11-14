// Copyright (C) 2010, 2011, 2012, 2013 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//-------------------------------------------------------------------------
//

package com.glavsoft.viewer.swing;

import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.mvp.Model;

/**
* @author dime at tightvnc.com
*/
public class ConnectionParams implements Model {
	private static final int DEFAULT_RFB_PORT = 5900;

	public String hostName;
	private int portNumber;

	private boolean useSSL;

	public ConnectionParams(String hostName, int portNumber, boolean useSSL) {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.useSSL = useSSL;
	}

	public ConnectionParams(ConnectionParams cp) {
		this.hostName = cp.hostName != null? cp.hostName: "";
		this.portNumber = cp.portNumber;
		this.useSSL = cp.useSSL;
	}

	public ConnectionParams() {
		hostName = "";
	}

	public boolean isHostNameEmpty() {
		return Strings.isTrimmedEmpty(hostName);
	}

	public void parseRfbPortNumber(String port) throws WrongParameterException {
		try {
			portNumber = Integer.parseInt(port);
		} catch (NumberFormatException e) {
            portNumber = 0;
            if ( ! Strings.isTrimmedEmpty(port)) {
                throw new WrongParameterException("Wrong port number: " + port + "\nMust be in 0..65535");
            }
        }
        if (portNumber > 65535 || portNumber < 0) throw new WrongParameterException("Port number is out of range: " + port + "\nMust be in 0..65535");
	}

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public String getHostName() {
        return this.hostName;
    }

    public void setPortNumber(String port) throws WrongParameterException {
        this.parseRfbPortNumber(port);
    }

    public void setPortNumber(int port) {
        this.portNumber = port;
    }

    public int getPortNumber() {
        return 0 == portNumber ? DEFAULT_RFB_PORT : portNumber;
    }

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public boolean getUseSSL() {
		return true; // || useSSL;
	}

    public void completeEmptyFieldsFrom(ConnectionParams from) {
        if (null == from) return;
		if (Strings.isTrimmedEmpty(hostName) && ! Strings.isTrimmedEmpty(from.hostName)) {
			hostName = from.hostName;
		}
        if ( 0 == portNumber && from.portNumber != 0) {
			portNumber = from.portNumber;
		}
	}

	@Override
	public String toString() {
		return hostName != null ? hostName : "";
//        return (hostName != null ? hostName : "") + ":" + portNumber + " " + useSsh + " " + sshUserName + "@" + sshHostName + ":" + sshPortNumber;
    }

    public String toPrint() {
        return "ConnectionParams{" +
                "hostName='" + hostName + '\'' +
                ", portNumber=" + portNumber +
				", useSSL=" + useSSL +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || ! (obj instanceof ConnectionParams)) return false;
        if (this == obj) return true;
        ConnectionParams o = (ConnectionParams) obj;
        return isEqualsNullable(hostName, o.hostName) && getPortNumber() == o.getPortNumber() && getUseSSL() == o.getUseSSL();
    }

    private boolean isEqualsNullable(String one, String another) {
        //noinspection StringEquality
        return one == another || (null == one? "" : one).equals(null == another? "" : another);
    }

    @Override
    public int hashCode() {
        long hash = (hostName != null? hostName.hashCode() : 0) +
                portNumber * 17 + (useSSL ? 33 : 0);
        return (int)hash;
    }

    public void clearFields() {
        hostName = "";
        portNumber = 0;
		useSSL = false;
    }
}
