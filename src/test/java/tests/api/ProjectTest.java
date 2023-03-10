package tests.api;

import baseEntities.BaseApiTest;
import com.google.gson.Gson;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.path.json.JsonPath;
import models.Project;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.Endpoints;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

public class ProjectTest extends BaseApiTest {
    private int projectId;

    @Test
    public void createProjectSuccessTest() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get("src/test/resources/createProjectData.json"));
        Project project = new Gson().fromJson(reader, Project.class);
        JsonPath responseContent = given()
                .body(project, ObjectMapperType.GSON)
                .when()
                .post(Endpoints.CREATE_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath();

        projectId = responseContent.get("id");

        Assert.assertEquals(responseContent.get("name"), project.getName());
        Assert.assertEquals(responseContent.get("announcement"), project.getAnnouncement());
        Assert.assertEquals((int) responseContent.get("suite_mode"), project.getType());
        Assert.assertEquals(responseContent.get("show_announcement"), project.isShowAnnouncement());
    }

    @Test
    public void createProjectFailureTest() {
        Project project = new Project.Builder()
                .withName(RandomStringUtils.randomAlphabetic(251))
                .build();

        JsonPath responseContent = given()
                .body(project, ObjectMapperType.GSON)
                .when()
                .post(Endpoints.CREATE_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().jsonPath();

        Assert.assertEquals(responseContent.get("error"), "Field :name is too long (250 characters at most).");
    }

    @Test(dependsOnMethods = "createProjectSuccessTest")
    public void getProjectSuccessTest() {
        given()
                .pathParam("project_id", projectId)
                .when()
                .get(Endpoints.GET_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getProjectWithNotFoundTest() {
        JsonPath responseContent = given()
                .pathParam("project_id", 0)
                .when()
                .get(Endpoints.GET_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().jsonPath();

        Assert.assertEquals(responseContent.get("error"), "Field :project_id is not a valid or accessible project.");
    }

    @Test
    public void getProjectWithInvalidIdTest() {
        JsonPath responseContent = given()
                .pathParam("project_id", "invalidId")
                .when()
                .get(Endpoints.GET_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().jsonPath();

        Assert.assertEquals(responseContent.get("error"), "Field :project_id is not a valid ID.");
    }

    @Test(dependsOnMethods = {"createProjectSuccessTest", "getProjectSuccessTest"})
    public void deleteProjectSuccessTest() {
        given()
                .pathParam("project_id", projectId)
                .when()
                .post(Endpoints.DELETE_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void deleteProjectWithNotFoundTest() {
        JsonPath responseContent = given()
                .pathParam("project_id", 0)
                .when()
                .post(Endpoints.DELETE_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().jsonPath();

        Assert.assertEquals(responseContent.get("error"), "Field :project_id is not a valid or accessible project.");
    }

    @Test
    public void defectTest() {
        given()
                .pathParam("project_id", 0)
                .when()
                .post(Endpoints.DELETE_PROJECT)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().jsonPath();
    }
}
