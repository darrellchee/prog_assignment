import java.util.*;

public class postalService implements Sorting {

    private final ArrayList<Customer> customerList = new ArrayList<>();
    private final ArrayList<Order> orderList = new ArrayList<>();
    private final ArrayList<Depot> depotList = new ArrayList<>();
    private final ArrayList<Vehicle> vehicleFleet = new ArrayList<>();
    private int currentHour = 8;
    private int currentDay = 1;

    public static void main(String[] args) {
        postalService postalServiceInstance = new postalService();
        postalServiceInstance.initializeObjects();
        new Menu(postalServiceInstance).start();
    }

    postalService() { }

    private void initializeObjects() {
        Depot centralDepot = new Depot("Central Depot");
        Depot northDepot = new Depot("North Depot");
        Depot southDepot = new Depot("South Depot");
        depotList.add(centralDepot);
        depotList.add(northDepot);
        depotList.add(southDepot);

        Vehicle vehicleOne = new Vehicle("VAN-1", 1000.0);
        Vehicle vehicleTwo = new Vehicle("VAN-2", 800.0);
        vehicleFleet.add(vehicleOne);
        vehicleFleet.add(vehicleTwo);

        centralDepot.attachVehicle(vehicleOne);
        northDepot.attachVehicle(vehicleTwo);

        Individual alice = new Individual("Alice", "123i", "555-1234", "123 Main St");
        alice.setPassword("123");

        Vendor bob = new Vendor("Bob", "123v", "555-5678", "456 Market St",
                "BobsCo", "BRN001", 0.12);
        bob.setPassword("123");

        Individual charlie = new Individual("Charlie", "charlie@demo.com",
                "555-2222", "789 King St");
        charlie.setPassword("123");

        Vendor megaMart = new Vendor("MegaMart", "mega@store.com",
                "555-3333", "12 High St",
                "MegaMart Pty Ltd", "BRN002", 0.15);
        megaMart.setPassword("123");

        addCustomer(alice);
        addCustomer(bob);
        addCustomer(charlie);
        addCustomer(megaMart);

        placeOrder(
                alice,
                alice.getAddress(),
                "88 Broadway",
                ServiceType.SAME_DAY,
                PackageType.SMALL,
                1.2,
                10.0,
                8
        );

        placeOrder(
                alice,
                alice.getAddress(),
                "200 Harris St",
                ServiceType.STANDARD,
                PackageType.MEDIUM,
                4.5,
                10.0,
                9
        );

        placeOrder(
                bob,
                bob.getAddress(),
                "15 Customer Lane",
                ServiceType.EXPRESS,
                PackageType.LARGE,
                7.0,
                12.0,
                7
        );

        placeOrder(
                charlie,
                charlie.getAddress(),
                "300 Crown St",
                ServiceType.STANDARD,
                PackageType.SMALL,
                2.0,
                9.0,
                6
        );

        placeOrder(
                megaMart,
                megaMart.getAddress(),
                "500 Pitt St",
                ServiceType.SAME_DAY,
                PackageType.LARGE,
                10.0,
                15.0,
                10
        );
    }

    private int absoluteValue(int value) {
        if (value < 0) {
            return -value;
        } else {
            return value;
        }
    }

    public int getCurrentHour() { return currentHour; }
    public int getCurrentDay() { return currentDay; }

    public void stepHours(int hour) {
        if (hour == 0) {
            return;
        }
        int numberOfSteps = absoluteValue(hour);
        int direction;
        if (hour > 0) {
            direction = 1;
        } else {
            direction = -1;
        }

        for (int index = 0; index < numberOfSteps; index++) {
            int previousHour = currentHour;

            int newHour = currentHour + direction;
            int modHour = newHour % 24;
            if (modHour < 0) {
                modHour = modHour + 24;
            }
            currentHour = modHour;

            if (direction > 0 && previousHour == 23 && currentHour == 0) {
                currentDay = currentDay + 1;
            }
            if (direction < 0 && previousHour == 0 && currentHour == 23) {
                if (currentDay > 1) {
                    currentDay = currentDay - 1;
                }
            }

            if (currentHour == 9 && direction > 0) {
                dailyDeparture();
            }
            if (currentHour == 9 && direction < 0) {
                dailyRollback();
            }
        }
    }

    private void dailyDeparture() {
        for (Vehicle vehicle : vehicleFleet) {
            vehicle.travelOnce(orderList);
        }
    }

