package simulator

import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Month

class SimulatorImplSpec extends Specification {

    def clock = new SimulatorClockImpl()
    def broker = Mock(SimulatorForexBroker)

    def 'should run simulation from start to end time'() {


        given: 'a simulation with a start and end timestamp'
        def start = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30)
        def end = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 37)
        Simulation simulation = new Simulation(start, end, 0L);

        SimulatorImpl simulator = new SimulatorImpl(simulation, clock, broker)
        simulator.init()

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

        Simulation simulation = new Simulation(start, end, 0L);
        SimulatorImpl simulator = new SimulatorImpl(simulation, clock, broker)

        when: 'the simulation is over'
        simulator.run()

        and: 'we try to go further'
        simulator.nextMinute()

        then: 'an exception is thrown'
        thrown IllegalStateException
    }

    def 'should notify broker at each interval'() {

        given: 'a simulation with a start and end timestamp'
        def start = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30)
        def end = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 32)

        Simulation simulation = new Simulation(start, end, 0L);
        SimulatorImpl simulator = new SimulatorImpl(simulation, clock, broker)

        when: 'the simulation is ran'
        simulator.run()

        then: 'the marketEngine was notified of each minute'
        3 * broker.processUpdates()
    }
}
