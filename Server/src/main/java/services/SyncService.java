package services;

import Utils.Utils;
import com.google.gson.JsonObject;
import sync.GoogleDriveAuthentication;
import sync.GoogleSyncThread;
import sync.SyncStatus;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/Sync")
public class SyncService {

    @Path("/googleauthurl")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGoogleAuthUrl() {
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("result", GoogleDriveAuthentication.generatePermissionUrl());
        return Response.ok(jsonResult.toString())
                .build();
    }

    @Path("/status")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("result", SyncStatus.getSyncStatus());
        return Response.ok(jsonResult.toString())
                .build();
    }

    @Path("/startsync")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response startSync(@HeaderParam("g_access_token") String accessToken,
                              @HeaderParam("g_refresh_token") String refreshToken) {

        if (SyncStatus.getSyncStatus().equals("RUNNING")) {
            JsonObject jsonRes = new JsonObject();
            jsonRes.addProperty("result", "Sync already running");
            return Response.ok(jsonRes.toString())
                    .build();
        }

        try {
            GoogleSyncThread gsync = new GoogleSyncThread(accessToken, refreshToken);
            SyncStatus.setSyncStatus("STARTED");

            Thread t = new Thread(gsync);
            t.setName("SyncThread");
            t.start();

        } catch (Exception e) {
            SyncStatus.setSyncStatus("ERROR");
            e.printStackTrace();
            JsonObject err = Utils.errorMessageToJson("Check Logs and try again later");
            return Response.serverError().entity(err.toString())
                    .build();
        }
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("result", SyncStatus.getSyncStatus());
        return Response.ok(jsonResult.toString())
                .build();
    }

    @Path("/stopsync")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopSync() {
        SyncStatus.setSyncStatus("STOP");
        JsonObject jsonResult = new JsonObject();
        jsonResult.addProperty("result", SyncStatus.getSyncStatus());
        return Response.ok(jsonResult.toString())
                .build();
    }
}
