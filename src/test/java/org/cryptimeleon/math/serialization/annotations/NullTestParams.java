package org.cryptimeleon.math.serialization.annotations;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;

import java.util.*;

/**
 * Verify that all serialization Annotations support (de-)serialization of null values
 */
public class NullTestParams {
    public static class NullTestClass implements StandaloneRepresentable {

        private StandaloneRepresentable nullTestObject;
        private String nullTestString;
        private List<Object> nullTestList;
        private Set<Object> nullTestSet;
        private Map<Object, Object> nullTestMap;
        private Map<Object, Map<Object, Object>> nullTestMapOfMaps;
        private Map<Object, List<Object>> nullTestMapOfLists;
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
            new ReprUtil(this).deserialize(representation);
        }


        @Override
        public Representation getRepresentation() {
            return ReprUtil.serialize(this);
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
