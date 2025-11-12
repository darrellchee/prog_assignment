import java.util.*;

public class postalService {
    private ArrayList<Customer> customers;
    private ArrayList<Order> orders;

    
    // public ArrayList<Order> getNextDayOrders(){
    //     ArrayList<Order> nextDayOrders = new ArrayList<>();
    // }

    ArrayList;<Customer> getCustomers() {
        customers = new ArrayList<Customer>();
        return customers;
    }

    void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    void addCustomer(Customer c) {
        customers.add(c);
    }

    void addCustomer(String name, String email, String phoneNumber) {
        customers.add(c);
    }

    void addCustomer(Individual individual) {
        customers.add(c);
    }

    void addCustomer(Vendor vendor) {
        customers.add(c);
    }

    postalService() {
        customers = new ArrayList<Customer>();
    }

    public static void main(String[] args) {
        postalService service = new postalService();
        ArrayList<Customer> custs = service.getCustomers();

        // objects
        Individual alice = new Individual("Alice", "alice@example.com", "555-1234", "123 Main St");
        alice.setPassword("alicepass");
        custs.add(alice);

        Vendor bob = new Vendor("Bob", "bob@biz.com", "555-5678", "456 Market St", "BobsCo", "BRN001", 0.12);
        bob.setPassword("bobpass");
        custs.add(bob);

        HashMap<Integer, String> vanOrders = new HashMap<>();
        Vehicle van1 = new Vehicle(vanOrders, 8.30, 1000.0, 200.0, null, null);

        ArrayList<Vehicle> depotVehicles = new ArrayList<>();
        depotVehicles.add(van1);
        HashMap<Order, Vehicle> depotOrders = new HashMap<>();
        Depot centralDepot = new Depot(depotVehicles, depotOrders, "Central Depot");

        Order o1 = new Order(1, 1001, "123 Main St", "789 Oak Ave", Tracking.PENDING);
        o1.setPrice(25);
        van1.addOrder(o1);
        depotOrders.put(o1, van1);

        System.out.println("Created sample objects: " + custs.size() + " customers, 1 vehicle, 1 depot, 1 order.");
        ArrayList<Vehicle> vehicles;
        Menu menu = new Menu();
        menu.pickUserType();

    }

}

// jake
// enums, postal service

// ihuhju
// darrell
// order, vehicle, depot

class Order {
    private int orderNumber;
    private int trackingNumber;
    private String pickUpAddress;
    private String deliveryAddress;
    private Tracking tracking;
    private int price;

