package forex.market

import forex.market.DataRetriever.RequestHandler
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS

class DataRetrieverSpec extends Specification {

    def 'should throttle multiple requests'() {

        def clock = Mock(MarketTime)
        def handler = Mock(RequestHandler)

        def service = new DataRetriever<String, String>(clock, handler)

        when: 'multiple requests are provided'
        def actual = service.retrieve(['request1', 'request2'])

        then: 'a request is handled'
        1 * handler.handleRequest('request1') >> 'response1'

        and: 'a throttle is performed'
        1 * clock.sleep(2, SECONDS)

        and: 'another request is handled'
        1 * handler.handleRequest('request2') >> 'response2'

        and: 'the results are returned'
        actual == ['response1', 'response2']
    }

    def 'should not throttle single requests'() {

        def clock = Mock(MarketTime)
        def handler = Mock(RequestHandler)

        def service = new DataRetriever<String, String>(clock, handler)

        when: 'a single request is provided'
        def actual = service.retrieve(['request1'])

        then: 'a request is handled'
        1 * handler.handleRequest('request1') >> 'response1'

        and: 'no throttling occurs'
        0 * clock.sleep(_, _)
    }
}
