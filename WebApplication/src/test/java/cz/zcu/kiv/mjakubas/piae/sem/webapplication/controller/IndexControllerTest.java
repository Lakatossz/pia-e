package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller;

import cz.zcu.kiv.mjakubas.piae.sem.core.repository.IUserRepository;
import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.awt.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class IndexControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void given_auth_request_index_unauthorized() throws Exception {
        mvc.perform(get("/index")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_index_authorized() throws Exception {
        mvc.perform(get("/index")).andExpect(status().isOk());
    }
}