package com.monkey.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import static java.awt.GridBagConstraints.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import com.jme.math.Vector3f;
import com.jme.scene.*;
import com.jme.scene.batch.TriangleBatch;
import com.jme.scene.state.CullState;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;

import com.monkey.event.*;
import com.monkey.ModelLoaderCanvas;
import com.monkey.MonkeyProperties;
import com.monkey.MonkeyScene;

public class ObjectProperties extends JPanel implements ModelChanged {
	
	protected ModelLoaderCanvas parent;
	protected MonkeyScene scene;
	protected Spatial selectedObject;
	protected DefaultMutableTreeNode selectedNode;
	protected DefaultMutableTreeNode savedNode;
	
	protected JTree modelTree;
	protected DefaultTreeModel modelTreeModel;
	
	protected JPopupMenu popup;
	protected JMenuItem renameItem;
	protected JMenuItem saveItem;
	protected JMenuItem cullItem;
	protected JMenuItem materialItem;
	protected JMenuItem cutItem;
	protected JMenuItem pasteItem;
	protected JMenuItem deleteItem;
	
	protected MaterialPanel materialPanel;
	protected TransformPanel transformPanel;
	protected LightPanel lightPanel;
	protected JTabbedPane propPane;
	
	protected boolean ignoreStateChange = false;
	
	protected TreeSelectionListener selectionListener;
	
	public ObjectProperties(ModelLoaderCanvas parent, MonkeyScene monkeyScene) {
		this.parent = parent;
		scene = monkeyScene;
		
		selectedObject = null;
		
		addComponents();
		addListeners();
	}
	
	protected void addComponents() {
		setLayout(new GridBagLayout());

		modelTree = new JTree(buildTree(scene.getRootNode()));
		JScrollPane scroll = new JScrollPane(modelTree);
		
		scroll.setPreferredSize(new Dimension(400,768));
		scroll.getViewport().setPreferredSize(new Dimension(1600,1200)); 
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		add(scroll, GBC.makeGBC(0, 0, BOTH, 0, 1.0));
		
		propPane = new JTabbedPane();
		
		transformPanel = new TransformPanel(scene);
		materialPanel = new MaterialPanel(scene);
		lightPanel = new LightPanel(scene);

		propPane.addTab("Transform", transformPanel);
		propPane.addTab("Material", materialPanel);
		propPane.addTab("Lights", lightPanel);
		
		add(propPane, GBC.makeGBC(0,1,BOTH,1,0));
		
		popup = new JPopupMenu();
		renameItem = new JMenuItem("rename");
		saveItem = new JMenuItem("save");
		materialItem = new JMenuItem("add material");
		cullItem = new JMenuItem("add cull");
		cutItem = new JMenuItem("cut");
		pasteItem = new JMenuItem("paste");
		deleteItem = new JMenuItem("delete");
		
		popup.add(renameItem);
		popup.add(saveItem);
		popup.add(materialItem);
		popup.add(cullItem);
		popup.add(new JSeparator());
		popup.add(cutItem);
		popup.add(pasteItem);
		popup.add(new JSeparator());
		popup.add(deleteItem);
	}
	
