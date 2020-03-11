package de.upb.crypto.math.serialization.converter;

import de.upb.crypto.math.serialization.*;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ConverterTest {
    protected Converter converter;

    //Constants
    protected static final BigIntegerRepresentation five = new BigIntegerRepresentation(5);
    protected static final BigIntegerRepresentation twoTo100 = new BigIntegerRepresentation(BigInteger.valueOf(2).pow(100));
    protected static final StringRepresentation someString = new StringRepresentation("this is a test string");
    protected static final StringRepresentation emptyString = new StringRepresentation("");
    protected static final RepresentableRepresentation reprOfZn = new RepresentableRepresentation(new Zn(BigInteger.TEN));
    protected static final ByteArrayRepresentation bytes = new ByteArrayRepresentation(new byte[] {0,1,2,3,4,5,6,7,-13});

    private static boolean debugLog = true;

    public ConverterTest(Converter converter) {
        this.converter = converter;
    }

    protected void assertSerializationWorks(Representation repr) {
        Object serialization = converter.serialize(repr);
        debugLog("Serialized:", serialization);

        Representation deserialization = converter.deserialize(serialization);
        debugLog("Deserialized:", deserialization);
        debugLog("Original:", repr);

        Assert.assertEquals(repr, deserialization);
    }

    protected void debugLog(String explanation, Object value) {
        if (!debugLog)
            return;
        if (value == null)
            System.out.println(explanation+ " null");
        else if (value instanceof byte[])
            System.out.println(explanation + " " + Arrays.toString((byte[]) value));
        else
            System.out.println(explanation + " " + value.toString());
    }

    protected void debugLog(String str) {
        if (!debugLog)
            return;
        System.out.println(str);
    }

    @Test
    public void checkBigInt() {
        assertSerializationWorks(five);
        assertSerializationWorks(twoTo100);
    }

    @Test
    public void checkString() {
        assertSerializationWorks(someString);
        assertSerializationWorks(emptyString);
    }

    @Test
    public void checkRepresentableRepresentation() {
        assertSerializationWorks(reprOfZn);
    }

    @Test
    public void checkNull() {
        assertSerializationWorks(null);
    }

    @Test
    public void checkByteArray() {
        assertSerializationWorks(bytes);
    }

    @Test
    public void checkList() {
        ListRepresentation list = new ListRepresentation(
            bytes, emptyString, someString, null, five, twoTo100
        );
        assertSerializationWorks(list);
    }

    @Test
    public void checkObject() {
        ObjectRepresentation repr = new ObjectRepresentation();
        repr.put("bytes", bytes);
        repr.put("emptyStr", emptyString);
        repr.put("null", null);
        repr.put("someString", someString);
        repr.put("twoTo100", twoTo100);
        assertSerializationWorks(repr);
    }

    @Test
    public void checkMap() {
        MapRepresentation repr = new MapRepresentation();
        repr.put(bytes, emptyString);
        repr.put(someString, twoTo100);

        assertSerializationWorks(repr);
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Converter> getParams() {
        ArrayList<Converter> list = new ArrayList<>();
        list.add(new JSONConverter());
        list.add(new BinaryFormatConverter());
        list.add(new BinaryFormatConverter(Arrays.asList(someString.get()), Arrays.asList(Zn.class)));
        return list;
    }
}
