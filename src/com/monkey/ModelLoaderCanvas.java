package com.monkey;

import java.io.*;
import java.net.URL;

import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.awt.SimpleCanvasImpl;

import com.monkey.model.LoadModel;

public class ModelLoaderCanvas extends SimpleCanvasImpl {

	protected MonkeyScene monkeyScene;

    protected StringBuffer updateBuffer = new StringBuffer( 30 );
    protected StringBuffer tempBuffer = new StringBuffer();
    protected Node fpsNode;
    protected Text fps;
    
    protected boolean pause;

	public ModelLoaderCanvas(int width, int height) {
        super(width, height);		
        
        monkeyScene = new MonkeyScene();
	}

	public void simpleSetup() {
        // Then our font Text object.
        /** This is what will actually have the text at the bottom. */
        fps = Text.createDefaultTextLabel( "FPS label" );
        fps.setCullMode( SceneElement.CULL_NEVER );
        fps.setTextureCombineMode( TextureState.REPLACE );

        // Finally, a stand alone node (not attached to root on purpose)
        fpsNode = new Node( "FPS node" );
        fpsNode.setRenderState( fps.getRenderState( RenderState.RS_ALPHA ) );
        fpsNode.setRenderState( fps.getRenderState( RenderState.RS_TEXTURE ) );
        fpsNode.attachChild( fps );
        fpsNode.setCullMode( SceneElement.CULL_NEVER );
		
		monkeyScene.init(rootNode, cam);

	    rootNode.updateGeometricState(tpf, true);
	    rootNode.updateRenderState();
        fpsNode.updateGeometricState( 0.0f, true );
        fpsNode.updateRenderState();
	}
	
    public void simpleUpdate() {
    	super.simpleUpdate();

        timer.update();
    	
        updateBuffer.setLength( 0 );
        updateBuffer.append( "FPS: " ).append( (int) timer.getFrameRate() ).append(
                " - " );
        updateBuffer.append( renderer.getStatistics( tempBuffer ) );
        /** Send the fps to our fps bar at the bottom. */
        fps.print( updateBuffer );
        renderer.clearStatistics();
    	
		// Update the geometric state of the rootNode
	    rootNode.updateGeometricState(tpf, true);
    }
    
    public void simpleRender() {
    	super.simpleRender();
    	
    	renderer.draw(fpsNode);
    }
    
	public MonkeyScene getScene() {
		return monkeyScene;
	}

	public void saveScene(String fileName) {
		try {		
			File f = new File(fileName);
			System.out.println("saving file: " + f.getAbsolutePath());
			BinaryExporter.getInstance().save(rootNode, f);
			System.out.println("saving successful: " + f.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadModel(String fileName) throws Exception {
		String ext = fileName.substring(fileName.lastIndexOf('.')+1);
		System.out.println("preparing to load " + ext);
		
		if (ext == null) {
			return;
		}
		
		Spatial scene = null;
		if (ext.equals("obj"))
			scene = LoadModel.loadObjModel(fileName);
		else if (ext.equals("dae"))
			scene = LoadModel.loadColladaModel(fileName);
		else if (ext.equals("jme")) 
			scene = (Spatial) BinaryImporter.getInstance().load( new File(fileName) );

		if (scene == null)
			return;
		
		if (!ext.equals("jme"))
			saveModel(fileName, scene);

		rootNode.attachChild(scene);
		rootNode.updateRenderState();
		monkeyScene.notifyModelBuild();
	}

	public void loadAnimatedModel(String fileName) throws Exception {
		String ext = fileName.substring(fileName.lastIndexOf('.')+1);
		System.out.println("preparing to load " + ext);
		
		if (ext == null) {
			return;
		}
		
		Spatial scene = null;
		if (ext.equals("dae"))
			scene = LoadModel.loadColladaAnimatedModel(fileName);
		else if (ext.equals("jme")) 
			scene = (Node) BinaryImporter.getInstance().load( new File(fileName) );
		
		if (scene == null) 
			return;
		
		if (!ext.equals("jme"))
			saveModel(fileName, scene);
		rootNode.attachChild(scene);
		rootNode.updateRenderState();
		
		monkeyScene.notifyModelBuild();
	}
	
	public void saveModel(String fileName, Spatial s) {
		try {		
			int fileStart = fileName.lastIndexOf(File.separator);
			String directory = fileName.substring(0, fileStart);
			URL dir = new URL("file://" + directory + File.separator);

			int fileEnd = fileName.lastIndexOf('.');
			File f = new File(dir.getPath() + fileName.substring(fileStart+1, fileEnd) + ".jme");
			System.out.println("saving file: " + f.getAbsolutePath());
			BinaryExporter.getInstance().save(s, f);
			System.out.println("saving successful: " + f.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
