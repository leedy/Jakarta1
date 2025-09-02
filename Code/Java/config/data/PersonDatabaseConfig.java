package config.data;

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class PersonDatabaseConfig extends AbstractDominoDatabaseConfig {

    @Produces
    @Database(value = DatabaseType.DOCUMENT, provider = "personDocsBroke")
    public DominoDocumentManager namesDatabaseManager() {
        return produceManagerFor("demoapp\\data.nsf");
    }
}
