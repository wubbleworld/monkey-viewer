package com.monkey.swing;

import static java.awt.GridBagConstraints.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;

import com.monkey.MonkeyScene;

import com.jme.math.*;
import com.jme.scene.*;

public class TransformPanel extends JPanel {

	protected MonkeyScene scene;
	protected Spatial selectedObject;
	
	protected boolean ignoreStateChange;

	public TransformPanel(MonkeyScene monkeyScene) {
		scene = monkeyScene;
		
		ignoreStateChange = false;
		
		addComponents();
		addListeners();
	}
	
	protected void addComponents() {
		descFields = new JSpinner[3][3];
		descWorldFields = new JSpinner[3][3];	
		
		setLayout(new GridBagLayout());
		add(addFields(descFields, false), GBC.makeGBC(0,1,BOTH,0,0));
		add(addFields(descWorldFields, true), GBC.makeGBC(0,2,BOTH,0,0));
		
	}
	
	protected void addListeners() {
	    addTranslationListener(descFields[0]);
		addRotationListener(descFields[1]);
	    addScaleListener(descFields[2]);
	}
	
	protected void setFieldValues(JSpinner[] fields, Vector3f values) {
		fields[0].setValue(values.x);
		fields[1].setValue(values.y);
		fields[2].setValue(values.z);
	}
	
	protected void setFieldValues(JSpinner[] fields, Quaternion values) {
		float[] angles = new float[3];
		values.toAngles(angles);
		
		for (int i = 0; i < angles.length; ++i) {
			fields[0].setValue(angles[i]*FastMath.RAD_TO_DEG);
		}
	}

	protected JPanel addFields(JSpinner[][] fields, boolean world) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		String[] labels = new String[3];
		labels[0] = "Position";
		labels[1] = "Rotation";
		labels[2] = "Scale";

		for (int i = 0; i < 3; ++i) {
			panel.add(new JLabel(labels[i]), GBC.makeGBC(0,i,BOTH,new Insets(5,5,5,5),0.0,0.0));
			
			for (int j = 0; j < 3; ++j) {
				SpinnerModel sm = new SpinnerNumberModel(0.0, -10000, 10000, 1.0); 
				fields[i][j] = new JSpinner(sm);
				panel.add(fields[i][j], GBC.makeGBC(j+1,i,BOTH,new Insets(5,5,5,5),0.3,0.0));
				
				if (world) 
					fields[i][j].setEnabled(false);
			}
		}
		return panel;
	}

	/*
	 * add the translation listeners to the right spinner fields
	 * use a hacked update object to actually change the transforms
	 */
	protected void addTranslationListener(JSpinner[] fields) {
	    for (int i = 0; i < 3; ++i) {
	    	final int spinI = i;
	    	final JSpinner spinny = fields[i];
	    	spinny.addChangeListener(new ChangeListener() {
	    		public void stateChanged(ChangeEvent arg0) {
	    			if (ignoreStateChange) return;
	    			if (selectedObject == null) return;
	    			float value = (float) ((Double) spinny.getValue()).doubleValue();
	    			updateObject(value, 0, spinI);
				}
	    	});
	    }
	}

	protected void addScaleListener(JSpinner[] fields) {
	    for (int i = 0; i < 3; ++i) {
	    	final int spinI = i;
	    	final JSpinner spinny = fields[i];
	    	spinny.addChangeListener(new ChangeListener() {
	    		public void stateChanged(ChangeEvent arg0) {
	    			if (ignoreStateChange) return;
	    			if (selectedObject == null) return;
	    			float value = (float) ((Double) spinny.getValue()).doubleValue();
	    			updateObject(value, 1, spinI);
				}
	    	});
	    }
	}
	
	protected void addRotationListener(JSpinner[] fields) {
	    for (int i = 0; i < 3; ++i) {
	    	final int spinI = i;
	    	final JSpinner spinny = fields[i];
	    	spinny.addChangeListener(new ChangeListener() {
	    		public void stateChanged(ChangeEvent arg0) {
	    			if (ignoreStateChange) return;
	    			if (selectedObject == null) return;
	    			
	    			float value = (float) ((Double) spinny.getValue()).doubleValue();
	    			updateObject(value, 2, spinI);
				}
	    	});
	    }
	}	
	
	public void setSelectedObject(Spatial s) {
    	selectedObject = s;
    	ignoreStateChange = true;
    	
    	setFieldValues(descFields[0], s.getLocalTranslation());
    	setFieldValues(descFields[1], s.getLocalRotation());
    	setFieldValues(descFields[2], s.getLocalScale());
    	
    	setFieldValues(descWorldFields[0], s.getWorldTranslation());
    	setFieldValues(descWorldFields[1], s.getWorldRotation());
    	setFieldValues(descWorldFields[2], s.getWorldScale());
    	
    	ignoreStateChange = false;
	}
	
	protected void updateObject(float value, int transform, int index) {
		switch (index) {
		case 0:
			if (transform == 0) 
				selectedObject.getLocalTranslation().setX(value);
			else if (transform == 1)
				selectedObject.getLocalScale().setX(value);
			else if (transform == 2) {
				float[] angles = new float[3];
				Quaternion local = selectedObject.getLocalRotation();
				local.toAngles(angles);
				local.fromAngles(value*FastMath.DEG_TO_RAD, angles[1], angles[2]);
				selectedObject.setLocalRotation(local);
			}
			break;
		case 1:
			if (transform == 0) 
				selectedObject.getLocalTranslation().setY(value);
			else if (transform == 1)
				selectedObject.getLocalScale().setY(value);
			else if (transform == 2) {
				float[] angles = new float[3];
				Quaternion local = selectedObject.getLocalRotation();
				local.toAngles(angles);
				local.fromAngles(angles[0], value*FastMath.DEG_TO_RAD, angles[2]);
				selectedObject.setLocalRotation(local);
			}
			break;
		case 2:
			if (transform == 0) 
				selectedObject.getLocalTranslation().setZ(value);
			else if (transform == 1)
				selectedObject.getLocalScale().setZ(value);
			else if (transform == 2) {
				float[] angles = new float[3];
				Quaternion local = selectedObject.getLocalRotation();
				local.toAngles(angles);
				local.fromAngles(angles[0], angles[1], value*FastMath.DEG_TO_RAD);
				selectedObject.setLocalRotation(local);
			}
			break;
		}
		selectedObject.updateWorldData(0.0f);
		scene.notifyModelRefresh();
	}	
	
	protected JSpinner[][] descFields;
	protected JSpinner[][] descWorldFields;

}
