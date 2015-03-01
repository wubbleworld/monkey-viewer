package com.monkey.swing;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.*;

import static java.awt.GridBagConstraints.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.util.*;
import java.util.concurrent.Callable;

import com.jme.math.*;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.*;
import com.jme.scene.state.*;
import com.jme.util.GameTaskQueueManager;
import com.jme.light.*;

import com.monkey.MonkeyScene;

public class LightPanel extends JPanel {

	protected MonkeyScene scene;
	protected LightState selectedLightState;
	protected boolean ignoreChangeState;
	protected boolean changingSpinner;
	protected int selectedLight;
	
	protected float[] values = new float[3];
	
	public LightPanel(MonkeyScene scene) {
		this.scene = scene;
		
		ignoreChangeState = false;
		
		addComponents();
		addListeners();
	}
	
	protected void addComponents() {
		setLayout(new GridBagLayout());
		
		lightList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane scroll = new JScrollPane(lightList);
		scroll.setPreferredSize(new Dimension(400,200));
		scroll.getViewport().setPreferredSize(new Dimension(400,300)); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll, GBC.makeGBC(0, 0, 4, 1, BOTH, 1.0, 1.0));
		
		add(lightLabel, GBC.makeGBC(0, 1, BOTH, 0, 0));
		for (int i = 0; i < lightValues.length; ++i) {
			lightValues[i] = new JSpinner();
			add(lightValues[i], GBC.makeGBC(i+1, 1, BOTH, new Insets(5,5,5,5), 0.3, 0));
		}

		popup.add(addPointItem);
		popup.add(addDirectionalItem);
		popup.add(deleteItem);		
	}
	
	protected void addListeners() {
		lightList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int selectedIndex = lightList.getSelectedIndex();
				if (selectedIndex < 0) {
					setEnabledSpinners(false);
					return;
				}
				
				ignoreChangeState = true;
				setEnabledSpinners(true);
				Light l = selectedLightState.get(selectedIndex);
				if (l.getType() == Light.LT_POINT) {
					lightLabel.setText("Position");
					
					Vector3f pos = ((PointLight) l).getLocation();
					lightValues[0].setModel(new SpinnerNumberModel(pos.x, -10000.0, 10000.0, 1.0));
					lightValues[1].setModel(new SpinnerNumberModel(pos.y, -10000.0, 10000.0, 1.0));
					lightValues[2].setModel(new SpinnerNumberModel(pos.z, -10000.0, 10000.0, 1.0));
				} else if (l.getType() == Light.LT_DIRECTIONAL) {
					lightLabel.setText("Direction");
					
					Vector3f dir = ((DirectionalLight) l).getDirection();
					lightValues[0].setModel(new SpinnerNumberModel(dir.x, -1.0, 1.0, 0.01));
					lightValues[1].setModel(new SpinnerNumberModel(dir.y, -1.0, 1.0, 0.01));
					lightValues[2].setModel(new SpinnerNumberModel(dir.z, -1.0, 1.0, 0.01));
				}
				ignoreChangeState = false;
			}
		});
		
	    MouseListener mouseListener = new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getButton() == MouseEvent.BUTTON3) {
					popup.show(lightList, arg0.getX(), arg0.getY());
				}
			}

			public void mouseEntered(MouseEvent e) { }
			public void mouseExited(MouseEvent e) { }

			public void mousePressed(MouseEvent e) { }
			public void mouseReleased(MouseEvent e) { }
	    };
	    lightList.addMouseListener(mouseListener);	
	    
	    deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (selectedLightState != null && lightList.getSelectedIndex() >= 0) {
					int index = lightList.getSelectedIndex();
					selectedLightState.detach(selectedLightState.get(index));
					
		            Callable<?> call = new Callable<Object>() {
		                public Object call() throws Exception {
		                	scene.getRootNode().updateRenderState();
		                   	return null;
		                }
		            };
		            GameTaskQueueManager.getManager().update(call);					
		            scene.notifyModelRefresh();
				}
			}
		});	
	    
	    addPointItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent evt) {
	    		if (selectedLightState == null)
	    			return;
	    		
	    		LightDialog ld = new LightDialog(null, "Position");
				if (!ld.getStatus()) {
					return;
				}

				final Vector3f data = ld.getData();
				PointLight pl = new PointLight();
				pl.setLocation(data);
				pl.setEnabled(true);
				selectedLightState.attach(pl);
	            scene.updateRenderState();
				scene.notifyModelRefresh();
	    	}
	    });
	    
	    addDirectionalItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent evt) {
	    		if (selectedLightState == null)
	    			return;
	    		
	    		LightDialog ld = new LightDialog(null, "Direction");
				if (!ld.getStatus()) {
					return;
				}

				final Vector3f data = ld.getData();
				DirectionalLight pl = new DirectionalLight();
				pl.setDirection(data);
				pl.setEnabled(true);
				selectedLightState.attach(pl);
				scene.updateRenderState();
	            scene.notifyModelRefresh();
	    	}
	    });
	    
	    for (int i = 0; i < lightValues.length; ++i) {
	    	final int x = i;
	    	lightValues[i].addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					if (ignoreChangeState) return;
					if (selectedLightState == null) return;
					if (lightList.getSelectedIndex() < 0) return;
					
					changingSpinner = true;
					selectedLight = lightList.getSelectedIndex();
					
					JSpinner spinny = (JSpinner) arg0.getSource();
					values[x] = (float) ((Double) spinny.getValue()).doubleValue();
					Vector3f v = new Vector3f(values[0], values[1], values[2]);
					Light l = selectedLightState.get(selectedLight);
					if (l instanceof PointLight) {
						((PointLight) l).setLocation(v);
					} else if (l instanceof DirectionalLight) {
						((DirectionalLight) l).setDirection(v);
					}
					scene.updateRenderState();
					scene.notifyModelRefresh();
				}
	    	});
	    }
	}

	public void setSelectedObject(Spatial s) {
		ignoreChangeState = true;
		if (s.getRenderState(RenderState.RS_LIGHT) == null) {
			lightList.setModel(new DefaultListModel());
			setEnabledSpinners(false);
			ignoreChangeState = false;
			return;
		}
		
		if (changingSpinner) {
			changingSpinner = false;
			ignoreChangeState = false;
			return;
		}
		
		DefaultListModel dlm = (DefaultListModel) lightList.getModel();
		dlm.clear();
		selectedLightState = (LightState) s.getRenderState(RenderState.RS_LIGHT);
		for (int i = 0; i < selectedLightState.getQuantity(); ++i) {
			if (selectedLightState.get(i) != null) {
				Light l = selectedLightState.get(i);
				if (l.getType() == Light.LT_POINT) 
					dlm.addElement("Point");
				else if (l.getType() == Light.LT_DIRECTIONAL) 
					dlm.addElement("Directional");
				else
					System.out.println("Unsupported light");
			}
		}
		ignoreChangeState = false;
	}
	
	protected void setEnabledSpinners(boolean enabled) {
		lightValues[0].setEnabled(enabled);
		lightValues[1].setEnabled(enabled);
		lightValues[2].setEnabled(enabled);
	}
	
	private JList lightList = new JList(new DefaultListModel());
	
	private JSpinner[] lightValues = new JSpinner[3];
	private JLabel lightLabel = new JLabel("Position");
	
	protected JPopupMenu popup             = new JPopupMenu();
	protected JMenuItem addPointItem       = new JMenuItem("Add Point Light");
	protected JMenuItem addDirectionalItem = new JMenuItem("Add Directional Light");
	protected JMenuItem deleteItem         = new JMenuItem("Delete Light");	
}

