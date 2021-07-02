import java.util.*;
import java.time.*;

class Restaurant {
    private ArrayList<Table> tables = new ArrayList<>();
    private ArrayList<Waitstaff> waitstaff = new ArrayList<>();

    private class Table{
        private int num;
        private int max_capacity;
        private int cur_capacity = 0;
        private long timeSeated;
        private double bill = 0.0;
        private String staff = "NONE";

        // Initializer for table
        private Table(int n, int max) {
            num = n;
            max_capacity = max;
        }

        // Assigns staff member to table
        private void assign(String name) {
            staff = name;
        }

        // Returns staff member assigned to table
        private String getStaff() {
            return staff;
        }

        // Returns table number
        private int getNum() {
            return num;
        }

        // Returns maximum capacity of table
        private int getMax_capacity() {
            return max_capacity;
        }

        // Returns current capacity of table
        private int getCur_capacity() {
            return cur_capacity;
        }

        /* Checks if table can hold the given number of guests. Prints error message and returns 1 if unable to seat
           guests.
         */
        private int seat(int guests) {
            if (guests > max_capacity) {
                System.out.print("Too many guests! Select a bigger table.");
                return 1;
            }
            else {
                cur_capacity = guests;
                timeSeated = System.currentTimeMillis();
            }
            return 0;
        }

        /* Checks if table is empty. If not, prints the number of the elapsed number of minutes a table has been
           in-use for.
         */
        private float getSeatTime() {
            if (cur_capacity == 0) {
                return 0;
            }
            long current_time = System.currentTimeMillis();
            float milli_sec = current_time - timeSeated;
            float min = (milli_sec / 1000f) / 60f;
            return min;
        }

        // Frees up a table and returns 0. Prints error message and returns 1 if table is empty.
        private int freeTable() {
            if (cur_capacity == 0) {
                System.out.print("Table is already empty.");
                return 1;
            }
            else {
                cur_capacity = 0;
                timeSeated = 0;
                // Resets bill of table to $0.00
                bill = 0;
            }
            return 0;

        }

        // Returns the bill
        private double getBill() {
            return bill;
        }

        // Adds price of item to bill.
        private void addToBill(double item) {
            bill += item;
        }

    }

    private class Waitstaff {
        private String name;
        private long startTime;
        private double total = 0.0;

        private Waitstaff(String n) {
            name = n;
            startTime = System.currentTimeMillis();
        }

        private String getName() {
            return name;
        }

        private double getTotal() {
            return total;
        }

        private double addToTotal(double bill) {
            total += bill;
            return total;
        }

        private long getStartTime() {
            return startTime;
        }

        private float getTimeWorked() {
            long current_time = System.currentTimeMillis();
            float milli_sec = current_time - startTime;
            float min = (milli_sec / 1000f) / 60f;
            return min;
        }


    }

    // Restaurant initializer
    public Restaurant(String[] staff) {
        for (int i = 0; i < staff.length; i++) {
            waitstaff.add(new Waitstaff(staff[i]));
        }
    }

    public Restaurant() {}


    // Returns waitstaff.
    public ArrayList<Waitstaff> getWaitstaff() {
        return waitstaff;
    }

    public ArrayList<String> getStaffNames() {
        ArrayList<String> names = new ArrayList<>();
        Iterator<Waitstaff> iter = waitstaff.iterator();
        while (iter.hasNext()) {
            names.add(iter.next().getName());
        }
        return names;
    }

