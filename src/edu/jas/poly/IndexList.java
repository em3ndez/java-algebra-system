/*
 * $Id$
 */

package edu.jas.poly;


import java.util.Collection;
import java.util.Random;
import java.util.Arrays;

import edu.jas.arith.BigInteger;
import edu.jas.structure.MonoidElem;
import edu.jas.structure.MonoidFactory;


/**
 * IndexList implements index lists for exterior polynomials. Index
 * lists are implemented as arrays of Java int type. Objects of this
 * class are intended to be immutable, except for the sign. If in
 * doubt use <code>valueOf</code> to get a conformant index list.
 * @author Heinz Kredel
 */

public class IndexList implements MonoidElem<IndexList> {


    /**
     * Random number generator.
     */
    private final static Random random = new Random();


    /**
     * Representation of index list as int arrays.
     */
    public final int[] val;


    /**
     * Sign of index list.
     */
    public int sign;


    /**
     * Constructor for IndexList.
     */
    public IndexList() {
        this(0, null);
    }


    /**
     * Constructor for IndexList.
     */
    public IndexList(int[] v) {
        this(1, v);
    }


    /**
     * Constructor for IndexList.
     */
    public IndexList(int s, int[] v) {
        sign = s;
        val = v;
    }


    /**
     * Get the corresponding element factory.
     * @return factory for this Element.
     * @see edu.jas.structure.Element#factory()
     */
    public MonoidFactory<IndexList> factory() {
        throw new UnsupportedOperationException("no factory implemented for IndexList");
    }


    /**
     * Is this structure finite or infinite.
     * @return true if this structure is finite, else false.
     * @see edu.jas.structure.ElemFactory#isFinite() <b>Note: </b> returns true
     *      because of finite set of values in each index.
     */
    public boolean isFinite() {
        return true;
    }


    /**
     * Value of other.
     * @param e other ExpVector.
     * @return value as IndexList.
     */
    public static IndexList valueOf(ExpVector e) {
        if (e == null) {
            return new IndexList();
        }
        int r = e.length();
        int[] w = new int[r];
        int ii = 0;
        for (int i = 0; i < r; i++) {
            long x = e.getVal(i);
            if (x <= 0l) {
                continue;
            }
            if (x > 1l) {
                return new IndexList(); // = 0
            }
            w[ii++] = i;
        }
        int[] v = Arrays.copyOf(w, ii);
        return new IndexList(v);
    }


    /**
     * Value of other.
     * @param e other Collection of Integer indexes.
     * @return value as IndexList.
     */
    public static IndexList valueOf(Collection<Integer> e) {
        if (e == null) {
            return new IndexList();
        }
        int r = e.size();
        int[] w = new int[r];
        int ii = 0;
        for (Integer x : e) {
            int xi = (int) x;
            if (xi < 0) {
                continue;
            }
            w[ii++] = xi;
        }
        int[] v = Arrays.copyOf(w, ii);
        return new IndexList(v);
    }


    /**
     * Value of other.
     * @param e other int[] of indexes, may not be conform to IndexList specification.
     * @return value as IndexList.
     */
    public static IndexList valueOf(int[] e) {
        if (e == null) {
            return new IndexList();
        }
        int r = e.length;
        IndexList w = new IndexList(new int[] {}); // = 1
        int[] v = new int[1];
        for (int i = 0; i < r; i++) {
            v[0] = e[i];
	    IndexList vs = new IndexList(v);
            w = w.exteriorProduct(vs);
            if (w.isZERO()) {
                return w;
            }
        }
        //System.out.println("valueOf: " + w);
        return w;
    }


    /**
     * Value of other.
     * @param e other IndexList, may not be conform to IndexList specification.
     * @return value as IndexList.
     */
    public static IndexList valueOf(IndexList e) {
        if (e == null) {
            return new IndexList();
        }
        return IndexList.valueOf(e.val);
    }


    /**
     * Check for IndexList conformant specification.
     * @return true if this a a conformant IndexList, else false.
     */
    public boolean isConformant() {
        if (sign == 0 && val == null) {
            return true;
        }
        IndexList ck = IndexList.valueOf(val);
        return this.abs().equals(ck.abs());
    }


    /**
     * Clone this.
     * @see java.lang.Object#clone()
     */
    @Override
    public IndexList copy() {
        return new IndexList(sign, val);
    }


    /**
     * Get the index vector.
     * @return val.
     */
    public int[] getVal() {
        return val;
    }


    /**
     * Get the index at position i.
     * @param i position.
     * @return val[i].
     */
    public int getVal(int i) {
        return val[i];
    }


    /**
     * Set the index at position i to e.
     * @param i
     * @param e
     * @return old val[i].
     */
    protected int setVal(int i, int e) {
        int v = val[i];
        val[i] = e;
        return v;
    }



    /**
     * Get the length of this index vector.
     * @return val.length.
     */
    public int length() {
        if (sign == 0) {
            return -1;
        }
        return val.length;
    }


