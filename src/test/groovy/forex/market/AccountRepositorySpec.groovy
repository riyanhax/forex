package forex.market

import forex.broker.Account
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

@SpringBootTest
class AccountRepositorySpec extends Specification {

    @SpringBootConfiguration
    @Import(PersistenceConfig.class)
    static class SpecConfig {
    }

    @Autowired
    AccountRepository repo

    def 'should be able to save and retrieve accounts'() {

        def id = '1'
        def entity = new Account(id, 50000L, '2', 200L)

        when: 'an account is saved'
        repo.save(entity)

        def retrieved = repo.findById(id).get()

        then: 'it can be retrieved'
        retrieved == entity
    }

}
