package live

import broker.Price
import spock.lang.Specification

import static market.Instrument.EURUSD

class BrokerQuoteSpec extends Specification {

    def 'should use broker closeout bid and ask respectively'() {

        def brokerPrice = new Price(EURUSD, 10L, 20L)
        def quote = new BrokerQuote(brokerPrice)

        expect:
        quote.bid == 10L
        quote.ask == 20L
    }

}