    /**
     * Get the string representation.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (sign == 0) {
            return "0";
        }
        StringBuffer s = new StringBuffer(sign + "*(");
        for (int i = 0; i < length(); i++) {
            s.append(getVal(i));
            if (i < length() - 1) {
                s.append(",");
            }
        }
        s.append(")");
        return s.toString();
    }


    /**
     * Get a scripting compatible string representation.
     * @return script compatible representation for this Element.
     * @see edu.jas.structure.Element#toScript()
     */
    @Override
    public String toScript() {
        return toString();
    }


    /**
     * Get a scripting compatible string representation of the factory.
     * @return script compatible representation for this ElemFactory.
     * @see edu.jas.structure.Element#toScriptFactory()
     */
    @Override
    public String toScriptFactory() {
        // Python case
        return "IndexList()";
    }


    /**
     * Comparison with any other object.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object B) {
        if (!(B instanceof IndexList)) {
            return false;
        }
        IndexList b = (IndexList) B;
        int t = this.compareTo(b);
        //System.out.println("equals: this = " + this + " B = " + B + " t = " + t);
        return (0 == t);
    }


    /**
     * hashCode. Optimized for small indexs, i.e. &le; 2<sup>4</sup> and
     * small number of variables, i.e. &le; 8.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(val) + sign;
        return hash;
    }


    /**
     * Returns the number of bits in the representation of this index vector.
     * @return number of bits in the representation of this IndexList, including
     *         sign bits.
     */
    public long bitLength() {
        long blen = 2; // sign
        for (int i = 0; i < val.length; i++) {
             blen += BigInteger.bitLength(val[i]);
        }
        return blen;
    }


    /**
     * Is IndexList zero.
     * @return If this sign is 0 then true is returned, else false.
     */
    public boolean isZERO() {
        return (sign == 0);
    }


    /**
     * Is IndexList one.
     * @return If this sign != 0 and length val is zero then true returned, else false.
     */
    public boolean isONE() {
        return (sign != 0 && val.length == 0);
    }


    /**
     * Is IndexList a unit.
     * @return If this is a unit then true is returned, else false.
     */
    public boolean isUnit() {
        return isONE();
    }


    /**
     * IndexList absolute value.
     * @return abs(this).
     */
    public IndexList abs() {
        if (sign == 1) {
            return this;
        }
        return new IndexList(1, val);
    }



    /**
     * IndexList negate.
     * @return -this.
     */
    public IndexList negate() {
        if (sign == 0) {
            return this;
        }
        return new IndexList( -sign, val);
    }


    /**
     * IndexList exterior product. Also called wegde product.
     * @param V
     * @return this /\ V.
     */
    public IndexList exteriorProduct(IndexList V) {
        if (isZERO() || V.isZERO()) {
            return new IndexList(); // = 0
        }
        int s = 1;
        int m = 0, n = 0;
        int[] u = val;
        int[] v = V.val;
        int ii = 0;
        int[] w = new int[u.length + v.length];
        int i = 0, j = 0;
        while (i < u.length && j < v.length) {
            int ul = u[i];
            int vl = v[j];
            if (ul == vl) {
                return new IndexList(); // = 0
            }
            if (ul < vl) {
                w[ii++] = ul;
                i++;
                m++;
            } else {
                w[ii++] = vl;
                j++;
                n++;
                if (m % 2 != 0) {
                    s = -s;
                }
            }
        }
        if (i == u.length) {
            while (j < v.length) {
                w[ii++] = v[j++];
            }
        } else {
            m += u.length - i; // - 1;
            while (i < u.length) {
                w[ii++] = u[i++];
            }
        }
        if (m % 2 != 0 && n % 2 != 0) {
            s = -s;
        }
        //System.out.println("i = " + i + ", j = " + j + ", m = " + m + ", n = " + n);
        //System.out.println("s = " + s + ", w = " + Arrays.toString(w));
        //int[] x = Arrays.copyOf(w, ii);
        return new IndexList(s, w);
    }


    /**
     * IndexList multiply.
     * <b>Note:</b> implemented by exteriorProduct.
     * @param V
     * @return this * V.
     */
    public IndexList multiply(IndexList V) {
        return exteriorProduct(V);
    }


    /**
     * IndexList inner left product.
     * @param V
     * @return this _| V.
     */
    public IndexList innerLeftProduct(IndexList V) {
        return V.innerRightProduct(this);
    }


    /**
     * IndexList inner right product.
     * @param V
     * @return this |_ V.
     */
    public IndexList innerRightProduct(IndexList V) {
        if (! this.divides(V)) {
            return new IndexList(); // = 0
        }
        int[] u = val;
        int[] v = V.val;
        int[] w = new int[v.length - u.length];
        int ii = 0;
        int s = 1;
        int m = 0;
        for (int i = 0; i < v.length; i++) {
            int vl = v[i];
            boolean found = false;
            for (int j = 0; j < u.length; j++) {
                if (vl == u[j]) {
                    found = true;
                    break;
                }
            }
            if (! found) {
                w[ii++] = vl;
                m++;
            } else {
                if (m % 2 != 0) {
                    s = -s;
                }
            }
        }
	//int[] x = Arrays.copyOf(w, ii);
        return new IndexList(s, w);
    }


