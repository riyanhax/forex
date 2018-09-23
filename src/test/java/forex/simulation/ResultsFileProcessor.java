package forex.simulation;

import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import forex.broker.CandlestickData;
import forex.broker.LiveTraders;
import forex.simulator.ResultsProcessor;
import forex.simulator.SimulatorContext;
import forex.simulator.SimulatorProperties;
import forex.simulator.TradeHistory;
import forex.trader.ForexTrader;
import forex.trader.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static com.google.common.io.Files.write;
import static forex.market.MarketTime.formatTimestamp;

@Service
class ResultsFileProcessor implements ResultsProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ResultsFileProcessor.class);
    private static final DateTimeFormatter SIMPLE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd HHmm");

    @Override
    public void done(LiveTraders liveTraders, SimulatorContext context, SimulatorProperties simulatorProperties) {
        Map<TradingStrategy, List<ForexTrader>> tradersByStrategy = liveTraders.getTraders().stream()
                .collect(Collectors.groupingBy(ForexTrader::getStrategy));

        for (Map.Entry<TradingStrategy, List<ForexTrader>> e : tradersByStrategy.entrySet()) {
            Collection<ForexTrader> traders = e.getValue();

            for (ForexTrader trader : traders) {

                SortedSet<TradeHistory> closedTrades = context.closedTradesForAccountId(trader.getAccountNumber());

                File traderDirectory = new File("build", trader.getAccountNumber());
                File tradesDirectory = new File(traderDirectory, "trades");

                if (traderDirectory.exists()) {
                    try {
                        MoreFiles.deleteRecursively(traderDirectory.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }

                StringBuilder sb = new StringBuilder();
                long portfolioPips = 0L;
                sb.append(formatTimestamp(simulatorProperties.getStartTime()))
                        .append(",")
                        .append(portfolioPips);

                for (TradeHistory trade : closedTrades) {
                    portfolioPips += trade.getRealizedProfitLoss();

                    sb.append("\n");
                    sb.append(formatTimestamp(trade.getCloseTime()))
                            .append(",")
                            .append(portfolioPips);
                }

                File file = new File(traderDirectory, "portfolio.csv");
                writeFile(file, sb);

                for (TradeHistory trade : closedTrades) {
                    sb = new StringBuilder();
                    sb.append("Timestamp,Price\n");

                    for (Map.Entry<LocalDateTime, CandlestickData> it : trade.getCandles().entrySet()) {
                        sb.append(it.getKey().format(SIMPLE_TIME_FORMAT))
                                .append(",")
                                .append(it.getValue().getO())
                                .append("\n");
                    }

                    file = new File(tradesDirectory, trade.getInstrument() + "-" +
                            trade.getOpenTime().format(SIMPLE_TIME_FORMAT) + "-" +
                            (trade.getRealizedProfitLoss() > 0 ? "WIN" : "LOSS") +
                            ".csv");
                    writeFile(file, sb);
                }
            }
        }
    }

    private static void writeFile(File file, StringBuilder contents) {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            Preconditions.checkArgument(dir.mkdirs(), "Unable to create directory %s", dir.getAbsolutePath());
        }

        try {
            if (!file.exists()) {
                Preconditions.checkArgument(file.createNewFile(), "Unable to create file!");
            }
            write(contents.toString().getBytes(), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


