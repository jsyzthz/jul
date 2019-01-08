/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImageTransformationPanel.java
 *
 * Created on 18.08.2011, 21:34:08
 */
package org.openbase.jul.visual.swing.component;

/*-
 * #%L
 * JUL Visual Swing
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.visual.swing.image.ImageLoader;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ImageTransformationPanel extends javax.swing.JPanel {

	private BufferedImage originalImage;
	private Image scaledImage;
	private boolean onTheFly;
	private boolean holdRazio;

	/** Creates new form ImageTransformationPanel */
	public ImageTransformationPanel() {
		initComponents();
		onTheFly = true;
		holdRazio = false;
	}
	
	public ImageTransformationPanel(String imageURI) throws CouldNotPerformException {
		setImage(imageURI);
	}
	
	public final void setImage(String imageURI) throws CouldNotPerformException {
		setImage(ImageLoader.getInstance().loadImage(imageURI));
	}
	
	public void setImage(BufferedImage image) {
		originalImage = image;
		repaint();
	}

	public void setRazio(final boolean razio) {
		this.holdRazio = razio;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (originalImage != null) {
			Graphics2D g2 = (Graphics2D) g;
			if (onTheFly) {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				if(holdRazio) {
					Rectangle2D bounds = getBounds();
					double scalex = bounds.getWidth() / getWidth();
					double scaley = bounds.getHeight() / getHeight();
					double scale = Math.min(scalex, scaley);

					g2.drawImage(scaledImage, new AffineTransform(scale, 1, 1, scale, 0, 0),this);
				} else {
					g2.drawImage(originalImage, 0, 0, getWidth(), getHeight(), this);
				}
			} else {
				g2.drawImage(scaledImage, 0, 0, this);
			}
		}
	}



		/** This method is called from within the constructor to
		 * initialize the form.
		 * WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
		@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDoubleBuffered(false);
        setOpaque(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 247, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 145, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

	private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
		// TODO add your handling code here:
	}//GEN-LAST:event_formComponentResized
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