    /**
     * IndexList inverse.
     * <b>Note:</b> not implemented.
     * @return 1 / this.
     */
    public IndexList inverse() {
        throw new UnsupportedOperationException("inverse not implemented");
    }


    /**
     * IndexList divide.
     * <b>Note:</b> not implemented.
     * @param V other IndexList.
     * @return this/V. <b>Note:</b> not useful.
     */
    public IndexList divide(IndexList V) {
        throw new UnsupportedOperationException("divide not implemented");
    }


    /**
     * IndexList remainder.
     * <b>Note:</b> not implemented.
     * @param V other IndexList.
     * @return this - (this/V). <b>Note:</b> not useful.
     */
    public IndexList remainder(IndexList V) {
        throw new UnsupportedOperationException("remainder not implemented");
    }


    /**
     * Generate a random IndexList.
     * @param r length of new IndexList.
     * @return random IndexList.
     */
    public final static IndexList random(int r) {
        return random(r, 0.5f, random);
    }


    /**
     * Generate a random IndexList.
     * @param r length of new IndexList.
     * @param q density of nozero indexs.
     * @return random IndexList.
     */
    public final static IndexList random(int r, float q) {
        return random(r, q, random);
    }


    /**
     * Generate a random IndexList.
     * @param r length of new IndexList.
     * @param q density of nozero indexs.
     * @param rnd is a source for random bits.
     * @return random IndexList.
     */
    public final static IndexList random(int r, float q, Random rnd) {
        int[] w = new int[r];
        int s = 1;
        float f;
        f = rnd.nextFloat();
        if (f < q*0.001f) {
            return new IndexList(); // = 0
        }
        if (f < q*q) {
            s = -1;
        }
        int ii = 0;
        for (int i = 0; i < w.length; i++) {
            f = rnd.nextFloat();
            if (f < q) {
                w[ii++] = i;
            }
        }
        int[] v = Arrays.copyOf(w, ii);
        //System.out.println("v = " + Arrays.toString(v));
        return new IndexList(s, v);
    }


    /**
     * Generate a sequence IndexList.
     * @param s starting index.
     * @param r length of new IndexList.
     * @return sequence (s, s+1, ..., s+r-1) IndexList.
     */
    public final static IndexList sequence(int s, int r) {
        int[] w = new int[r];
        for (int i = 0; i < w.length; i++) {
             w[i] = s+i;
        }
        //System.out.println("v = " + Arrays.toString(w));
        return new IndexList(w);
    }


    /**
     * IndexList signum.
     * @return sign;
     */
    public int signum() {
        return sign;
    }


    /**
     * IndexList degree.
     * @return number of of all indexes.
     */
    public int degree() {
        if (sign == 0) {
            return -1;
        }
        return val.length;
    }


    /**
     * IndexList maximal degree.
     * @return maximal index.
     */
    public int maxDeg() {
        if (degree() < 1) {
            return -1;
        }
        return val[val.length - 1];
    }



    /**
     * IndexList minimal degree.
     * @return minimal index.
     */
    public int minDeg()  {
        if (degree() < 1) {
            return -1;
        }
        return val[0];
    }


    /**
     * IndexList divides test. Test if this is contained in V.
     * @param V
     * @return true if this divides V, else false.
     */
    public boolean divides(IndexList V) {
        if (isZERO() || V.isZERO()) {
            return false;
        }
        if (val.length > V.val.length) {
                return false;
        }
        int[] vval = V.val;
        for (int i = 0; i < val.length; i++) {
            int v = val[i];
            boolean found = false;
            for (int j = i; j < vval.length; j++) {
                if (v == vval[j]) {
                    found = true;
                    break;
                }
            }
            if (! found) {
                return false;
            }
        }
        return true;
    }


    /**
     * IndexList compareTo.
     * @param V
     * @return 0 if U == V, -1 if U &lt; V, 1 if U &gt; V.
     */
    @Override
    public int compareTo(IndexList V) {
        if (sign == 0 && V.sign == 0) {
            return 0;
        }
        if (sign == 0) {
            return -1;
        }
        if (V.sign == 0) {
            return +1;
        }
        // both not zero
        if (sign < V.sign) {
            return -1;
        }
        if (sign > V.sign) {
            return 1;
        }
        // both have same sign
        int[] vval = V.val;
        int m = Math.min(val.length, vval.length);
        for (int i = 0; i < m; i++) {
	    if (val[i] < vval[i]) {
                return -1;
            }
	    if (val[i] > vval[i]) {
                return 1;
            }
        }
        if (val.length < vval.length) {
                return -1;
        }
        if (val.length > vval.length) {
                return 1;
        }
        return 0;
    }

}