	/*
	 * Add listeners to the modelTree so that when we select an item
	 * the features are updated so that we can modify them.
	 */
	protected void addListeners() {
	    modelTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    
	    selectionListener = new TreeSelectionListener() {
		    public void valueChanged(TreeSelectionEvent e) {
		    	DefaultMutableTreeNode node = (DefaultMutableTreeNode) modelTree.getLastSelectedPathComponent();
		    	
		    	if (node == null)
		    		return;

		    	Spatial s = (Spatial) node.getUserObject();
		    	if (s == null) return;

		    	selectedNode = node;
		    	selectedObject = s;
		    	
		    	materialPanel.setSelectedObject(s);
		    	transformPanel.setSelectedObject(s);
		    	lightPanel.setSelectedObject(s);
		    }
	    	
	    };
	    modelTree.addTreeSelectionListener(selectionListener);
	    
	    MouseListener mouseListener = new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getButton() == MouseEvent.BUTTON3) {
					popup.show(modelTree, arg0.getX(), arg0.getY());
				}
			}

			public void mouseEntered(MouseEvent e) { }
			public void mouseExited(MouseEvent e) { }

			public void mousePressed(MouseEvent e) { }
			public void mouseReleased(MouseEvent e) { }
	    };
	    modelTree.addMouseListener(mouseListener);
	    
	    KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent arg0) { }
			public void keyReleased(KeyEvent e) { 
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					System.out.println("deleting");
					delete();
				}
			}

			public void keyTyped(KeyEvent e) { }
	    };
	    modelTree.addKeyListener(keyListener);
	    
	    materialItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent evt) {
	    		if (selectedObject != null) {
	    			scene.addMaterial(selectedObject);
	    			modelRefresh();
	    		}
	    	}
	    });
	    
	    final Object[] options = new Object[] {"Back", "Front", "Front and Back", "None"};
	    cullItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent evt) {
	    		if (selectedObject != null) {
	    			Object obj = JOptionPane.showInputDialog(null, "Select: ", "Add Cull State",
	    							INFORMATION_MESSAGE, null, options, "Back");
	    			if (obj == null)
	    				return;

	    			CullState cs = DisplaySystem.getDisplaySystem().getRenderer().createCullState();
	    			if (options[0].equals(obj)) {
	    				cs.setCullMode(CullState.CS_BACK);
	    			} else if (options[1].equals(obj)) {
	    				cs.setCullMode(CullState.CS_FRONT);
	    			} else if (options[2].equals(obj)) {
	    				cs.setCullMode(CullState.CS_FRONT_AND_BACK);
	    			} else if (options[3].equals(obj)) {
	    				cs.setCullMode(CullState.CS_NONE);
	    			}
	    			selectedObject.setRenderState(cs);
	    			selectedObject.updateRenderState();
	    		}
	    	}
	    });
	    
	    deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				delete();
			}
		});
	    
	    cutItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		cut();
	    	}
	    });
	    
	    pasteItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) { 
	    		paste();
	    	}
	    });
	    
	    saveItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		if (selectedObject != null) {
	    			String path = MonkeyProperties.inst().getPath();
					JFileChooser jf = new JFileChooser(path);
					if (jf.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						final String modelName = jf.getSelectedFile().getAbsolutePath();
			            Callable<?> call = new Callable<Object>() {
			                public Object call() throws Exception {
			                	try {
			                		parent.saveModel(modelName, selectedObject);
								} catch (Exception e1) {
									System.out.println("Error saving" + modelName);
									e1.printStackTrace();
								}
			                   	return null;
			                }
			            };
			            GameTaskQueueManager.getManager().update(call);
			            
						MonkeyProperties.inst().saveProps(jf.getSelectedFile().getPath());
					}
	    		}
	    	}
	    });
	    
	    renameItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	    		if (selectedObject != null) {
					String name = showInputDialog(null, "Please enter a name:", "Rename", INFORMATION_MESSAGE);
					if (name.indexOf(' ') != -1) {
						showMessageDialog(null, "No spaces allowed in the name.", "Error", ERROR_MESSAGE);
						return;
					}
					
					selectedObject.setName(name);
	    		}
	    	}
	    });
	    
	}
	
	protected void cut() {
		savedNode = selectedNode;
		selectedNode = null;
		
		removeFromTree(savedNode);
		scene.deleteObject(selectedObject);
	}
	
	protected void delete() {
		if (selectedObject == null) {
			return;
		}
		
		removeFromTree(selectedNode);
		selectedNode = null;
		scene.deleteObject(selectedObject);
	}
	
	protected void removeFromTree(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		int[] index = new int[] { parent.getIndex(node) };
		Object[] obj = new Object[] { node };
		node.removeFromParent();
		modelTreeModel.nodesWereRemoved(parent, index, obj);
	}
	
	protected void paste() {
		if (selectedObject == null || savedNode == null) {
			return;
		}
		
		String results = scene.pasteObject(selectedObject, (Spatial) savedNode.getUserObject());
		if (!results.equals("")) {
			JOptionPane.showMessageDialog(null, results, "ERROR", ERROR_MESSAGE);
		} else {
			selectedNode.insert(savedNode, selectedNode.getChildCount());
			modelTreeModel.nodesWereInserted(selectedNode, new int[] { selectedNode.getChildCount()-1 });
			savedNode = null;
		}
	}
	
	/**
	 * iterate over the nodes and add them as a tree node.
	 * @param n - current node to expand
	 * @return
	 */
	protected DefaultMutableTreeNode buildTree(Node n) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(n);

		if (n == null || n.getChildren() == null) 
			return top;

		Iterator<Spatial> iter = n.getChildren().iterator();
		while (iter.hasNext()) {
			Spatial s = iter.next();
			if (s instanceof Node) {
				top.add(buildTree((Node) s));
//			will come back to this later.   We need to be able to modify TriMesh props
//			} else if (s instanceof TriMesh) {
//				TriMesh t = (TriMesh) s;
//				for (int i = 0; i < t.getBatchCount(); ++i) {
//					TriangleBatch tb = t.getBatch(i);
//					top.add(new DefaultMutableTreeNode(tb));
//				}
			} else {
				top.add(new DefaultMutableTreeNode(s));
			}
		}

		return top;
	}
	
	/**
	 * 
	 * @param node
	 * @param n
	 * @param s
	 */
	protected boolean findAndAdd(DefaultMutableTreeNode node, Node n, Spatial s) {
		if (node.getUserObject().equals(n)) {
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(s);
			node.insert(newNode, 0);
			modelTreeModel.nodesWereInserted(node, new int[] {0});
			return true;
		}
		
		for (int i = 0; i < node.getChildCount(); ++i) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			if (findAndAdd(child, n, s)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * add the spatial s to the node n.  First you have to find it
	 * in the tree.  Once you have the correct DefaultMutableTreeNode
	 * you can add a new DefaultMutableTreeNode containing the spatial 
	 * to it.
	 * @param n
	 * @param s
	 * 
	 */
	public void modelAdd(Node n, Spatial s) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) modelTreeModel.getRoot();
		findAndAdd(root, n, s);
	}
	
	public void modelRefresh() {
		TreeSelectionEvent evt = new TreeSelectionEvent(this, modelTree.getSelectionPath(),
				true, modelTree.getLeadSelectionPath(), modelTree.getLeadSelectionPath());
		
		selectionListener.valueChanged(evt);
	}

	public void modelBuild(Node rootNode) {
		modelTreeModel = new DefaultTreeModel(buildTree(rootNode));
		modelTree.setModel(modelTreeModel);
	}
}
