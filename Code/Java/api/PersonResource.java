package api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


import java.util.Map;

import repository.PersonRepository;

@Path("people/{id}")                       // singular item under the collection
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {
	 @Inject
	    PersonRepository personRepository;

	    @GET
	    @Operation(summary = "Get a person by UNID", description = "Returns the person document if found, otherwise 404")
	    @APIResponse(responseCode = "200", description = "Person found")
	    @APIResponse(responseCode = "404", description = "Person not found")
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
