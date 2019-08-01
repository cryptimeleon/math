package de.upb.crypto.math.structures.cartesian;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.Arrays;

public class ProductGroupElement implements GroupElement {
    protected GroupElement[] elems;

    public ProductGroupElement(GroupElement... elems) {
        this.elems = elems;
    }

    public ProductGroupElement(Representation repr) {
        this.elems = new GroupElement[repr.list().size()];
        for (int i=0;i<repr.list().size();i++)
            this.elems[i] = elems[i].getStructure().getElement(repr.list().get(i));
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
            Arrays.stream(elems).forEachOrdered(accumulator::escapeAndAppendAndSeparate);
        }

        return accumulator;
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
