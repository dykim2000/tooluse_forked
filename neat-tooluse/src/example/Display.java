package example;

import javax.swing.*; 

import java.awt.*; 
import java.util.ArrayList;
import example.ExampleFitnessFunction.StickState;

class ArmPanel extends JPanel {
    public int bx, by, mx, my, ex, ey, tx, ty, hand_size, l1, l2, l3, l4, pick_x, pick_y, pick2_x, pick2_y;
    boolean holding; //True if the arm is holding tool1, false if not
    boolean holding2; //True if the arm is holding tool2, false if not
    StickState stickState; //How many sticks are currently in use (0, 1, 2)

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // clears the background

        // draw the range
        g.setColor(Color.black);
        g.drawArc(bx-(l1+l2), by-(l1+l2), 2*(l1+l2), 2*(l1+l2), 0, 180);
        if(stickState != StickState.NO_STICK) g.drawArc(bx-(l1+l2+l3), by-(l1+l2+l3), 2*(l1+l2+l3), 2*(l1+l2+l3), 0, 180);
        if(stickState == StickState.TWO_STICKS) g.drawArc(bx-(l1+l2+l3+l4), by-(l1+l2+l3+l4), 2*(l1+l2+l3+l4), 2*(l1+l2+l3+l4), 0, 180);

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));

        // draw base point
        g.fillRect(bx, by, 2, 2);

        // draw arm 1
        g.setColor(Color.blue);
        g.fillRect(mx, my, 2, 2);
        g2.drawLine(bx, by, mx, my);

        // draw arm 2
        g.setColor(Color.red);
        g.drawLine(mx, my, ex, ey);
        g.fillRect(ex, ey, 2, 2);
        g.drawOval(ex-hand_size, ey-hand_size, 2*hand_size, 2*hand_size);

        // draw the target
        g.setColor(Color.black);
        g.fillRect(tx-3, ty-3, 6, 6);

        // Only draw the first stick if it's not already being held and the stick state is either 1 or 2 sticks
        if (!holding && stickState != StickState.NO_STICK) {
            g.setColor(Color.green);
            g.drawLine(pick_x, pick_y, pick_x+l3, pick_y);//Tool1 is always drawn horizontally with a length of l3
            g.fillRect(pick_x, pick_y, 2, 2);
        }
        
        //Only draw the second stick if it's not already being held and the stick state is 2 sticks
        if (!holding2 && stickState == StickState.TWO_STICKS) {
            g.setColor(Color.magenta);
            g.drawLine(pick2_x, pick2_y, pick2_x+l4, pick2_y);//Tool2 is always drawn horizontally with a length of l4
            g.fillRect(pick2_x, pick2_y, 2, 2);
        }
    }
}

class Display {
    
    public Display(){
		this.panel = new ArmPanel();}
    
	public void init( int x, int y, int boxlen, int boxwid ) {
		JFrame frame = new JFrame("Arm Animation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel.bx = x;
        panel.by = y;

        frame.setSize(boxlen, boxwid);
        frame.add(panel);
        frame.setVisible(true);
	}

    public void nextFrame(ArmReaching arm) {
        panel.mx = arm.mx;
        panel.my = arm.my;
        panel.ex = arm.ex;
        panel.ey = arm.ey;
        panel.tx = arm.tx;
        panel.ty = arm.ty;
        panel.hand_size = arm.hand_size;
        panel.l1 = arm.l1;
        panel.l2 = arm.l2;
        panel.l3 = arm.l3;
        panel.l4 = arm.l4;
        panel.holding = arm.holding;
        panel.pick_x = arm.pick_x;
        panel.pick_y = arm.pick_y;
        panel.holding2 = arm.holding2;
        panel.pick2_x = arm.pick2_x;
        panel.pick2_y = arm.pick2_y;
        panel.stickState = arm.stickState;
        panel.repaint();
    }

    private final ArmPanel panel;
}

