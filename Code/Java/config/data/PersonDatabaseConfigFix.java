package config.data;

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lotus.domino.NotesException;
import lotus.domino.Session;

@Dependent
public class PersonDatabaseConfigFix {
	@Inject @Named("dominoSessionAsSigner")
	private Session sessionAsSigner;
	
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "personDocs")
	public DominoDocumentManager personDocsManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> {
				try {
					return sessionAsSigner.getDatabase("", "demoapp\\data.nsf"); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			},
			() -> sessionAsSigner
		);
	}
	
}
