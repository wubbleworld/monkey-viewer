package com.monkey.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.jme.animation.AnimationController;
import com.jme.animation.Bone;
import com.jme.animation.BoneAnimation;
import com.jme.animation.SkinNode;
import com.jme.bounding.BoundingSphere;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.*;
import com.jmex.model.converters.ObjToJme;
import com.jmex.model.collada.ColladaImporter;

public class LoadModel {
    private static final Logger logger = Logger	
    	.getLogger(LoadModel.class.getName());
    
	public static Spatial loadObjModel(String fileName) {
		ObjToJme converter = new ObjToJme();
		try {
			File f = new File(fileName);
			URL objFile = f.toURL();

			converter.setProperty("mtllib", objFile);
			converter.setProperty("texdir",objFile);
			ByteArrayOutputStream BO = new ByteArrayOutputStream();

			System.out.println("Starting to convert .obj to .jme");
			converter.convert(objFile.openStream(), BO);
			//load as a TriMesh if single object
			TriMesh model = (TriMesh) BinaryImporter.getInstance().load(
					new ByteArrayInputStream(BO.toByteArray()));
			//load as a node if multiple objects
			//Node model=(Node)BinaryImporter.getInstance().load(
			//                new ByteArrayInputStream(BO.toByteArray()));
			//model.setLocalScale(new Vector3f(10,10,10));
			model.setModelBound(new BoundingSphere());
			model.updateModelBound();

			return model;
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return null;
	}

	public static Spatial loadColladaModel(String fileName) throws Exception {
		ModelTransparencyFix mtf = new ModelTransparencyFix();
		mtf.fixFile(fileName);

		int fileStart = fileName.lastIndexOf(File.separator);
		String directory = fileName.substring(0, fileStart);
		URL dir = new URL("file://" + directory + File.separator);
        try {
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, 
            		new SimpleResourceLocator(dir));
            URL textureDir = new URL("file://" + directory + File.separator + ".." + File.separator + "textures" + File.separator);
            System.out.println("texture dir: " + textureDir);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, 
            		new SimpleResourceLocator(textureDir));
        } catch (URISyntaxException e1) {
            logger.warning("Unable to add texture directory to RLT: "
                    + e1.toString());
        }

		InputStream stream = new FileInputStream(fileName);
		ColladaImporter.load(stream, "model");

		Node model = ColladaImporter.getModel();
		ColladaImporter.cleanUp();
		
		return model;
	}

	public static Spatial loadColladaAnimatedModel(String fileName) throws Exception {
		ModelTransparencyFix mtf = new ModelTransparencyFix();
		mtf.fixFile(fileName);
		
		int fileStart = fileName.lastIndexOf(File.separator);
		String directory = fileName.substring(0, fileStart);
		URL dir = new URL("file://" + directory + File.separator);
        try {
        	System.out.println("url: " + dir);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(dir));
        } catch (URISyntaxException e1) {
            logger.warning("Unable to add texture directory to RLT: "
                    + e1.toString());
        }
		
		//URL url = ClassLoader.getSystemClassLoader().getResource("media/models/");
		InputStream stream = new FileInputStream(fileName);
		ColladaImporter.load(stream, "model");

		AnimationController ac;
		SkinNode sn = ColladaImporter.getSkinNode(ColladaImporter.getSkinNodeNames().get(0));
		Bone skel = ColladaImporter.getSkeleton(ColladaImporter.getSkeletonNames().get(0));

		//this file might contain multiple animations, (in our case it's one)
		ArrayList<String> animations = ColladaImporter.getControllerNames();
		if(animations != null) {
			System.out.println("Number of animations: " + animations.size());
			BoneAnimation ba = new BoneAnimation();
			for(int i = 0; i < animations.size(); i++) {
				BoneAnimation anim1 = ColladaImporter.getAnimationController(animations.get(i));
				//set up a new animation controller with our BoneAnimation
				ba.addBoneAnimation(anim1);

				System.out.println("ba: " + anim1.getKeyFrameTimes() + " " + 
						anim1.getStartFrame() + " " + anim1.getEndFrame());

			}
			System.out.println("ba: " + ba.getKeyFrameTimes());
			System.out.println("ba: " + ba.getStartFrame() + " " + ba.getEndFrame());
			//Obtain the animation from the file by name
			ac = new AnimationController();
			ac.addAnimation(ba);
			ac.setRepeatType(Controller.RT_WRAP);
			ac.setActive(true);
			ac.setActiveAnimation(ba);

			//assign the animation controller to our skeleton
			skel.addController(ac);
		}

		//attach the skeleton and the skin to the rootnode. Skeletons could possibly
		//be used to update multiple skins, so they are seperate objects.
		Node n = new Node("model");
		n.attachChild(sn);
		n.attachChild(skel);
		
		ColladaImporter.cleanUp();
		
		return n;
	}
	
}
