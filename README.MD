

## Architecture
Rather than using doubles to represent dollar/cent values, each value is multiplied by 10000 and used as longs. This means 
all values are converted, passed around, and stored as pipettes. Values are modified and formatted for displaying profits, losses, 
and other dollar centric displays.

Positions are only opened in a long (buy) trade. This simplifies checking for directional indicators (for instance, is the market going
up in the four candle, one day candle, and week candle simultaneously), because it allows for checking both directions (all up or all down) with the 
same algorithm.  This is accomplished by treating the inverse of an instrument as a tradeable instrument. For example, to open a 
short position on EUR/USD the algorithm can open a long position on USD/EUR instead. To convert the EUR/USD open, high, low, and close
values for a candle correctly we take the inverse value `(1/value)` and swap the high and low values.  An example:
- EUR/USD: open: 1.13715, high: 1.14450, low: 1.13010, close: 1.14376
- USD/EUR: open: 0.87939, high: 8.84880, low: 0.87374, close: 0.87431

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

The first time you run `LiveApplication` it will retrieve as much Oanda history data as it can up to the present. Requests are throttled,
so this will take quite a while. From that point forward it will capture any closed one minute candles during each processing loop (every minute).
 
##  Running
### LiveApplication
- Spring Boot type
- In *Program Arguments*: --oanda.api.token=\<your token>
- In *Active Profiles*: mysql

### SimulatorApplication
- Spring Boot type
