package com.monkey;

import java.awt.GridBagLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;

import static java.awt.GridBagConstraints.*;
import javax.swing.*;

import com.jme.input.KeyInput;
import com.jme.system.DisplaySystem;
import com.jmex.awt.JMECanvas;
import com.jmex.awt.input.AWTMouseInput;
import com.jmex.awt.lwjgl.LWJGLCanvas;
import com.monkey.swing.GBC;
import com.monkey.swing.ModelMenuBar;
import com.monkey.swing.ObjectProperties;

/**
 * <code>JMESwingTest</code> is a test demoing the JMEComponent and
 * HeadlessDelegate integration classes allowing jME generated graphics to be
 * displayed in a AWT/Swing interface.
 * 
 * Note the Repaint thread and how you grab a canvas and add an implementor to it.
 * 
 * @author Joshua Slack
 * @version $Id: MonkeyViewer.java,v 1.1 2007/09/05 01:05:12 wkerr Exp $
 */

public class MonkeyViewer extends JFrame {
    private static final long serialVersionUID = 1L;

	protected int width = 724;
	protected int height = 768;

    protected ModelLoaderCanvas impl;
    protected Canvas comp = null;
    protected ObjectProperties objProps = null;
    
    protected MonkeyUpdateThread mut;

    public MonkeyViewer() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        addComponents();
        addListeners();

        setJMenuBar(new ModelMenuBar(this, impl));
        setSize(1024,768);

        mut = new MonkeyUpdateThread(comp);
        mut.start();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Component initialization
    protected void addComponents() {
    	Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        setTitle("Model Loader");

        // -------------GL STUFF------------------
        // make the canvas:
        comp = DisplaySystem.getDisplaySystem("lwjgl").createCanvas(width, height);

        // add a listener... if window is resized, we can do something about it.
        comp.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ce) {
                doResize();
            }
        });
        KeyInput.setProvider( KeyInput.INPUT_AWT );
        AWTMouseInput.setup( comp, false );

        
        // Important!  Here is where we add the guts to the panel:
        impl = new ModelLoaderCanvas(width, height);
        JMECanvas jmeCanvas = ( (JMECanvas) comp );
        jmeCanvas.setImplementor(impl);
        jmeCanvas.setUpdateInput( true );

        ModelerCameraHandler camhand = new ModelerCameraHandler(impl);

        comp.addMouseWheelListener(camhand);
        comp.addMouseListener(camhand);
        comp.addMouseMotionListener(camhand);

        // -----------END OF GL STUFF-------------

        contentPane.add(comp, GBC.makeGBC(1, 0, BOTH, 1.0, 1.0));
        
        objProps = new ObjectProperties(impl, impl.getScene());
        impl.getScene().addModelChangedListener(objProps);
        contentPane.add(objProps, GBC.makeGBC(0, 0, BOTH, 0, 1.0));
    }
    
    protected void addListeners() {
    	this.addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent arg0) {
				unpauseRefreshThread();
			}

			public void windowLostFocus(WindowEvent e) {
				pauseRefreshThread();
			}
    	});
    	
    }

    protected void doResize() {
    	impl.resizeCanvas(width, height);
    }

    // Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        }
    }
    
    public void pauseRefreshThread() {
    	mut.pause();
    }

    public void unpauseRefreshThread() {
    	mut.unPause();
    }
    
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MonkeyViewer();
    }
    
}