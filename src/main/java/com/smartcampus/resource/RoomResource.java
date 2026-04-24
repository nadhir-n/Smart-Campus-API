package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(DataStore.rooms.values())).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 400);
            err.put("error", "Bad Request");
            err.put("message", "Room ID is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 409);
            err.put("error", "Conflict");
            err.put("message", "A room with ID '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        DataStore.rooms.put(room.getId(), room);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Room created successfully.");
        resp.put("room", room);
        return Response.status(Response.Status.CREATED).entity(resp).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 404);
            err.put("error", "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        DataStore.rooms.remove(roomId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Room '" + roomId + "' has been successfully decommissioned.");
        return Response.ok(resp).build();
    }
}