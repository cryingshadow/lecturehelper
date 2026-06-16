package lecturehelper;

import org.testng.*;
import org.testng.annotations.*;

public class NameTransformerTest {

    @DataProvider
    public Object[][] decodeNameFromFileData() {
        return new Object[][] {
            {"", java.util.Optional.empty()},
            {"[Name]", java.util.Optional.of("Name")},
            {"[Thomas_Stroeder]", java.util.Optional.of("Thomas Stroeder")},
            {"[Anna_Maria_von_Musterfrau]", java.util.Optional.of("Anna Maria von Musterfrau")}
        };
    }

    @Test(dataProvider="decodeNameFromFileData")
    public void decodeNameFromFileTest(final String fileName, final java.util.Optional<String> expected) {
        Assert.assertEquals(NameTransformer.decodeNameFromFile(fileName), expected);
    }

    @DataProvider
    public Object[][] encodeNameForFileData() {
        return new Object[][] {
            {"", "[]"},
            {"Name", "[Name]"},
            {"Thomas Ströder", "[Thomas_Stroeder]"},
            {"Anna Maria von Musterfrau", "[Anna_Maria_von_Musterfrau]"}
        };
    }

    @Test(dataProvider="encodeNameForFileData")
    public void encodeNameForFileTest(final String name, final String expected) {
        Assert.assertEquals(NameTransformer.encodeNameForFile(name), expected);
    }

    @DataProvider
    public Object[][] matchesData() {
        return new Object[][] {
            {"", "", true},
            {"Name", "Name", true},
            {"Thomas Ströder", "Thomas Stroeder", true},
            {"Thomas Stroeder", "Thomas Stroeder", true},
            {"Anna Maria von Musterfrau", "Anna Maria von Musterfrau", true},
            {"Thomas Strüder", "Thomas Stroeder", false},
            {"Name", "name", false}
        };
    }

    @Test(dataProvider="matchesData")
    public void matchesTest(final String name, final String decodedNameFromFile, final boolean expected) {
        Assert.assertEquals(NameTransformer.matches(name, decodedNameFromFile), expected);
    }

}
