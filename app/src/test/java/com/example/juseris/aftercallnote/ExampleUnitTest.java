package com.example.juseris.aftercallnote;

import org.junit.Test;

import java.util.ArrayList;

import static com.example.juseris.aftercallnote.UtilsPackage.fixNumber;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        ArrayList<String> actuals = new ArrayList<>();
        ArrayList<String> expecteds = new ArrayList<>();
        actuals.add(fixNumber("8-6708478965"));
        actuals.add(fixNumber("+370(60829562)"));
        actuals.add(fixNumber("+370 6 444 444 "));
        actuals.add(fixNumber("8- (670) 847 8965"));
        actuals.add(fixNumber("+3706 (670)8478965  "));
        expecteds.add("+3706708478965");
        expecteds.add("+37060829562");
        expecteds.add("+3706444444");
        expecteds.add("+3706708478965");
        expecteds.add("+37066708478965");

        for(int i = 0;i < 5;i++){
            assertEquals("failed",expecteds.get(i),actuals.get(i));
        }
    }

}