    Order(int orderNumber, int trackingNumber, String pickUpAddress, String deliveryAddress, Tracking tracking) {
        this.orderNumber = orderNumber;
        this.trackingNumber = trackingNumber;
        this.pickUpAddress = pickUpAddress;
        this.deliveryAddress = deliveryAddress;
        this.tracking = tracking;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(int trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getPickUpAddress() {
        return pickUpAddress;
    }

    public void setPickUpAddress(String pickUpAddress) {
        this.pickUpAddress = pickUpAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Tracking getStatus() {
        return status;
    }

    public void setStatus(Tracking status) {
        this.status = status;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}

class Vehicle implements Deliverable {
    // order number, address
    private HashMap<Integer, String> orders;
    private double estimatedDeparture;
    private double maximumCapacity;
    private double currentCapacity;
    private Depot currentDepotLocation;
    private Depot nextDepotLocation;
    private ArrayList<Order> orderList;

    Vehicle(HashMap<Integer, String> orders, double estimatedDeparture, double maximumCapacity,
            double currentCapacity, Depot currentDepotLocation, Depot nextDepotLocation) {
        this.orders = orders;
        this.estimatedDeparture = estimatedDeparture;
        this.maximumCapacity = maximumCapacity;
        this.currentCapacity = currentCapacity;
        this.currentDepotLocation = currentDepotLocation;
        this.nextDepotLocation = nextDepotLocation;
        this.orderList = new ArrayList<Order>();
    }

    public HashMap<Integer, String> getOrders() {
        return this.orders;
    }

    public void setOrders(HashMap<Integer, String> order) {
        this.orders = order;
    }

    @Override
    public void addOrder(Order order) {
        orders.put(order.getOrderNumber(), order.getDeliveryAddress());
        orderList.add(order);
    }

    public double getEstimatedDeparture() {
        return estimatedDeparture;
    }

    public void setEstimatedDeparture(double estimatedDeparture) {
        this.estimatedDeparture = estimatedDeparture;
    }

    public double getMaximumCapacity() {
        return maximumCapacity;
    }

    public void setMaximumCapacity(double maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public double getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(double currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public Depot getCurrentDepotLocation() {
        return currentDepotLocation;
    }

    public void setCurrentDepotLocation(Depot currentDepotLocation) {
        this.currentDepotLocation = currentDepotLocation;
    }

    public Depot getNextDepotLocation() {
        return nextDepotLocation;
    }

    public void setNextDepotLocation(Depot nextDepotLocation) {
        this.nextDepotLocation = nextDepotLocation;
    }

}

class Depot implements Deliverable {
    private ArrayList<Vehicle> vehicles;
    private HashMap<Order, Vehicle> orders;
    private String depotLocation;
    private String depotCode;

    Depot(ArrayList<Vehicle> vehicles, HashMap<Order, Vehicle> orders, String depotLocation) {
        this.vehicles = vehicles;
        this.orders = orders;
        this.depotLocation = depotLocation;
        this.depotCode = "DEPOT " + depotLocation;
    }

    @Override
    public void addOrder(Order order) {
        Vehicle availableVehicle = findAvailableVehicle();
        if (availableVehicle != null) {
            orders.put(order, availableVehicle);
            availableVehicle.addOrder(order);
        }
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public HashMap<Order, Vehicle> getOrders() {
        return orders;
    }

    public void setOrders(HashMap<Order, Vehicle> orders) {
        this.orders = orders;
    }

    @Override
    public void updateTrackingStatus(Order order, Tracking status) {
        order.setStatus(status);
        logTrackingUpdate(order, this.depotCode, status);
    }

}

// louis
// customer, vendor, individual customer,
abstract class Customer {
    protected String customerID;
    protected String name;
    protected String email;
    protected int phoneNumber;
    protected String address;
    protected String password;
    protected ArrayList<Order> orders;
    protected ArrayList<Integer> orderNumbers;

    Customer(String name, String email, int phoneNumber, String address) {
        this.customerID = "CUST " + name + email;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.orders = new ArrayList<Order>();
        this.orderNumbers = new ArrayList<Integer>();
    }

    // Getters
    public String getCustomerID() {
        return customerID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Order> getAllOrder() {
        return this.orders;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Integer> getOrderNumbers() {
        return orderNumbers;
    }

    public int getTotalOrders() {
        return orderNumbers.size();
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

class Individual extends Customer {

    Individual(String name, String email, String phoneNumber, String address) {
        super(name, email, phoneNumber, address);
    }

}

class Vendor extends Customer {
    private String businessName;
    private String businessRegistrationNumber;
    private double discountRate; // for bulk orders

    Vendor(String name, String email, String phoneNumber, String address, String businessName,
            String businessRegistrationNumber, double discountRate) {
        super(name, email, phoneNumber, address);
        this.businessName = businessName;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.discountRate = 0.1;
    }

    public double calculateDiscount(double orderAmount) {
        return orderAmount * discountRate;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    public void setBusinessRegistrationNumber(String businessRegistrationNumber) {
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    // Have this track all orders across the system, not just for their respective
    // vendor or individual
    @Override
    public void trackAllOrders() {
        for (Order order : this.orders) {
            System.out.println("Order Number: " + order.getOrderNumber() + " - Status: " + order.getStatus());
        }
    }
}

enum Tracking {
    PENDING,
    IN_TRANSIT,
    DELIVERED,

}

enum PackageType {
    SMALL,
    MEDIUM,
    LARGE,
}

enum ServiceType {
    STANDARD,
    EXPRESS,
    SAME_DAY,
}

class Menu {
    ArrayList<Customer> customers;
    Individual individual;

    public void pickUserType() {
        System.out.println("Welcome to the postal service");
        System.out.println("Pick what best defines you");
        System.out.println("(1) Individual Customer");
        System.out.println("(2) Vendor");
        System.out.println("(3) Admin");
        while (true) {
            try {
                int userInput = In.nextInt();
                if (userInput == 1) {
                    customerMenu();
                } else if (userInput == 2) {
                    vendorMenu();
                } else if (userInput == 3) {
                    admin();
                } else {
                    System.out.println("Please input either 1, 2, 3");
                }
            } catch (Exception e) {
                System.out.println("Please input either 1, 2, 3");

            }
        }
    }

    public void loginCustomer() {
        System.out.println("(1) To login");
        System.out.println("(2) To register");
        int userInput = In.nextInt();
        if (userInput == 1) {
            while (true) {
                System.out.println("Please input your associated email");
                String customerEmail = In.nextLine();
                System.out.println("Please input your associated password");
                String password = In.nextLine();
                for (Customer customer : customers) {
                    if (customer.getEmail().equals(customerEmail) && customer.getPassword().equals(password)) {
                        individual = (Individual) customer;
                        customerMenu();
                    } else {
                        System.out.println("Email or password incorrect, please try again");
                    }
                }
            }
        } else if (userInput == 2) {
            registerCustomer();
        } else {
            System.out.println("Please input either 1 or 2");
        }

    }

    public void registerCustomer() {
        System.out.println("Please input your name");
        String name = In.nextLine();
        System.out.println("Please input your email");
        String email = In.nextLine();
        System.out.println("Please input your phone number");
        int phoneNumber = In.nextLine();
        System.out.println("Please input your address");
        String address = In.nextLine();

        individual newCustomer = new Individual(name, email, phoneNumber, address);
        System.out.println("Please input your new password");
        String password = In.nextLine();
        newCustomer.setPassword(password);
        customers.add(newCustomer);

        System.out.println("Registration complete, redirecting to customer menu");
        loginCustomer();

    }

    public void customerMenu() {
        System.out.println("Hello there " + individual.getName());
        System.out.println("(1) To orders menu");
        System.out.println("(2) To profile menu");
        System.out.println("(3) To main menu");
        while (true) {
            try {
                int userInput = In.nextInt();
                if (userInput == 1) {
                    customerOrdersMenu();
                } else if (userInput == 2) {
                    customerProfileMenu();
                } else if (userInput == 3) {
                    pickUserType();
                } else {
                    System.out.println("Please input either 1 or 2");
                }
            } catch (Exception e) {
                System.out.println("Please input either 1 or 2");
            }
        }

    }

    public void customerOrdersMenu() {
        System.out.println("(1) To place order");
        System.out.println("(2) To track order");
        while (true) {
            try {
                int userInput = In.nextInt();
                if (userInput == 1) {
                    ArrayList<Integer> orders = individual.getOrderNumbers();
                } else if (userInput == 2) {
                    // pass
                } else {
                    System.out.println("Please input either 1, 2, or 3");
                }
            } catch (Exception e) {
                System.out.println("Please input either 1, 2, or 3");
            }
        }
    }

    public void customerProfileMenu() {
        System.out.println("(1) To view profile");
        System.out.println("(2) To edit profile");
        System.out.println("(3) To main menu");
        while (true) {
            try {
                int userInput = In.nextInt();
                if (userInput == 1) {
                    System.out.println("Name: " + individual.getName());
                    System.out.println("Email: " + individual.getEmail());
                    System.out.println("Phone Number: " + individual.getPhoneNumber());
                    System.out.println("Address: " + individual.getAddress());
                } else if (userInput == 2) {
                    System.out.println("Please input your name");
                    String name = In.nextLine();
                    System.out.println("Please input your email");
                    String email = In.nextLine();
                    System.out.println("Please input your phone number");
                    String phoneNumber = In.nextLine();
                    System.out.println("Please input your address");
                    String address = In.nextLine();
                    individual.setName(name);
                    individual.setEmail(email);
                    individual.setPhoneNumber(phoneNumber);
                    individual.setAddress(address);
                } else if (userInput == 3) {
                    customerMenu();
                } else {
                    System.out.println("Please input either 1 or 2");
                }
            } catch (Exception e) {
                System.out.println("Please input either 1 or 2");
            }
        }
    }

    public void trackOrder() {
        System.out.println("Enter your tracking number:");
        int trackingNumber = In.nextInt();

        for (Depot depot : allDepots) {
            if (depot.hasOrder(trackingNumber)) {
                Order order = depot.findOrder(trackingNumber);
                System.out.println("Order Status: " + order.getStatus());
                System.out.println("Current Location: " + depot.getLocation());
                if (order.getStatus() == Tracking.IN_TRANSIT) {
                    Vehicle vehicle = depot.getOrders().get(
                            System.out.println("Next stop: " + vehicle.getNextDepotLocation()));
                    System.out.println("Estimated arrival: " + vehicle.getEstimatedDeparture());
                }
                break;

            }
        }
    }

    public void vendorMenu() {
        System.out.println("(1) To track all orders");
        System.out.println("(2) To profile menu");
        System.out.println("(3) To go back to main menu");
        while (true) {
            try {
                int userInput = In.nextInt();
                if (userInput == 1) {

                } else if (userInput == 2) {
                    pickUserType();
                } else {
                    System.out.println("Please input either 1 or 2");
                }
            } catch (Exception e) {
                System.out.println("Please input either 1 or 2");
            }
        }

    }

    // Add comparators here for truck fill
    public void admin() {

    }

}

public interface Sorting {
    void sortPrice();

    void sortDate();
}

public interface Deliverable {
    void addOrder(Order order);

    void updateTrackingStatus(Order order, Tracking status);
}
