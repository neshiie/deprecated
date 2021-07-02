package edu.team9.restaurantms;

public class UITest {

    NitroUI ui;

    UITest() {
        ui = new NitroUI();
        ui.drawLongList(new testCallback(), "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M");
    }

    public static void main(String[] args) {
        new UITest();
    }

    class testCallback implements Callback {
        @Override
        public void onCallback(int selection) {
            System.out.println("Button pressed: " + selection);

            ui.drawList(new testCallback(), NitroUI.Direction.RIGHT, "Mistake!");
        }
    }
}