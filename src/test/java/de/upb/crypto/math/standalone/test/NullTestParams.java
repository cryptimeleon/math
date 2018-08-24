package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.serialization.annotations.*;

import java.util.*;

/**
 * Verify that all serialization Annotations support (de-)serialization of null values
 */
public class NullTestParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(NullTestClass.class, new NullTestClass());
    }

    public static class NullTestClass implements StandaloneRepresentable {

        @Represented
        private StandaloneRepresentable nullTestObject;
        @Represented
        private String nullTestString;
        @RepresentedList(elementRestorer = @Represented)
        private List<Object> nullTestList;
        @RepresentedSet(elementRestorer = @Represented)
        private Set<Object> nullTestSet;
        @RepresentedMap(keyRestorer = @Represented, valueRestorer = @Represented)
        private Map<Object, Object> nullTestMap;
        @RepresentedMapAndMap(keyRestorer = @Represented, valueRestorer = @RepresentedMap(keyRestorer = @Represented, valueRestorer = @Represented))
        private Map<Object, Map<Object, Object>> nullTestMapOfMaps;
        @RepresentedMapAndList(keyRestorer = @Represented, valueRestorer = @RepresentedList(elementRestorer = @Represented))
        private Map<Object, List<Object>> nullTestMapOfLists;
        @RepresentedArray(elementRestorer = @Represented)
        private Object[] nullTestArray;

        public NullTestClass() {
            nullTestObject = null;
            nullTestString = null;
            nullTestList = null;
            nullTestSet = null;
            nullTestArray = null;
            nullTestMap = null;
            nullTestMapOfMaps = null;
            nullTestMapOfLists = null;
        }

        public NullTestClass(Representation representation) {
            AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(representation, this);
        }


        @Override
        public Representation getRepresentation() {
            return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NullTestClass that = (NullTestClass) o;
            return Objects.equals(nullTestObject, that.nullTestObject) &&
                    Objects.equals(nullTestString, that.nullTestString) &&
                    Objects.equals(nullTestList, that.nullTestList) &&
                    Objects.equals(nullTestSet, that.nullTestSet) &&
                    Objects.equals(nullTestMap, that.nullTestMap) &&
                    Objects.equals(nullTestMapOfMaps, that.nullTestMapOfMaps) &&
                    Objects.equals(nullTestMapOfLists, that.nullTestMapOfLists) &&
                    Arrays.equals(nullTestArray, that.nullTestArray);
        }

        @Override
        public int hashCode() {

            int result = Objects.hash(nullTestObject, nullTestString, nullTestList, nullTestSet, nullTestMap, nullTestMapOfMaps, nullTestMapOfLists);
            result = 31 * result + Arrays.hashCode(nullTestArray);
            return result;
        }
    }
}
