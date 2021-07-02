package edu.team9.restaurantms;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

class NitroUI extends JPanel implements MouseListener, KeyListener {

    private enum State {
        NOTHING, TRANSITION, GRID, LIST, TEXTBOX
    }

    enum Direction {
        NEITHER, LEFT, RIGHT, BOTH
    }

    private enum Timepiece {
        FADEIN, FADEOUT, SLIDELEFT
    }

    // TODO: Decide on a default font!

    private final Font FONT =  new Font("Arial", Font.PLAIN, 16);

    private final Color SCREEN = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getGreen(), Color.DARK_GRAY.getBlue(), 225);
    private final FontMetrics METRICS = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR).createGraphics().getFontMetrics(FONT);

    private final int UISIZE = 500;
    private final int MARGIN = 30;
    private final int FONTMARGIN = 5;
    private final int SPACING = 15;
    private final int LFTARROW = -2;
    private final int RGTARROW = -3;

    // TODO: Maybe use arrow images?
    private final String LFTARROWSTR = "\u2190"; // One char, but everything uses a String anyways.
    private final String RGTARROWSTR = "\u2192";

    private final int ARROWWID = FONTMARGIN * 2 + METRICS.stringWidth(LFTARROWSTR);
    private final int ARROWHEI = FONTMARGIN * 2 + METRICS.getHeight();

    private final int MAXLIST = (UISIZE - MARGIN * 2 - ARROWHEI - SPACING) / (METRICS.getHeight() + FONTMARGIN * 2 + SPACING);

    private BufferedImage bgImage, uiImage, outImage;
    private Container overContain;
    private Callback callPtr;
    private JFrame overFrame;

    // States:
    // Nothing
    // Transition    substate - Data for next state
    // Grid layout   substate - grid width
    // List layout   substate - list length
    private State state, nextState;
    private Direction dir;

    private int substate, downBtn, uiXPos;
    private float btnWid, btnHei, uiAlpha;

    /**
     * Setups for drawing the window. Mostly GUI stuff and initializing variables.
     */
    NitroUI() {
        System.setProperty("sun.java2d.opengl", "true");
        state = State.NOTHING;
        uiAlpha = 1;

        overFrame = new JFrame("Restaurant Management System");
        overContain = overFrame.getContentPane();
        overContain.add(this);
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);
        overFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        bgImage = new BufferedImage(UISIZE, UISIZE, BufferedImage.TYPE_4BYTE_ABGR);

        // TEST CODE!!!
        Graphics2D g = bgImage.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 500, 500);
        g.setColor(Color.RED);
        g.fillRect(150, 150, 200, 200);
        // END TEST CODE!!!

        uiImage = new BufferedImage(UISIZE, UISIZE, BufferedImage.TYPE_4BYTE_ABGR);
        g = uiImage.createGraphics();
        g.setBackground(SCREEN);
        g.clearRect(0, 0, UISIZE, UISIZE);

        setPreferredSize(new Dimension(UISIZE, UISIZE));
        overFrame.pack();
        overFrame.setLocation(600, 200);
        overFrame.setVisible(true);

        setOpaque(true);
    }

    /**
     * Replaces the current menu with a grid of buttons.
     * @param ptr an object containing the method to be called when a button is pressed
     * @param wid The number of buttons per row
     * @param hei The number of buttons per column
     */
    void drawGrid(Callback ptr, Direction d, int wid, int hei) {
        callPtr = ptr;

        outImage = uiImage;
        uiXPos = UISIZE;
        uiImage = new BufferedImage(UISIZE, UISIZE, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = uiImage.createGraphics();
        prepGraphics(g);

        int mgn = 2 * MARGIN;
        dir = d;
        if(dir != Direction.NEITHER) {
            drawArrows(g);
            mgn += ARROWHEI + SPACING;
        }

        // Use floats to minimize truncation "floating"
        btnWid = (UISIZE - 2 * MARGIN - SPACING * (wid - 1)) / (float)wid;
        btnHei = (UISIZE - mgn - SPACING * (hei - 1)) / (float)hei;
        for(int i = 0; i < hei; i++) {
            for(int j = 0; j < wid; j++) {
                g.drawRoundRect((int)(MARGIN + (btnWid + SPACING) * j), (int)(MARGIN + (btnHei + SPACING) * i), (int)btnWid, (int)btnHei, 5, 5);
            }
        }

        state = State.TRANSITION;
        nextState = State.GRID;
        substate = wid;
        slideLeft();
    }

    /**
     * Replaces the current UI with a vertical list of buttons.
     * @param ptr an object containing the method to be called when a button is pressed
     * @param text the text to be displayed on each button
     */
    void drawList(Callback ptr, Direction d, String ... text) {
        if(text.length > MAXLIST) throw new IllegalArgumentException("Tried to draw more list elements than allowed!");
        callPtr = ptr;
        outImage = uiImage;
        uiXPos = UISIZE;
        uiImage = new BufferedImage(UISIZE, UISIZE, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = uiImage.createGraphics();
        prepGraphics(g);

        dir = d;
        drawArrows(g);

        btnWid = (UISIZE - 2 * MARGIN);
        btnHei = METRICS.getHeight() + 2 * FONTMARGIN;
        float spc = METRICS.getAscent();
        for(int i = 0; i < text.length; i++) {
            g.drawRoundRect(MARGIN, (int)(MARGIN + (btnHei + SPACING) * i), (int)btnWid, (int)btnHei, 5, 5);
            g.drawString(text[i], MARGIN + FONTMARGIN, (int)(MARGIN + (btnHei + SPACING) * i + FONTMARGIN + spc));
        }

        state = State.TRANSITION;
        nextState = State.LIST;
        substate = text.length;
        slideLeft();
    }

    void drawLongList(Callback ptr, String ... text) {
        new SuperCallback(ptr, text);
    }

    // Prepares the foreground graphics.
    private void prepGraphics(Graphics2D g) {
        g.setBackground(SCREEN);
        g.clearRect(0, 0, UISIZE, UISIZE);
        g.setFont(FONT);
    }

    // Draws the bottom directional arrows. Dir should already be set.
    private void drawArrows(Graphics2D g) {
        if(dir == Direction.LEFT || dir == Direction.BOTH) {
            g.drawRoundRect(MARGIN, UISIZE - MARGIN - ARROWHEI, ARROWWID, ARROWHEI, 5, 5);
            g.drawString(LFTARROWSTR, MARGIN + FONTMARGIN, UISIZE - MARGIN - ARROWHEI + FONTMARGIN + METRICS.getAscent());
        }
        if(dir == Direction.RIGHT || dir == Direction.BOTH) {
            g.drawRoundRect(UISIZE - MARGIN - ARROWWID, UISIZE - MARGIN - ARROWHEI, ARROWWID, ARROWHEI, 5, 5);
            g.drawString(RGTARROWSTR, UISIZE - MARGIN + FONTMARGIN - ARROWWID, UISIZE - MARGIN - ARROWHEI + FONTMARGIN + METRICS.getAscent());
        }
    }

    // Gets the selected grid square. Inputs are the mouse coordinates.
    private int getGridPos(int x, int y) {
        if(x < MARGIN || y < MARGIN || x > UISIZE - MARGIN || y > UISIZE - MARGIN) return -1;

        if(dir != Direction.NEITHER && y > UISIZE - MARGIN - ARROWHEI) {
            if((dir == Direction.LEFT || dir == Direction.BOTH) && x < MARGIN + ARROWWID) {
                return -2;
            }
            if((dir == Direction.RIGHT || dir == Direction.BOTH) && x > UISIZE - MARGIN - ARROWWID) {
                return -3;
            }
            return -1;
        }

        x -= MARGIN;
        y -= MARGIN;
        if(x % (btnWid + SPACING) > btnWid || y % (btnHei + SPACING) > btnHei) return -1;
        return (int)(x / (btnWid + SPACING)) + (int)(y / (btnHei + SPACING)) * substate;
    }

    // Gets the selected list item. Inputs are the mouse coordinates.
    private int getListPos(int x, int y) {
        if(x < MARGIN || y < MARGIN || x > UISIZE - MARGIN || y > UISIZE - MARGIN) return -1;

        if(dir != Direction.NEITHER && y > UISIZE - MARGIN - ARROWHEI) {
            if((dir == Direction.LEFT || dir == Direction.BOTH) && x < MARGIN + ARROWWID) {
                return LFTARROW;
            }
            if((dir == Direction.RIGHT || dir == Direction.BOTH) && x > UISIZE - MARGIN - ARROWWID) {
                return RGTARROW;
            }
            return -1;
        }

        y -= MARGIN;
        if(y % (btnHei + SPACING) > btnHei) return -1;
        int num = (int)(y / (btnHei + SPACING));
        if(num >= substate) return -1;
        return num;
    }

    // The drawing method.
    // DO NOT CALL THIS!!! CALL REDRAW!!!
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
		super.paintComponent(g2);

        if(bgImage != null) g2.drawImage(bgImage, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, uiAlpha));
        int uix = uiXPos; // "Freeze" the x position to avoid tearing
        if(uiImage != null) g2.drawImage(uiImage, uix, 0, null);
        // TODO: Adapt this for other transitions!
        if(uix != 0 && outImage != null) g2.drawImage(outImage, uix - UISIZE, 0, null);
    }

    // Don't use this.
    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    // Called when the mouse button is pressed down while in the window.
    @Override
    public void mousePressed(MouseEvent e) {
        switch(state) {
            default: return;
            case GRID:
                downBtn = getGridPos(e.getX(), e.getY());
                return;
            case LIST:
                downBtn = getListPos(e.getX(), e.getY());
                return;
        }
    }

    // Called when the mouse button is released while in the window.
    @Override
    public void mouseReleased(MouseEvent e) {
        switch(state) {
            default: return;
            case GRID:
                if(downBtn != -1) {
                    int pos = getGridPos(e.getX(), e.getY());
                    if(downBtn == pos) {
                        if(callPtr != null) callPtr.onCallback(pos);
                    }
                }
                return;
            case LIST:
                if(downBtn != -1) {
                    int pos = getListPos(e.getX(), e.getY());
                    if(downBtn == pos) {
                        if(callPtr != null) callPtr.onCallback(pos);
                    }
                }
                return;
        }
    }

    // Don't use this.
    @Override
    public void mouseEntered(MouseEvent e) {}

    // Don't use this.
    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    // Call to have the UI fade in.
    private void fadeIn() {
        uiAlpha = 0;
        new TimeKeeper(Timepiece.FADEIN).start();
    }

    // Call to have the UI fade out.
    private void fadeOut() {
        uiAlpha = 1;
        new TimeKeeper(Timepiece.FADEOUT).start();
    }

    // Call this to have the UI slide to the left.
    // SET THE UI OFFSET BEFOREHAND!!!
    private void slideLeft() {
        new TimeKeeper(Timepiece.SLIDELEFT).start();
    }

    // Class for running a time-sensitive loop in the background.
    private class TimeKeeper extends Thread {

        private static final int SKIPTIME = 10;
        private static final float ALPHASPEED = 0.05f;
        private static final float MOVESCALE = 0.9f;

        private Timepiece timeState;
        private long pretime, sleeptime;

        private TimeKeeper(Timepiece s) {
            timeState = s;
        }

        // The time-sensitive loop method.
        // DO NOT CALL THIS!!! CALL START!!!
        @Override
        public void run() {
            pretime = System.currentTimeMillis();
            while(true) {
                switch(timeState) {
                    // EVERY CASE NEEDS A RETURN STATEMENT!!!
                    // REPAINT IF SOMETHING ONSCREEN CHANGED!!!
                    default: return;
                    case FADEIN:
                        float tempAlpha = uiAlpha + ALPHASPEED;
                        if(tempAlpha > 1) {
                            uiAlpha = 1;
                            repaint();
                            return;
                        }
                        uiAlpha = tempAlpha;
                        repaint();
                        break;
                    case FADEOUT:
                        tempAlpha = uiAlpha - ALPHASPEED;
                        if(tempAlpha < 0) {
                            uiAlpha = 0;
                            repaint();
                            return;
                        }
                        uiAlpha = tempAlpha;
                        repaint();
                        break;
                    case SLIDELEFT:
                        uiXPos *= MOVESCALE;
                        if(uiXPos == 0) {
                            uiXPos = 0;
                            state = nextState;
                            repaint();
                            return;
                        }
                        repaint();
                        break;

                }
                pretime += SKIPTIME;
                sleeptime = pretime - System.currentTimeMillis();
				if(sleeptime > 0) {
					try {
						sleep(sleeptime);
					} catch (InterruptedException e) {
						System.err.println(e.getMessage());
						e.printStackTrace();
						System.exit(0);
					}
				}
            }
        }
    }

    private class SuperCallback implements Callback {

        Callback subCallback;
        String[] text;

        int pos, wid;

        private SuperCallback(Callback c, String[] t) {
            if(t.length <= MAXLIST) {
                drawList(c, Direction.LEFT, t);
                return;
            }
            subCallback = c;
            text = t;
            pos = 0;
            wid = text.length / MAXLIST;

            String[] textBlock = new String[MAXLIST];
            System.arraycopy(text, 0, textBlock, 0, MAXLIST);
            drawList(this, Direction.BOTH, textBlock);
        }

        @Override
        public void onCallback(int selection) {
            if(selection > 0) {
                subCallback.onCallback(selection + pos * MAXLIST);
            } else if(selection == LFTARROW) {
                if(pos == 0) {
                    subCallback.onCallback(selection);
                } else {
                    pos--;
                    String[] textBlock = new String[MAXLIST];
                    System.arraycopy(text, pos * MAXLIST, textBlock, 0, MAXLIST);
                    drawList(this, Direction.BOTH, textBlock);
                }
            } else if(selection == RGTARROW) {
                pos++;
                int len = MAXLIST;
                if(pos == wid) len = text.length % MAXLIST;
                String[] textBlock = new String[len];
                System.arraycopy(text, pos * MAXLIST, textBlock, 0, len);
                drawList(this, pos == wid ? Direction.LEFT : Direction.BOTH, textBlock);
            }
        }
    }
}