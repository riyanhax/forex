package live;

import market.MarketTime;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

}
