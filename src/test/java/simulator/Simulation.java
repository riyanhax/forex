package simulator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@ConfigurationProperties(prefix = "simulation")
public class Simulation {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long millisDelayBetweenMinutes;
    private long pippeteSpread;
    private int instancesPerTraderType;

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
}
