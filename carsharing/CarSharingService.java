package carsharing;

import carsharing.persistance.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CarSharingService {
    static public String DB_NAME;

    private EntityDao<Company> companyDao;
    private EntityDao<Car> carDao;
    private EntityDao<Customer> customerDao;
    private List<Company> companies = new ArrayList<>();
    //    private List<Car> cars = new ArrayList<>();
    private Customer currentCustomer;

    public CarSharingService(String dbName) {
        CarSharingService.DB_NAME = dbName;
        try {
            init();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void init() throws ClassNotFoundException, SQLException {
        Class.forName(DataBaseClient.JDBC_DRIVER);
        companyDao = new CompanyDao();
        carDao = new CarDao();
        customerDao = new CustomerDao();
        new CarSharingController(this).init();
    }

    /*
     * Company related methods
     * */
    public List<Company> getCompanies() {
        companies = companyDao.findAll();
        return companies;
    }

    public void addCompany(String companyName) {
        companyDao.add(new Company(0, companyName));
    }

    /*
     * Cars related methods
     * */
    public List<Car> getCars(String companyName) {
        Company company = companies.stream()
                .filter(c -> Objects.equals(c.name(), companyName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Company " + companyName + " does not exist"));
        return carDao.findAll().stream().filter(c -> c.companyId() == company.id()).toList();
    }

    public void addCar(String carName, String companyName) {
        Company company = companies.stream()
                .filter(c -> Objects.equals(c.name(), companyName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Company " + companyName + " does not exist"));
        carDao.add(new Car(0, carName, company.id()));
    }

    /*
     * Customer related methods
     * */
    public void addCustomer(String customerName) {
        customerDao.add(new Customer(0, customerName, 0));
    }

    public List<Customer> getCustomers() {
        return customerDao.findAll();
    }

    public void setCurrentCustomer(String customerName) {
        currentCustomer = customerDao.findAll()
                .stream()
                .filter(customer -> customer.name().equals(customerName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Customer " + customerName + " does not exist"));
    }

    public boolean carReturnByCurrentCustomer() {
        if (currentCustomer.rentedCarId() != 0) {
            Customer customer = new Customer(currentCustomer.id(), currentCustomer.name(), null);
            customerDao.update(customer);
            currentCustomer = customer;
            return true;
        } else {
            return false;
        }
    }

    public String getRentedCarByCurrentCustomer() {
        Integer rentedCarId = currentCustomer.rentedCarId();
        if (rentedCarId != 0) {
            return carDao.findById(rentedCarId).name();
        } else {
            return null;
        }
    }

    public String getRentedCarCompanyByCurrentCustomer() {
        Car car = carDao.findById(currentCustomer.rentedCarId());
        return companyDao.findById(car.companyId()).name();
    }

    public void setRentedCarByCurrentCustomer(String carName) {
        Integer carId = carDao.findAll().stream()
                .filter(car -> car.name().equals(carName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Car " + carName + " does not exist"))
                .id();
        Customer customer = new Customer(currentCustomer.id(), currentCustomer.name(), carId);
        customerDao.update(customer);
        currentCustomer = customer;
    }

    public List<Car> getAvailableCars(String companyName) {
        List<Integer> rentedCarIds = getCustomers().stream()
                .map(Customer::rentedCarId)
                .filter(carId -> carId != 0)
                .toList();
        List<Car> cars = getCars(companyName);
        return cars.stream()
                .filter(car -> !rentedCarIds.contains(car.id()))
                .toList();
    }
}
