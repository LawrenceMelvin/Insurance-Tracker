```markdown
# Insurance Tracker

Insurance Tracker is a Spring Boot application that helps users manage and track their insurance policies. The app provides features for adding, updating, deleting, and viewing insurance entries, as well as automated email reminders for upcoming policy expirations.

## Features

- User authentication and authorization
- Add, update, and delete insurance policies (max 10 per user)
- View insurance details
- Automated email reminders for policies expiring in 7, 5, and 2 days
- RESTful API endpoints
- Secure password storage with BCrypt

## Technologies Used

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Maven
- ProtrgesSQL (configurable)
- SLF4J (logging)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven

### Running the Application

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/insurance-tracker.git
   cd insurance-tracker
   ```

2. Build and run:
   ```sh
   mvn spring-boot:run
   ```

3. Access the application at `http://localhost:8080`

## API Endpoints

- `POST /insurance/add` — Add a new insurance policy
- `PUT /insurance/update/{insuranceId}` — Update an existing policy
- `DELETE /insurance/delete/{insuranceId}` — Delete a policy
- `GET /insurance/{insuranceId}` — Get policy details

## License

This project is licensed under the MIT License.
```