## Banking Transaction Request-Response UI

### Project Overview

This project is a Maven-based banking application that provides a user interface for sending HTTP requests and displaying API responses related to transactions, accounts, and customers.

The application follows a layered architecture:

-   UI: Sends HTTP requests and displays responses.
-   Spring Boot: Handles REST API endpoints.
-   Service: Implements business logic.
-   JDBC: Manages database operations.
-   MySQL: Stores banking data.

### Project Structure

-   src/main/java/ – Backend Java source code.
-   src/main/resources/ – Frontend UI and application resources.
-   [sqlscript](https://github.com/kieudieungan5/Banking-Transaction-Request-Response-UI/blob/b2d5e11/sqlscript) - SQL scripts for database initialization.

### Database Configuration

#### 1. Prerequisites

Install:

-   JDK compatible with the project.
-   MySQL Server.
-   IntelliJ IDEA.
-   the EnvFile plugin in IntelliJ IDEA.

#### 2. Create Database

Create the `digital_bank` database and its tables by using the SQL scripts provided.

#### 3. Configure Environment Variables

Copy the example environment file:

```bash
cp database.env.example database.env
```

Open `database.env` with your MySQL credentials:

```properties
DB_URL=jdbc:mysql://127.0.0.1:3306/digital_bank
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

Replace the sample values with your own MySQL configuration.

#### 4. Configure IntelliJ IDEA

1.  Open Run → Edit Configurations.
2.  Select the Spring Boot configuration.
3.  Enable EnvFile.
4.  Add the `database.env` file.
5.  Apply the configuration.

#### 5. Run the Application

Start the Spring Boot application from IntelliJ IDEA.
Open the frontend by navigating to src/main/resources/static and opening [(index.html)](src/main/resources/index.html) in your browser.