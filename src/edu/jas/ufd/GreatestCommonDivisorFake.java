/*
 * $Id$
 */

package edu.jas.ufd;


import org.apache.log4j.Logger;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.PolyUtil;
import edu.jas.structure.GcdRingElem;
import edu.jas.structure.RingFactory;
import edu.jas.structure.Power;


/**
 * Greatest common divisor algorithms with gcd always 1.
 * The computation is faked as the gcd is always 1.
 * @author Heinz Kredel
 */

public class GreatestCommonDivisorFake<C extends GcdRingElem<C>> extends GreatestCommonDivisorAbstract<C> {


    private static final Logger logger = Logger.getLogger(GreatestCommonDivisorFake.class);


    private final boolean debug = logger.isDebugEnabled();


    /**
     * GenPolynomial base coefficient content.
     * Always returns 1.
     * @param P GenPolynomial.
     * @return cont(P).
     */
    @Override
    public C baseContent(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P.ring.getZEROCoefficient();
        }
        return P.ring.getONECoefficient();
    }


    /**
     * GenPolynomial base coefficient primitive part.
     * Always returns P.
     * @param P GenPolynomial.
     * @return pp(P).
     */
    @Override
    public GenPolynomial<C> basePrimitivePart(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        return P;
    }


    /**
     * Univariate GenPolynomial greatest comon divisor. 
     * Always returns 1.
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return gcd(P,S).
     */
    @Override
    public GenPolynomial<C> baseGcd(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        return P.ring.getONE();
    }


    /**
     * GenPolynomial recursive content.
     * Always returns 1.
     * @param P recursive GenPolynomial.
     * @return cont(P).
     */
    @Override
    public GenPolynomial<C> recursiveContent(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P.ring.getZEROCoefficient();
        }
        return P.ring.getONECoefficient();
    }


    /**
     * GenPolynomial recursive primitive part.
     * Always returns P.
     * @param P recursive GenPolynomial.
     * @return pp(P).
     */
    @Override
    public GenPolynomial<GenPolynomial<C>> recursivePrimitivePart(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        return P;
    }


    /**
     * Univariate GenPolynomial recursive greatest comon divisor. 
     * Always returns 1.
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return gcd(P,S).
     */
    @Override
    public GenPolynomial<GenPolynomial<C>> recursiveUnivariateGcd(GenPolynomial<GenPolynomial<C>> P,
            GenPolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        return P.ring.getONE();
    }

}
