package market;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataRetriever<REQUEST, RESPONSE> {

    private static final Logger LOG = LoggerFactory.getLogger(DataRetriever.class);

    @FunctionalInterface
    interface RequestHandler<REQUEST, RESPONSE> {
        RESPONSE handleRequest(REQUEST request);
    }

    private final MarketTime clock;
    private final RequestHandler<REQUEST, RESPONSE> handler;

    public DataRetriever(MarketTime clock, RequestHandler<REQUEST, RESPONSE> handler) {
        this.clock = clock;
        this.handler = handler;
    }

    public List<RESPONSE> retrieve(List<REQUEST> requests) {

        List<RESPONSE> responses = new ArrayList<>(requests.size());

        boolean throttle = false;

        for (REQUEST request : requests) {
            if (throttle) {
                try {
                    clock.sleep(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    LOG.error("Interrupted while throttling!", e);
                }
            } else {
                throttle = true;
            }

            responses.add(handler.handleRequest(request));
        }

        return responses;
    }
}