class LightDialog extends JDialog {

	protected Vector3f data;
	
	protected boolean status;
	
	public LightDialog(Frame owner, String labelName) {
		super(owner, "Set Camera...", true);
		
		status = false;
		addComponents(labelName);
		addListeners();

		pack();
		setVisible(true);
	}
	
	protected void addComponents(String labelName) {
		label = new JLabel(labelName);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.add(label, GBC.makeGBC(0, 0, BOTH, 0, 1));
		panel.add(xField, GBC.makeGBC(1, 0, BOTH, 0.33, 1));
		panel.add(yField, GBC.makeGBC(2, 0, BOTH, 0.33, 1));
		panel.add(zField, GBC.makeGBC(3, 0, BOTH, 0.33, 1));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		
		Container pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		pane.add(panel, GBC.makeGBC(0, 0, BOTH, 1, 0.0));
		pane.add(buttonPanel, GBC.makeGBC(0, 1, BOTH, 1, 0.3));
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
	
	public Vector3f getData() {
		return data;
	}
	
	protected void validateData() throws Exception {
		float x = Float.parseFloat(xField.getText());
		float y = Float.parseFloat(yField.getText());
		float z = Float.parseFloat(zField.getText());
		data = new Vector3f(x,y,z);
	}
	
	protected JLabel label;
	
	protected JTextField xField = new JTextField();
	protected JTextField yField = new JTextField();
	protected JTextField zField = new JTextField();

	protected JButton okButton = new JButton("Ok");
	protected JButton cancelButton = new JButton("Cancel");
}
