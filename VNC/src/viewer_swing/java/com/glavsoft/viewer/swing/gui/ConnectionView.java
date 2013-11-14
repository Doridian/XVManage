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

package com.glavsoft.viewer.swing.gui;

import com.glavsoft.viewer.mvp.View;
import com.glavsoft.viewer.swing.ConnectionParams;
import com.glavsoft.viewer.ConnectionPresenter;
import com.glavsoft.viewer.swing.Utils;
import com.glavsoft.viewer.swing.WrongParameterException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * Dialog window for connection parameters get from.
 */
@SuppressWarnings("serial")
public class ConnectionView extends JPanel implements View {
    private static final int PADDING = 4;
    public static final int COLUMNS_HOST_FIELD = 30;
    public static final int COLUMNS_PORT_USER_FIELD = 13;
    public static final String CLOSE = "Close";
    public static final String CANCEL = "Cancel";
    private WindowListener appWindowListener;
    private final JTextField serverPortField;
	private final JComboBox serverNameCombo;
    private JButton clearHistoryButton;
    private JButton connectButton;
    private final JFrame view;
    private final ConnectionPresenter presenter;
    private final StatusBar statusBar;
    private boolean connectionInProgress;
    private JButton closeCancelButton;

    public ConnectionView(final WindowListener appWindowListener,
                          final ConnectionPresenter presenter) {
        this.appWindowListener = appWindowListener;
        this.presenter = presenter;

        setLayout(new BorderLayout(0, 0));
		JPanel optionsPane = new JPanel(new GridBagLayout());
		add(optionsPane, BorderLayout.CENTER);
		optionsPane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		setLayout(new GridBagLayout());

		int gridRow = 0;

        serverNameCombo = new JComboBox();
        initConnectionsHistoryCombo();
        serverNameCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Object item = serverNameCombo.getSelectedItem();
                if (item instanceof ConnectionParams) {
                    presenter.populateFromHistoryItem((ConnectionParams) item);
                }
            }
        });

        addFormFieldRow(optionsPane, gridRow, new JLabel("Remote Host:"), serverNameCombo, true);
        ++gridRow;

        serverPortField = new JTextField(COLUMNS_PORT_USER_FIELD);

        addFormFieldRow(optionsPane, gridRow, new JLabel("Port:"), serverPortField, false);
        ++gridRow;

        JPanel buttonPanel = createButtons();

		GridBagConstraints cButtons = new GridBagConstraints();
		cButtons.gridx = 0; cButtons.gridy = gridRow;
		cButtons.weightx = 100; cButtons.weighty = 100;
		cButtons.gridwidth = 2; cButtons.gridheight = 1;
		optionsPane.add(buttonPanel, cButtons);

        view = new JFrame("New TightVNC Connection");
        view.add(this, BorderLayout.CENTER);
        statusBar = new StatusBar();
        view.add(statusBar, BorderLayout.SOUTH);

        view.getRootPane().setDefaultButton(connectButton);
        view.addWindowListener(appWindowListener);
