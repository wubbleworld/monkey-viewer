/*
 * GBC.java
 *
 * Created on May 3, 2007, 7:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.monkey.swing;

import java.awt.*;
import static java.awt.GridBagConstraints.*;

/**
 *
 * @author wkerr
 */
public class GBC {
   
   /** Creates a new instance of GBC */
   public GBC() {
   }
   
   /* helper code to avoid copy and paste errors */
   public static GridBagConstraints makeGBC(int x, int y, int fill, double wx, double wy) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.fill = fill;
      gbc.weightx = wx;
      gbc.weighty = wy;
      return gbc;
   }
   
   /* helper code to avoid copy and paste errors */
   public static GridBagConstraints makeGBC(int x, int y, int fill, Insets insets, double wx, double wy) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.fill = fill;
      gbc.insets = insets;
      gbc.weightx = wx;
      gbc.weighty = wy;
      return gbc;
   }

   /* helper code to avoid copy and paste errors */
   public static GridBagConstraints makeGBC(int x, int y, int w, int h,  int fill, 
                                            double wx, double wy) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = w;
      gbc.gridheight = h;
      gbc.fill = fill;
      gbc.weightx = wx;
      gbc.weighty = wy;
      return gbc;
   }   

   /* helper code to avoid copy and paste errors */
   public static GridBagConstraints makeGBC(int x, int y, int w, int h,  int fill, 
                                            Insets insets, double wx, double wy) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = w;
      gbc.gridheight = h;
      gbc.fill = fill;
      gbc.insets = insets;
      gbc.weightx = wx;
      gbc.weighty = wy;
      return gbc;
   }   
}
