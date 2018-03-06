package trader;

import org.springframework.stereotype.Service;

@Service
public class OpenRandomPositionFactory implements ForexTraderFactory {

    @Override
    public TradingStrategy create() {
        return new OpenRandomPosition();
    }
}