//        view.setResizable(false);
        Utils.decorateDialog(view);
		Utils.centerWindow(view);
	}

    private void initConnectionsHistoryCombo() {
        serverNameCombo.setEditable(true);

        new AutoCompletionComboEditorDocument(serverNameCombo); // use autocompletion feature for ComboBox
        serverNameCombo.setRenderer(new HostnameComboboxRenderer());

        ConnectionParams prototypeDisplayValue = new ConnectionParams();
        prototypeDisplayValue.hostName = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXЧЧ";
        serverNameCombo.setPrototypeDisplayValue(prototypeDisplayValue);
    }

    public void showReconnectDialog(final String title, final String message) {
            JOptionPane reconnectPane = new JOptionPane(message + "\nTry another connection?",
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
            final JDialog reconnectDialog = reconnectPane.createDialog(ConnectionView.this, title);
            Utils.decorateDialog(reconnectDialog);
            reconnectDialog.setVisible(true);
            if (reconnectPane.getValue() == null ||
                    (Integer)reconnectPane.getValue() == JOptionPane.NO_OPTION) {
                presenter.setNeedReconnection(false);
                closeView();
                view.dispose();
                closeApp();
            } else {
                // TODO return when allowInteractive, close window otherwise
//                forceConnectionDialog = allowInteractive;
            }
    }

    public void setConnectionInProgress(boolean enable) {
        if (enable) {
            connectionInProgress = true;
            closeCancelButton.setText(CANCEL);
            connectButton.setEnabled(false);
        } else {
            connectionInProgress = false;
            closeCancelButton.setText(CLOSE);
            connectButton.setEnabled(true);
        }
    }

    private JPanel createButtons() {
        JPanel buttonPanel = new JPanel();

        closeCancelButton = new JButton(CLOSE);
        closeCancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectionInProgress) {
                    presenter.cancelConnection();
                    setConnectionInProgress(false);
                } else {
                    closeView();
                    closeApp();
                }
            }
        });

        connectButton = new JButton("Connect");
        buttonPanel.add(connectButton);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMessage("");
                Object item = serverNameCombo.getSelectedItem();
                String hostName = item instanceof ConnectionParams ?
                        ((ConnectionParams) item).hostName :
                        (String) item;
                try {
                    setConnectionInProgress(true);
                    presenter.submitConnection(hostName);
                } catch (WrongParameterException wpe) {
                    if (ConnectionPresenter.PROPERTY_HOST_NAME.equals(wpe.getPropertyName())) {
                        serverNameCombo.requestFocusInWindow();
                    }
                    if (ConnectionPresenter.PROPERTY_RFB_PORT_NUMBER.equals(wpe.getPropertyName())) {
                        serverPortField.requestFocusInWindow();
                    }
                    showConnectionErrorDialog(wpe.getMessage());
                    setConnectionInProgress(false);
                }
            }
        });

        JButton optionsButton = new JButton("Options...");
        buttonPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDialog od = new OptionsDialog(view);
                od.initControlsFromSettings(presenter.getRfbSettings(), presenter.getUiSettings(), true);
                od.setVisible(true);
                view.toFront();
            }
        });

        clearHistoryButton = new JButton("Clear history");
        clearHistoryButton.setToolTipText("Clear connections history");
        buttonPanel.add(clearHistoryButton);
        clearHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                presenter.clearHistory();
                clearHistoryButton.setEnabled(false);
                view.toFront();
            }
        });
        buttonPanel.add(closeCancelButton);
        return buttonPanel;
    }

    private void addFormFieldRow(JPanel pane, int gridRow, JLabel label, JComponent field, boolean fill) {
        GridBagConstraints cLabel = new GridBagConstraints();
        cLabel.gridx = 0; cLabel.gridy = gridRow;
        cLabel.weightx = 0;
        cLabel.weighty = 100;
        cLabel.gridwidth = cLabel.gridheight = 1;
        cLabel.anchor = GridBagConstraints.LINE_END;
        cLabel.ipadx = PADDING;
        cLabel.ipady = 10;
        pane.add(label, cLabel);

        GridBagConstraints cField = new GridBagConstraints();
        cField.gridx = 1; cField.gridy = gridRow;
        cField.weightx = 0; cField.weighty = 100;
        cField.gridwidth = cField.gridheight = 1;
        cField.anchor = GridBagConstraints.LINE_START;
        if (fill) cField.fill = GridBagConstraints.HORIZONTAL;
        pane.add(field, cField);
    }

    /*
     * Implicit View interface
     */
    public void setMessage(String message) {
        statusBar.setMessage(message);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPortNumber(int portNumber) {
        serverPortField.setText(String.valueOf(portNumber));
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getPortNumber() {
        return serverPortField.getText();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setConnectionsList(LinkedList<ConnectionParams> connections) {
        serverNameCombo.removeAllItems();
        for (ConnectionParams cp : connections) {
            serverNameCombo.addItem(new ConnectionParams(cp));
        }
        serverNameCombo.setPopupVisible(false);
        clearHistoryButton.setEnabled(serverNameCombo.getItemCount() > 0);
    }
    /*
     * /Implicit View interface
     */

    @Override
    public void showView() {
        view.setVisible(true);
        view.toFront();
        view.repaint();
    }

    @Override
    public void closeView() {
        view.setVisible(false);
    }

    public void showConnectionErrorDialog(final String message) {
        JOptionPane errorPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        final JDialog errorDialog = errorPane.createDialog(view, "Connection error");
        Utils.decorateDialog(errorDialog);
        errorDialog.setVisible(true);
        if ( ! presenter.allowInteractive()) {
            presenter.cancelConnection();
            closeApp();
        }
    }

    public void closeApp() {
        appWindowListener.windowClosing(null);
    }

    public JFrame getFrame() {
        return view;
    }

}

class StatusBar extends JPanel {

    private JLabel messageLabel;

    public StatusBar() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 23));

        messageLabel = new JLabel("");
        final Font f = messageLabel.getFont();
        messageLabel.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
        add(messageLabel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);


        add(rightPanel, BorderLayout.EAST);
        setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Color oldColor = g.getColor();
                g.translate(x, y);
                g.setColor(c.getBackground().darker());
                g.drawLine(0, 0, width -1, 0);
                g.setColor(c.getBackground().brighter());
                g.drawLine(0, 1, width -1, 1);
                g.translate(-x, -y);
                g.setColor(oldColor);
            }
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(2, 2, 2, 2);
            }
            @Override
            public boolean isBorderOpaque() {
                return false;
            }
        });
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
