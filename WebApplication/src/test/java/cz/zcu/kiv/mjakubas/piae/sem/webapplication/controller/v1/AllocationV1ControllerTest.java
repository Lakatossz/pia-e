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
class AllocationV1ControllerTest {

    @Autowired
    private MockMvc mvc;

    @WithMockUser(value = "projekt")
    @Test
    public void given_auth_allocations_authorized_as_project_manager() throws Exception {
        mvc.perform(get("/a/of/1/manage/for/5/add")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_projects_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/a/of/1/manage/for/5/add")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_projects_employees_unauthorized() throws Exception {
        mvc.perform(get("/a/of/1/manage/for/5/add")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "projekt")
    @Test
    public void given_auth_edit_allocations_authorized_as_project_manager() throws Exception {
        mvc.perform(get("/a/1/edit")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_edi_projects_employees_authorized_as_user() throws Exception {
        mvc.perform(get("/a/1/edit")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_edit_projects_employees_unauthorized() throws Exception {
        mvc.perform(get("/a/1/edit")).andExpect(status().is4xxClientError());
    }

    @WithMockUser(value = "projekt")
    @Test
    public void given_auth_workloads_as_project_manager() throws Exception {
        mvc.perform(get("/a/workload")).andExpect(status().isOk());
    }

    @WithMockUser(value = "prvni")
    @Test
    public void given_auth_workloads_authorized_as_user() throws Exception {
        mvc.perform(get("/a/workload")).andExpect(status().is4xxClientError());
    }

    @Test
    public void given_auth_workloads_employees_unauthorized() throws Exception {
        mvc.perform(get("/a/workload")).andExpect(status().is4xxClientError());
    }
}