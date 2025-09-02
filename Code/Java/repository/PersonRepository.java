package repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.RepositoryProvider;

import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewDocuments;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;
import java.util.List;
import model.Person;

@RepositoryProvider("personDocs")
public interface PersonRepository extends DominoRepository<Person, String> {
	
	Stream<Person> findAll();
	
	// View-backed (requires a view named "byLastName" with first sorted/categorized column = LastName)
    @ViewDocuments("byLastName")
    Stream<Person> findByLastNameInView(ViewQuery query);

    // Item-based (no view required; uses @Column("LastName") mapping)
    Stream<Person> findByLastName(String lastName);
	

}
