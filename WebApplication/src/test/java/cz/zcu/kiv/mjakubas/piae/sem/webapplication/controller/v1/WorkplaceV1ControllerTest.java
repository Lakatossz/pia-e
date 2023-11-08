package cz.zcu.kiv.mjakubas.piae.sem.webapplication.controller.v1;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class WorkplaceV1ControllerTest {

    @Autowired
    private MockMvc mvc;

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_workplaces_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/w")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_workplaces_authorized_as_user() throws Exception {
        mvc.perform(get("/w")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_workplaces_unauthorized() throws Exception {
        mvc.perform(get("/w")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_create_workplace_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/w/create")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_create_workplace_authorized_as_user() throws Exception {
        mvc.perform(get("/w/create")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_create_workplace_unauthorized() throws Exception {
        mvc.perform(get("/w/create")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_delete_workplace_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/w/1/delete")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_delete_workplace_authorized_as_user() throws Exception {
        mvc.perform(get("/w/1/delete")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_delete_workplace_unauthorized() throws Exception {
        mvc.perform(get("/w/1/delete")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_edit_workplace_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/w/1/edit")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_edit_workplace_authorized_as_user() throws Exception {
        mvc.perform(get("/w/1/edit")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_edit_workplace_unauthorized() throws Exception {
        mvc.perform(get("/w/1/edit")).andExpect(status().is4xxClientError());
    }
}