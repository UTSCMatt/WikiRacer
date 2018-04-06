package kappa.wikiracer.api;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import kappa.wikiracer.runner.Runner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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

  @Before
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
        .apply(MockMvcRestDocumentation.documentationConfiguration(this.restDocumentation))
        .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()))).build();
  }

  @Test
  public void login() throws Exception {
    this.mockMvc.perform(post("/login/").contentType(MediaType.APPLICATION_JSON).param("username", "test").param("password", "test")).andExpect(status().isOk())
        .andDo(document("login"));
  }

}
