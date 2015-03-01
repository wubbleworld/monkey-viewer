package com.monkey.model;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ModelTransparencyFix {

	protected Namespace ns;
	public ModelTransparencyFix() {
		
	}
	
	public void fixFile(String fileName) throws Exception {
		File f = new File(fileName);
		
		DocumentBuilder dom = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		org.w3c.dom.Document d = dom.parse(f);
		Document doc = (new DOMBuilder()).build(d);
		
		/*
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName + ".bak"));
			XMLOutputter output = new XMLOutputter();
			output.output(doc, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

		Element root = doc.getRootElement();
		ns = Namespace.getNamespace("http://www.collada.org/2005/11/COLLADASchema");
		root.removeChild("library_cameras", ns);
		
		Element libraryEffects = root.getChild("library_effects", ns);
		System.out.println("root: " + root + " library: " + libraryEffects);
		System.out.println(libraryEffects.getName());

		List effectList = libraryEffects.getChildren("effect", ns);
		Iterator effectIter = effectList.iterator();
		while (effectIter.hasNext()) {
			fixEffect((Element) effectIter.next());
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			XMLOutputter output = new XMLOutputter();
			output.output(doc, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void fixEffect(Element effect) {
		Element profile = effect.getChild("profile_COMMON", ns);
		Element technique = profile.getChild("technique", ns);
		Element shader = technique.getChild("phong", ns);
		if (shader != null) {
			Element transparency = shader.getChild("transparency", ns);
			Element floatElement = transparency.getChild("float", ns);
			floatElement.setText("0");
		} else {
			System.out.println("unknown shader type");
		}
	}
	
	public static void main(String[] args) {
		ModelTransparencyFix f = new ModelTransparencyFix();
		try {
			f.fixFile("media/models/JeanieOrigAnimBake.dae");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
