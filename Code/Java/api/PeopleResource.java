package api;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriBuilder;

import util.KeyGen;


import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import repository.PersonRepository;
import model.Person;

@Path("people")                               // plural
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource {
	@Inject
    PersonRepository personRepository;

    // View-backed search
    @GET
    @Path("by-lastname/view/{lastName}")
    public Response byLastNameView(@PathParam("lastName") String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "lastName is required"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        var q = ViewQuery.query().key(lastName, true); // exact match
        long t0 = System.nanoTime();
        List<Person> list;
        try (var stream = personRepository.findByLastNameInView(q)) {
            list = stream.toList();
        }
        long durationMs = (System.nanoTime() - t0) / 1_000_000;

        var out = new LinkedHashMap<String, Object>();
        out.put("method", "view");
        out.put("lastName", lastName);
        out.put("count", (long) list.size());
        out.put("durationMs", durationMs);
        out.put("results", list);

        return list.isEmpty()
                ? Response.status(Response.Status.NOT_FOUND).entity(out).type(MediaType.APPLICATION_JSON).build()
                : Response.ok(out, MediaType.APPLICATION_JSON).build();
    }

    // Item-based search (no view)
    @GET
    @Path("by-lastname/item/{lastName}")
    public Response byLastNameItem(@PathParam("lastName") String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "lastName is required"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        long t0 = System.nanoTime();
        List<Person> list;
        try (var stream = personRepository.findByLastName(lastName)) {
            list = stream.toList();
        }
        long durationMs = (System.nanoTime() - t0) / 1_000_000;

        var out = new LinkedHashMap<String, Object>();
        out.put("method", "item");
        out.put("lastName", lastName);
        out.put("count", (long) list.size());
        out.put("durationMs", durationMs);
        out.put("results", list);

        return list.isEmpty()
                ? Response.status(Response.Status.NOT_FOUND).entity(out).type(MediaType.APPLICATION_JSON).build()
                : Response.ok(out, MediaType.APPLICATION_JSON).build();
    }

    // Compare both
    @GET
    @Path("by-lastname/compare/{lastName}")
    public Response compareBoth(@PathParam("lastName") String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "lastName is required"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // View
        var q = ViewQuery.query().key(lastName, true);
        long v0 = System.nanoTime();
        long viewCount;
        try (var s = personRepository.findByLastNameInView(q)) {
            viewCount = s.count();
        }
        long viewMs = (System.nanoTime() - v0) / 1_000_000;

        // Item
        long i0 = System.nanoTime();
        long itemCount;
        try (var s = personRepository.findByLastName(lastName)) {
            itemCount = s.count();
        }
        long itemMs = (System.nanoTime() - i0) / 1_000_000;

        var out = new LinkedHashMap<String, Object>();
        out.put("lastName", lastName);
        out.put("view", Map.of("count", viewCount, "durationMs", viewMs));
        out.put("item", Map.of("count", itemCount, "durationMs", itemMs));

        return Response.ok(out, MediaType.APPLICATION_JSON).build();
    }

    // Find all (no paging)
    @GET
    @Path("all")
    public Response findAllNoPaging() {
        List<Person> list;
        try (var stream = personRepository.findAll()) {
            list = stream.toList();
        }

        var out = new LinkedHashMap<String, Object>();
        out.put("count", (long) list.size());
        out.put("results", list);

        return list.isEmpty()
                ? Response.status(Response.Status.NOT_FOUND).entity(out).type(MediaType.APPLICATION_JSON).build()
                : Response.ok(out, MediaType.APPLICATION_JSON).build();
    }

    // Find all (paged)
    @GET
    @Path("all/paged")
    public Response findAllPaged(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("50") int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(Math.min(pageSize, 200), 1);
        long offset = (long) (safePage - 1) * safeSize;

        List<Person> items;
        long fetched;
        try (var stream = personRepository.findAll()) {
            items = stream.skip(offset).limit(safeSize).toList();
            fetched = items.size();
        }

        var out = new LinkedHashMap<String, Object>();
        out.put("page", safePage);
        out.put("pageSize", safeSize);
        out.put("itemsCount", fetched);
        out.put("totalItems", -1); // not computed
        out.put("results", items);

        return items.isEmpty() && safePage == 1
                ? Response.status(Response.Status.NOT_FOUND).entity(out).type(MediaType.APPLICATION_JSON).build()
                : Response.ok(out, MediaType.APPLICATION_JSON).build();
    }
    
    
 //   @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response create(Person person, @Context UriInfo uriInfo) {
//        if (person == null) {
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity(Map.of("error", "Request body required"))
 //                   .type(MediaType.APPLICATION_JSON)
  //                  .build();
 //       }

        // Optional sanity: ignore any client-sent UNID; Domino will assign one
//        person.setUnid(null);

//        var saved = personRepository.save(person);   // Domino assigns UNID here
//        var unid  = saved.getUnid();

//        var location = uriInfo.getAbsolutePathBuilder()
//                             .path(unid)
//                              .build();

//        return Response.created(location)
//                .entity(saved)                          // return the saved Person
//                .type(MediaType.APPLICATION_JSON)
//                .build();
//    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(Person incoming, @Context UriInfo uriInfo) {
      if (incoming == null) {
        throw new BadRequestException("Missing JSON body");
      }
      if (isBlank(incoming.getFirstName()) || isBlank(incoming.getLastName()) || isBlank(incoming.getState())) {
        throw new BadRequestException("firstName, lastName, and state are required");
      }

      // If client didn't provide a key, generate a short one
      if (isBlank(incoming.getKey())) {
        incoming.setKey(KeyGen.defaultKey()); // e.g., 11-char Base64URL or 12-char hex
      }

      // Optional: ensure uniqueness of 'key' (retry a few times if you like)
      // for (int i = 0; i < 3; i++) {
      //   if (!personRepository.existsByKey(incoming.getKey())) break;
      //   incoming.setKey(KeyGen.defaultKey());
      // }

      Person saved = personRepository.save(incoming);

      // Build Location: /people/{unid}
      UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(saved.getUnid());
      return Response.created(ub.build()).entity(saved).build(); // 201 + JSON of created doc
    }
    
    @GET
    @Path("by-state/item/{state}")
    public Response byStateItem(@PathParam("state") String state) {
        if (state == null || state.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "state is required"))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        state = state.trim().toUpperCase();
        
        long t0 = System.nanoTime();
        List<Person> list;
        try (var stream = personRepository.findByState(state)) {
            list = stream.toList();
        }
        long durationMs = (System.nanoTime() - t0) / 1_000_000;

        var out = new LinkedHashMap<String, Object>();
        out.put("method", "item");
        out.put("state", state);
        out.put("count", (long) list.size());
        out.put("durationMs", durationMs);
        out.put("results", list);

        return list.isEmpty()
                ? Response.status(Response.Status.NOT_FOUND).entity(out).type(MediaType.APPLICATION_JSON).build()
                : Response.ok(out, MediaType.APPLICATION_JSON).build();
    }
    
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
      }
}
