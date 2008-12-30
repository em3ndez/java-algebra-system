
/*
 * $Id$
 */

package edu.jas.ufd;


import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import edu.jas.structure.Power;

import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
import edu.jas.arith.PrimeList;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.ExpVector;
import edu.jas.poly.PolyUtil;

import edu.jas.util.PowerSet;
import edu.jas.util.KsubSet;


/**
 * Rational number coefficients factorization algorithms.
 * @author Heinz Kredel
 */

public class FactorRational //<C extends GcdRingElem<C> > 
       extends FactorAbstract<BigRational> {


    private static final Logger logger = Logger.getLogger(FactorRational.class);
    private boolean debug = true || logger.isInfoEnabled();


    /**
     * GenPolynomial base factorization of a squarefree polynomial.
     * @param P squarefree and primitive! GenPolynomial<BigRational>.
     * @return [p_1,...,p_k] with P = prod_{i=1, ..., k} p_i.
     */
    public List<GenPolynomial<BigRational>> baseFactorsSquarefree(GenPolynomial<BigRational> P) {
        if ( P == null ) {
            throw new RuntimeException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<BigRational>> factors = new ArrayList<GenPolynomial<BigRational>>();
        if ( P.isZERO() ) {
            return factors;
        }
        if ( P.isONE() ) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<BigRational> pfac = P.ring;
        if ( pfac.nvar > 1 ) {
            throw new RuntimeException(this.getClass().getName()
                    + " only for univariate polynomials");
        }
        BigInteger bi = BigInteger.ONE;
        GenPolynomialRing<BigInteger> ifac = new GenPolynomialRing<BigInteger>(bi,pfac);
        GenPolynomial<BigInteger> Pi =  PolyUtil.integerFromRationalCoefficients(ifac,P);
        System.out.println("Pi = " + Pi);
        FactorInteger faci = new FactorInteger();
        List<GenPolynomial<BigInteger>> ifacts = faci.baseFactorsSquarefree(Pi);
        System.out.println("ifacts = " + ifacts);
        List<GenPolynomial<BigRational>> rfacts = PolyUtil.fromIntegerCoefficients(pfac,ifacts);
        factors.addAll( rfacts );
        return factors;
    }

}
