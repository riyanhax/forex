package trader;

import org.springframework.stereotype.Service;

@Service
public class SmarterRandomPositionFactory implements ForexTraderFactory {

    @Override
    public TradingStrategy create() {
        return new SmarterRandomPosition();
    }
}
