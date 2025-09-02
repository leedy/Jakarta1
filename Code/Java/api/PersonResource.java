package api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import repository.PersonRepository;

@Path("people/{id}")                       // singular item under the collection
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {
	 @Inject
	    PersonRepository personRepository;

	    @GET
	    public Response getOne(@PathParam("id") String id) {
	        var personOpt = personRepository.findById(id);
	        if (personOpt.isEmpty()) {
	            return Response.status(Response.Status.NOT_FOUND)
	                    .entity(Map.of("error", "Person not found: " + id))
	                    .type(MediaType.APPLICATION_JSON)
	                    .build();
	        }
	        return Response.ok(personOpt.get(), MediaType.APPLICATION_JSON).build();
	    }
}
