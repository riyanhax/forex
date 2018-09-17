package forex.live;

import forex.market.MarketTime;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
class SystemTime implements MarketTime {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(getZone());
    }

    @Override
    public LocalDate nowLocalDate() {
        return LocalDate.now(getZone());
    }

    @Override
    public void sleep(long amount, TimeUnit unit) throws InterruptedException {
        unit.sleep(amount);
    }

}
