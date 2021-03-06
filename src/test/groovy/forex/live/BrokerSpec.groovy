package forex.live

import com.google.common.collect.Range
import forex.broker.Account
import forex.broker.AccountSummary
import forex.broker.Broker
import forex.broker.CandlestickData
import forex.broker.Context
import forex.broker.LiveTraders
import forex.broker.MarketOrderRequest
import forex.broker.OrderService
import forex.broker.Orders
import forex.broker.Quote
import forex.broker.StopLossDetails
import forex.broker.TakeProfitDetails
import forex.broker.TradeCloseResponse
import forex.broker.TradeSummary
import forex.market.AccountSnapshot
import forex.market.InstrumentDataRetriever
import forex.market.InstrumentHistoryService
import forex.market.MarketTime
import forex.simulator.TestClock
import forex.trader.ForexTrader
import forex.trader.TraderService
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static forex.broker.OrderService.createMarketOrderRequest
import static forex.market.Instrument.EURUSD
import static forex.market.Instrument.USDEUR
import static java.time.Month.APRIL
import static java.time.Month.AUGUST
import static java.time.Month.NOVEMBER
import static java.time.Month.SEPTEMBER

class BrokerSpec extends Specification {

    def instrumentHistoryService = Mock(InstrumentHistoryService)
    def instrumentDataRetriever = Mock(InstrumentDataRetriever)
    def orderService = Mock(OrderService)

