

## Architecture
Rather than using doubles to represent dollar/cent values, each value is multiplied by 10000 and used as longs. This means 
all values are converted, passed around, and stored as pipettes. Values are modified and formatted for displaying profits, losses, 
and other dollar centric displays.

`Context` is the main interface to a broker. The two implementations of this interface are `OandaContext` and `SimulatorContextImpl`, 
which are used to interact with Oanda (a live broker) and a simulator using historical data respectively.

Using the `prod` profile when running `LiveApplication` will point to the live Oanda API endpoint, whereas the default is 
to use the practice API endpoint.

`application.yml` contains most simulation configuration options. Keep in mind the `integration` profile is used for a regression
test (`IntegrationSpec`), so those values should not be changed without also changing the assertions in the specification.
 
## Database
Flyway is used to initialize and migrate the database. Just provide an appropriate jdbc url for `spring.datasources.url` and 
credentials for `spring.datasource.username` and `spring.datasource.password`.  Reference the [Spring documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) 
for the many ways these values can be provided.
 
##  Running
### LiveApplication
- Spring Boot type
- In *Program Arguments*: --oanda.api.token=\<your token>
- In *Active Profiles*: mysql

### SimulatorApplication
- Spring Boot type
