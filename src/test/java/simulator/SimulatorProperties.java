package simulator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import trader.TradingStrategies;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "simulation")
public class SimulatorProperties {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long millisDelayBetweenMinutes;
    private int accountBalanceDollars;
    private long pippeteSpread;
    private int instancesPerTraderType;
    private List<TradingStrategies> tradingStrategies;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getMillisDelayBetweenMinutes() {
        return millisDelayBetweenMinutes;
    }

    public void setMillisDelayBetweenMinutes(long millisDelayBetweenMinutes) {
        this.millisDelayBetweenMinutes = millisDelayBetweenMinutes;
    }

    public int getAccountBalanceDollars() {
        return accountBalanceDollars;
    }

    public void setAccountBalanceDollars(int accountBalanceDollars) {
        this.accountBalanceDollars = accountBalanceDollars;
    }

    public long getPippeteSpread() {
        return pippeteSpread;
    }

    public void setPippeteSpread(long pippeteSpread) {
        this.pippeteSpread = pippeteSpread;
    }

    public int getInstancesPerTraderType() {
        return instancesPerTraderType;
    }

    public void setInstancesPerTraderType(int instancesPerTraderType) {
        this.instancesPerTraderType = instancesPerTraderType;
    }

    public List<TradingStrategies> getTradingStrategies() {
        return tradingStrategies;
    }

    public void setTradingStrategies(List<TradingStrategies> tradingStrategies) {
        this.tradingStrategies = tradingStrategies;
    }
}
