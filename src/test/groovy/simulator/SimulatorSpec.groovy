package simulator

import broker.ForexBroker
import live.LiveTraders
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Month

class SimulatorSpec extends Specification {

    def broker = Mock(ForexBroker)

    def 'should run simulation from start to end time'() {

        given: 'a simulation with a start and end timestamp'
        def start = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30)
        def end = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 37)
        SimulatorProperties simulation = new SimulatorProperties(startTime: start, endTime: end, millisDelayBetweenMinutes: 0L);

        def clock = new SimulatorClock(simulation)
        Simulator simulator = new Simulator(simulation, clock, broker, Mock(SimulatorContext), new LiveTraders([]))

        def times = []

        when: 'we process each minute of a simulation'
        while (clock.now().isBefore(end)) {
            times += clock.now()

            simulator.nextMinute()
        }

        then: 'we processed each minute'
        times == [
                LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30),
                LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 31),
                LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 32),
                LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 33),
                LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 34),
                LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 35),
                LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 36)
        ]
    }

    def 'should not be able to advance past the end of a simulation'() {
        given: 'a simulation with a start and end timestamp'
        def start = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30)
        def end = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 32)

        SimulatorProperties simulation = new SimulatorProperties(startTime: start, endTime: end, millisDelayBetweenMinutes: 0L);
        Simulator simulator = new Simulator(simulation, new SimulatorClock(simulation), broker, Mock(SimulatorContext), new LiveTraders([]))

        when: 'the simulation is over'
        simulator.run()

        and: 'we try to go further'
        simulator.nextMinute()

        then: 'an exception is thrown'
        thrown IllegalStateException
    }

    def 'should notify broker at each interval'() {

        def context = Mock(SimulatorContext)
        context.isAvailable() >> true

        given: 'a simulation with a start and end timestamp'
        def start = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30)
        def end = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 32)

        SimulatorProperties simulation = new SimulatorProperties(startTime: start, endTime: end, millisDelayBetweenMinutes: 0L);
        Simulator simulator = new Simulator(simulation, new SimulatorClock(simulation), broker, context, new LiveTraders([]))

        when: 'the simulation is ran'
        simulator.run()

        then: 'the marketEngine was notified of each minute'
        3 * broker.processUpdates()
    }
}