Correction to Original Data

    Option, Order No.14, the Price column should be "10.60" (NOT "10.06").
    Stock, Order No. 3, the Alert should be "Yes" (NOT "No").

Acceptance Criteria

    1. Functional Requirements

    AC1: Order Evaluation
    Given an Order, the system must evaluate the Order, and give an EvaluationResult.
    The EvaluationResult need to have a flag indicating whether the Price Variation Limiter is activates, and a description for why and how it is activated.

    AC2: Data Source / Auxiliary functions
    The data needed to evaluate the Order, such as, Reference Price, Tick Table, Limit Rules, are already provided.
    The can be queried or calculated via corresponding functions, such as calculatePercentage(), calculateTickCount(), calculateAbsoluteValue().

    AC3: Data Validation
    Assume input data given to the projects are valid.
    Of course, function input parameter validation is still needed.

    2. Non-Functional Requirements

    AC4: Performance
    The application should evaluate an Order within 100ms.

    3. Testing

    AC5: Unit Tests
    The codebase should be covered by unit tests.
    Critical functions should have corresponding unit tests.

    4. Documentation

    AC6: Code Documentation
    The code should be properly documented with comments explaining complex logic and functions.
    A README file with setup instructions must be provided.

    5. Deployment

    AC7: Deployment Process
    The application should be deployable to the staging and production environments with minimal manual intervention.
    Preferably, the application should be deployed via a uber jar, or a Docker image.

    AC8: Scalability
    The system should be horizontal scalable.


Scenarios Description

    Traders might place Orders with price far away from current market price, which is risky.
    This is called Price Variation.

    We will implement a Price Variation Limiter to check the variation, and accordingly restrict Orders that violates the variation rules.

Goal

    A simple implementation of a Price Variation Limiter;
    A local Java application;
    ready to be deployed to production environment
    logging, unit tests are enabled

Optional, Lower Priority Goal

    log the output of the limiter for compliance/audit purpose;

    stress test / load test on QPS (queries per second) of the database;
    stress test / load test on the latency of each validation process;

Non Goal

    Front end (either local GUI, or, Web UI) is not covered;
    Authentitation and authorization are not covered;
    Code coverage is not covered.

    We are NOT building a full-fledged Web App, or RESTful API, or HTTP service to provide the functionality. RESTful API, microservices are out of scope for this project.

    Handling of Alerts is NOT a goal.
        The functions in this app will only output the alert result. How that alert is sent to trader (either through message queue, or, database, or, via sms/email/phone call) is out of scope for this project.

    Who and how to add/create/set/import limit rules into the database are not covered;

Terminology

    Instrument, or, symbol, refer to either stock, or option, or future.
        In the following discussion, we will use "instrument" to refer to any one of stock, or option, or future.

Functionality and interfaces

    Refer to the codes for details.

    NOTE: it seems the Instrument Type does NOT affect any functionalities for this project.
    In other words, it is dummy, and can be safely ignored for this project.

Assumptions

    Assume, the running/deployment environment is Linux server, NOT Windows operating system.
        The development and test is run against Ubuntu 24.04 inside Docker containers, though other Linux distro are expected to work.

    Assume, the function will validate one Order at a time, i.e. for each Order to be validated, a function call is needed.

    Assume, the schema of the Tick table, the Reference Price table do not change;
    but the content/records in the tables may change dynamically (i.e. from time to time).
    In other words, we can NOT assume the Tick size, the Reference price of a specific instrument to be static/not changing.
        ==> We need to fetch these data when needed.
        ==> This is also a reason why we would like to use a database to store these data, rather than a file or embedded database.

    Assume, there are other processes/applications to add records into the Tick table, the Reference Price table, or, the Price Variation Limiter rules table, either manually or programmatically. We simply assume we can fetch/get data from these tables.
        For test purpose, we have prepared several SQL files to create table and insert data

    Assume, the records in Reference Price table are always valid. In other words, for the Price Variation Limiter purpose, we do NOT assume that there might be errors with the Reference Price.

    Assume, a single PostgreSQL instance is capable of handling all the queries for the Price Variation Limiter. If needed, both the PostgreSQL and Price Variation Limiter can be horizontally scaled out to be distributed system. But that is out of scope for this project.

    Assume, database has been initialized, the Price Variation Limiter does NOT need to handle database initialization (table creation, data loading, etc).
        But the query results may still be NULL or empty, even if there are records in the database (but the records does not meet the query criteria).

    Assume, instrument names are valid strings; in other words, all the given instrument symbols are valid.

    Assume, each instrument has ONLY ONE limit rule. If more than one limit rule needs to be supported, we can add a "for" loop to handle that.

    Assume, for tick_size related calculation, the given Order price (and reference price) will always be dividable by the tick_size. Even if it is not dividable, we will fall back to integer by Math.round().

