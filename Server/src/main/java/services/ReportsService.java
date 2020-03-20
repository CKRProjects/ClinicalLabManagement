package services;

import Utils.Utils;
import com.google.api.client.json.Json;
import storage.FileSystemStorage;
import beans.Report;
import beans.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;

@Path("/reports")
public class ReportsService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllReports() {
        JsonObject allReports = FileSystemStorage.getAllReports();
        return Response.ok(allReports.toString())
                .build();
    }


    @GET
    @Path("{reportId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReport(@PathParam("reportId") String reportName) {
        Report report = FileSystemStorage.getReport(reportName);

        ObjectMapper mapper = new ObjectMapper();
        try {
            return Response.ok(mapper.writeValueAsString(report))
                    .build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            JsonObject err = Utils.errorMessageToJson(e.getMessage());
            return Response.serverError().entity(err)
                    .build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createReport(InputStream is) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(is, writer, StandardCharsets.UTF_8);
            String payload = writer.toString();

            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonObject resJson = new JsonObject();
            JsonArray reportIdArray = new JsonArray();

            JsonObject jsonPayload = new Gson().fromJson(payload, JsonObject.class);
            JsonArray tests = jsonPayload.getAsJsonArray("testNames");
            if (tests == null || tests.size() == 0) {
                JsonObject err = Utils.errorMessageToJson("Cannot create Reports with no Tests");
                return Response.serverError().entity(err)
                        .build();
            }

            Iterator<JsonElement> iterator = tests.iterator();
            while (iterator.hasNext()) {
                JsonElement element = iterator.next();
                Test test =  null;
                String testName = element.getAsString();
                try {
                     test = FileSystemStorage.getTest(testName);
                } catch (Exception e) {
                    JsonObject err = Utils.errorMessageToJson("Unable to find Test with Name - " + testName);
                    return Response.serverError().entity(err)
                            .build();
                }
                Report report = mapper.readValue(payload, Report.class);

                report.setTestName(testName);
                report.setTest(test);

                if(report.getPatientName() == null || report.getPatientName().isEmpty()){
                    JsonObject err = Utils.errorMessageToJson("Unable to create a test without Patient Name");
                    return Response.serverError().entity(err)
                            .build();
                }

                report.setCreated(new Date(System.currentTimeMillis()));
                report.setLastModified(new Date(System.currentTimeMillis()));

                String reportId = report.getPatientName() + "_" + report.getTestName() + "_" + System.currentTimeMillis();

                FileSystemStorage.storeReport(reportId, report);

                reportIdArray.add(reportId);

            }

            resJson.add("reportIds", reportIdArray);
            return Response.ok(resJson.toString())
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            JsonObject err = Utils.errorMessageToJson("Failed to create Reports, check logs and try again");
            return Response.serverError().entity(err)
                    .build();
        }
    }

    @PUT
    @Path("{reportId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateReport(@PathParam("reportId") String reportId,
                                 InputStream is) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(is, writer, StandardCharsets.UTF_8);
            String payload = writer.toString();

            ObjectMapper mapper = new ObjectMapper();
            Test test = mapper.readValue(payload, Test.class);

            Report storedReport = FileSystemStorage.getReport(reportId);
            Test storedTest = storedReport.getTest();

            storedTest.setFields(test.getFields());

            storedReport.setTest(storedTest);

            storedReport.setLastModified(new Date(System.currentTimeMillis()));

            FileSystemStorage.updateReport(reportId, storedReport);

            return Response.ok()
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            JsonObject err = Utils.errorMessageToJson("Report Updating Failed check server logs for more details");
            return Response.serverError().entity(err)
                    .build();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @POST
    @Path("{reportId}/save")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveReport(InputStream is,
                               @PathParam("reportId") String reportId) {
        if (reportId == null || reportId.isEmpty()) {
            JsonObject err = Utils.errorMessageToJson("Report Name cannot be null");
            return Response.serverError()
                    .entity(err).build();
        }
        String filePath = null;
        try {
            filePath = FileSystemStorage.saveReport(reportId, is);
            return Response.created(new URI(filePath))
                    .build();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            JsonObject err = Utils.errorMessageToJson("Report Saving Failed check server logs for more details");
            return Response.serverError().entity(err)
                    .build();
        }
    }

    @GET
    @Path("{reportId}/download")
    public Response downloadReport(@PathParam("reportId") String reportId) {
        if (reportId == null || reportId.isEmpty()) {
            JsonObject err = Utils.errorMessageToJson("Report Name cannot be null");
            return Response.serverError()
                    .entity(err).build();
        }
        try {
            InputStream is = FileSystemStorage.downloadReport(reportId);
            return Response.ok(is, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("not Found")) {
                return Response.status(404)
                        .entity("Report Not Found").build();
            }
            JsonObject err = Utils.errorMessageToJson("Server error occurred, please check logs for more details");
            return Response.serverError().entity(err)
                    .build();
        }
    }
}