    public Waitstaff getStaff(String name) {
        Iterator<Waitstaff> iter = waitstaff.iterator();
        while (iter.hasNext()) {
            Waitstaff staff = iter.next();
            if (staff.getName().equals(name)) {
                return staff;
            }
        }
        return null;
    }
    // Initializer for tables.
    public void init_tables(int num) {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < num; i++) {

            // Loops until user enters valid integer when prompted.
            int valid = 1;
            while (valid == 1) {
                try {
                    System.out.printf("What is the maximum seating capacity for table #%d: ", i + 1);
                    String input = scanner.nextLine();
                    int cap = Integer.parseInt(input);
                    tables.add(new Table(i + 1, cap));
                    valid = 0;
                } catch (NumberFormatException e) {
                    System.out.println("Invlaid input. Please only enter an integer.\n");
                }
            }

        }
    }

    public int numOfTables() {
        return tables.size();
    }

    public int numOfStaff() {
        return waitstaff.size();
    }

    // Retrieves table given its number
    public Table getTable(int num) { return tables.get(num - 1);}

    // Retrieves ArrayList of all tables associated with a staff member.
    public ArrayList<Integer> getTablesForStaff(String name) {
        if (!getStaffNames().contains(name)) {
            System.out.println("Staff member does not exist. Please make sure they have been added to the shift.");
            return null;
        }

        ArrayList<Integer> tbls = new ArrayList<>();
        Iterator<Table> iter = tables.iterator();
        while (iter.hasNext()) {
            Table t = iter.next();
            if (t.getStaff().equals(name)) {
                tbls.add(t.getNum());
            }
        }
        return tbls;
    }

    // Retrieved an ArrayList of the empty tables.
    public ArrayList<Integer> getEmptyTables() {
        ArrayList<Integer> empty = new ArrayList<>();
        Iterator<Table> iter = tables.iterator();
        while (iter.hasNext()) {
            Table t = iter.next();
            if (t.getCur_capacity() == 0) {
                empty.add(t.getNum());
            }
        }
        return empty;
    }

    // Provides status of each table including bill, staff member, and seat time.
    public void floor_status() {
        System.out.println("\n+---------------------------------------+");
        System.out.println("Floor Status: ");
        Iterator<Waitstaff> iter = waitstaff.iterator();
        while (iter.hasNext()) {
            Waitstaff staff = iter.next();
            System.out.printf("Product Sold by %s: $%.2f\n", staff.getName(), staff.getTotal());
        }
        Iterator<Table> iter2 = tables.iterator();
        while (iter2.hasNext()) {
            Table t = iter2.next();
            System.out.println("\n+---------------------------------------+");
            System.out.printf("Table #%d has %d guests. Bill is $%.2f", t.getNum(), t.getCur_capacity(), t.getBill());
            System.out.printf("\nElapsed seating time: %.3f", t.getSeatTime());
            System.out.printf("\nStaff member assigned to table: %s", t.getStaff());
            System.out.println("\n+---------------------------------------+");
        }
    }
    // Assigns staff member to table.
    public int assignToTable(String name, int num) {
        // Checks if table number is valid
        if ( (num <= 0) || (num > tables.size())) {
            System.out.println("Invalid entry for table number.");
            return 1;
        }
        // Checks if staff member's name is valid.
        if (!getStaffNames().contains(name)) {
            System.out.println("Invalid entry for staff member.");
            return -1;
        }
        tables.get(num - 1).assign(name);
        return 0;

    }

    // Seats a number of guests at a table. Returns 0 if completed. Upon error, prints message and returns 1;
    public int seatGuests(int tblnum, int gnum) {
        // Checks if input for table number is valid
        if ( (tblnum <= 0) || (tblnum > tables.size())) {
            System.out.println("Invalid entry for table number.");
            return 1;
        }
        Table t = getTable(tblnum);
        // Checks if table is empty
        if (getEmptyTables().contains(t)) {
            // Checks if number of guests exceeds the max capacity of table
            if (gnum > t.getMax_capacity()) {
                System.out.println("Number of guests above table seating capacity.");
                return 1;
            }
            else {
                t.seat(gnum);
                return 0;
            }
        }
        else {
            System.out.println("Table is already being used.");
        }
        return 1;
    }

    // Adds staff to waistaff
    public int addStaff(String name) {
        if (waitstaff.contains(name)) {
            System.out.println("Staff member already added.");
            return 1;
        }

        waitstaff.add(new Waitstaff(name));
        return 0;
    }

    // Removes staff from waistaff
    public int remStaff(String name) {
        if (!getStaffNames().contains(name)) {
            System.out.println("Staff member does not exist.");
            return 1;
        }
        // Removes staff name from table assignments
        Iterator<Table> iter = tables.iterator();
        while (iter.hasNext()) {
            Table t = iter.next();
            if (t.getStaff().equals(name)) {
                t.assign("NONE");
            }
        }
        waitstaff.remove(getStaff(name));
        return 0;
    }

    public static void main (String[] args) {
        Scanner scanner = new Scanner(System.in);
        Restaurant r = new Restaurant();
        System.out.println("+---------------------------------------------+");
        if (r.numOfTables() < 1) {
            boolean done = false;
            while (!done) {
                try {
                    System.out.println("Tables have not been initialized.");
                    System.out.print("How many tables is your restaurant using? ");
                    int n =  Integer.parseInt(scanner.nextLine());
                    r.init_tables(n);
                    done = true;
                } catch (Exception e) {
                    System.out.println("Invalid input. Please try again.");
                }
            }
            System.out.println("\n+---------------------------------------------+");
        }
        if (r.numOfStaff() < 1) {
            boolean done = false;
            while (!done) {
                try {
                    System.out.println("No staff entered onto current shift.");
                    System.out.print("Please enter your name. ");
                    String name = scanner.nextLine();
                    r.addStaff(name);
                    done = true;
                } catch (Exception e) {
                    System.out.println("Invalid input. Please try again.");
                }
            }
            System.out.println("\n+---------------------------------------------+");
        }

        int exit = 0;
        while (exit == 0) {
            r.floor_status();
            System.out.println("Select one of the following menu options:");
            System.out.println("[1] Seat Guest");
            System.out.println("[2] Add Employee to Shift");
            System.out.println("[3] Assign Staff to Table");
            System.out.println("[4] Edit Layout of Restaurant");
            System.out.println("[0] Exit");
            System.out.print("Input: ");
            String input = scanner.nextLine();
            if (Integer.parseInt(input) == 2) {
                System.out.print("Please enter their name. ");
                String name = scanner.nextLine();
                r.addStaff(name);
                System.out.printf("%s has been added to the shift.\n", name);
            }
            if (Integer.parseInt(input) == 3) {
                int a = 1;
                String name = null;
                int num = 0;
                while (a != 0) {
                    System.out.println("Current Staff:");
                    System.out.println(r.getStaffNames());
                    System.out.print("Who would you like to assign to a table? ");
                    name = scanner.nextLine();
                    System.out.printf("What table would you like to assign %s to? ", name);
                    num = Integer.parseInt(scanner.nextLine());
                    a = r.assignToTable(name, num);
                }
                System.out.printf("%s has been assigned to table #%d\n", name, num);
            }
            if (Integer.parseInt(input) == 0) {
                exit = 1;
                System.out.println("Closing Application...");
            }
        }

    }



}