Some rough, back-of-the-envelop guestimates

    1. The total count of instruments are around one million.

    There are 2292 stock instruments listed on New York Stock Exchange. [https://wallstreetnumbers.com/screeners/nyse-companies]

    There are 60 major stock exchanges world wide. [https://en.wikipedia.org/wiki/List_of_major_stock_exchanges]

    ==> This gives us a total count of **stock** instruments around 2500 x 100 = 250, 000

    For **options and futures**, we do not have an exact count.
    For simplicity, we assume it is about the same count as stocks.

    ==> In total, we have about 250,000 x 3 = 750,000 instruments ==> one million instruments.

    2. The count of records for tick size, reference price, price variable limiters each are around one miilion.

    3. An ordinary sized instance of PostgreSQL can reach a QPS (Queries Per Second) of 2000.

    # https://scalegrid.io/blog/pg-benchmark-scalegrid-rds/
    With an instance with hardware specification as 8GB RAM, 2 vCPU, 120GB SSD,
    AWS RDS can reach ~2000 QPS

What We Choose

    A Java application;
        There are multiple languages to choose from, Java, Python, JavaScript.
        Choose Java simply because we are familar with Java, and Java can achieve the goal.

    A separate database for data storage;
        We have many data to be stored, such as the Tick Table, the Reference Price, the Price Variation Limiter rules.
        We can save/store these data in local file, embedded database (e.g. SQLite), or, separate database (e.g. PostgreSQL).
        For **development or test** purpose, a local file, or embedded database is OK.
        For a **realist production deployment**, a separate database is preferred.
        As we have Docker containers, there is no need to use different techniques in different environments, so we will stick with a separate database, and will NOT use local file or embedded database.

    A PostgreSQL instance for database;
        For database, we have multiple choices.
        Here we are focusing on relational databases, such as PostgreSQL, mySQL, or commerical databases (such as MS SQL Server, Oracle DB).
        It is also possible to use key-value store, such as Redis. For simplicity, we will NOT cover that in this project.
        As most operations expected in our implementations are simply SQL queries, there is no much difference between these databases.
        We will simply choose PostgreSQL, which is high performant, free, open source, easy to use, and has strong community.

    We will NOT use ORM (Object-Relational Mapping) for mapping between Java class and database tables.
        The app is ONLY READING from the database;
        it is not doing CUD (Create/Update/Delete) operations to the database;

How to Run

    1. Prerequisite
        PostgreSQL client "psql" on local host
        Java 8+ on host to run the jar application

    2. setup a PostgreSQL server instance
        By default, one instance is already running.
        Refer to src/main/resources/database.properties for connection details.

        A local docker container instance also works.

    3. initialize the database
        A set of sql statements to create database, table, and insert data are provided in the PostgreSQLstatements/ directory.

        run below commands to initialize the database:
        psql --host 35.85.156.184 --username postgres --file create.database.sql
        psql --host 35.85.156.184 --username postgres --file create.tables.sql --dbname trading
        psql --host 35.85.156.184 --username postgres --file insert.data.sql --dbname trading

    4. (optional) build the project

        git clone https://github.com/shunliang/homework.git
        mvn clean install

        # to run unit tests
        mvn test

    5. run the application

        java -jar target/PriceVariationLimiter-1.0-SNAPSHOT.jar

    6. (optional) deploy the application

        create a Docker image for the application, and deploy the docker instance
        TODO: to add Dockerfile

Future Improvements

    1. In "PriceVariationLimiter.java", we made an assumption to simplify the calculation of the variation value, to only ABSOLUTE value.
        This does NOT affect the PriceVariationLimiter alert evaluation results.
        This only affect the description of the evaluation results.
        In the future, we could improve this.

    2. in "PriceVariationLimiter.java", there are still room for improvement for PriceVariationLimiter.evaluate() method, and for PriceVariationLimiter.calculateTickCount() method.
        The current implementation is a straightfoward implementation, not optimized yet.

    3. the unit tests can be further improved, to be more granular, and to add more coverage of test cases.

    4. comments and documentation can be improved further
