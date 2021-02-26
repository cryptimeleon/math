package org.cryptimeleon.math.structures.rings.cartesian;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class ProductRingElement implements RingElement {
    protected RingElement[] elems;

    public ProductRingElement(List<? extends RingElement> elems) {
        //this.elems = elems.toArray(GroupElement[]::new); //Java 11
        this.elems = new RingElement[elems.size()];
        for (int i = 0; i < this.elems.length; i++) {
            this.elems[i] = elems.get(i);
        }
    }

    public ProductRingElement(RingElement... elems) {
        this.elems = elems;
    }

    public ProductRingElement(Representation repr) {
        this.elems = new RingElement[repr.list().size()];
        for (int i=0;i<repr.list().size();i++)
            this.elems[i] = elems[i].getStructure().restoreElement(repr.list().get(i));
    }

    @Override
    public ProductRing getStructure() {
        return new ProductRing(Arrays.stream(elems).map(RingElement::getStructure).toArray(Ring[]::new));
    }

    @Override
    public ProductRingElement add(Element e) {
        RingElement[] result = new RingElement[elems.length];

        for (int i=0;i<result.length;i++)
            result[i] = elems[i].add(((ProductRingElement) e).elems[i]);

        return new ProductRingElement(result);
    }

    @Override
    public ProductRingElement neg() {
        RingElement[] result = new RingElement[elems.length];

        for (int i=0;i<result.length;i++)
            result[i] = elems[i].neg();

        return new ProductRingElement(result);
    }

    @Override
    public ProductRingElement mul(Element e) {
        RingElement[] result = new RingElement[elems.length];

        for (int i=0;i<result.length;i++)
            result[i] = elems[i].mul(((ProductRingElement) e).elems[i]);

        return new ProductRingElement(result);
    }

    @Override
    public ProductRingElement mul(BigInteger k) {
        RingElement[] result = new RingElement[elems.length];
        for (int i=0;i<result.length;i++)
            result[i] = elems[i].mul(k);
        return new ProductRingElement(result);
    }

    @Override
    public ProductRingElement inv() {
        return new ProductRingElement(Arrays.stream(elems).map(RingElement::inv).toArray(RingElement[]::new));
    }

    @Override
    public boolean divides(RingElement e) throws UnsupportedOperationException {
        RingElement[] other = ((ProductRingElement) e).elems;
        if (other.length != elems.length)
            throw new IllegalArgumentException();

        for (int i=0;i<other.length;i++)
            if (!elems[i].divides(other[i]))
                return false;

        return true;
    }

    @Override
    public RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
        RingElement[] other = ((ProductRingElement) e).elems;

        if (other.length != elems.length)
            throw new IllegalArgumentException();

        RingElement[][] result = new RingElement[2][elems.length];
        for (int i=0;i<elems.length;i++) {
            RingElement[] subresult = elems[i].divideWithRemainder(other[i]);
            result[0][i] = subresult[0];
            result[1][i] = subresult[1];
        }

        return new RingElement[] {new ProductRingElement(result[0]), new ProductRingElement(result[1])};
    }

    @Override
    public BigInteger getRank() throws UnsupportedOperationException {
        return Arrays.stream(elems).map(RingElement::getRank).reduce(BigInteger.ZERO, BigInteger::max);
    }

    @Override
    public ProductRingElement pow(BigInteger k) {
        return new ProductRingElement(Arrays.stream(elems).map(g -> g.pow(k)).toArray(RingElement[]::new));
    }

    public RingElement get(int index) {
        return elems[index];
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        if (getStructure().getUniqueByteLength().isPresent()) { //if all rings have fixed-length byte representations, I can just concatenate all of them.
            Arrays.stream(elems).forEachOrdered(elem -> {
                elem.updateAccumulator(accumulator);
            });
        }
        else {
            Arrays.stream(elems).forEachOrdered(accumulator::escapeAndSeparate);
        }

        return accumulator;
    }

    @Override
    public Representation getRepresentation() {
        ListRepresentation repr = new ListRepresentation();
        for (RingElement elem : elems)
            repr.put(elem.getRepresentation());
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductRingElement)) return false;
        ProductRingElement that = (ProductRingElement) o;
        return Arrays.equals(elems, that.elems);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elems);
    }
}