    @Unroll
    def 'should evaluate isClosed correctly: #description'() {

        def clock = Mock(MarketTime)
        clock.now() >> now

        def broker = new Broker(clock, new LiveTraders([]), instrumentHistoryService, instrumentDataRetriever, orderService)
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
        def broker = new Broker(Mock(MarketTime), new LiveTraders([trader]), instrumentHistoryService, instrumentDataRetriever, orderService) {
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
        def accountID = 'accountId'
        def expectedAccountData = new AccountSummary(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('1234')
                .withProfitLoss(13L)
                .build(), [], Orders.empty())

        def trader = Mock(ForexTrader)
        trader.accountNumber >> expectedAccountData.id
        trader.account >> Optional.of(expectedAccountData)

        def clock = new TestClock(LocalDateTime.now())

        def traders = new LiveTraders([trader])
        def broker = new Broker(clock, traders, instrumentHistoryService, instrumentDataRetriever, orderService)

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

        def actual = createMarketOrderRequest(quote, instrument, 2, 300L, 600L)

        expect:
        actual == new MarketOrderRequest(instrument: instrument, units: 2,
                stopLossOnFill: new StopLossDetails(price: expectedStopLoss),
                takeProfitOnFill: new TakeProfitDetails(price: expectedTakeProfit))

        where:
        instrument | bid     | ask     | expectedStopLoss | expectedTakeProfit
        EURUSD     | 123060L | 123106L | 122760L          | 123660
        USDEUR     | 81235L  | 81265L  | 81067L           | 81663L
    }

    def 'should create the correct close position request'() {

        def id = '1'
        def position = new TradeSummary('309', id, USDEUR, 86239L, LocalDateTime.of(2018, SEPTEMBER, 7, 7, 43, 13, 567036542),
                3, 3, 6L, 0L, LocalDateTime.of(2018, SEPTEMBER, 7, 07, 45, 11, 338759441))

        def currentAccount = new AccountSummary(new Account.Builder(id)
                .withBalanceDollars(50)
                .withLastTransactionID('3')
                .withProfitLoss(1L)
                .build(), [position], Orders.empty())

        def context = Mock(Context)

        def clock = Mock(MarketTime)
        def trader = new TestTrader(id, context, Mock(TraderService), clock) {
            @Override
            Optional<AccountSummary> getAccount() {
                return Optional.ofNullable(currentAccount);
            }
        }

        Broker oanda = new Broker(clock, new LiveTraders([trader]), instrumentHistoryService, instrumentDataRetriever, orderService)

        when: 'a trader submits a close position request'
        oanda.closePosition(trader, position, null)

        then: 'the context request is created correctly'
        1 * context.closeTrade({
            it.units == position.currentUnits &&
                    it.tradeSpecifier.id == position.tradeId && it.accountID == currentAccount.id
        }) >> new TradeCloseResponse()
    }

    @Unroll
    def 'should create correct one week candles requests: #instrument'() {

        def start = LocalDateTime.of(2018, AUGUST, 24, 16, 0, 0)
        def end = LocalDateTime.of(2018, SEPTEMBER, 7, 16, 0, 0)

        def context = Mock(Context)
        def accountID = 'accountId'
        def account = new AccountSummary(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('1234')
                .withProfitLoss(13L)
                .build(), [], Orders.empty())

        def trader = Mock(ForexTrader)
        trader.accountNumber >> accountID
        trader.account >> Optional.of(account)
        trader.context >> context

        when: 'one day candles are requested'
        def actual = new Broker(Mock(MarketTime), new LiveTraders([trader]), instrumentHistoryService, instrumentDataRetriever, orderService)
                .getOneWeekCandles(trader, instrument, Range.closed(start, end))

        then: 'the request has the correct arguments'
        1 * instrumentHistoryService.getOneWeekCandles(instrument, Range.closed(start, end)) >> {
            NavigableMap<LocalDateTime, CandlestickData> response = new TreeMap<>()
            response.put(LocalDateTime.of(2018, AUGUST, 24, 16, 0, 0), new CandlestickData(15L, 25L, 10L, 12L))
            response.put(LocalDateTime.of(2018, AUGUST, 31, 16, 0, 0), new CandlestickData(16L, 26L, 11L, 13L))
            response.put(LocalDateTime.of(2018, SEPTEMBER, 7, 16, 0, 0), new CandlestickData(17L, 27L, 12L, 14L))

            return response
        }

        and: 'the response had an exclusive end'
        actual.keySet() == [
                LocalDateTime.of(2018, AUGUST, 24, 16, 0, 0),
                LocalDateTime.of(2018, AUGUST, 31, 16, 0, 0)
        ] as Set

        and: 'the values were correct'
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
        def accountID = 'accountId'
        def account = new AccountSummary(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('1234')
                .withProfitLoss(13L)
                .build(), [], Orders.empty())

        def trader = Mock(ForexTrader)
        trader.accountNumber >> accountID
        trader.account >> Optional.of(account)
        trader.context >> context

        when: 'one day candles are requested'
        def actual = new Broker(Mock(MarketTime), new LiveTraders([trader]), instrumentHistoryService, instrumentDataRetriever, orderService)
                .getOneDayCandles(trader, instrument, Range.closed(
                start, end))

        then: 'the request has the correct arguments'
        1 * instrumentHistoryService.getOneDayCandles(instrument, Range.closed(start, end)) >> {
            NavigableMap<LocalDateTime, CandlestickData> response = new TreeMap<>()
            response.put(LocalDateTime.of(2018, SEPTEMBER, 5, 16, 0, 0), new CandlestickData(15L, 25L, 10L, 12L))
            response.put(LocalDateTime.of(2018, SEPTEMBER, 6, 16, 0, 0), new CandlestickData(16L, 26L, 11L, 13L))
            response.put(LocalDateTime.of(2018, SEPTEMBER, 7, 16, 0, 0), new CandlestickData(17L, 27L, 12L, 14L))

            return response
        }

        and: 'the response had an exclusive end'
        actual.keySet() == [
                LocalDateTime.of(2018, SEPTEMBER, 5, 16, 0, 0),
                LocalDateTime.of(2018, SEPTEMBER, 6, 16, 0, 0)
        ] as Set

        and: 'values were correct'
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
        def accountID = 'accountId'
        def account = new AccountSummary(new Account.Builder(accountID)
                .withBalanceDollars(50)
                .withLastTransactionID('1234')
                .withProfitLoss(13L)
                .build(), [], Orders.empty())

        def trader = Mock(ForexTrader)
        trader.accountNumber >> accountID
        trader.account >> Optional.of(account)
        trader.context >> context

        when: 'one day candles are requested'
        def actual = new Broker(Mock(MarketTime), new LiveTraders([trader]), instrumentHistoryService, instrumentDataRetriever, orderService)
                .getFourHourCandles(trader, instrument, Range.closed(
                start, end))

        then: 'the request has the correct arguments'
        1 * instrumentHistoryService.getFourHourCandles(instrument, Range.closed(start, end)) >> {
            NavigableMap<LocalDateTime, CandlestickData> response = new TreeMap<>()
            response.put(LocalDateTime.of(2018, SEPTEMBER, 5, 0, 0, 0), new CandlestickData(15L, 25L, 10L, 12L))
            response.put(LocalDateTime.of(2018, SEPTEMBER, 5, 4, 0, 0), new CandlestickData(16L, 26L, 11L, 13L))
            response.put(LocalDateTime.of(2018, SEPTEMBER, 5, 8, 0, 0), new CandlestickData(17L, 27L, 12L, 14L))

            return response
        }

        and: 'the response had an exclusive end'
        actual.keySet() == [
                LocalDateTime.of(2018, SEPTEMBER, 5, 0, 0, 0),
                LocalDateTime.of(2018, SEPTEMBER, 5, 4, 0, 0)
        ] as Set

        and: 'values were correct'
        actual.values() as Set == [
                new CandlestickData(15L, 25L, 10L, 12L),
                new CandlestickData(16L, 26L, 11L, 13L)
        ] as Set

        where:
        instrument << [
                EURUSD, USDEUR
        ]
    }

    def 'should retrieve new closed minute candles when processing updates'() {

        def clock = Mock(MarketTime)
        clock.now() >> LocalDateTime.of(2018, SEPTEMBER, 14, 12, 0)

        def trader = Mock(ForexTrader)
        trader.accountNumber >> '1234'

        def broker = new Broker(clock, new LiveTraders([trader]), instrumentHistoryService, instrumentDataRetriever, orderService)

        when: 'the broker is processing updates'
        broker.processUpdates()

        then: 'absent closed instrument candles are retrieved first'
        1 * instrumentDataRetriever.retrieveClosedCandles()

        then: 'traders are allowed to process updates'
        1 * trader.processUpdates(broker)
    }

}
