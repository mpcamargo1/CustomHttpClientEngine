package com.mpcamargo;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

public interface Resource {

    @GET
    @Path("/template")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Example getUsers(Example example);

    @POST
    @Path("/template")
    @Consumes(MediaType.APPLICATION_JSON)
    void sendUsers(Example example);
}
