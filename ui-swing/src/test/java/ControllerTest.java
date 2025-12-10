import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static ui.AppConfig.DATA_PATH;

import controller.Controller;

class ControllerTest {

    private Controller controller;

    @BeforeEach
    void setup() throws Exception {
        controller = new Controller("ExampleModel"); // Initialize with ExampleModel
    }

    @Test
    void testNullScript() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> controller.runScript(null));
        assertTrue(exception.getMessage().contains("Invalid Groovy script"));
    }

    @Test
    void testEmptyScript() throws Exception {
        controller.setBindField("LL", 5);
        controller.runScript("");
    }

    @Test
    void testUninitializedArrayWithoutLL() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Field field = getModelField("KI");
            field.setAccessible(true);
            field.set(controller.getModel(), null); // Simulate uninitialized array

            controller.runScript("dummy script");
        });
        assertTrue(exception.getMessage().contains("uninitialized array but LL is not set"));
    }

    @Test
    void testInvalidLLValue() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.setBindField("LL", -1);
        });
        assertTrue(exception.getMessage().contains("LL must be greater than zero."));
    }

    @Test
    void testScriptWithUndefinedVariable() throws Exception {
        controller.setBindField("LL", 5);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.runScript("result = nonExistentVariable + 1");
        });
        assertTrue(exception.getMessage().contains("Undefined variable in script: nonExistentVariable"));
    }

    @Test
    void testScriptModifyingFields() throws Exception {
        controller.setBindField("LL", 5);
        controller.runScript("KI[0] = 100.0; KS[0] = 200.0;");
        double[] KI = (double[]) controller.getBindFieldValue("KI");
        double[] KS = (double[]) controller.getBindFieldValue("KS");
        assertEquals(100.0, KI[0]);
        assertEquals(200.0, KS[0]);
    }

    @Test
    void testRunModel() throws Exception {
        controller.setBindField("LL", 3);
        controller.setBindField("KI", new double[]{100, 0, 0});
        controller.setBindField("KS", new double[]{200, 0, 0});
        controller.setBindField("INW", new double[]{50, 0, 0});
        controller.setBindField("EKS", new double[]{30, 0, 0});
        controller.setBindField("IMP", new double[]{20, 0, 0});
        controller.setBindField("twKI", new double[]{1.1, 1.2, 1.3});
        controller.setBindField("twKS", new double[]{1.0, 1.0, 1.0});
        controller.setBindField("twINW", new double[]{1.0, 1.0, 1.0});
        controller.setBindField("twEKS", new double[]{1.0, 1.0, 1.0});
        controller.setBindField("twIMP", new double[]{1.0, 1.0, 1.0});
        controller.runModel();

        double[] PKB = (double[]) controller.getBindFieldValue("PKB");
        assertNotNull(PKB);
        assertEquals(360.0, PKB[0]);
    }

    @Test
    void testLargeSimulation() throws Exception {
        controller.setBindField("LL", 1000); // Large simulation length
        controller.setBindField("KI", new double[1000]);
        controller.setBindField("KS", new double[1000]);
        controller.setBindField("INW", new double[1000]);
        controller.setBindField("EKS", new double[1000]);
        controller.setBindField("IMP", new double[1000]);
        controller.setBindField("twKI", new double[1000]);
        controller.setBindField("twKS", new double[1000]);
        controller.setBindField("twINW", new double[1000]);
        controller.setBindField("twEKS", new double[1000]);
        controller.setBindField("twIMP", new double[1000]);

        controller.runModel();
    }

    @Test
    void testReadDataFromFile() throws Exception {
        String testFilePath = DATA_PATH + "test_data.txt";
        File testFile = new File(testFilePath);

        assertTrue(testFile.exists(), "Test data file does not exist.");

        controller.readDataFrom(testFilePath);

        assertEquals(testFilePath, controller.getCurrentDataPath());

        // Verify LL
        int LL = (int) controller.getBindFieldValue("LL");
        assertEquals(5, LL, "LL should match the number of columns after 'LATA' in the header row.");

        // Verify the arrays
        double[] KI = (double[]) controller.getBindFieldValue("KI");
        assertArrayEquals(new double[]{100.0, 110.0, 121.0, 5, 6}, KI);

        double[] KS = (double[]) controller.getBindFieldValue("KS");
        assertArrayEquals(new double[]{200.0, 220.0, 242.0, 5, 6}, KS);
    }


    @Test
    void testGetResultsAsTsv() throws Exception {
        controller.setBindField("LL", 3); // Set simulation length
        controller.setBindField("KI", new double[]{100.0, 110.0, 121.0}); // Initialize KI
        controller.setBindField("PKB", new double[]{360.0, 400.0, 440.0}); // Initialize PKB

        String result = controller.getResultsAsTsv();

        assertTrue(result.contains("LATA\t3"));
        assertTrue(result.contains("KI\t100.0\t110.0\t121.0"));
        assertTrue(result.contains("PKB\t360.0\t400.0\t440.0"));
    }

    private Field getModelField(String fieldName) throws NoSuchFieldException {
        Class<?> clazz = controller.getModel().getClass();
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field not found: " + fieldName);
    }
}
