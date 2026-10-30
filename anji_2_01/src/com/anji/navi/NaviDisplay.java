/*
 * Copyright (C) 2009 Yoonsuck Choe
 * 
 * This file is an add-on to ANJI (Another NEAT Java Implementation).
 * This add-on has the same terms and conditions as ANJI (see below).
 * 
 * Fri Nov 27 23:19:12 CST 2009
 *----------------------------------------------------------------------------
 * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 *
 */
package com.anji.navi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import java.awt.event.ActionEvent;

import java.util.Random;

public class NaviDisplay extends JPanel{
    private int    width;
    private int    height;
    private double start_x = 0.0;
    private double start_y = 0.0;
    private double goal_x = 0.0;
    private double goal_y = 0.0;
    private double cur_x = 0.0;
    private double cur_y = 0.0;
    private double[][] motionSteps;
    private Graphics2D naviG;
    private Color[] pallett;
    private Color[] shortPallett;
    private Color[] grayPallett;
    private Color[] pallettType;
    private Color goalColor;
    private Color startColor;
    private int pallettSize;

    NaviDisplay (int w, int h, double sx, double sy, double gx, double gy, 
	double[][] motionSteps) {

	width = w;
	height = h; 

	start_x = cur_x = sx;
	start_y = cur_y = sy;
	goal_x = gx;
	goal_y = gy;

	this.motionSteps = motionSteps;

	// set up pallett for trajectory
	pallett = new Color[256*3];
	for (int i=0; i<256; ++i) {
		pallett[i] = new Color(i,0,0);
		pallett[i+255] = new Color(255,i,0);
		pallett[i+255*2] = new Color(255,255,i);
	}

	shortPallett = new Color[256];
	for (int i=0; i<86; ++i) {
		shortPallett[i] = new Color(i*3,0,0);
		shortPallett[i+85] = new Color(255,i*3,0);
		shortPallett[i+85*2] = new Color(255,255,i*3);
	}
	
	grayPallett = new Color[128];
	for (int i=0; i<128; ++i) {
		grayPallett[i] = new Color(i,i,i);
	}

	pallettType = shortPallett;
        pallettSize = 256;
	
	goalColor = new Color(0,255,0);
	startColor = new Color(255,0,0);

	/*
	Action actionListener = new AbstractAction() {
      	    public void actionPerformed(ActionEvent actionEvent) {
        	System.out.println("Got an q");
      	    }	
   	};
	*/

	// set up frame
	JFrame frame = new JFrame("NaviDisplay");
	frame.setContentPane(this);
	Container con = frame.getContentPane();
	//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	con.setBackground(Color.gray);
	//KeyStroke stroke = KeyStroke.getKeyStroke("q");
	//InputMap inputMap = ((JPanel)con).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        //inputMap.put(stroke, "OPEN");
        //((JPanel)con).getActionMap().put("OPEN", actionListener);
	frame.setSize(w+10,h+25);
	frame.setLocationRelativeTo(null);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setVisible(true);

    }

    public void moveTo(double x, double y) {
	BasicStroke stroke = new BasicStroke(3.0f);
	naviG.setStroke(stroke);
	double next_x = rangeCheck(cur_x + x);
	double next_y = rangeCheck(cur_y + y);
	naviG.drawLine((int)(cur_x*width),(int)(cur_y*height),
		(int)(next_x*width),(int)(next_y*height));
	cur_x = next_x;
	cur_y = next_y;
    }

    public void fromTo(double x, double y, double x2, double y2) {
	BasicStroke stroke = new BasicStroke(3.0f);
	naviG.setStroke(stroke);
	naviG.drawLine((int)(x*width),(int)(y*height),
		(int)(x2*width),(int)(y2*height));
    }

    private void plotLoc(double x, double y, Color c) {
	double offset = 3.0/width;
	naviG.setColor(c);
	naviG.fillOval(
		(int)(rangeCheck(x)*width-width/80.0),
		(int)(rangeCheck(y)*height-height/80.0),
		(int)(width/40.0),(int)(height/40.0));
    }

    public void plotStart(double x, double y) {
	plotLoc(x,y,startColor);
    }

    public void plotGoal(double x, double y) {
	plotLoc(x,y,goalColor);
    }

    public double rangeCheck(double x) {
	if (x < 0.0) return 0.0;
	if (x >= 1.0) return 1.0;
	return x;
    }
   
    public static double naviRangeCheck(double x) {
	if (x < 0.0) return 0.0;
	if (x >= 1.0) return 1.0;
	return x;
    }
   
    public void paintComponent (Graphics g) {
	super.paintComponent(g);
	naviG = (Graphics2D) g;
	drawTraj();
    }

    public void drawTraj () {
	naviG.setColor(Color.white);
	naviG.drawRect(0,0,width,height);
	for (int i = 1; i<motionSteps.length; ++i) {
		if (motionSteps[i][0]>10.0) break;
		naviG.setColor(pallettType[i%(pallettSize)]);
		fromTo(motionSteps[i-1][0],motionSteps[i-1][1],
			motionSteps[i][0],motionSteps[i][1]);
	}	
	plotStart(start_x,start_y);
	plotGoal(goal_x,goal_y);
    }

    public void drawSteps () {
	for (int i = 0; i<motionSteps.length; ++i) {
		naviG.setColor(pallettType[i%(pallettSize)]);
		moveTo(motionSteps[i][0],motionSteps[i][1]);
	}	
	plotStart(start_x,start_y);
	plotGoal(cur_x,cur_y);
    }

/*
    // example main function to show how to invoke this object
    public static void main (String args[]) {

	// set up frame
	int w=300, h=300;

	// set up motion steps array: something like this needs to 
	// be in your main algorithm side.
	double[][] motion = new double[256*3*10][2];
	Random r = new Random(12345);
	motion[0][0]=(r.nextDouble()-0.5)/20.0;
	motion[0][1]=(r.nextDouble()-0.5)/20.0;
	for (int i=1; i<motion.length; ++i) {
		motion[i][0]=NaviDisplay.naviRangeCheck(motion[i-1][0]+(r.nextDouble()-0.5)/20.0);
		motion[i][1]=NaviDisplay.naviRangeCheck(motion[i-1][1]+(r.nextDouble()-0.5)/20.0);
	}
	
	// create object to plot the data
	NaviDisplay navi = new NaviDisplay(w,h,r.nextDouble(),r.nextDouble(),
		r.nextDouble(), r.nextDouble(),motion);

    }
*/
}
       
