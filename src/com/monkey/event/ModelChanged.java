package com.monkey.event;

import com.jme.scene.Node;
import com.jme.scene.Spatial;

public interface ModelChanged {
	public void modelAdd(Node parent, Spatial child);
	public void modelBuild(Node rootNode);
	public void modelRefresh();
}
