package com.monkey.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.awt.GridBagConstraints.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import com.jme.math.Vector3f;

public class CameraDialog extends JDialog {

	protected Vector3f position;
	protected Vector3f lookAt;
	
	protected boolean status;
	
	public CameraDialog(Frame owner) {
		super(owner, "Set Camera...", true);
		
		status = false;
		addComponents();
		addListeners();

		pack();
		setVisible(true);
	}
	
	protected void addComponents() {
		JPanel posPanel = new JPanel();
		posPanel.setLayout(new GridBagLayout());
		posPanel.add(posLabel, GBC.makeGBC(0, 0, BOTH, 0, 1));
		posPanel.add(posXField, GBC.makeGBC(1, 0, BOTH, 0.33, 1));
		posPanel.add(posYField, GBC.makeGBC(2, 0, BOTH, 0.33, 1));
		posPanel.add(posZField, GBC.makeGBC(3, 0, BOTH, 0.33, 1));

		JPanel lookAtPanel = new JPanel();
		lookAtPanel.setLayout(new GridBagLayout());
		lookAtPanel.add(lookAtLabel, GBC.makeGBC(0, 0, BOTH, 0, 1));
		lookAtPanel.add(lookAtXField, GBC.makeGBC(1, 0, BOTH, 0.33, 1));
		lookAtPanel.add(lookAtYField, GBC.makeGBC(2, 0, BOTH, 0.33, 1));
		lookAtPanel.add(lookAtZField, GBC.makeGBC(3, 0, BOTH, 0.33, 1));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		Container pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		pane.add(posPanel, GBC.makeGBC(0, 0, BOTH, 1, 0.0));
		pane.add(lookAtPanel, GBC.makeGBC(0, 1, BOTH, 1, 0.3));
		pane.add(buttonPanel, GBC.makeGBC(0, 2, BOTH, 1, 0.3));
	}
	
	protected void addListeners() {
		final JDialog parent = this;
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					validateData();
					status = true;
					dispose();
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(parent, "Failed validation", "Error", ERROR_MESSAGE);
				}
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				status = false;
				dispose();
			}
		});
	}
	
	public boolean getStatus() {
		return status;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getLookAt() {
		return lookAt;
	}
	
	protected void validateData() throws Exception {
		float x = Float.parseFloat(posXField.getText());
		float y = Float.parseFloat(posYField.getText());
		float z = Float.parseFloat(posZField.getText());
		position = new Vector3f(x,y,z);
		
		x = Float.parseFloat(lookAtXField.getText());
		y = Float.parseFloat(lookAtYField.getText());
		z = Float.parseFloat(lookAtZField.getText());
		lookAt = new Vector3f(x,y,z);
	}
	
	protected JLabel posLabel = new JLabel("Position");
	
	protected JTextField posXField = new JTextField();
	protected JTextField posYField = new JTextField();
	protected JTextField posZField = new JTextField();

	protected JLabel lookAtLabel = new JLabel("Look At");
	
	protected JTextField lookAtXField = new JTextField();
	protected JTextField lookAtYField = new JTextField();
	protected JTextField lookAtZField = new JTextField();
	
	protected JButton okButton = new JButton("Ok");
	protected JButton cancelButton = new JButton("Cancel");
}
