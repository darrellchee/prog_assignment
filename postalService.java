import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

// MAIN POSTAL SERVICE CLASS - Jake
public class postalService implements Sorting {
    private ArrayList<Customer> customers;
    private ArrayList<Order> orders;
    private ArrayList<Depot> depots;
    private ArrayList<Vehicle> vehicles;
    private int currentDay = 1;
    private int currentHour = 8;

    // Initalize all objects
    postalService() {
        customers = new ArrayList<Customer>();
        vehicles = new ArrayList<Vehicle>();
        orders = new ArrayList<Order>();
        depots = new ArrayList<Depot>();

    }

    @Override
    public String toString() {
        return "PostalService{" + "customers=" + customers + ", orders=" + orders + ", depots=" + depots + ", vehicles="
                + vehicles + ", currentDay=" + currentDay + ", currentHour=" + currentHour + '}';
    }

    ArrayList<Customer> getCustomers() {
        return customers;

    }

    void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;

    }

    void addCustomer(Customer c) {
        customers.add(c);

    }

    // Overloaded method
    void addCustomer(String name, String email, String phoneNumber, String address) {
        Individual individual = new Individual(name, email, phoneNumber, address);
        customers.add(individual);

    }

    // Overloaded method
    void addCustomer(Individual individual) {
        customers.add(individual);
    }

    // Overloaded method
    void addCustomer(Vendor vendor) {
        customers.add(vendor);
    }

    ArrayList<Depot> getDepots() {
        return depots;
    }

    void addDepot(Depot depot) {
        depots.add(depot);
    }

    ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    ArrayList<Order> getAllOrders() {
        return orders;
    }

    // Customer remove from email
    boolean removeCustomer(String email) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getEmail().equals(email)) {
                customers.remove(i);
                return true;
            }
        }
        return false;
    }

    // Find customer with Email
    Customer findCustomer(String email) {
        for (Customer c : customers) {
            if (c.getEmail().equals(email)) {
                return c;
            }
        }
        return null;
    }

    // Use instanceof to only return vendors
    ArrayList<Vendor> getVendorsOnly() {
        ArrayList<Vendor> vendors = new ArrayList<>();
        for (Customer c : customers) {
            if (c instanceof Vendor) { // Using instanceof for polymorphism
                vendors.add((Vendor) c);
            }
        }
        return vendors;
    }

    // Filer by status
    ArrayList<Order> getOrdersByStatus(Tracking status) {
        ArrayList<Order> sorted = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == status) {
                sorted.add(order);
            }
        }
        return sorted;
    }

    // THE method to place orders

    Order placeOrder(Customer customer, String pickupAddress, String deliveryAddress,
            ServiceType serviceType, PackageType packageType, double weight) {
        int orderNum = orders.size() + 1;
        int trackNum = 1000 + orderNum;
        // Constructor for oder
        Order order = new Order(orderNum, trackNum, pickupAddress, deliveryAddress,
                Tracking.PENDING, packageType, serviceType);
        order.setWeight(weight);
        order.setCreatedDay(currentDay);

        // calculate base price
        double basePrice = order.calculatePrice();

        // Apply vendor discount if it is vendor and not normal customer
        if (customer instanceof Vendor) {
            Vendor vendor = (Vendor) customer;
            double discount = vendor.calculateDiscount(basePrice);
            order.setPrice(basePrice - discount);
        } else {
            order.setPrice(basePrice);
        }

        // add order to the order list
        customer.addOrder(order);
        orders.add(order);

        if (depots.size() > 0) {
            Depot depot = findBestDepot(pickupAddress);
            depot.addOrder(order);
        }

        return order;
    }

    // finding closest depot based on keywords like "Central" or "main"
    Depot findBestDepot(String address) {
        for (Depot depot : depots) {
            if (address.contains("Central") || address.contains("Main")) {
                if (depot.getLocation().contains("Central")) {
                    return depot;
                }
            }
        }
        // Uses depo index 0 as default if no matching
        return depots.get(0);
    }

    // method for time advancement
    void advanceTime(int hours) {
        currentHour = currentHour + hours;
        // while loop to calculate if more than 24 hours so it will be one day
        while (currentHour >= 24) {
            currentHour = currentHour - 24;
            currentDay++;
            processDeliveries(); // Process deliveries each new day
        }
    }

    // process delivery
    void processDeliveries() {
        System.out.println("Processing deliveries for day " + currentDay);

        for (Order order : orders) {
            // Move all pending orders to in transit
            if (order.getStatus() == Tracking.PENDING) {
                order.setStatus(Tracking.IN_TRANSIT);
            }
            // Check if in transit orders should be delivered
            else if (order.getStatus() == Tracking.IN_TRANSIT) {
                int daysInTransit = currentDay - order.getCreatedDay();

                // Deliver on service type
                if (order.getServiceType() == ServiceType.SAME_DAY && daysInTransit >= 1) {
                    order.setStatus(Tracking.DELIVERED);
                } else if (order.getServiceType() == ServiceType.EXPRESS && daysInTransit >= 2) {
                    order.setStatus(Tracking.DELIVERED);
                } else if (order.getServiceType() == ServiceType.STANDARD && daysInTransit >= 3) {
                    order.setStatus(Tracking.DELIVERED);
                }
            }
        }
    }

    int getCurrentDay() {
        return currentDay;
    }

    int getCurrentHour() {
        return currentHour;
    }

    double getTotalRevenue() {
        double total = 0.0;
        for (Order order : orders) {
            total = total + order.getPrice();
        }
        return total;
    }

    // INTERFACE IMPLEMENTATION FOR SORTING
    @Override
    public void sortPrice(List<Order> list) {
        Collections.sort(list, new Comparator<Order>() {
            public int compare(Order o1, Order o2) {
                if (o1.getPrice() < o2.getPrice())
                    return -1;
                if (o1.getPrice() > o2.getPrice())
                    return 1;
                return 0;
            }
        });
    }

    @Override
    public void sortDate(List<Order> list) {
        Collections.sort(list, new Comparator<Order>() {
            public int compare(Order o1, Order o2) {
                return o1.getCreatedDay() - o2.getCreatedDay();
            }
        });
    }

    @Override
    public void sortStatus(List<Order> list) {
        Collections.sort(list, new Comparator<Order>() {
            public int compare(Order o1, Order o2) {
                return o1.getStatus().ordinal() - o2.getStatus().ordinal();
            }
        });
    }

    public static void main(String[] args) {
        postalService service = new postalService();

        // Setup initial test data
        ArrayList<Customer> custs = service.getCustomers();

        // Create people
        Individual Jake = new Individual("Jake", "Jake@gmail.com", "04127432", "123 Main St");
        Jake.setPassword("jake");
        custs.add(Jake);

        Vendor Darrell = new Vendor("Darrell", "darrell@gmail.com", "042349205", "456 Market St",
                "BobsCo", "BRN001", 0.12);
        Darrell.setPassword("darrell");
        custs.add(Darrell);

        Individual louis = new Individual("louis", "louis@gmail.com", "04124895", "789 Oak St");
        louis.setPassword("louis");
        custs.add(louis);

        Vendor timothy = new Vendor("timothy", "mega@mart.com", "04134825", "12 High St",
                "MegaMart Ltd", "BRN002", 0.15);
        timothy.setPassword("timothy");
        custs.add(timothy);

        ArrayList<Vehicle> centralVehicles = new ArrayList<>();
        HashMap<Order, Vehicle> centralOrders = new HashMap<>();
        Depot centralDepot = new Depot(centralVehicles, centralOrders, "Central Depot");
        service.addDepot(centralDepot);

        ArrayList<Vehicle> northVehicles = new ArrayList<>();
        HashMap<Order, Vehicle> northOrders = new HashMap<>();
        Depot northDepot = new Depot(northVehicles, northOrders, "North Depot");
        service.addDepot(northDepot);

        HashMap<Integer, String> vanOrders = new HashMap<>();
        Vehicle van1 = new Vehicle(vanOrders, 8.30, 1000.0, 200.0, centralDepot, null);
        centralVehicles.add(van1);
        service.addVehicle(van1);

        HashMap<Integer, String> truckOrders = new HashMap<>();
        Vehicle truck1 = new Vehicle(truckOrders, 9.00, 1500.0, 300.0, northDepot, null);
        northVehicles.add(truck1);
        service.addVehicle(truck1);

        // Createorder
        Order o1 = new Order(1, 1001, "123 Main St", "789 Oak Ave", Tracking.PENDING,
                PackageType.MEDIUM, ServiceType.STANDARD);
        o1.setPrice(25);
        van1.addOrder(o1);
        centralOrders.put(o1, van1);
        service.orders.add(o1);

        System.out.println("Postal Service System Started!");
        System.out.println("Created: " + custs.size() + " customers, " +
                service.vehicles.size() + " vehicles, " +
                service.depots.size() + " depots, 1 order.");

        // Start the actual code
        Menu menu = new Menu(service);
        menu.pickUserType();
    }
}

