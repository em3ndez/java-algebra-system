/*
 * $Id$
 */

package edu.jas.application;

import java.util.Random;
import java.io.Reader;

import org.apache.log4j.Logger;

import edu.jas.structure.RingElem;
import edu.jas.structure.GcdRingElem;
import edu.jas.structure.RingFactory;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;

import edu.jas.ufd.GreatestCommonDivisor;
import edu.jas.ufd.GreatestCommonDivisorAbstract;
import edu.jas.ufd.GCDFactory;


/**
 * Residue ring factory based on GenPolynomial with RingFactory interface.
 * Objects of this class are immutable.
 * @author Heinz Kredel
 */
public class ResidueRing<C extends GcdRingElem<C> > 
             implements RingFactory< Residue<C> >  {

    private static final Logger logger = Logger.getLogger(ResidueRing.class);
    //private boolean debug = logger.isDebugEnabled();


    /**
     * Greatest common divisor engine for coefficient content and primitive parts.
     */
    protected final GreatestCommonDivisorAbstract<C> engine;


    /** Polynomial ideal for the reduction. 
     */
    protected final Ideal<C> ideal;


    /** Polynomial ring of the factory. 
     * Shortcut to ideal.list.ring. 
     */
    protected final GenPolynomialRing<C> ring;


    /** Indicator if this ring is a field.
     */
    protected int isField = -1; // initially unknown


    /** The constructor creates a ResidueRing object 
     * from an Ideal. 
     * @param i polynomial ideal.
     */
    public ResidueRing(Ideal<C> i) {
        ideal = i.GB(); // cheap if isGB
        ring = ideal.list.ring;
        engine = (GreatestCommonDivisorAbstract<C>)GCDFactory.getImplementation( ring.coFac );
        System.out.println("rr engine = " + engine.getClass().getName());
        System.out.println("rr ring   = " + ring.getClass().getName());
        System.out.println("rr cofac  = " + ring.coFac.getClass().getName());
    }


    /** Copy Residue element c.
     * @param c
     * @return a copy of c.
     */
    public Residue<C> copy(Residue<C> c) {
        return new Residue<C>( c.ring, c.val );
    }


    /** Get the zero element.
     * @return 0 as Residue.
     */
    public Residue<C> getZERO() {
        return new Residue<C>( this, ring.getZERO() );
    }


    /**  Get the one element.
     * @return 1 as Residue.
     */
    public Residue<C> getONE() {
        Residue<C> one = new Residue<C>( this, ring.getONE() );
        if ( one.isZERO() ) {
           logger.warn("ideal is one, so all residues are 0");
        }
        return one;
    }

    
    /**
     * Query if this ring is commutative.
     * @return true if this ring is commutative, else false.
     */
    public boolean isCommutative() {
        return ring.isCommutative();
    }


    /**
     * Query if this ring is associative.
     * @return true if this ring is associative, else false.
     */
    public boolean isAssociative() {
        return ring.isAssociative();
    }


    /**
     * Query if this ring is a field.
     * @return false.
     */
    public boolean isField() {
        if ( isField > 0 ) { 
           return true;
        }
        if ( isField == 0 ) { 
           return false;
        }
        // ideal is prime ?
        return false;
    }


    /**
     * Characteristic of this ring.
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
        return ring.characteristic();
    }


    /** Get a Residue element from a BigInteger value.
     * @param a BigInteger.
     * @return a Residue.
     */
    public Residue<C> fromInteger(java.math.BigInteger a) {
        return new Residue<C>( this, ring.fromInteger(a) );
    }


    /** Get a Residue element from a long value.
     * @param a long.
     * @return a Residue.
     */
    public Residue<C> fromInteger(long a) {
        return new Residue<C>( this, ring.fromInteger(a) );
    }
    

    /** Get the String representation as RingFactory.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "ResidueRing[ " 
                + ideal.toString() + " ]";
    }


    /** Comparison with any other object.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked") // not jet working
    public boolean equals(Object b) {
        if ( ! ( b instanceof ResidueRing ) ) {
           return false;
        }
        ResidueRing<C> a = null;
        try {
            a = (ResidueRing<C>) b;
        } catch (ClassCastException e) {
        }
        if ( a == null ) {
            return false;
        }
        if ( ! ring.equals( a.ring ) ) {
            return false;
        }
        return ideal.equals( a.ideal );
    }


    /** Hash code for this residue ring.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() { 
       int h;
       h = ideal.hashCode();
       return h;
    }


    /** Residue random.
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @return a random residue element.
     */
    public Residue<C> random(int n) {
      GenPolynomial<C> x = ring.random( n ).monic();
      return new Residue<C>( this, x );
    }


    /**
     * Generate a random residum polynomial.
     * @param k bitsize of random coefficients.
     * @param l number of terms.
     * @param d maximal degree in each variable.
     * @param q density of nozero exponents.
     * @return a random residue polynomial.
     */
    public Residue<C> random(int k, int l, int d, float q) {
      GenPolynomial<C> x = ring.random(k,l,d,q).monic();
      return new Residue<C>( this, x );
    }


    /** Residue random.
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return a random residue element.
     */
    public Residue<C> random(int n, Random rnd) {
      GenPolynomial<C> x = ring.random( n, rnd ).monic();
      return new Residue<C>( this, x);
    }


    /** Parse Residue from String.
     * @param s String.
     * @return Residue from s.
     */
    public Residue<C> parse(String s) {
        GenPolynomial<C> x = ring.parse( s );
        return new Residue<C>( this, x );
    }


    /** Parse Residue from Reader.
     * @param r Reader.
     * @return next Residue from r.
     */
    public Residue<C> parse(Reader r) {
        GenPolynomial<C> x = ring.parse( r );
        return new Residue<C>( this, x );
    }

}
