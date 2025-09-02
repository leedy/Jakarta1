package config.data;

import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lotus.domino.NotesException;
import lotus.domino.Session;

@Dependent
public abstract class AbstractDominoDatabaseConfig {
	 @Inject @Named("dominoSessionAsSigner")
	    Session sessionAsSigner;

	    /** Shared helper so subclasses only supply the NSF path. */
	    protected DominoDocumentManager produceManagerFor(String filePath) {
	        return new DefaultDominoDocumentCollectionManager(
	            () -> {
	                try {
	                    return sessionAsSigner.getDatabase("", filePath);
	                } catch (NotesException e) {
	                    throw new RuntimeException(e);
	                }
	            },
	            () -> sessionAsSigner
	        );
	    }
}