// ORDER CLASS - Darrell
class Order {
    private int orderNumber;
    private int trackingNumber;
    private String pickUpAddress;
    private String deliveryAddress;
    private Tracking status;
    private double price;
    private double weight;
    private PackageType packageType;
    private ServiceType serviceType;
    private int createdDay = 1;

    Order(int orderNumber, int trackingNumber, String pickUpAddress, String deliveryAddress,
            Tracking status, PackageType packageType, ServiceType serviceType) {
        this.orderNumber = orderNumber;
        this.trackingNumber = trackingNumber;
        this.pickUpAddress = pickUpAddress;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.packageType = packageType;
        this.serviceType = serviceType;
        this.price = calculatePrice();
    }

    // Calculate price based on service and package type
    public double calculatePrice() {
        double basePrice = 10; // Starting price

        // Add cost on service type
        if (serviceType == ServiceType.STANDARD) {
            basePrice = basePrice + 0; // No extra
        } else if (serviceType == ServiceType.EXPRESS) {
            basePrice = basePrice * 1.5; // 50% extra
        } else if (serviceType == ServiceType.SAME_DAY) {
            basePrice = basePrice * 2.5; // 150% extra
        }

        // Add cost on package size
        if (packageType == PackageType.SMALL) {
            basePrice = basePrice + 0; // No extra charge
        } else if (packageType == PackageType.MEDIUM) {
            basePrice = basePrice + 5; // Add $5
        } else if (packageType == PackageType.LARGE) {
            basePrice = basePrice + 10; // Add $10
        }

        return basePrice;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public int getTrackingNumber() {
        return trackingNumber;
    }

    public String getPickUpAddress() {
        return pickUpAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public Tracking getStatus() {
        return status;
    }

    public double getPrice() {
        return price;
    }

    public double getWeight() {
        return weight;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public int getCreatedDay() {
        return createdDay;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setTrackingNumber(int trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setPickUpAddress(String pickUpAddress) {
        this.pickUpAddress = pickUpAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setStatus(Tracking status) {
        this.status = status;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setCreatedDay(int day) {
        this.createdDay = day;
    }

    // toString of order
    @Override
    public String toString() {
        return "Order #" + orderNumber + " Tracking #" + trackingNumber + " Status: " + status + " Price: $" + price;
    }
}

// VEHICLE CLASS - Darrell
class Vehicle implements Deliverable {
    private HashMap<Integer, String> orders; // Order number to address
    private double estimatedDeparture;
    private double maximumCapacity;
    private double currentCapacity;
    private Depot currentDepotLocation;
    private Depot nextDepotLocation;
    private ArrayList<Order> orderList;

    // Constructor for vehicle
    Vehicle(HashMap<Integer, String> orders, double estimatedDeparture, double maximumCapacity, double currentCapacity,
            Depot currentDepotLocation, Depot nextDepotLocation) {
        this.orders = orders;
        this.estimatedDeparture = estimatedDeparture;
        this.maximumCapacity = maximumCapacity;
        this.currentCapacity = currentCapacity;
        this.currentDepotLocation = currentDepotLocation;
        this.nextDepotLocation = nextDepotLocation;
        this.orderList = new ArrayList<Order>();
    }

    // Interface method - add order to vehicle
    @Override
    public void addOrder(Order order) {
        // Check if we have capacity
        double newCapacity = currentCapacity + order.getWeight();
        if (newCapacity > maximumCapacity) {
            System.out.println("Cannot add order - exceeds vehicle capacity");
            return;
        }

        // Add the order
        orders.put(order.getOrderNumber(), order.getDeliveryAddress());
        orderList.add(order);
        currentCapacity = newCapacity;
    }

    // Interface method update order tracking
    @Override
    public void updateTrackingStatus(Order order, Tracking status) {
        order.setStatus(status);
        System.out.println("Vehicle updated order " + order.getOrderNumber() + " to " + status);
    }

    // Process all deliveries in this vehicle
    public void processDeliveries() {
        for (Order order : orderList) {
            if (order.getStatus() == Tracking.IN_TRANSIT) {
                // Mark as delivered
                updateTrackingStatus(order, Tracking.DELIVERED);
            }
        }
    }

    // All getters
    public HashMap<Integer, String> getOrders() {
        return this.orders;
    }

    public double getEstimatedDeparture() {
        return estimatedDeparture;
    }

    public double getMaximumCapacity() {
        return maximumCapacity;
    }

    public double getCurrentCapacity() {
        return currentCapacity;
    }

    public Depot getCurrentDepotLocation() {
        return currentDepotLocation;
    }

    public Depot getNextDepotLocation() {
        return nextDepotLocation;
    }

    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    // All setters
    public void setOrders(HashMap<Integer, String> order) {
        this.orders = order;
    }

    public void setEstimatedDeparture(double estimatedDeparture) {
        this.estimatedDeparture = estimatedDeparture;
    }

    public void setMaximumCapacity(double maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public void setCurrentCapacity(double currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public void setCurrentDepotLocation(Depot currentDepotLocation) {
        this.currentDepotLocation = currentDepotLocation;
    }

    public void setNextDepotLocation(Depot nextDepotLocation) {
        this.nextDepotLocation = nextDepotLocation;
    }

@Override
public String toString() {
    String nextDepotCode = "None";
    if (nextDepotLocation != null) {
        nextDepotCode = nextDepotLocation.getDepotCode();
    }

    return "Vehicle{" + "orders=" + orders + ", estimatedDeparture=" + estimatedDeparture + ", maximumCapacity=" + maximumCapacity + ", currentCapacity=" + currentCapacity + ", currentDepotLocation=" + currentDepotLocation.getDepotCode() + ", nextDepotLocation=" + nextDepotCode + '}';
}

}

// DEPOT CLASS - Darrell
class Depot implements Deliverable {
    private ArrayList<Vehicle> vehicles;
    private HashMap<Order, Vehicle> orders; // Maps orders to vehicles
    private String depotLocation;
    private String depotCode;

    // Constructor for depot
    Depot(ArrayList<Vehicle> vehicles, HashMap<Order, Vehicle> orders, String depotLocation) {
        this.vehicles = vehicles;
        this.orders = orders;
        this.depotLocation = depotLocation;
        this.depotCode = "DEPOT " + depotLocation;
    }

    // Interface methoda dd order to depot
    @Override
    public void addOrder(Order order) {
        Vehicle availableVehicle = findAvailableVehicle();
        if (availableVehicle != null) {
            orders.put(order, availableVehicle);
            availableVehicle.addOrder(order);
        }
    }

    // Interface method update tracking status
    @Override
    public void updateTrackingStatus(Order order, Tracking status) {
        order.setStatus(status);
        System.out.println("[" + depotCode + "] Updated order " + order.getOrderNumber() + " to " + status);
    }

    // find available vehicle
    private Vehicle findAvailableVehicle() {
        if (vehicles.size() > 0) {
            return vehicles.get(0);
        }
        return null;
    }

    // check if depot has a order 
    public boolean hasOrder(int trackingNumber) {
        for (Order order : orders.keySet()) {
            if (order.getTrackingNumber() == trackingNumber) {
                return true;
            }
        }
        return false;
    }

    // Find order by the order's vtracking number
    public Order findOrder(int trackingNumber) {
        for (Order order : orders.keySet()) {
            if (order.getTrackingNumber() == trackingNumber) {
                return order;
            }
        }
        return null;
    }

    // Getters
    public String getLocation() {
        return depotLocation;
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public String getDepotCode() {
        return depotCode;
    }

    public HashMap<Order, Vehicle> getOrders() {
        return orders;
    }

    // Setters
    public void setVehicles(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void setOrders(HashMap<Order, Vehicle> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "Depot{" + "vehicles=" + vehicles + ", orders=" + orders.keySet() + ", depotLocation=" + depotLocation
                + ", depotCode=" + depotCode + '}';
    }
}

// ABSTRACT CUSTOMER CLASS - Louis
// parent class for Individual and Vendor
abstract class Customer {
    protected String customerID;
    protected String name;
    protected String email;
    protected String phoneNumber;
    protected String address;
    protected String password;
    protected ArrayList<Order> orders;
    protected ArrayList<Integer> orderNumbers;

    // constructor for all customers
    Customer(String name, String email, String phoneNumber, String address) {
        this.customerID = "CUST " + name + email;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.orders = new ArrayList<Order>();
        this.orderNumbers = new ArrayList<Integer>();
    }

    // Abstract methods
    public abstract double calculateShippingRate(double baseRate);

    public abstract String getCustomerType();

    public abstract void displayCustomerInfo();

    // Add an order to this customer
    public void addOrder(Order order) {
        orders.add(order);
        orderNumbers.add(order.getOrderNumber());
        order.setStatus(Tracking.PENDING);
    }

    // track orders for this customer
    public void trackMyOrders() {
        for (Order order : this.orders) {
            System.out.println("Order Number: " + order.getOrderNumber() +
                    " Status: " + order.getStatus() +
                    " Tracking #: " + order.getTrackingNumber() +
                    " Delivery Address: " + order.getDeliveryAddress());
        }
        System.out.println("Total Orders: " + orders.size());
    }

    // track all orders
    public void trackAllOrders() {
        for (Order order : this.orders) {
            System.out.println("Order Number: " + order.getOrderNumber() + " - Status: " + order.getStatus());
        }
    }

    // All the getter
    public String getCustomerID() {
        return customerID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Order> getAllOrders() {
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

    public ArrayList<Integer> getOrderNumbers() {
        return orderNumbers;
    }

    public int getTotalOrders() {
        return orderNumbers.size();
    }

    // all setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @Override
    public String toString() {
        return "Customer{" + "customerID=" + customerID + ", name=" + name + ", email=" + email + ", phoneNumber="
                + phoneNumber + ", address=" + address + ", orders=" + orders + '}';
    }
}

// INDIVIDUAL CLASS - Louis - child class of Customer for individual customers

class Individual extends Customer {

    Individual(String name, String email, String phoneNumber, String address) {
        super(name, email, phoneNumber, address);
    }

    // abstract method ov1erride
    @Override
    public double calculateShippingRate(double baseRate) {
        return baseRate; // Individuals pay full price
    }

    // abstract method
    @Override
    public String getCustomerType() {
        return "Individual";
    }

    // abstract method
    @Override
    public void displayCustomerInfo() {
        System.out.println("=== Individual Customer Info ===");
        System.out.println("Customer ID: " + customerID);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Total Orders: " + getTotalOrders());
    }

    @Override
    public String toString() {
        return "Individual Customer: " + name + " (" + email + ")";
    }
}

// VENDOR CLASS - Louis
//child class of Customer for vendor customers
class Vendor extends Customer {
    private String businessName;
    private String businessRegistrationNumber;
    private double discountRate;
    private int monthlyOrderCount;

    Vendor(String name, String email, String phoneNumber, String address, String businessName,
            String businessRegistrationNumber, double discountRate) {
        super(name, email, phoneNumber, address);
        this.businessName = businessName;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.discountRate = discountRate;
        this.monthlyOrderCount = 0;
    }

    // abstract method - apply vendor discount
    @Override
    public double calculateShippingRate(double baseRate) {
        return baseRate * (1 - discountRate); // Apply discount
    }

    // abstract method - return customer type
    @Override
    public String getCustomerType() {
        return "Vendor";
    }

    // abstract method - display vendor info
    @Override
    public void displayCustomerInfo() {
        System.out.println("=== Vendor Customer Info ===");
        System.out.println("Customer ID: " + customerID);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Business Name: " + businessName);
        System.out.println("Business Registration Number: " + businessRegistrationNumber);
        System.out.println("Discount Rate: " + (discountRate * 100) + "%");
        System.out.println("Total Orders: " + getTotalOrders());
        System.out.println("Business Tier: " + getVendorTier());
    }

    // calculate discount amount for an order


    public double calculateDiscount(double orderAmount) {
        return orderAmount * discountRate;
    }

    // Calculate bulk discount based on order count

    public double calculateBulkDiscount(int orderCount, double totalAmount) {
        double discount = discountRate;
        // increase discount for bulk orders
        if (orderCount > 3) {
            discount = discount + 0.05;
        } else if (orderCount > 5) {
            discount = discount + 0.10;
        } else if (orderCount > 10) {
            discount = discount + 0.15;
        }
        return totalAmount * discount;
    }

    // Get vendor tier based on total orders

    public String getVendorTier() {
        int totalOrders = getTotalOrders();
        if (totalOrders >= 10) {
            return "Platinum";
        } else if (totalOrders >= 5) {
            return "Gold";
        } else if (totalOrders >= 3) {
            return "Silver";
        } else {
            return "Bronze";
        }
    }

    // Display business info stats
    public void displayBusinessAnalytics() {
        System.out.println("=== Business Analytics ===");
        System.out.println("Business Name: " + businessName);
        System.out.println("Total Orders This Month: " + monthlyOrderCount);
        System.out.println("Current Vendor Tier: " + getVendorTier());
        System.out.println("Discount Rate: " + (discountRate * 100) + "%");
    }

   
    public void increaseMonthlyOrders() {
        monthlyOrderCount++;
    }

    // Override parent method for order trzcking
    @Override
    public void trackAllOrders() {
        System.out.println("=== Vendor Order Tracking ===");
        System.out.println("Business: " + businessName);
        super.trackAllOrders(); // Call parent method
        System.out.println("Monthly Order Count: " + monthlyOrderCount);
    }

    // Getters
    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    // Setters
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setBusinessRegistrationNumber(String businessRegistrationNumber) {
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    @Override
    public String toString() {
        return "Vendor: " + businessName + " (" + name + " - " + email + ")";
    }
}

// ENUMS - Jake

// Enum for tracking
enum Tracking {
    PENDING, IN_TRANSIT, DELIVERED;

    // Check order final status
    public boolean isFinalStatus() {
        if (this == DELIVERED) {
            return true;
        } else {
            return false;
        }
    }
}

// Enum for package types
enum PackageType {
    SMALL, MEDIUM, LARGE
}

// Enum for service types
enum ServiceType {
    STANDARD, EXPRESS, SAME_DAY
}

// INTERFACES - Jake

interface Sorting {

    void sortPrice(List<Order> list);

    void sortDate(List<Order> list);

    void sortStatus(List<Order> list);

}

// Interface for deliverable items
interface Deliverable {
    void addOrder(Order order);

    void updateTrackingStatus(Order order, Tracking status);

}

// MENU CLASS
class Menu {

    private postalService service;
    private ArrayList<Customer> customers;
    private Individual currentIndividual;
    private Vendor currentVendor;

    // Constructor
    Menu(postalService service) {
        this.service = service;
        this.customers = service.getCustomers();
    }

    // Main menu - pick user type
    public void pickUserType() {
        System.out.println("\n=== POSTAL SERVICE SYSTEM ===");
        System.out.println("Pick what best defines you:");
        System.out.println("(1) Individual Customer");
        System.out.println("(2) Vendor");
        System.out.println("(3) Admin");
        System.out.println("(4) Exit System");

        while (true) {
            int userInput = In.nextInt();
            if (userInput == 1) {
                loginCustomer();
            } else if (userInput == 2) {
                loginVendor();
            } else if (userInput == 3) {
                admin();
            } else if (userInput == 4) {
                System.out.println("Thank you for using Postal Service System!");
                System.exit(0);
            } else {
                System.out.println("Please input either 1, 2, 3, or 4");
            }
        }
    }

    // Customer login screen
    public void loginCustomer() {
        System.out.println("\n=== CUSTOMER LOGIN ===");
        System.out.println("(1) To login");
        System.out.println("(2) To register");
        int userInput = In.nextInt();

        if (userInput == 1) {
            System.out.println("Please input your associated email:");
            String customerEmail = In.nextLine();
            System.out.println("Please input your associated password:");
            String password = In.nextLine();

            // Look for if customer is correct customer
            for (Customer customer : customers) {
                if (customer instanceof Individual) {
                    if (customer.getEmail().equals(customerEmail) && customer.getPassword().equals(password)) {
                        currentIndividual = (Individual) customer;
                        customerMenu();
                        return;
                    }
                }
            }
            System.out.println("Email or password incorrect, please try again");
            loginCustomer();
        } else if (userInput == 2) {
            registerCustomer();
        } else {
            System.out.println("Please input either 1 or 2");
            loginCustomer();
        }
    }

    //rgister new customer
    public void registerCustomer() {
        System.out.println("\n=== CUSTOMER REGISTRATION ===");
        System.out.println("Please input your name:");
        String name = In.nextLine();
        System.out.println("Please input your email:");
        String email = In.nextLine();
        System.out.println("Please input your phone number:");
        String phoneNumber = In.nextLine();
        System.out.println("Please input your address:");
        String address = In.nextLine();

        //Create new individual customer
        Individual newCustomer = new Individual(name, email, phoneNumber, address);
        System.out.println("Please input your new password:");
        String password = In.nextLine();
        newCustomer.setPassword(password);
        service.addCustomer(newCustomer);

        System.out.println("Registration complete! Welcome " + name);
        currentIndividual = newCustomer;
        customerMenu();
    }

    //main customer menu
    public void customerMenu() {
        System.out.println("\n=== CUSTOMER MENU ===");
        System.out.println("Hello " + currentIndividual.getName() + "!");
        System.out.println("Current Time: Day " + service.getCurrentDay() + ", Hour " + service.getCurrentHour());
        System.out.println("(1) Orders menu");
        System.out.println("(2) Profile menu");
        System.out.println("(3) Advance time");
        System.out.println("(4) Back to main menu");

        while (true) {
            int userInput = In.nextInt();
            if (userInput == 1) {
                customerOrdersMenu();
            } else if (userInput == 2) {
                customerProfileMenu();
            } else if (userInput == 3) {
                // New time advance option for customers
                System.out.println("Enter hours to advance:");
                int hours = In.nextInt();
                service.advanceTime(hours);
                System.out.println("Time advanced. Now: Day " + service.getCurrentDay() +
                        ", Hour " + service.getCurrentHour());
                customerMenu();
            } else if (userInput == 4) {
                pickUserType();
            } else {
                System.out.println("Please input 1, 2, 3 or 4");
            }
        }
    }

    //Customer orders menu
    public void customerOrdersMenu() {
        System.out.println("\n=== ORDERS MENU ===");
        System.out.println("(1) Place order");
        System.out.println("(2) Track my orders");
        System.out.println("(3) Back");

        int userInput = In.nextInt();
        if (userInput == 1) {
            placeCustomerOrder();
        } else if (userInput == 2) {
            currentIndividual.trackMyOrders();
            customerOrdersMenu();
        } else if (userInput == 3) {
            customerMenu();
        } else {
            System.out.println("Please input either 1, 2, or 3");
            customerOrdersMenu();
        }
    }

    //Place order for customer
    public void placeCustomerOrder() {
        System.out.println("\n=== PLACE ORDER ===");
        System.out.println("Enter delivery address:");
        String deliveryAddress = In.nextLine();

        System.out.println("Select package type: (1) SMALL (2) MEDIUM (3) LARGE");
        int packageChoice = In.nextInt();
        PackageType packageType = PackageType.MEDIUM; 
        if (packageChoice == 1)
            packageType = PackageType.SMALL;
        if (packageChoice == 3)
            packageType = PackageType.LARGE;

        System.out.println("Select service: (1) STANDARD (2) EXPRESS (3) SAME_DAY");
        int serviceChoice = In.nextInt();
        ServiceType serviceType = ServiceType.STANDARD; 
        if (serviceChoice == 2)
            serviceType = ServiceType.EXPRESS;
        if (serviceChoice == 3)
            serviceType = ServiceType.SAME_DAY;

        System.out.println("Enter weight (kg):");
        double weight = In.nextDouble();

        // Place the order
        Order order = service.placeOrder(currentIndividual, currentIndividual.getAddress(),
                deliveryAddress, serviceType, packageType, weight);

        System.out.println("\n=== ORDER CONFIRMATION ===");
        System.out.println("Order placed successfully!");
        System.out.println("Tracking number: " + order.getTrackingNumber());
        System.out.println("Price: $" + order.getPrice());

        customerOrdersMenu();
    }

    //customer profile menu
    public void customerProfileMenu() {
        System.out.println("\n=== PROFILE MENU ===");
        System.out.println("(1) View profile");
        System.out.println("(2) Edit profile");
        System.out.println("(3) Back to customer menu");

        int userInput = In.nextInt();
        if (userInput == 1) {
            currentIndividual.displayCustomerInfo();
            customerProfileMenu();
        } else if (userInput == 2) {
            System.out.println("\n=== EDIT PROFILE ===");
            System.out.println("Please input your name:");
            String name = In.nextLine();
            System.out.println("Please input your email:");
            String email = In.nextLine();
            System.out.println("Please input your phone number:");
            String phoneNumber = In.nextLine();
            System.out.println("Please input your address:");
            String address = In.nextLine();

    //Update profile
            currentIndividual.setName(name);
            currentIndividual.setEmail(email);
            currentIndividual.setPhoneNumber(phoneNumber);
            currentIndividual.setAddress(address);

            System.out.println("Profile updated successfully!");
            customerProfileMenu();
        } else if (userInput == 3) {
            customerMenu();
        }
    }

//vendor login screen
    public void loginVendor() {
        System.out.println("\n=== VENDOR LOGIN ===");
        System.out.println("(1) To login");
        System.out.println("(2) To register vendor");
        int userInput = In.nextInt();

        if (userInput == 1) {
            System.out.println("Please input business registration number:");
            String brn = In.nextLine();
            System.out.println("Please input your password:");
            String password = In.nextLine();

            // Look for matching vendor
            for (Customer customer : customers) {
                if (customer instanceof Vendor) {
                    Vendor vendor = (Vendor) customer;
                    if (vendor.getBusinessRegistrationNumber().equals(brn) &&
                            vendor.getPassword().equals(password)) {
                        currentVendor = vendor;
                        vendorMenu();
                        return;
                    }
                }
            }
            System.out.println("BRN or password incorrect");
            loginVendor();
        } else if (userInput == 2) {
            registerVendor();
        }
    }

    @Override
    public String toString() {
        return "Menu{" + "service=" + service + ", customers=" + customers + ", currentIndividual=" + currentIndividual + ", currentVendor=" + currentVendor + '}';
    }

        // Register new vendor
    public void registerVendor() {
        System.out.println("\n=== VENDOR REGISTRATION ===");
        System.out.println("Contact person name:");
        String name = In.nextLine();
        System.out.println("Business email:");
        String email = In.nextLine();
        System.out.println("Business phone:");
        String phone = In.nextLine();
        System.out.println("Business address:");
        String address = In.nextLine();
        System.out.println("Business name:");
        String businessName = In.nextLine();
        System.out.println("Business registration number:");
        String brn = In.nextLine();
        System.out.println("Discount rate (0.0 - 0.2):");
        double discount = In.nextDouble();

            //create new vendor
        Vendor newVendor = new Vendor(name, email, phone, address, businessName, brn, discount);
        System.out.println("Create password:");
        String password = In.nextLine();
        newVendor.setPassword(password);

        service.addCustomer(newVendor);
        System.out.println("Vendor registered successfully!");
        currentVendor = newVendor;
        vendorMenu();
    }

    //Main vendor menu
    public void vendorMenu() {
        System.out.println("\n=== VENDOR PORTAL ===");
        System.out.println("Welcome " + currentVendor.getBusinessName());
        System.out.println("Current Time: Day " + service.getCurrentDay() + ", Hour " + service.getCurrentHour());
        System.out.println("(1) Track all orders");
        System.out.println("(2) Place bulk order");
        System.out.println("(3) Business analytics");
        System.out.println("(4) Calculate bulk discount");
        System.out.println("(5) Profile menu");
        System.out.println("(6) Advance time");
        System.out.println("(7) Back to main menu");

        int userInput = In.nextInt();
        if (userInput == 1) {
            currentVendor.trackAllOrders();
            vendorMenu();
        } else if (userInput == 2) {
            placeVendorOrder();
        } else if (userInput == 3) {
            currentVendor.displayBusinessAnalytics();
            vendorMenu();
        } else if (userInput == 4) {
            calculateBulkDiscountMenu();
        } else if (userInput == 5) {
            vendorProfileMenu();
        } else if (userInput == 6) {
            // time option
            System.out.println("Enter hours to advance:");
            int hours = In.nextInt();
            service.advanceTime(hours);
            System.out.println("Time advanced. Now: Day " + service.getCurrentDay() + ", Hour " + service.getCurrentHour());
            vendorMenu();
        } else if (userInput == 7) {
            pickUserType();
        } else {
            System.out.println("Invalid option");
            vendorMenu();
        }
    }

    // place order for vendors 
    public void placeVendorOrder() {
        System.out.println("\n=== PLACE VENDOR ORDER ===");
        System.out.println("Enter delivery address:");
        String deliveryAddress = In.nextLine();

        System.out.println("Select package type: (1) SMALL (2) MEDIUM (3) LARGE");
        int packageChoice = In.nextInt();
        PackageType packageType = PackageType.MEDIUM; 
        if (packageChoice == 1)
            packageType = PackageType.SMALL;
        if (packageChoice == 3)
            packageType = PackageType.LARGE;

        System.out.println("Select service: (1) STANDARD (2) EXPRESS (3) SAME_DAY");
        int serviceChoice = In.nextInt();
        ServiceType serviceType = ServiceType.STANDARD; 
        if (serviceChoice == 2)
            serviceType = ServiceType.EXPRESS;
        if (serviceChoice == 3)
            serviceType = ServiceType.SAME_DAY;

        System.out.println("Enter weight (kg):");
        double weight = In.nextDouble();

        Order order = service.placeOrder(currentVendor, currentVendor.getAddress(),
                deliveryAddress, serviceType, packageType, weight);

        // Increase the vendor monthly order count
        currentVendor.increaseMonthlyOrders();

        System.out.println("\n=== ORDER CONFIRMATION ===");
        System.out.println("Vendor order placed successfully!");
        System.out.println("Tracking number: " + order.getTrackingNumber());
        System.out.println("Original price: $" + (order.getPrice() / (1 - currentVendor.getDiscountRate())));
        System.out.println("Discounted price: $" + order.getPrice());
        System.out.println("You saved: $" + currentVendor.calculateDiscount(order.getPrice()));

        vendorMenu();
    }

    // Calculate their bulk discount
    public void calculateBulkDiscountMenu() {
        System.out.println("\n=== BULK DISCOUNT CALCULATOR ===");
        System.out.println("Enter number of orders:");
        int orderCount = In.nextInt();
        System.out.println("Enter total amount:");
        double totalAmount = In.nextDouble();

        double discount = currentVendor.calculateBulkDiscount(orderCount, totalAmount);
        System.out.println("Your bulk discount: $" + discount);
        System.out.println("Final amount: $" + (totalAmount - discount));

        vendorMenu();
    }

    // Vendor profile menu
    public void vendorProfileMenu() {
        System.out.println("\n=== VENDOR PROFILE ===");
        System.out.println("(1) View business info");
        System.out.println("(2) Update discount rate");
        System.out.println("(3) Back");

        int choice = In.nextInt();
        if (choice == 1) {
            currentVendor.displayCustomerInfo();
            vendorProfileMenu();
        } else if (choice == 2) {
            System.out.println("Current discount: " + (currentVendor.getDiscountRate() * 100) + "%");
            System.out.println("Enter new discount rate (0.0 - 0.2):");
            double newRate = In.nextDouble();
            currentVendor.setDiscountRate(newRate);
            System.out.println("Discount rate updated!");
            vendorProfileMenu();
        } else {
            vendorMenu();
        }
    }

    // Admin menu
    public void admin() {
        System.out.println("\n=== ADMIN PORTAL ===");
        System.out.println("Current Time: Day " + service.getCurrentDay() + ", Hour " + service.getCurrentHour());
        System.out.println("(1) View all orders");
        System.out.println("(2) Sort orders by price");
        System.out.println("(3) Sort orders by date");
        System.out.println("(4) Sort orders by status");
        System.out.println("(5) View total revenue");
        System.out.println("(6) View all customers");
        System.out.println("(7) View vendors only");
        System.out.println("(8) Advance time");
        System.out.println("(9) View all vehicles");
        System.out.println("(10) Back to main");

        int choice = In.nextInt();
        ArrayList<Order> orders = service.getAllOrders();

        if (choice == 1) {
            System.out.println("\n=== ALL ORDERS ===");
            for (Order o : orders) {
                System.out.println(o);
            }
            admin();
        } else if (choice == 2) {
            service.sortPrice(orders);
            System.out.println("\n=== ORDERS SORTED BY PRICE ===");
            for (Order o : orders) {
                System.out.println(o);
            }
            admin();
        } else if (choice == 3) {
            service.sortDate(orders);
            System.out.println("\n=== ORDERS SORTED BY DATE ===");
            for (Order o : orders) {
                System.out.println(o);
            }
            admin();
        } else if (choice == 4) {
            service.sortStatus(orders);
            System.out.println("\n=== ORDERS SORTED BY STATUS ===");
            for (Order o : orders) {
                System.out.println(o);
            }
            admin();
        } else if (choice == 5) {
            System.out.println("\n=== REVENUE REPORT ===");
            System.out.println("Total Revenue: $" + service.getTotalRevenue());
            admin();
        } else if (choice == 6) {
            System.out.println("\n=== ALL CUSTOMERS ===");
            for (Customer c : service.getCustomers()) {
                System.out.println(c);
            }
            admin();
        } else if (choice == 7) {
            ArrayList<Vendor> vendors = service.getVendorsOnly();
            System.out.println("\n=== VENDORS ===");
            for (Vendor v : vendors) {
                v.displayBusinessAnalytics();
                System.out.println("---");
            }
            admin();
        } else if (choice == 8) {
            System.out.println("Enter hours to advance:");
            int hours = In.nextInt();
            service.advanceTime(hours);
            System.out.println("Time advanced. Now: Day " + service.getCurrentDay() +
                    " Hour " + service.getCurrentHour());
            admin();
        } else if (choice == 9) {
            displayVehicleOverview();
            admin();
        } else if (choice == 10){
            pickUserType();
        }
    }

    private void displayVehicleOverview() {
    System.out.println("\n=== VEHICLE FLEET ===");
    ArrayList<Vehicle> vehicles = service.getVehicles();
    if (vehicles.isEmpty()) {
        System.out.println("No vehicles currently registered.");
        return;
    }
    for (int i = 0; i < vehicles.size(); i++) {
        Vehicle vehicle = vehicles.get(i);
        System.out.println("Vehicle #" + (i + 1));
        Depot currentDepot = vehicle.getCurrentDepotLocation();
        Depot nextDepot = vehicle.getNextDepotLocation();
            String currentDepotName = "Unknown";
            if (currentDepot != null) {
                currentDepotName = currentDepot.getLocation();
            }
            String nextDepotName = "Route not set";
            if (nextDepot != null) {
                nextDepotName = nextDepot.getLocation();
            }
        System.out.println("Current Depot: " + currentDepotName);
        System.out.println("Next Destination: " + nextDepotName);
        System.out.println("Orders on board:");
        ArrayList<Order> orderList = vehicle.getOrderList();
        if (orderList.isEmpty()) {
            System.out.println("  No orders assigned.");
        } else {
            for (Order order : orderList) {
                System.out.println("  Order #" + order.getOrderNumber() + " -> " + order.getDeliveryAddress() + " [" + order.getStatus() + "]");
            }
        }
        System.out.println("-------------------------------------------------");
    }
    }
    
}
