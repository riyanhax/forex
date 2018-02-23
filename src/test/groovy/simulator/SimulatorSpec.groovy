package simulator

import spock.lang.Specification

import java.time.LocalDateTime
import java.time.Month

class SimulatorSpec extends Specification {

    def 'should run simulation from start to end time'() {

        given: 'a simulation with a start and end time'
        def start = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30)
        def end = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 37)

        Simulation simulation = new Simulation(start, end, 0L);
        Simulator simulator = new Simulator(simulation)

        def times = []

        when: 'we process each minute of a simulation'
        while (simulator.currentTime().isBefore(end)) {
            times += simulator.currentTime()

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
        given: 'a simulation with a start and end time'
        def start = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 30)
        def end = LocalDateTime.of(2017, Month.FEBRUARY, 2, 3, 32)

        Simulation simulation = new Simulation(start, end, 0L);
        Simulator simulator = new Simulator(simulation)

        when: 'the simulation is over'
        simulator.run()

        and: 'we try to go further'
        simulator.nextMinute()

        then: 'an exception is thrown'
        thrown IllegalStateException
    }

}