    private void dailyRollback() {
        for (Order order : orderList) {
            if (order.getStatus() == Tracking.DELIVERED) {
                continue;
            }
            if (order.getLastHopHour() == 9 && order.routeIndex > 0) {
                order.routeIndex = order.routeIndex - 1;
                order.setLastHopHour(currentHour);
                order.setStatus(Tracking.IN_TRANSIT);
            }
        }
    }

    public void addCustomer(String name, String email, String phoneNumber, String address) {
        customerList.add(new Individual(name, email, phoneNumber, address));
    }

    public void addCustomer(Customer customer) {
        customerList.add(customer);
    }

    public void addCustomer(Individual individual) {
        customerList.add(individual);
    }

    public void addCustomer(Vendor vendor) {
        customerList.add(vendor);
    }

    public ArrayList<Customer> getCustomers() {
        return customerList;
    }

    public double calculateOrderPrice(Customer customer, double weightKilograms, double basePrice) {
        double price = basePrice + weightKilograms * 2.0;
        if (customer instanceof Vendor) {
            Vendor vendor = (Vendor) customer;
            price = price - vendor.calculateDiscount(price);
        }
        return price;
    }

    public Order placeOrder(Customer customer,
                            String pickupAddress, String deliveryAddress,
                            ServiceType serviceType, PackageType packageType,
                            double weightKilograms, double basePrice) {
        return placeOrder(customer, pickupAddress, deliveryAddress, serviceType, packageType, weightKilograms, basePrice, currentHour);
    }

    public Order placeOrder(Customer customer,
                            String pickupAddress, String deliveryAddress,
                            ServiceType serviceType, PackageType packageType,
                            double weightKilograms, double basePrice,
                            int createdAtHourOfDay) {

        int nextOrderNumber = orderList.size() + 1;
        int trackingNumber = 1000 + nextOrderNumber;

        Order order = new Order(nextOrderNumber, trackingNumber, pickupAddress, deliveryAddress, Tracking.PENDING);
        order.setServiceType(serviceType);
        order.setPackageType(packageType);
        order.setWeight(weightKilograms);
        order.setCustomerName(customer.getName());
        order.setCreatedDay(currentDay);
        order.createdHour = createdAtHourOfDay;

        double price = calculateOrderPrice(customer, weightKilograms, basePrice);
        order.setPrice(price);

        ArrayList<String> routeLocationList = new ArrayList<>();
        routeLocationList.add("Vendor@" + pickupAddress);

        if (serviceType == ServiceType.SAME_DAY) {
            routeLocationList.add("Customer@" + deliveryAddress);
        } else if (serviceType == ServiceType.EXPRESS) {
            Depot nearestDepot = depotList.get(0);
            routeLocationList.add(nearestDepot.getLocation());
            routeLocationList.add("Customer@" + deliveryAddress);
        } else if (serviceType == ServiceType.STANDARD) {
            for (Depot depot : depotList) {
                routeLocationList.add(depot.getLocation());
            }
            routeLocationList.add("Customer@" + deliveryAddress);
        }

        order.setRoute(routeLocationList);
        order.setStatus(Tracking.PENDING);

        Depot assignedDepot = depotList.get(0);
        Vehicle availableVehicle = assignedDepot.findAvailableVehicle();
        assignedDepot.mapOrderToVehicle(order, availableVehicle);
        if (availableVehicle != null) {
            availableVehicle.addOrder(order);
        }

        customer.addOrder(order);

        orderList.add(order);
        return order;
    }

    public ArrayList<Order> getAllOrders() {
        return orderList;
    }

    public ArrayList<Depot> getDepots() {
        return depotList;
    }

    public ArrayList<Vehicle> getFleet() {
        return vehicleFleet;
    }

    @Override
    public void sortDate(List<Order> listToSort) {
        Collections.sort(listToSort, Comparator.comparingInt(Order::getCreatedHour));
    }

    @Override
    public void sortPrice(List<Order> listToSort) {
        Collections.sort(listToSort, Comparator.comparingDouble(Order::getPrice));
    }

    @Override
    public void sortStatus(List<Order> listToSort) {
        Collections.sort(listToSort, Comparator.comparing(Order::getStatus));
    }
}

class Order {
    private final int orderNumber;
    private final int trackingNumber;
    private String pickUpAddress;
    private String deliveryAddress;
    private Tracking status;
    private double price;

