package api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.HEAD;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import util.Misc;
import java.util.Map;

import repository.PersonRepository;

@Path("people")                      // singular item under the collection
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {
	 @Inject
	    PersonRepository personRepository;

	 @GET
	    @Operation(summary = "Get a person by UNID", description = "Returns the person document if found, otherwise 404")
	    @APIResponse(responseCode = "200", description = "Person found")
	    @APIResponse(responseCode = "404", description = "Person not found")
	    @Path("{id}")
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
	    
	 // HEAD /people/{id} — existence check, no body (handy for clients)
	 @HEAD
	 @Operation(summary = "Check if a person exists by UNID", description = "Returns 200 if the person exists, 404 if not, 400 if the UNID is invalid")
	 @APIResponse(responseCode = "200", description = "Person exists (no body)")
	 @APIResponse(responseCode = "400", description = "Invalid UNID format")
	 @APIResponse(responseCode = "404", description = "Person not found")
	 @Path("{id}")
	 public Response headOne(@PathParam("id") String id) {
	     String normalized = util.Misc.normalizeUnid(id);
	     if (normalized == null) {
	         return Response.status(Response.Status.BAD_REQUEST).build();
	     }
	     return personRepository.findById(normalized).isPresent()
	             ? Response.ok().build()
	             : Response.status(Response.Status.NOT_FOUND).build();
	 }
}
