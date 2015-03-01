package com.monkey.swing;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.monkey.model.ModelTransparencyFix;

import java.util.Enumeration;
import java.util.Properties;

import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.monkey.ModelLoaderCanvas;
import com.monkey.MonkeyProperties;
import com.monkey.MonkeyViewer;

import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import static javax.swing.JOptionPane.*;

public class ModelMenuBar extends JMenuBar {

	protected MonkeyViewer _parent;
	
	protected ModelLoaderCanvas canvas;
	protected ActionListener nameListener;
	
	public ModelMenuBar(MonkeyViewer parent, ModelLoaderCanvas canvas) {
		_parent = parent;
		this.canvas = canvas;
		
		addComponents();
		addListeners();
	}

	private void addComponents() {
		fileMenu.add(loadMenuItem);
		fileMenu.add(loadAnimatedMenuItem);
		fileMenu.add(new JSeparator());
		
		fileMenu.add(addPathMenuItem);
		fileMenu.add(new JSeparator());
		
		fileMenu.add(quitMenuItem);

		add(fileMenu);

		editMenu.add(setCameraMenuItem);
		add(editMenu);
		
		addMenu.add(addNodeItem);
		basicMenu.add(addBoxItem);
		basicMenu.add(addSphereItem);
		basicMenu.add(addQuadItem);
		addMenu.add(basicMenu);
		add(addMenu);

		renderMenu.add(pauseItem);
		add(renderMenu);
	}

	private void addListeners() {
		nameListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String name = showInputDialog(null, "Please enter a name:", "Add", INFORMATION_MESSAGE);
				if (name.indexOf(' ') != -1) {
					showMessageDialog(null, "No spaces allowed in the name.", "Error", ERROR_MESSAGE);
					return;
				}
				
				JMenuItem item = (JMenuItem) arg0.getSource();
				if (item.equals(addNodeItem)) {
					canvas.getScene().addNode(name);
				} else if (item.equals(addBoxItem)) {
					canvas.getScene().addBox(name);
				} else if (item.equals(addSphereItem)) {
					canvas.getScene().addSphere(name);
				} else if (item.equals(addQuadItem)) {
					canvas.getScene().addQuad(name);
				}
			}
		};
		
		addNodeItem.addActionListener(nameListener);
		addBoxItem.addActionListener(nameListener);
		addSphereItem.addActionListener(nameListener);
		addQuadItem.addActionListener(nameListener);
		
		loadMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = MonkeyProperties.inst().getPath();
				JFileChooser jf = new JFileChooser(path);
				if (jf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					final String modelName = jf.getSelectedFile().getAbsolutePath();
		            Callable<?> call = new Callable<Object>() {
		                public Object call() throws Exception {
		                	try {
		                		canvas.loadModel(modelName);
							} catch (Exception e1) {
								System.out.println("Error loading" + modelName);
								e1.printStackTrace();
							}
		                   	return null;
		                }
		            };
		            GameTaskQueueManager.getManager().update(call);
		            
					MonkeyProperties.inst().saveProps(jf.getSelectedFile().getPath());
				}
			}
		});
		
		loadAnimatedMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = MonkeyProperties.inst().getPath();
				JFileChooser jf = new JFileChooser(path);
				if (jf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					final String modelName = jf.getSelectedFile().getAbsolutePath();
		            Callable<?> call = new Callable<Object>() {
		                public Object call() throws Exception {
		                	try {
		                		canvas.loadAnimatedModel(modelName);
							} catch (Exception e1) {
								System.out.println("Error loading" + modelName);
								e1.printStackTrace();
							}
		                   	return null;
		                }
		            };
		            GameTaskQueueManager.getManager().update(call);
		            
					MonkeyProperties.inst().saveProps(jf.getSelectedFile().getPath());
				}
			}
		});
		
		
		addPathMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = MonkeyProperties.inst().getPath();
				JFileChooser jf = new JFileChooser(path);
				jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (jf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					URL url;
					try {
						url = jf.getSelectedFile().toURL();
                        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, 
                        		new SimpleResourceLocator(url));
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		});
		
		quitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
				
		setCameraMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CameraDialog cd = new CameraDialog(null);
				if (!cd.getStatus()) {
					return;
				}

				final Vector3f position = cd.getPosition();
				final Vector3f lookAt = cd.getLookAt();
	            Callable<?> call = new Callable<Object>() {
	                public Object call() throws Exception {
	                	Camera cam = canvas.getCamera();
	                	cam.setLocation(position);
	                	cam.lookAt(lookAt, new Vector3f(0,1,0));
	                   	return null;
	                }
	            };
	            GameTaskQueueManager.getManager().update(call);			
			}
		});
		
		pauseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (pauseItem.getState()) {
					_parent.pauseRefreshThread();
				} else {
					_parent.unpauseRefreshThread();
				}
			}
		});
	}

	private JMenu fileMenu                 = new JMenu("File");
	private JMenuItem loadMenuItem         = new JMenuItem("Load");
	private JMenuItem loadAnimatedMenuItem = new JMenuItem("Load Animated");
	private JMenuItem addPathMenuItem      = new JMenuItem("Add Texture Path");
	private JMenuItem quitMenuItem         = new JMenuItem("Quit");
	
	private JMenu editMenu                 = new JMenu("Edit");
	private JMenuItem setCameraMenuItem    = new JMenuItem("Set Camera");
	
	private JMenu addMenu                  = new JMenu("Add");
	private JMenuItem addNodeItem          = new JMenuItem("Add Node");
	private JMenu basicMenu                = new JMenu("Add JME Builtin");
	private JMenuItem addBoxItem           = new JMenuItem("Box");
	private JMenuItem addSphereItem        = new JMenuItem("Sphere");
	private JMenuItem addQuadItem		   = new JMenuItem("Quad");
	
	private JMenu renderMenu               = new JMenu("Render");
	private JCheckBoxMenuItem pauseItem    = new JCheckBoxMenuItem("Pause");
}

