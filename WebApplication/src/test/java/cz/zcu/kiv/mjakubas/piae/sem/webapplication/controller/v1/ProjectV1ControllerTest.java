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
class ProjectV1ControllerTest {

    @Autowired
    private MockMvc mvc;

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_projects_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/p")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_projects_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/p")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_projects_employees_unauthorized() throws Exception {
        mvc.perform(get("/p")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_create_projects_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/p/create")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_projects_create_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/p/create")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_projects_create_employees_unauthorized() throws Exception {
        mvc.perform(get("/p/create")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_edit_projects_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/p/1/edit")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_edit_projects_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/p/1/edit")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_edit_projects_employees_unauthorized() throws Exception {
        mvc.perform(get("/p/1/edit")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "projekt")
    @Test
    public void given_auth_edit_projects_employees_authorized_as_project_manager() throws Exception {
        mvc.perform(get("/p/1/edit")).andExpect(status().isOk());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_edit_add_employee_projects_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/p/1/employees/add")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_edit_add_employee_projects_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/p/1/employees/add")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_edit_add_employee_projects_employees_unauthorized() throws Exception {
        mvc.perform(get("/p/1/employees/add")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "sekretariat", authorities = {"SEKRETARIAT"})
    @Test
    public void given_auth_edit_manage_projects_authorized_as_secretariat() throws Exception {
        mvc.perform(get("/p/1/manage")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_edit_manage_employee_projects_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/p/1/manage")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_edit_manage_employee_projects_employees_unauthorized() throws Exception {
        mvc.perform(get("/p/1/manage")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "projekt")
    @Test
    public void given_auth_edit_manage_employee_projects_employees_as_project_manager() throws Exception {
        mvc.perform(get("/p/1/manage")).andExpect(status().isOk());
    }
}