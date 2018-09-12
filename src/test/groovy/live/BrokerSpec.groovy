package live

import broker.Account
import broker.AccountAndTrades
import broker.AccountID
import broker.Candlestick
import broker.CandlestickData
import broker.Context
import broker.InstrumentCandlesResponse
import broker.MarketOrderRequest
import broker.MarketOrderTransaction
import broker.OpenPositionRequest
import broker.OrderCreateResponse
import broker.Price
import broker.PricingGetResponse
import broker.Quote
import broker.StopLossDetails
import broker.TakeProfitDetails
import broker.TradeCloseResponse
import broker.TradeSummary
import broker.TransactionID
import com.google.common.collect.Range
import market.AccountSnapshot
import market.MarketTime
import simulator.TestClock
import spock.lang.Specification
import spock.lang.Unroll
import trader.ForexTrader

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

import static broker.CandlePrice.ASK
import static broker.CandlePrice.BID
import static broker.CandlePrice.MID
import static broker.CandlestickGranularity.D
import static broker.CandlestickGranularity.H4
import static broker.CandlestickGranularity.W
import static java.time.Month.APRIL
import static java.time.Month.AUGUST
import static java.time.Month.NOVEMBER
import static java.time.Month.SEPTEMBER
import static market.Instrument.EURUSD
import static market.Instrument.USDEUR

class BrokerSpec extends Specification {

    @Unroll
    def 'should evaluate isClosed correctly: #description'() {

        def clock = Mock(MarketTime)
        clock.now() >> now

        def broker = new Broker(clock, new LiveTraders([]))
        def actual = broker.isClosed()

        expect:
        actual == expected

        where:
        description                             | now                                              | expected
        'spring friday one second before close' | LocalDateTime.of(2018, APRIL, 6, 15, 59, 59)     | false
        'spring friday at close'                | LocalDateTime.of(2018, APRIL, 6, 16, 0, 0)       | true
        'spring saturday'                       | LocalDateTime.of(2018, APRIL, 7, 12, 0, 0)       | true
        'spring sunday 1 hour before open'      | LocalDateTime.of(2018, APRIL, 8, 15, 0, 0)       | true
        'spring sunday 1 second before open'    | LocalDateTime.of(2018, APRIL, 8, 15, 59, 59)     | true
        'spring sunday at open'                 | LocalDateTime.of(2018, APRIL, 8, 16, 0, 0)       | false
        'spring sunday 1 hour after open'       | LocalDateTime.of(2018, APRIL, 8, 17, 0, 0)       | false

        'fall friday one second before close'   | LocalDateTime.of(2018, NOVEMBER, 16, 15, 59, 59) | false
        'fall friday at close'                  | LocalDateTime.of(2018, NOVEMBER, 16, 16, 0, 0)   | true
        'fall saturday'                         | LocalDateTime.of(2018, NOVEMBER, 17, 12, 0, 0)   | true
        'fall sunday 1 hour before open'        | LocalDateTime.of(2018, NOVEMBER, 18, 15, 0, 0)   | true
        'fall sunday 1 second before open'      | LocalDateTime.of(2018, NOVEMBER, 18, 15, 59, 59) | true
        'fall sunday at open'                   | LocalDateTime.of(2018, NOVEMBER, 18, 16, 0, 0)   | false
        'fall sunday 1 hour after open'         | LocalDateTime.of(2018, NOVEMBER, 18, 17, 0, 0)   | false
    }

    @Unroll
    def 'should not process traders when the broker is closed, open: #open'() {

        ForexTrader trader = Mock(ForexTrader)
        trader.accountNumber >> '1234'

        boolean closed = !open
        def broker = new Broker(Mock(MarketTime), new LiveTraders([trader])) {
            @Override
            boolean isClosed() {
                return closed
            }
        }

        when: 'if the broker is closed'
        broker.processUpdates()

        then: 'traders are not processed'
        (open ? 1 : 0) * trader.processUpdates(broker)

        where:
        open << [true, false]
    }