    private ServiceType serviceType = ServiceType.STANDARD;
    private PackageType packageType = PackageType.MEDIUM;
    private double weight;
    private String customerName;
    private int createdDay = 1;

    ArrayList<String> route = new ArrayList<>();
    int routeIndex = 0;
    int createdHour = 0;
    int lastHopHour = -1;

    Order(int orderNumber, int trackingNumber, String pickUpAddress, String deliveryAddress, Tracking status) {
        this.orderNumber = orderNumber;
        this.trackingNumber = trackingNumber;
        this.pickUpAddress = pickUpAddress;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
    }

    void advanceOneHop() {
        if (routeIndex < route.size() - 1) {
            routeIndex = routeIndex + 1;
            if (routeIndex == route.size() - 1) {
                status = Tracking.DELIVERED;
            } else {
                status = Tracking.IN_TRANSIT;
            }
        }
    }

    public int getOrderNumber() { return orderNumber; }
    public int getTrackingNumber() { return trackingNumber; }
    public String getPickUpAddress() { return pickUpAddress; }
    public void setPickUpAddress(String pickUpAddress) { this.pickUpAddress = pickUpAddress; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Tracking getStatus() { return status; }
    public void setStatus(Tracking status) { this.status = status; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public PackageType getPackageType() { return packageType; }
    public void setPackageType(PackageType packageType) { this.packageType = packageType; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setRoute(ArrayList<String> routeLocationList) { this.route = routeLocationList; }
    public ArrayList<String> getRoute() { return route; }
    public int getRouteIndex() { return routeIndex; }
    public int getCreatedHour() { return createdHour; }
    public int getLastHopHour() { return lastHopHour; }
    public void setLastHopHour(int lastHopHour) { this.lastHopHour = lastHopHour; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getCreatedDay() { return createdDay; }
    public void setCreatedDay(int createdDay) { this.createdDay = createdDay; }

    public String getEtaDescription() {
        int numberOfHops;
        if (route == null) {
            numberOfHops = 0;
        } else {
            int sizeMinusOne = route.size() - 1;
            if (sizeMinusOne < 1) {
                numberOfHops = 1;
            } else {
                numberOfHops = sizeMinusOne;
            }
        }
        int etaDay = createdDay + numberOfHops;
        return "Day " + etaDay + " 09:00";
    }

    public String getCurrentStop() {
        if (route == null || route.size() == 0) {
            return "-";
        }
        if (routeIndex < 0 || routeIndex >= route.size()) {
            return "-";
        }
        return route.get(routeIndex);
    }

    @Override
    public String toString() {
        String etaDescription = getEtaDescription();
        String currentStop = getCurrentStop();
        String customerDisplay;
        if (customerName == null) {
            customerDisplay = "-";
        } else {
            customerDisplay = customerName;
        }

        return "Order#" + orderNumber
                + " Track:" + trackingNumber
                + " Cust:" + customerDisplay
                + " Pickup:" + pickUpAddress
                + " Service:" + serviceType
                + " Pack:" + packageType
                + " Status:" + status
                + " W:" + weight
                + " Price:$" + price
                + " ETA:" + etaDescription
                + " Stop:" + currentStop;
    }
}

class Vehicle implements Deliverable {
    private final String vehicleId;
    private final HashMap<Integer, String> orderLookupByNumber = new HashMap<>();
    private final ArrayList<Order> orderList = new ArrayList<>();
    private final double maximumCapacity;
    private double currentLoad = 0.0;
    private Depot currentDepotLocation;

    Vehicle(String vehicleId, double maximumCapacity) {
        this.vehicleId = vehicleId;
        this.maximumCapacity = maximumCapacity;
    }

    void attachDepot(Depot depot) {
        this.currentDepotLocation = depot;
    }

    @Override
    public void addOrder(Order order) {
        double potentialNewLoad = currentLoad + order.getWeight();
        if (potentialNewLoad > maximumCapacity) {
            System.out.println("Cannot add order #" + order.getOrderNumber()
                    + " to vehicle " + vehicleId + ": capacity exceeded.");
            return;
        }
        orderLookupByNumber.put(order.getOrderNumber(), order.getDeliveryAddress());
        orderList.add(order);
        currentLoad = potentialNewLoad;
    }

    @Override
    public void updateTrackingStatus(Order order, Tracking status) {
        order.setStatus(status);
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    public double getMaximumCapacity() {
        return maximumCapacity;
    }

    public double getCurrentLoad() {
        return currentLoad;
    }

    public String getCurrentDepotLocationName() {
        if (currentDepotLocation == null) {
            return "No depot assigned";
        }
        return currentDepotLocation.getLocation();
    }

    public void travelOnce(List<Order> globalOrderList) {
        for (Order order : orderList) {
            if (order.getStatus() == Tracking.DELIVERED) {
                continue;
            }
            order.advanceOneHop();
            order.setLastHopHour(9);
        }
        currentLoad = 0.0;
        for (Order order : orderList) {
            if (order.getStatus() != Tracking.DELIVERED) {
                currentLoad = currentLoad + order.getWeight();
            }
        }
    }
}

class Depot implements Deliverable {
    private final String depotLocation;
    private final String depotCode;
    private final ArrayList<Vehicle> vehicles = new ArrayList<>();
    private final HashMap<Order, Vehicle> orderToVehicleMap = new HashMap<>();

    Depot(String depotLocation) {
        this.depotLocation = depotLocation;
        this.depotCode = "DEPOT@" + depotLocation;
    }

    public String getLocation() { return depotLocation; }

    public void attachVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicle.attachDepot(this);
    }

    public Vehicle findAvailableVehicle() {
        if (vehicles.size() != 0) {
            return vehicles.get(0);
        }
        return null;
    }

    public void mapOrderToVehicle(Order order, Vehicle vehicle) {
        if (vehicle != null) {
            orderToVehicleMap.put(order, vehicle);
        }
    }

    public ArrayList<Vehicle> getVehicles() { return vehicles; }
    public HashMap<Order, Vehicle> getOrders() { return orderToVehicleMap; }

    @Override
    public void addOrder(Order order) {
        Vehicle vehicle = findAvailableVehicle();
        if (vehicle != null) {
            orderToVehicleMap.put(order, vehicle);
            vehicle.addOrder(order);
        }
    }

    @Override
    public void updateTrackingStatus(Order order, Tracking status) {
        order.setStatus(status);
        System.out.println("[" + depotCode + "] status update: order #" + order.getOrderNumber() + " -> " + status);
    }
}

abstract class Customer {
    protected String customerID;
    protected String name;
    protected String email;
    protected String phoneNumber;
    protected String address;
    protected String password;
    protected final ArrayList<Order> orders = new ArrayList<>();

    Customer(String name, String email, String phoneNumber, String address) {
        this.customerID = "CUST:" + name + ":" + email;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getCustomerID() { return customerID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public ArrayList<Order> getAllOrder() { return orders; }
    public void addOrder(Order order) { orders.add(order); }

    public void trackAllOrders() {
        for (Order order : orders) {
            System.out.println(order);
        }
    }

    public double calculateTotalOrderPrice() {
        double sum = 0.0;
        for (Order order : orders) {
            sum = sum + order.getPrice();
        }
        return sum;
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
    private double discountRate;

    Vendor(String name, String email, String phoneNumber, String address,
           String businessName, String businessRegistrationNumber, double discountRate) {
        super(name, email, phoneNumber, address);
        this.businessName = businessName;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.discountRate = discountRate;
        this.customerID = "VENDOR:" + businessRegistrationNumber;
    }

    public double calculateDiscount(double amount) { return amount * discountRate; }
    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    @Override
    public void trackAllOrders() {
        System.out.println("Vendor " + businessName + " (BRN: " + businessRegistrationNumber + ") orders:");
        super.trackAllOrders();
    }
}

enum Tracking {PENDING,IN_TRANSIT,DELIVERED;

    public boolean isFinalStatus() {
        if (this == DELIVERED) {
            return true;
        } else {
            return false;
        }
    }
}

enum PackageType { SMALL, MEDIUM, LARGE }

enum ServiceType { STANDARD, EXPRESS, SAME_DAY }

interface Sorting {
    void sortDate(List<Order> list);
    void sortPrice(List<Order> list);
    void sortStatus(List<Order> list);
}

interface Deliverable {
    void addOrder(Order order);
    void updateTrackingStatus(Order order, Tracking status);
}

class Menu {
    private final postalService postalServiceInstance;
    private Customer currentCustomer = null;

    Menu(postalService postalServiceInstance) {
        this.postalServiceInstance = postalServiceInstance;
    }

    public void start() {
        while (true) {
            printBanner("Postal Service - Main Menu");
            System.out.println("1) Customer");
            System.out.println("2) Vendor's Menu");
            System.out.println("3) Admin");
            System.out.println("4) Fast forward time (+1 hour)");
            System.out.println("5) Fast forward time (+1 day)");
            System.out.println("0) Exit");
            System.out.print("Select: ");
            int choice = In.nextInt();

            if (choice == 1) {
                customerPortal(false);
            } else if (choice == 2) {
                customerPortal(true);
            } else if (choice == 3) {
                adminPortal();
            } else if (choice == 4) {
                fastForwardOneHour();
            } else if (choice == 5) {
                fastForwardOneDay();
            } else if (choice == 0) {
                System.out.println("Bye");
                return;
            } else {
                System.out.println("Invalid.");
            }
        }
    }

    private void fastForwardOneHour() {
        postalServiceInstance.stepHours(1);
        System.out.println("Fast forwarded time by 1 hour.");
        snapshot();
    }

    private void fastForwardOneDay() {
        postalServiceInstance.stepHours(24);
        System.out.println("Fast forwarded time by 1 day.");
        snapshot();
    }

    private void snapshot() {
        printBanner("System Snapshot");
        System.out.println("Orders: " + postalServiceInstance.getAllOrders().size());

        for (Order order : postalServiceInstance.getAllOrders()) {
            printOrderWithDivider(order);
        }

        System.out.println();
        System.out.println("Fleet status:");
        for (Vehicle vehicle : postalServiceInstance.getFleet()) {
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Vehicle ID: " + vehicle.getVehicleId());
            System.out.println("Current depot location: " + vehicle.getCurrentDepotLocationName());
            System.out.println("Current load: " + vehicle.getCurrentLoad() + " kg");
            System.out.println("Maximum capacity: " + vehicle.getMaximumCapacity() + " kg");
            System.out.println("Orders inside vehicle:");
            if (vehicle.getOrderList().size() == 0) {
                System.out.println("  (No orders assigned)");
            } else {
                for (Order order : vehicle.getOrderList()) {
                    System.out.println("  " + order);
                }
            }
        }
    }

    private void customerPortal(boolean isVendorPortal) {
        while (true) {
            if (isVendorPortal) {
                printBanner("Vendor's Menu");
            } else {
                printBanner("Customer Portal");
            }
            System.out.println("1) Login");
            System.out.println("2) Register");
            System.out.println("3) Back");
            System.out.println("4) Fast forward time (+1 hour)");
            System.out.println("5) Fast forward time (+1 day)");
            System.out.print("Select: ");
            int choice = In.nextInt();

            if (choice == 1) {
                login(isVendorPortal);
                if (currentCustomer != null) {
                    customerMenu();
                }
            } else if (choice == 2) {
                register(isVendorPortal);
            } else if (choice == 3) {
                return;
            } else if (choice == 4) {
                fastForwardOneHour();
            } else if (choice == 5) {
                fastForwardOneDay();
            }
        }
    }

    private void login(boolean vendorPortal) {
        String identifier;
        String password;

        if (vendorPortal) {
            System.out.print("Business reg number: ");
            identifier = In.nextLine();
        } else {
            System.out.print("Email: ");
            identifier = In.nextLine();
        }

        System.out.print("Password: ");
        password = In.nextLine();

        currentCustomer = null;
        for (Customer customer : postalServiceInstance.getCustomers()) {
            if (vendorPortal) {
                if (customer instanceof Vendor) {
                    Vendor vendor = (Vendor) customer;
                    if (vendor.getBusinessRegistrationNumber().equals(identifier)
                            && password.equals(customer.getPassword())) {
                        currentCustomer = customer;
                        break;
                    }
                }
            } else {
                if (!(customer instanceof Vendor)) {
                    if (customer.getEmail().equals(identifier)
                            && password.equals(customer.getPassword())) {
                        currentCustomer = customer;
                        break;
                    }
                }
            }
        }

        if (currentCustomer == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Welcome, " + currentCustomer.getName());
        }
    }

    private void register(boolean vendorPortal) {
        if (vendorPortal) {
            printBanner("Vendor Registration");
        } else {
            printBanner("Customer Registration");
        }

        System.out.print("Name: ");
        String name = In.nextLine();
        System.out.print("Email: ");
        String email = In.nextLine();
        System.out.print("Phone: ");
        String phoneNumber = In.nextLine();
        System.out.print("Address: ");
        String address = In.nextLine();
        System.out.print("Password: ");
        String password = In.nextLine();

        if (vendorPortal) {
            System.out.print("Business name: ");
            String businessName = In.nextLine();
            System.out.print("Business reg number: ");
            String businessRegistrationNumber = In.nextLine();
            System.out.print("Discount rate 0..1: ");
            double discountRate = In.nextDouble();

            Vendor vendor = new Vendor(name, email, phoneNumber, address,
                    businessName, businessRegistrationNumber, discountRate);
            vendor.setPassword(password);
            postalServiceInstance.addCustomer(vendor);
        } else {
            Individual individual = new Individual(name, email, phoneNumber, address);
            individual.setPassword(password);
            postalServiceInstance.addCustomer(individual);
        }
        System.out.println("Registered.");
    }

    private void customerMenu() {
        while (true) {
            boolean isVendor = currentCustomer instanceof Vendor;
            if (isVendor) {
                Vendor vendor = (Vendor) currentCustomer;
                printBanner("Vendor - " + vendor.getBusinessName()
                        + " (BRN " + vendor.getBusinessRegistrationNumber() + ")");
            } else {
                printBanner("Customer - " + currentCustomer.getName());
            }

            System.out.println("1) Place order");
            System.out.println("2) My orders");
            if (isVendor) {
                System.out.println("3) Track all orders");
                System.out.println("4) View total revenue");
                System.out.println("5) Fast forward time (+1 hour)");
                System.out.println("6) Fast forward time (+1 day)");
            } else {
                System.out.println("3) View total expenditure");
                System.out.println("4) Fast forward time (+1 hour)");
                System.out.println("5) Fast forward time (+1 day)");
            }
            System.out.println("0) Logout");

            System.out.print("Select: ");
            int choice = In.nextInt();

            if (choice == 1) {
                placeOrderFlow();
            } else if (choice == 2) {
                displayOrdersByStatus(currentCustomer.getAllOrder());
            } else if (isVendor && choice == 3) {
                displayOrdersByStatus(currentCustomer.getAllOrder());
            } else if (isVendor && choice == 4) {
                showCustomerTotals();
            } else if (isVendor && choice == 5) {
                fastForwardOneHour();
            } else if (isVendor && choice == 6) {
                fastForwardOneDay();
            } else if (!isVendor && choice == 3) {
                showCustomerTotals();
            } else if (!isVendor && choice == 4) {
                fastForwardOneHour();
            } else if (!isVendor && choice == 5) {
                fastForwardOneDay();
            } else if (choice == 0) {
                currentCustomer = null;
                return;
            }
        }
    }

    private void showCustomerTotals() {
        double totalPrice = currentCustomer.calculateTotalOrderPrice();
        if (currentCustomer instanceof Vendor) {
            Vendor vendor = (Vendor) currentCustomer;
            System.out.println("Total revenue " + vendor.getBusinessName()
                    + " (BRN " + vendor.getBusinessRegistrationNumber()
                    + "): $" + totalPrice);
        } else {
            System.out.println("Total spent " + currentCustomer.getName() + ": $" + totalPrice);
        }
    }

    private void placeOrderFlow() {
        printBanner("Place Order");
        String pickupAddress = currentCustomer.getAddress();
        System.out.print("Delivery address: ");
        String deliveryAddress = In.nextLine();

        System.out.println("Service type: 1) SAME_DAY  2) EXPRESS  3) STANDARD");
        System.out.print("Select: ");
        int serviceSelection = In.nextInt();
        ServiceType serviceType;
        if (serviceSelection == 1) {
            serviceType = ServiceType.SAME_DAY;
        } else if (serviceSelection == 2) {
            serviceType = ServiceType.EXPRESS;
        } else {
            serviceType = ServiceType.STANDARD;
        }

        System.out.println("Package: 1) SMALL  2) MEDIUM  3) LARGE");
        System.out.print("Select: ");
        int packageSelection = In.nextInt();
        PackageType packageType;
        if (packageSelection == 1) {
            packageType = PackageType.SMALL;
        } else if (packageSelection == 3) {
            packageType = PackageType.LARGE;
        } else {
            packageType = PackageType.MEDIUM;
        }

        System.out.print("Weight (kg): ");
        double weightKilograms = In.nextDouble();
        In.nextLine();  // consume newline so nextLine() for confirmation works
        double basePrice = 10.0;

        double estimatedPrice = postalServiceInstance.calculateOrderPrice(currentCustomer, weightKilograms, basePrice);
        System.out.println("Estimated price: $" + estimatedPrice);
        System.out.print("Confirm order? (Y/N): ");
        String confirmation = In.nextLine();
        if (!(confirmation.equals("Y") || confirmation.equals("y"))) {
            System.out.println("Order cancelled.");
            return;
        }

        Order order = postalServiceInstance.placeOrder(
                currentCustomer,
                pickupAddress,
                deliveryAddress,
                serviceType,
                packageType,
                weightKilograms,
                basePrice
        );
        System.out.println("Order created:");
        printOrderWithDivider(order);
        System.out.println("Tracking ID: " + order.getTrackingNumber());
    }

    private void adminPortal() {
        while (true) {
            printBanner("Admin Menu");
            System.out.println("1) List orders");
            System.out.println("2) Sort by price and display");
            System.out.println("3) Sort by created time and display");
            System.out.println("4) Sort by status and display");
            System.out.println("5) Display truck details");
            System.out.println("6) View total revenue");
            System.out.println("7) Fast forward time (+1 hour)");
            System.out.println("8) Fast forward time (+1 day)");
            System.out.println("0) Back");
            System.out.print("Select: ");
            int choice = In.nextInt();

            if (choice == 0) {
                return;
            } else if (choice == 1) {
                postalServiceInstance.sortDate(postalServiceInstance.getAllOrders());
                listOrdersDetailed();
            } else if (choice == 2) {
                postalServiceInstance.sortPrice(postalServiceInstance.getAllOrders());
                listOrdersDetailed();
            } else if (choice == 3) {
                postalServiceInstance.sortDate(postalServiceInstance.getAllOrders());
                listOrdersDetailed();
            } else if (choice == 4) {
                postalServiceInstance.sortStatus(postalServiceInstance.getAllOrders());
                listOrdersDetailed();
            } else if (choice == 5) {
                snapshot();
            } else if (choice == 6) {
                showAdminRevenue();
            } else if (choice == 7) {
                fastForwardOneHour();
            } else if (choice == 8) {
                fastForwardOneDay();
            }
        }
    }

    private void showAdminRevenue() {
        double sum = 0.0;
        for (Order order : postalServiceInstance.getAllOrders()) {
            sum = sum + order.getPrice();
        }
        System.out.println("Total revenue: $" + sum);
    }

    private void listOrdersDetailed() {
        printBanner("Admin - Orders");
        System.out.println("ID | Track | Cust | Status | Price | Service | Size | ETA");
        for (Order order : postalServiceInstance.getAllOrders()) {
            System.out.println(
                    order.getOrderNumber()
                            + " | " + order.getTrackingNumber()
                            + " | " + order.getCustomerName()
                            + " | " + order.getStatus()
                            + " | $" + order.getPrice()
                            + " | " + order.getServiceType()
                            + " | " + order.getPackageType()
                            + " | " + order.getEtaDescription()
            );
        }
    }

    private void displayOrdersByStatus(ArrayList<Order> customerOrderList) {
        System.out.println("Orders for " + currentCustomer.getName());
        displayOrdersForSpecificStatus(customerOrderList, Tracking.PENDING, "PENDING");
        displayOrdersForSpecificStatus(customerOrderList, Tracking.IN_TRANSIT, "IN_TRANSIT");
        displayOrdersForSpecificStatus(customerOrderList, Tracking.DELIVERED, "DELIVERED");
    }

    private void displayOrdersForSpecificStatus(ArrayList<Order> customerOrderList, Tracking trackingStatus, String sectionTitle) {
        System.out.println("[" + sectionTitle + "]");
        boolean hasAny = false;
        for (Order order : customerOrderList) {
            if (order.getStatus() == trackingStatus) {
                hasAny = true;
                System.out.println(
                        "ID " + order.getOrderNumber()
                                + " | Track " + order.getTrackingNumber()
                                + " | To " + order.getDeliveryAddress()
                                + " | $" + order.getPrice()
                );
            }
        }
        if (!hasAny) {
            System.out.println("None");
        }
    }

    private void printBanner(String title) {
        System.out.println();
        System.out.println("==== " + title + " (Day " + postalServiceInstance.getCurrentDay()
                + " Hour " + postalServiceInstance.getCurrentHour() + ") ====");
    }

    private void printOrderWithDivider(Order order) {
        System.out.println("----------------------------------------");
        System.out.println(order);
    }
}
