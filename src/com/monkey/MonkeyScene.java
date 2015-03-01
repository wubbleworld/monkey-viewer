package com.monkey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import com.monkey.event.ModelChanged;

import com.jme.light.*;
import com.jme.math.*;
import com.jme.renderer.*;
import com.jme.scene.*;
import com.jme.scene.shape.*;
import com.jme.scene.state.*;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;

public class MonkeyScene {
    private Geometry grid;

    private static final int GRID_LINES = 51;
    private static final float GRID_SPACING = 100f;
    
    protected Camera cam;
	protected Node rootNode;
	
	protected ArrayList<ModelChanged> modelWatchers;

	
	public MonkeyScene() {
        modelWatchers = new ArrayList<ModelChanged>();
	}
	
	public Node getRootNode() {
		return rootNode;
	}
	
	protected void init(Node rootNode, Camera cam) {
		this.rootNode = rootNode;
		this.cam = cam;
		
		Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
		r.enableStatistics(true);

        rootNode.attachChild(grid = createGrid());
        grid.updateRenderState();
        
		CullState cs = DisplaySystem.getDisplaySystem().getRenderer().createCullState();
		cs.setCullMode(CullState.CS_BACK);
		rootNode.setRenderState(cs);

        cam.setFrustumFar(10000);
		initLights();
		notifyModelBuild();
	}
	
	/*
	 * create the initial lights for the scene
	 */
	protected void initLights() {
		LightState lightState = DisplaySystem.getDisplaySystem().getRenderer().createLightState();
		
		PointLight pl = new PointLight();
		pl.setAmbient(new ColorRGBA(0.5f,0.5f,0.5f,1));
		pl.setDiffuse(new ColorRGBA(1,1,1,1));
		//pl.setLocation(model.getLocalTranslation().add(new Vector3f(0,bs.getRadius()*2,0)));
		//pl.setLocation(model.getLocalTranslation().add(new Vector3f(0,100,0)));
		pl.setEnabled(true);

		lightState.detachAll();
		lightState.attach(pl);

		ArrayList<Vector3f> dirLights = new ArrayList<Vector3f>();
		//dirLights.add(new Vector3f(0, 0, 1));
		//dirLights.add(new Vector3f(0, 0, -1));
		//dirLights.add(new Vector3f(0, -1, 1));
		//dirLights.add(new Vector3f(0, 1, -1));
		//dirLights.add(new Vector3f(0, -1, 0));
		//dirLights.add(new Vector3f(0, 1, 0));

		Iterator<Vector3f> iter = dirLights.iterator();
		while (iter.hasNext()) {
			DirectionalLight dl = new DirectionalLight();
			dl.setDirection(iter.next());
			dl.setAmbient(new ColorRGBA(0.5f,0.5f,0.5f,1));
			dl.setDiffuse(new ColorRGBA(1,1,1,1));
			dl.setEnabled(true);
			lightState.attach(dl);
		}
		
		rootNode.setRenderState(lightState);
		rootNode.updateRenderState();
	}	
	
	public void addMaterial(Spatial s) {
		MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		s.setRenderState(ms);
		s.updateRenderState();
		notifyModelRefresh();
	}
	
	/*
	 * paste the object as a child of the given spatial
	 * @param s - The spatial that will be a parent (must be a Node)
	 * @return string containing the error or empty if successful
	 */
	public String pasteObject(Spatial parent, Spatial child) {
		try {
			Node n = (Node) parent;
			n.attachChild(child);
		} catch (Exception e) {
			return "Cannot paste object as child of the given object!";
		}
		return "";
	}
	
	/*
	 * remove the object from it's parent and refresh the screen.
	 */
	public void deleteObject(Spatial s) {
		s.removeFromParent();
	}
	
	/*
	 * always add the new node to the rootNode for simplicity.
	 * This forces the user to use cut and paste to move stuff around.
	 */
	public void addNode(String name) {
		Node n = new Node(name);
		rootNode.attachChild(n);
//		rootNode.updateRenderState();
		notifyModelAdd(rootNode, n);
	}
	
	/*
	 * always add box to the rootNode for simplicity.
	 * Forces the user to use cut and paste to move things around.
	 */
	public void addBox(String name) {
		Box b = new Box(name, new Vector3f(0,0,0), 0.5f, 0.5f, 0.5f);
		rootNode.attachChild(b);
		rootNode.updateRenderState();
		notifyModelAdd(rootNode, b);
	}
	
	/*
	 * always add sphere to the rootNode for simplicity.
	 * Forces teh user to use cut and paste to move things around.
	 */
	public void addSphere(String name) {
		Sphere s = new Sphere(name, new Vector3f(0,0,0), 20, 20, 0.5f);
		rootNode.attachChild(s);
		rootNode.updateRenderState();
		notifyModelAdd(rootNode, s);
	}
	
	public void addQuad(String name) {
		Quad q = new Quad(name, 1f, 1f);
		rootNode.attachChild(q);
		rootNode.updateRenderState();
		notifyModelAdd(rootNode, q);
	}	
	
	public void updateRenderState() {
        Callable<?> call = new Callable<Object>() {
            public Object call() throws Exception {
        		rootNode.updateRenderState();
        		return null;
            }
        };
        GameTaskQueueManager.getManager().update(call);			
	}


	public void addModelChangedListener(ModelChanged mc) {
		modelWatchers.add(mc);
	}
	
	/**
	 * notifyModelAdd will notify all listeners that we have added
	 * a Spatial to a child.
	 * @param parent
	 * @param child
	 */
	public void notifyModelAdd(Node parent, Spatial child) {
		final Node _parent = parent;
		final Spatial _child = child;
		Iterator<ModelChanged> iter = modelWatchers.iterator();
		while (iter.hasNext()) {
			final ModelChanged mc = iter.next();
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			        mc.modelAdd(_parent, _child);
			    }
			});
		}
	}
	
	public void notifyModelBuild() {
		Iterator<ModelChanged> iter = modelWatchers.iterator();
		while (iter.hasNext()) {
			final ModelChanged mc = iter.next();
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			        mc.modelBuild(rootNode);
			    }
			});
		}
	}
	
	public void notifyModelRefresh() {
		Iterator<ModelChanged> iter = modelWatchers.iterator();
		while (iter.hasNext()) {
			final ModelChanged mc = iter.next();
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			        mc.modelRefresh();
			    }
			});
		}
	}	
	
    private Geometry createGrid() {
        Vector3f[] vertices = new Vector3f[GRID_LINES * 2 * 2];
        float edge = GRID_LINES / 2 * GRID_SPACING;
        for (int ii = 0, idx = 0; ii < GRID_LINES; ii++) {
            float coord = (ii - GRID_LINES / 2) * GRID_SPACING;
            vertices[idx++] = new Vector3f(-edge, 0f, coord);
            vertices[idx++] = new Vector3f(+edge, 0f, coord);
            vertices[idx++] = new Vector3f(coord, 0f, -edge);
            vertices[idx++] = new Vector3f(coord, 0f, +edge);
        }
        Geometry grid = new com.jme.scene.Line("grid", vertices, null,
                null, null);
        grid.getBatch(0).getDefaultColor().set(ColorRGBA.darkGray);
        return grid;
    }


}