    def 'should return account snapshot data associated to the current time'() {
        def accountID = new AccountID('accountId')
        def expectedAccountData = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('1234'))
                .withProfitLoss(13L)
                .build()

        def trader = Mock(ForexTrader)
        trader.accountNumber >> expectedAccountData.id.id
        trader.account >> Optional.of(expectedAccountData)

        def clock = new TestClock(LocalDateTime.now())

        def traders = new LiveTraders([trader])
        def broker = new Broker(clock, traders)

        when: 'an account snapshot is requested for a trader'
        def actual = broker.getAccountSnapshot(traders.traders[0])

        then: 'account data is returned with the current timestamp'
        actual == new AccountSnapshot(expectedAccountData, clock.now())
    }

    @Unroll
    // Can't figure out why I have to set these weird expected stop loss and take profits
    def 'inverse instruments should use ask price and the strange stop loss and take profit: #instrument'() {
        Quote quote = Mock(Quote)
        quote.ask >> ask
        quote.bid >> bid

        def actual = Broker.createMarketOrderRequest(quote, instrument, 2, 300L, 600L)

        expect:
        actual == new MarketOrderRequest(instrument: instrument, units: 2,
                stopLossOnFill: new StopLossDetails(price: expectedStopLoss),
                takeProfitOnFill: new TakeProfitDetails(price: expectedTakeProfit))

        where:
        instrument | bid     | ask     | expectedStopLoss | expectedTakeProfit
        EURUSD     | 123060L | 123106L | 122760L          | 123660
        USDEUR     | 81235L  | 81265L  | 81067L           | 81663L
    }

    def 'should use the specified units from the request'() {
        def id = '1'
        def context = Mock(Context)
        context.initializeAccount(id, _) >> new AccountAndTrades(new Account.Builder(new AccountID(id))
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('someId'))
                .withProfitLoss(13L)
                .build(), [])
        context.getPricing(_) >> new PricingGetResponse([new Price(EURUSD, 10010L, 10020L)])

        def clock = Mock(MarketTime)
        def trader = new TestTrader(id, context, clock)

        Broker oanda = new Broker(clock, new LiveTraders([trader]))

        when: 'a trader opens a position with a specific number of units'
        oanda.openPosition(trader, new OpenPositionRequest(EURUSD, 3, null, null, null))

        then: 'the position is opened with the requested number of units'
        1 * context.createOrder({
            it.order.units == 3
        }) >> new OrderCreateResponse(EURUSD, new MarketOrderTransaction('6367',
                LocalDateTime.of(2016, Month.JUNE, 22, 13, 41, 29, 264030555), EURUSD, 3))
    }

    def 'should create the correct close position request'() {

        def id = '1'
        def position = new TradeSummary(USDEUR, 3, 86239L, 6L, 0L,
                LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542),
                LocalDateTime.of(2018, SEPTEMBER, 7, 07, 45, 11, 338759441), '309')

        def currentAccount = new Account.Builder(new AccountID(id))
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('3'))
                .withTrades([position])
                .withProfitLoss(1L)
                .build()

        def context = Mock(Context)
        context.initializeAccount(id, _) >> new AccountAndTrades(currentAccount, [])

        def clock = Mock(MarketTime)
        def trader = new TestTrader(id, context, clock)

        Broker oanda = new Broker(clock, new LiveTraders([trader]))

        when: 'a trader submits a close position request'
        oanda.closePosition(trader, position, null)

        then: 'the context request is created correctly'
        1 * context.closeTrade({
            it.units == position.currentUnits &&
                    it.tradeSpecifier.id == position.id && it.accountID == currentAccount.id
        }) >> new TradeCloseResponse()
    }

    @Unroll
    def 'should create correct one week candles requests: #instrument'() {

        def start = LocalDateTime.of(2018, AUGUST, 24, 16, 0, 0)
        def end = LocalDateTime.of(2018, SEPTEMBER, 7, 16, 0, 0)

        def context = Mock(Context)
        def accountID = new AccountID('accountId')
        def account = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('1234'))
                .withProfitLoss(13L)
                .build()

        def trader = Mock(ForexTrader)
        trader.accountNumber >> accountID.id
        trader.account >> Optional.of(account)
        trader.context >> context

        when: 'one day candles are requested'
        def actual = new Broker(Mock(MarketTime), new LiveTraders([trader]))
                .getOneWeekCandles(trader, instrument, Range.closed(
                start, end))

        then: 'the request has the correct arguments'
        1 * context.instrumentCandles({
            it.granularity == W &&
                    it.instrument == instrument &&
                    it.price == [BID, MID, ASK] as Set &&
                    it.from == start &&
                    it.to == end &&
                    it.includeFirst == true &&
                    it.weeklyAlignment == DayOfWeek.FRIDAY
        }) >> new InstrumentCandlesResponse(instrument, W, [
                new Candlestick(LocalDateTime.of(2018, AUGUST, 24, 16, 0, 0),
                        new CandlestickData(10L, 20L, 5L, 7L),
                        new CandlestickData(20L, 30L, 15L, 17L),
                        new CandlestickData(15L, 25L, 10L, 12L)
                ),
                new Candlestick(LocalDateTime.of(2018, AUGUST, 31, 16, 0, 0),
                        new CandlestickData(11L, 21L, 6L, 8L),
                        new CandlestickData(21L, 31L, 16L, 18L),
                        new CandlestickData(16L, 26L, 11L, 13L)
                ),
                new Candlestick(LocalDateTime.of(2018, SEPTEMBER, 7, 16, 0, 0),
                        new CandlestickData(12L, 22L, 7L, 9L),
                        new CandlestickData(22L, 32L, 17L, 19L),
                        new CandlestickData(17L, 27L, 12L, 14L)
                )
        ])

        and: 'the response had an exclusive end'
        actual.keySet() == [
                LocalDateTime.of(2018, AUGUST, 24, 16, 0, 0),
                LocalDateTime.of(2018, AUGUST, 31, 16, 0, 0)
        ] as Set

        and: 'mid prices were used'
        actual.values() as Set == [
                new CandlestickData(15L, 25L, 10L, 12L),
                new CandlestickData(16L, 26L, 11L, 13L)
        ] as Set

        where:
        instrument << [
                EURUSD, USDEUR
        ]
    }

    @Unroll
    def 'should create correct one day candles requests: #instrument'() {

        def start = LocalDateTime.of(2018, SEPTEMBER, 5, 16, 0, 0)
        def end = LocalDateTime.of(2018, SEPTEMBER, 7, 16, 0, 0)

        def context = Mock(Context)
        def accountID = new AccountID('accountId')
        def account = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('1234'))
                .withProfitLoss(13L)
                .build()

        def trader = Mock(ForexTrader)
        trader.accountNumber >> accountID.id
        trader.account >> Optional.of(account)
        trader.context >> context

        when: 'one day candles are requested'
        def actual = new Broker(Mock(MarketTime), new LiveTraders([trader]))
                .getOneDayCandles(trader, instrument, Range.closed(
                start, end))

        then: 'the request has the correct arguments'
        1 * context.instrumentCandles({
            it.granularity == D &&
                    it.instrument == instrument &&
                    it.price == [BID, MID, ASK] as Set &&
                    it.from == start &&
                    it.to == end &&
                    it.includeFirst == true &&
                    it.alignmentTimezone == ZoneId.of("America/Chicago") &&
                    it.dailyAlignment == 16 // 5 PM EST
        }) >> new InstrumentCandlesResponse(instrument, D, [
                new Candlestick(LocalDateTime.of(2018, SEPTEMBER, 5, 16, 0, 0),
                        new CandlestickData(10L, 20L, 5L, 7L),
                        new CandlestickData(20L, 30L, 15L, 17L),
                        new CandlestickData(15L, 25L, 10L, 12L)
                ),
                new Candlestick(LocalDateTime.of(2018, SEPTEMBER, 6, 16, 0, 0),
                        new CandlestickData(11L, 21L, 6L, 8L),
                        new CandlestickData(21L, 31L, 16L, 18L),
                        new CandlestickData(16L, 26L, 11L, 13L)
                ),
                new Candlestick(LocalDateTime.of(2018, SEPTEMBER, 7, 16, 0, 0),
                        new CandlestickData(12L, 22L, 7L, 9L),
                        new CandlestickData(22L, 32L, 17L, 19L),
                        new CandlestickData(17L, 27L, 12L, 14L)
                )
        ])

        and: 'the response had an exclusive end'
        actual.keySet() == [
                LocalDateTime.of(2018, SEPTEMBER, 5, 16, 0, 0),
                LocalDateTime.of(2018, SEPTEMBER, 6, 16, 0, 0)
        ] as Set

        and: 'mid prices were used'
        actual.values() as Set == [
                new CandlestickData(15L, 25L, 10L, 12L),
                new CandlestickData(16L, 26L, 11L, 13L)
        ] as Set

        where:
        instrument << [
                EURUSD, USDEUR
        ]
    }

    def 'should create correct four hour candles requests: #instrument'() {

        def start = LocalDateTime.of(2018, SEPTEMBER, 5, 0, 0, 0)
        def end = LocalDateTime.of(2018, SEPTEMBER, 5, 8, 0, 0)

        def context = Mock(Context)
        def accountID = new AccountID('accountId')
        def account = new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID(new TransactionID('1234'))
                .withProfitLoss(13L)
                .build()

        def trader = Mock(ForexTrader)
        trader.accountNumber >> accountID.id
        trader.account >> Optional.of(account)
        trader.context >> context

        when: 'one day candles are requested'
        def actual = new Broker(Mock(MarketTime), new LiveTraders([trader]))
                .getFourHourCandles(trader, instrument, Range.closed(
                start, end))

        then: 'the request has the correct arguments'
        1 * context.instrumentCandles({
            it.granularity == H4 &&
                    it.instrument == instrument &&
                    it.price == [BID, MID, ASK] as Set &&
                    it.from == start &&
                    it.to == end &&
                    it.includeFirst == true
        }) >> new InstrumentCandlesResponse(instrument, H4, [
                new Candlestick(LocalDateTime.of(2018, SEPTEMBER, 5, 0, 0, 0),
                        new CandlestickData(10L, 20L, 5L, 7L),
                        new CandlestickData(20L, 30L, 15L, 17L),
                        new CandlestickData(15L, 25L, 10L, 12L)
                ),
                new Candlestick(LocalDateTime.of(2018, SEPTEMBER, 5, 4, 0, 0),
                        new CandlestickData(11L, 21L, 6L, 8L),
                        new CandlestickData(21L, 31L, 16L, 18L),
                        new CandlestickData(16L, 26L, 11L, 13L)
                ),
                new Candlestick(LocalDateTime.of(2018, SEPTEMBER, 5, 8, 0, 0),
                        new CandlestickData(12L, 22L, 7L, 9L),
                        new CandlestickData(22L, 32L, 17L, 19L),
                        new CandlestickData(17L, 27L, 12L, 14L)
                )
        ])

        and: 'the response had an exclusive end'
        actual.keySet() == [
                LocalDateTime.of(2018, SEPTEMBER, 5, 0, 0, 0),
                LocalDateTime.of(2018, SEPTEMBER, 5, 4, 0, 0)
        ] as Set

        and: 'mid prices were used'
        actual.values() as Set == [
                new CandlestickData(15L, 25L, 10L, 12L),
                new CandlestickData(16L, 26L, 11L, 13L)
        ] as Set

        where:
        instrument << [
                EURUSD, USDEUR
        ]
    }

}
