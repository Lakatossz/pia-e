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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
class EmployeeV1ControllerTest {

    @Autowired
    private MockMvc mvc;

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_employees_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/e")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/e")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_employees_unauthorized() throws Exception {
        mvc.perform(get("/e")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_create_employee_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/e/create")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_create_employee_authorized_as_user() throws Exception {
        mvc.perform(get("/e/create")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_create_employee_unauthorized() throws Exception {
        mvc.perform(get("/e/create")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_edit_employee_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/e/1/edit")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_edit_employee_authorized_as_user() throws Exception {
        mvc.perform(get("/e/1/edit")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_edit_employee_unauthorized() throws Exception {
        mvc.perform(get("/e/1/edit")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_request_subordinate_add_employee_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/e/1/subordinate/add")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_request_subordinate_add_authorized_as_user() throws Exception {
        mvc.perform(get("/e/1/subordinate/add")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_request_subordinate_add_unauthorized() throws Exception {
        mvc.perform(get("/e/1/subordinate/add")).andExpect(status().is4xxClientError());
    }
}