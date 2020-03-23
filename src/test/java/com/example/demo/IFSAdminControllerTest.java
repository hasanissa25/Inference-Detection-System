import com.example.demo.view.IFSAdminController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
class IFSAdminControllerTest {
    private IFSAdminController controller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAddPolicyPage() throws Exception {
        String requestStr = "/addPolicy";

        this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add Input Column")));
    }

    @Test
    public void getEditPolicyPage() throws Exception {
        String requestStr = "/editPolicy";

        this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add Input Column")));
    }

    @Test
    public void deletePolicyInvalidID() throws Exception {
         String requestStr = "/editPolicy?id=-1";
         this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(content().string(containsString("Add Input Column")));
    }

    @Test
    public void deletePolicyValidID() throws Exception {
         String requestStr = "/editPolicy?id=1";
         this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(status().isOk())
    }

    @Test
    public void deletePolicyValidID() throws Exception {
         String requestStr = "/editPolicy?id=1";
         this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(status().isOk())
    }




}