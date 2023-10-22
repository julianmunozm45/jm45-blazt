package com.jmunoz.blazt.controller;

import com.jmunoz.blazt.exception.CatSurpriseException;
import com.jmunoz.blazt.model.CatFact;
import com.jmunoz.blazt.model.CatPic;
import com.jmunoz.blazt.model.CatSurprise;
import com.jmunoz.blazt.service.CatService;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatController.class)
public class CatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatService catService;

    @Test
    public void testGetFirstCat_FactIsFaster() throws Exception {
        CatSurprise fact = new CatFact("Cats are awesome!");

        when(catService.randomCatFact()).thenReturn(fact);

        // Simulating that pic service fails (or is slower)
        when(catService.randomCatPic()).thenThrow(new InterruptedException("Failed to fetch cat facts"));

        ResultActions result = mockMvc.perform(get("/cats/pic-or-fact"));

        result.andExpect(status().isOk())
                .andExpect(content().string("""
                        {"display":"Cats are awesome!","type":"fact"}"""));
    }

    @Test
    public void testGetFirstCat_PicIsFaster() throws Exception {
        CatSurprise pic = new CatPic("somePicUrl");

        // Simulating that fact service fails (or is slower)
        when(catService.randomCatFact()).thenThrow(new InterruptedException("Failed to fetch cat pics"));

        when(catService.randomCatPic()).thenReturn(pic);

        ResultActions result = mockMvc.perform(get("/cats/pic-or-fact"));

        result.andExpect(status().isOk())
                .andExpect(content().string("""
                        {"display":"somePicUrl","type":"picture"}"""));
    }

    @Test
    public void testGetFirstCat_CatSurpriseException() throws Exception {
        // Simulating that both services fail
        when(catService.randomCatFact()).thenThrow(new CatSurpriseException("Failed to fetch your cat surprise"));
        when(catService.randomCatPic()).thenThrow(new CatSurpriseException("Failed to fetch your cat surprise"));

        ResultActions result = mockMvc.perform(get("/cats/pic-or-fact"));

        String expectedJson = """
                {"status":404,"error":"Not Found",\
                "message":"Failed to fetch your cat surprise"}""";
        String actualJson = result.andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    @Test
    public void testGetFirstCat_InterruptedException() throws Exception {
        // Simulating that both services fail
        when(catService.randomCatFact()).thenThrow(new InterruptedException("Failed to fetch your cat surprise in time"));
        when(catService.randomCatPic()).thenThrow(new InterruptedException("Failed to fetch your cat surprise in time"));

        ResultActions result = mockMvc.perform(get("/cats/pic-or-fact"));

        String expectedJson = """
                {"status":500,"error":"Internal Server Error",\
                "message":"java.lang.InterruptedException: Failed to fetch your cat surprise in time"}""";
        String actualJson = result.andExpect(status().is5xxServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

}
