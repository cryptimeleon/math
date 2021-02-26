package org.cryptimeleon.math.structures.groups.cartesian;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class ProductGroupElement implements GroupElement {
    protected GroupElement[] elems;

    public ProductGroupElement(List<? extends GroupElement> elems) {
        //this.elems = elems.toArray(GroupElement[]::new); //Java 11
        this.elems = new GroupElement[elems.size()];
        for (int i = 0; i < this.elems.length; i++) {
            this.elems[i] = elems.get(i);
        }
    }

    public ProductGroupElement(GroupElement... elems) {
        this.elems = elems;
    }

    public ProductGroupElement(Representation repr) {
        this.elems = new GroupElement[repr.list().size()];
        for (int i=0;i<repr.list().size();i++)
            this.elems[i] = elems[i].getStructure().restoreElement(repr.list().get(i));
    }

    @Override
    public ProductGroup getStructure() {
        return new ProductGroup(Arrays.stream(elems).map(GroupElement::getStructure).toArray(Group[]::new));
    }

    @Override
    public ProductGroupElement inv() {
        return new ProductGroupElement(Arrays.stream(elems).map(GroupElement::inv).toArray(GroupElement[]::new));
    }

    @Override
    public ProductGroupElement op(Element e) throws IllegalArgumentException {
        if (!(e instanceof ProductGroupElement))
            throw new IllegalArgumentException("Illegal type");
        GroupElement[] result = new GroupElement[elems.length];
        for (int i=0;i<elems.length;i++)
            result[i] = elems[i].op(((ProductGroupElement) e).elems[i]);
        return new ProductGroupElement(result);
    }

    @Override
    public ProductGroupElement pow(BigInteger k) {
        return new ProductGroupElement(Arrays.stream(elems).map(g -> g.pow(k)).toArray(GroupElement[]::new));
    }

    @Override
    public GroupElement precomputePow(int windowSize) {
        for (GroupElement elem : elems)
            elem.precomputePow(windowSize);
        return this;
    }

    @Override
    public GroupElement compute() {
        for (GroupElement elem : elems)
            elem.compute();
        return this;
    }

    @Override
    public GroupElement computeSync() {
        for (GroupElement elem : elems)
            elem.computeSync();
        return this;
    }

    @Override
    public boolean isComputed() {
        return false;
    }

    public GroupElement get(int index) {
        return elems[index];
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        if (getStructure().getUniqueByteLength().isPresent()) { //if all groups have fixed-length byte representations, I can just concatenate all of them.
            Arrays.stream(elems).forEachOrdered(elem -> {
                elem.updateAccumulator(accumulator);
            });
        }
        else {
            Arrays.stream(elems).forEachOrdered(accumulator::escapeAndSeparate);
        }

        return accumulator;
    }

    public GroupElementVector asVector() {
        return new GroupElementVector(elems, true);
    }

    @Override
    public Representation getRepresentation() {
        ListRepresentation repr = new ListRepresentation();
        for (GroupElement elem : elems)
            repr.put(elem.getRepresentation());
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductGroupElement)) return false;
        ProductGroupElement that = (ProductGroupElement) o;
        return Arrays.equals(elems, that.elems);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elems);
    }
}
