package com.monkey.swing;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;

import com.jme.scene.*;
import com.jme.scene.state.*;
import com.jme.renderer.*;

import com.monkey.MonkeyScene;

import static java.awt.GridBagConstraints.*;
import static com.monkey.swing.GBC.*;

public class MaterialPanel extends JPanel {

	protected MonkeyScene scene;
	protected Spatial selectedObject;
	protected MaterialState selectedMaterial;
	
	protected float[][] activeValue;
	protected boolean ignoreStateChange;

	public MaterialPanel(MonkeyScene monkeyScene) {
		scene = monkeyScene;
		
		activeValue = new float[4][4];
		ignoreStateChange = false;
		
		addComponents();
		addListeners();
	}
	
	protected void addComponents() {
		setLayout(new GridBagLayout());
		
		for (int i = 0; i < colorNames.length; ++i) {
			add(colorNames[i], makeGBC(0, i, BOTH, 0, 0));
			for (int j = 0; j < colorValues[i].length; ++j) {
				colorValues[i][j] = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1.0, 0.01));
				add(colorValues[i][j], makeGBC(j+1, i, BOTH, new Insets(5,5,5,5), 0.25, 0));
			}
		}
		
		add(shininessLabel, makeGBC(0, 4, BOTH, 0, 0));
		add(shininessValue, makeGBC(1, 4, 4, 1, BOTH, new Insets(5,5,5,5), 0, 0));
		
		add(faceLabel, makeGBC(0, 5, BOTH, 0, 0));
		add(faceValue, makeGBC(1, 5, 4, 1, BOTH, new Insets(5,5,5,5), 0, 0));
	}
	
	protected void addListeners() {
		for (int i = 0; i < colorNames.length; ++i) {
			for (int j = 0; j < colorValues[i].length; ++j) {
				final int y = i;
				final int x = j;
				colorValues[i][j].addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent evt) {
						if (ignoreStateChange) return;
						if (selectedMaterial == null) return;
						
						JSpinner spinny = (JSpinner) evt.getSource();
						activeValue[y][x] = (float) ((Double) spinny.getValue()).doubleValue();
						float r = activeValue[y][0];
						float g = activeValue[y][1];
						float b = activeValue[y][2];
						float a = activeValue[y][3];

						ColorRGBA color = new ColorRGBA(r,g,b,a);
						switch (y) {
						case 0:
							selectedMaterial.setAmbient(color);
							break;
						case 1:
							selectedMaterial.setDiffuse(color);
							break;
						case 2:
							selectedMaterial.setEmissive(color);
							break;
						case 3:
							selectedMaterial.setSpecular(color);
							break;
						}
						selectedObject.updateRenderState();
						scene.notifyModelRefresh();
					}
				});
			}
		}
		
		shininessValue.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				if (ignoreStateChange) return;
				if (selectedMaterial == null) return;
				
				JSpinner spinny = (JSpinner) evt.getSource();
				float shiny = (float) ((Double) spinny.getValue()).doubleValue();
				selectedMaterial.setShininess(shiny);
				selectedObject.updateRenderState();
				scene.notifyModelRefresh();
			}
		});
	}
	
	public void setSelectedObject(Spatial s) {
		ignoreStateChange = true;
		if (s.getRenderState(RenderState.RS_MATERIAL) == null) {
			selectedMaterial = null;
			setDefaults();
			setEnabledAll(false);
			ignoreStateChange = false;
			return;
		}
		
		selectedObject = s;
		selectedMaterial = (MaterialState) s.getRenderState(RenderState.RS_MATERIAL);
		
		setColorValues(colorValues[0], activeValue[0], selectedMaterial.getAmbient());
		setColorValues(colorValues[1], activeValue[1], selectedMaterial.getDiffuse());
		setColorValues(colorValues[2], activeValue[2], selectedMaterial.getEmissive());
		setColorValues(colorValues[3], activeValue[3], selectedMaterial.getSpecular());
		
		shininessValue.setValue(selectedMaterial.getShininess());
		setEnabledAll(true);
		ignoreStateChange = false;
	}
	
	public void setDefaults() {
		setColorValues(colorValues[0], activeValue[0], new ColorRGBA());
		setColorValues(colorValues[1], activeValue[1], new ColorRGBA());
		setColorValues(colorValues[2], activeValue[2], new ColorRGBA());
		setColorValues(colorValues[3], activeValue[3], new ColorRGBA());
		
		shininessValue.setValue(0);
	}
	
	protected void setEnabledAll(boolean state) {
		for (int i = 0; i < colorNames.length; ++i) {
			for (int j = 0; j < colorValues[i].length; ++j) {
				colorValues[i][j].setEnabled(state);
			}
		}
		
		shininessValue.setEnabled(state);
		faceValue.setEnabled(state);
	}
	
	protected void setColorValues(JSpinner[] spinners, float[] values, ColorRGBA color) {
		spinners[0].setValue(color.r);
		spinners[1].setValue(color.g);
		spinners[2].setValue(color.b);
		spinners[3].setValue(color.a);
		
		values[0] = color.r;
		values[1] = color.g;
		values[2] = color.b;
		values[3] = color.a;
	}
	
	// construct all of the visual objects....
	
	private JLabel[] colorNames = new JLabel[] {
		new JLabel("ambient"), 
		new JLabel("diffuse"),
		new JLabel("emmisive"),
		new JLabel("specular")
	};
	private JSpinner[][] colorValues = new JSpinner[4][4];
	
	private JLabel shininessLabel = new JLabel("shininess");
	private JSpinner shininessValue = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
	
	private JLabel faceLabel = new JLabel("face");
	private JComboBox faceValue = new JComboBox(new String[] {"MF_FRONT", "MF_BACK", "MF_FRONT_AND_BACK"});
}
