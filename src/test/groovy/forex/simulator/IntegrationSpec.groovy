package forex.simulator

import forex.broker.LiveTraders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@SpringBootTest(webEnvironment = NONE, classes = SpecConfiguration.class)
@ActiveProfiles("integration")
@ContextConfiguration
class IntegrationSpec extends Specification {

    @Configuration
    @Import(SimulatorConfig.class)
    static class SpecConfiguration {
    }

    @Autowired
    Simulator simulator
    @Autowired
    LiveTraders traders
    @Autowired
    SimulatorContextImpl context

    def 'should return the correct profit for history comparator'() {

        def historyComparator1 = traders.traders[0]
        def historyComparator2 = traders.traders[1]
        def regressionComparator1 = traders.traders[2]
        def regressionComparator2 = traders.traders[3]

        simulator.run()

        def historyComparatorData1 = context.getTraderData(historyComparator1.accountNumber)
        def historyComparatorData12 = context.getTraderData(historyComparator2.accountNumber)
        def regressionComparatorData1 = context.getTraderData(regressionComparator1.accountNumber)
        def regressionComparatorData2 = context.getTraderData(regressionComparator2.accountNumber)

        def historyComparatorPortfolio1 = historyComparatorData1.mostRecentPortfolio
        def historyComparatorPortfolio2 = historyComparatorData12.mostRecentPortfolio
        def regressionComparatorPortfolio3 = regressionComparatorData1.mostRecentPortfolio
        def regressionComparatorPortfolio4 = regressionComparatorData2.mostRecentPortfolio

        expect: 'history comparator profit/loss was calculated correctly'
        historyComparatorPortfolio1.pipettesProfit == -12503L
        historyComparatorPortfolio1.getNetAssetValue() == 4987497L

        and: 'both history comparator traders had the same profit/loss'
        historyComparatorPortfolio2.pipettesProfit == historyComparatorPortfolio1.pipettesProfit

        and: 'regression comparator profit/loss was calculated correctly'
        regressionComparatorPortfolio3.pipettesProfit == 5444L
        regressionComparatorPortfolio3.getNetAssetValue() == 5005444L

        and: 'both regression comparator traders had the same profit/loss'
        regressionComparatorPortfolio4.pipettesProfit == regressionComparatorPortfolio3.pipettesProfit
    }
}
