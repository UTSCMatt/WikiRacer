package kappa.wikiracer.api;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import kappa.wikiracer.runner.Runner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties.Session;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;


// http://www.baeldung.com/spring-rest-docs
@RunWith(SpringRunner.class)
@WebMvcTest(Api.class)
@ContextConfiguration(classes = Runner.class)
public class ApiTest {

  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(
      "build/generated-snippets");

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;
  
  private MockHttpSession session;
  
  private MockHttpSession session2;
  
  @Value("${spring.datasource.username}")
  private String dbUsername;
  @Value("${spring.datasource.url}")
  private String dbUrl;
  @Value("${spring.datasource.password}")
  private String dbPassword;
  
  private MockMultipartFile file;

  @Before
  public void setUp() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
        .apply(MockMvcRestDocumentation.documentationConfiguration(this.restDocumentation))
        .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()))).build();
    MvcResult result = this.mockMvc.perform(post("/login/").contentType(MediaType.APPLICATION_JSON).param("username", "test").param("password", "test")).andExpect(status().isOk())
        .andDo(document("login200")).andReturn();
    session = (MockHttpSession) result.getRequest().getSession();
    MvcResult result2 = this.mockMvc.perform(post("/login/").contentType(MediaType.APPLICATION_JSON).param("username", "test2").param("password", "test2")).andExpect(status().isOk())
        .andDo(document("login200")).andReturn();
    session2 = (MockHttpSession) result2.getRequest().getSession();
    File f = new File("src/main/resources/static/images/profile_placeholder.png");
    FileInputStream fi = new FileInputStream(f);
    file = new MockMultipartFile("file", f.getName(), "multipart/form-data", fi);
  }
  
  @After
  public void cleanUp() throws Exception {
    Connection c = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    PreparedStatement stmt;
    String sql = "DELETE FROM Users WHERE Username=?";
    
    stmt = c.prepareStatement(sql);
    stmt.setString(1, "autosignup");
    stmt.executeUpdate();
  }

  @Test
  public void api() throws Exception {
    this.mockMvc.perform(post("/login/").contentType(MediaType.APPLICATION_JSON).param("username", "test").param("password", "test")).andExpect(status().isOk())
        .andDo(document("login200"));
    this.mockMvc.perform(post("/signup/").contentType(MediaType.APPLICATION_JSON).param("username", "autosignup").param("password", "password")).andDo(document("signup200"));
    this.mockMvc.perform(get("/logoff/")).andDo(document("logoff200"));
    this.mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/profile/image/").file(file).session(session)).andDo(document("addImage200"));
    this.mockMvc.perform(get("/profile/{user}/image/", "test")).andDo(document("getImage200"));
    this.mockMvc.perform(delete("/api/profile/image/").session(session)).andDo(document("deleteImage200"));
    JSONObject json = new JSONObject();
    JSONArray array1 = new JSONArray();
    array1.put("Rocky Mountains");
    array1.put("Urban sprawl");
    JSONArray array2 = new JSONArray();
    array2.put("North America");
    array2.put("Provinces and territories of Canada");
    json.put("categories", array1);
    json.put("articles", array2);
    MvcResult newGameResult = this.mockMvc.perform(post("/api/game/new/").session(session)
        .param("start", "Canada").param("end", "Toronto").param("gameMode", "clicks").param("isSync", "false")
        .param("rules", json.toString())).andDo(document("startGame200")).andReturn();
    JSONObject newGame = new JSONObject(newGameResult.getResponse().getContentAsString());
    String gameId = newGame.getString("id");
    this.mockMvc.perform(post("/api/game/{gameId}/goto/", gameId).session(session).param("nextPage", "Toronto")).andDo(document("goto200"));
    this.mockMvc.perform(post("/api/game/join/").session(session2).param("gameId", gameId)).andDo(document("joinGame200"));
    this.mockMvc.perform(get("/api/gameList").param("search", "a").param("offset", "0").param("limit", "10")).andDo(document("gameList200"));
    this.mockMvc.perform(get("/api/game/{gameId}/stats/", gameId)).andDo(document("gameStats200"));
    this.mockMvc.perform(get("/api/user/{username}/game", "test").param("showNonFinished", "false").param("offset", "0").param("limit", "10")).andDo(document("userGameStats200"));
    this.mockMvc.perform(get("/api/game/{gameId}/player/{username}/path/", gameId, "test")).andDo(document("path200"));
    this.mockMvc.perform(get("/api/article/mostused").param("limit", "10")).andDo(document("top200"));
    newGameResult = this.mockMvc.perform(post("/api/game/new/").session(session)
        .param("start", "Canada").param("end", "Toronto").param("gameMode", "clicks").param("isSync", "true")
        .param("rules", json.toString())).andDo(document("startGameSync200")).andReturn();
    newGame = new JSONObject(newGameResult.getResponse().getContentAsString());
    gameId = newGame.getString("id");
    this.mockMvc.perform(post("/api/game/join/").session(session2).param("gameId", gameId));
    this.mockMvc.perform(get("/api/game/realtime/{gameId}/players/", gameId).session(session)).andDo(document("lobbyList200"));
    this.mockMvc.perform(get("/api/game/{gameId}/leave/", gameId).session(session2)).andDo(document("leave200"));
    this.mockMvc.perform(post("/api/game/realtime/{gameId}/message/", gameId).session(session).param("messageContent", "Hello world")).andDo(document("message200"));
    this.mockMvc.perform(patch("/api/game/realtime/{gameId}/start/", gameId).session(session)).andDo(document("start200"));
  }

}
