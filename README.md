# Loan Eligibility and EMI Management System

A Spring Boot application for managing loan eligibility and EMI schedules with a clean, modular architecture.

## Project Structure

```
loan-eligibility-emi-system/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/loanmanagement/
│       │       ├── controller/          # REST API Controllers
│       │       │   ├── UserController.java
│       │       │   ├── LoanApplicationController.java
│       │       │   ├── LoanController.java
│       │       │   └── EmiScheduleController.java
│       │       ├── service/             # Service Layer
│       │       │   ├── UserService.java
│       │       │   ├── LoanApplicationService.java
│       │       │   ├── LoanService.java
│       │       │   ├── EmiScheduleService.java
│       │       │   └── impl/            # Service Implementations
│       │       │       ├── UserServiceImpl.java
│       │       │       ├── LoanApplicationServiceImpl.java
│       │       │       ├── LoanServiceImpl.java
│       │       │       └── EmiScheduleServiceImpl.java
│       │       ├── repository/          # Data Access Layer
│       │       │   ├── UserRepository.java
│       │       │   ├── LoanApplicationRepository.java
│       │       │   ├── LoanRepository.java
│       │       │   └── EmiScheduleRepository.java
│       │       ├── entity/              # JPA Entities
│       │       │   ├── User.java
│       │       │   ├── LoanApplication.java
│       │       │   ├── Loan.java
│       │       │   └── EmiSchedule.java
│       │       ├── dto/                 # Data Transfer Objects
│       │       │   └── ApiResponse.java
│       │       ├── exception/           # Exception Handling
│       │       │   ├── ResourceNotFoundException.java
│       │       │   └── GlobalExceptionHandler.java
│       │       └── LoanManagementApplication.java
│       └── resources/
│           ├── application.yml
│           └── application-prod.yml
├── pom.xml
└── README.md
```

## Architecture

The application follows a **three-tier layered architecture**:

1. **Controller Layer** - Handles HTTP requests and responses
2. **Service Layer** - Contains business logic (placeholder for future implementation)
3. **Repository Layer** - Manages data persistence using Spring Data JPA

## Entities

### User
- User information including personal details, employment, and income
- One-to-Many relationship with LoanApplication and Loan

### LoanApplication
- Loan application details and status
- Many-to-One relationship with User
- One-to-One relationship with Loan

### Loan
- Approved loan details including EMI information
- Many-to-One relationship with User
- One-to-Many relationship with EmiSchedule

### EmiSchedule
- Individual EMI payment details and schedule
- Many-to-One relationship with Loan

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: For data persistence
- **H2 Database**: In-memory database (development)
- **MySQL**: Production database support
- **Lombok**: Reduces boilerplate code
- **Maven**: Build and dependency management

## API Endpoints

### User Management
- `POST /api/users` - Create a new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users/phone/{phoneNumber}` - Get user by phone
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Loan Application Management
- `POST /api/loan-applications` - Create loan application
- `GET /api/loan-applications` - Get all loan applications
- `GET /api/loan-applications/{id}` - Get loan application by ID
- `GET /api/loan-applications/user/{userId}` - Get applications by user
- `GET /api/loan-applications/status/{status}` - Get applications by status
- `GET /api/loan-applications/loan-type/{loanType}` - Get applications by type
- `PUT /api/loan-applications/{id}` - Update loan application
- `PATCH /api/loan-applications/{id}/status` - Update application status
- `DELETE /api/loan-applications/{id}` - Delete loan application

### Loan Management
- `POST /api/loans` - Create a new loan
- `GET /api/loans` - Get all loans
- `GET /api/loans/{id}` - Get loan by ID
- `GET /api/loans/loan-number/{loanNumber}` - Get loan by number
- `GET /api/loans/user/{userId}` - Get loans by user
- `GET /api/loans/status/{status}` - Get loans by status
- `GET /api/loans/application/{applicationId}` - Get loan by application
- `PUT /api/loans/{id}` - Update loan
- `PATCH /api/loans/{id}/status` - Update loan status
- `DELETE /api/loans/{id}` - Delete loan

### EMI Schedule Management
- `POST /api/emi-schedules` - Create EMI schedule
- `GET /api/emi-schedules` - Get all EMI schedules
- `GET /api/emi-schedules/{id}` - Get EMI schedule by ID
- `GET /api/emi-schedules/loan/{loanId}` - Get schedules by loan
- `GET /api/emi-schedules/status/{status}` - Get schedules by payment status
- `GET /api/emi-schedules/overdue` - Get overdue EMI schedules
- `GET /api/emi-schedules/due-between?startDate=&endDate=` - Get schedules due between dates
- `PUT /api/emi-schedules/{id}` - Update EMI schedule
- `PATCH /api/emi-schedules/{id}/payment-status` - Update payment status
- `DELETE /api/emi-schedules/{id}` - Delete EMI schedule

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd loan-eligibility-emi-system
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Base URL: `http://localhost:8080/api`
   - H2 Console: `http://localhost:8080/api/h2-console`

### Running with Production Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Configuration

### Development (H2 Database)
The application uses H2 in-memory database by default. No additional configuration needed.

### Production (MySQL Database)
Update `application-prod.yml` with your MySQL credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/loandb
    username: your_username
    password: your_password
```

Set the `DB_PASSWORD` environment variable for security.

## API Response Format

All API responses follow a standard format:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-01-08T14:47:39"
}
```

## Exception Handling

The application includes global exception handling for:
- `ResourceNotFoundException` - Returns 404
- `MethodArgumentNotValidException` - Returns 400 with validation errors
- `Exception` - Returns 500 for unexpected errors

## Future Enhancements

- Add business logic for loan eligibility calculation
- Implement EMI calculation logic
- Add authentication and authorization
- Add input validation with @Valid annotations
- Implement pagination and sorting
- Add unit and integration tests
- Add API documentation with Swagger/OpenAPI
- Implement audit logging

## License

This project is open source and available under the [MIT License](LICENSE).

## Contributors

- Your Name

## Support

For support, please contact [your-email@example.com]
