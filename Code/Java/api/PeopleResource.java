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
}
