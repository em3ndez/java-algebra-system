
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
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
import edu.jas.arith.PrimeList;

import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.ExpVector;
import edu.jas.poly.PolyUtil;


/**
 * Modular factorization algorithms.
 * @author Heinz Kredel
 */

public class FactorModular //<C extends GcdRingElem<C> > 
       //extends FactorAbstract<BigInteger>
    {


    private static final Logger logger = Logger.getLogger(FactorModular.class);
    private boolean debug = logger.isInfoEnabled();


    /**
     * GenPolynomial base distinct degree factorization.
     * @param P GenPolynomial<ModInteger>.
     * @return (P).
     */
    public SortedMap<Long,GenPolynomial<ModInteger>> baseDistinctDegreeFactors(GenPolynomial<ModInteger> P) {
        if ( P == null ) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        SortedMap<Long,GenPolynomial<ModInteger>> facs = new TreeMap<Long,GenPolynomial<ModInteger>>();
        if ( P.isZERO() ) {
            return facs;
        }
        GenPolynomialRing<ModInteger> pfac = P.ring;
        if ( pfac.nvar > 1 ) {
            // baseContent not possible by return type
            throw new RuntimeException(this.getClass().getName()
                    + " only for univariate polynomials");
        }
        ModIntegerRing mr = (ModIntegerRing)pfac.coFac;
        java.math.BigInteger bi = mr.modul;
        long m = bi.longValue();
        GenPolynomial<ModInteger> one = pfac.getONE();
        GenPolynomial<ModInteger> x = pfac.univariate(0);
        GenPolynomial<ModInteger> h = x;
        GenPolynomial<ModInteger> f = P;
        GenPolynomial<ModInteger> g;
        GreatestCommonDivisor<ModInteger> engine = GCDFactory.<ModInteger>getImplementation( pfac.coFac );
        Power<GenPolynomial<ModInteger>> pow = new Power<GenPolynomial<ModInteger>>( pfac );
        long d = 0;
        while ( d+1 <= f.degree(0)/2 ) {
            d++;
            h = pow.modPower( h, m, f );
            g = engine.gcd( h.subtract(x), f );
            if ( ! g.isONE() ) {
                facs.put( d, g );
                f = f.divide(g);
            }
        }
        if ( ! f.isONE() ) {
            d = f.degree(0);
            facs.put( d, f );
        }
        return facs;
    }


    /**
     * GenPolynomial base equal degree factorization.
     * @param P GenPolynomial<ModInteger>.
     * @return (P).
     */
        public List<GenPolynomial<ModInteger>> baseEqualDegreeFactors(GenPolynomial<ModInteger> P, int deg) {
        if ( P == null ) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        List<GenPolynomial<ModInteger>> facs = new ArrayList<GenPolynomial<ModInteger>>();
        if ( P.isZERO() ) {
            return facs;
        }
        GenPolynomialRing<ModInteger> pfac = P.ring;
        if ( pfac.nvar > 1 ) {
            // baseContent not possible by return type
            throw new RuntimeException(this.getClass().getName()
                    + " only for univariate polynomials");
        }
        if ( P.degree(0) == deg ) {
            facs.add( P );
            return facs;
        }
        ModIntegerRing mr = (ModIntegerRing)pfac.coFac;
        java.math.BigInteger bi = mr.modul;
        long m = bi.longValue();
        GenPolynomial<ModInteger> one = pfac.getONE();
        GenPolynomial<ModInteger> r;
        GenPolynomial<ModInteger> h;
        GenPolynomial<ModInteger> f = P;
        GreatestCommonDivisor<ModInteger> engine = GCDFactory.<ModInteger>getImplementation( pfac.coFac );
        Power<GenPolynomial<ModInteger>> pow = new Power<GenPolynomial<ModInteger>>( pfac );
        GenPolynomial<ModInteger> g = null;
        do {
            r = pfac.random(7,deg,2*deg-1,0.5f).monic();
            System.out.println("r = " + r);
            BigInteger di = Power.<BigInteger>positivePower(new BigInteger(m),deg);
            long d = di.getVal().longValue()-1;
            h = pow.modPower( r, d/2, f );
            g = engine.gcd( h.subtract(one), f );
            System.out.println("g = " + g);
            }
        } while ( g.degree(0) == 0 || g.degree(0) == f.degree(0) );
        f = f.divide(g);
        facs.addAll( baseEqualDegreeFactors(f,deg) );
        facs.addAll( baseEqualDegreeFactors(g,deg) );
        return facs;
    }


    /**
     * GenPolynomial base factorization.
     * @param P GenPolynomial<ModInteger>.
     * @return (P).
     */
        public SortedMap<GenPolynomial<ModInteger>,Integer> baseFactors(GenPolynomial<ModInteger> P) {
        if ( P == null ) {
            throw new RuntimeException(this.getClass().getName() + " P != null");
        }
        SortedMap<GenPolynomial<ModInteger>,Integer> factors
           = new TreeMap<GenPolynomial<ModInteger>,Integer>();
        if ( P.isZERO() ) {
            return factors;
        }
        GenPolynomialRing<ModInteger> pfac = P.ring;
        if ( pfac.nvar > 1 ) {
            // baseContent not possible by return type
            throw new RuntimeException(this.getClass().getName()
                    + " only for univariate polynomials");
        }
        ModIntegerRing mr = (ModIntegerRing)pfac.coFac;
        GreatestCommonDivisorAbstract<ModInteger> engine 
         = (GreatestCommonDivisorAbstract<ModInteger>)GCDFactory.<ModInteger>getImplementation( pfac.coFac );
        SortedMap<Integer,GenPolynomial<ModInteger>> facs = engine.baseSquarefreeFactors(P);
        System.out.println("facs    = " + facs);
        for ( Integer d : facs.keySet() ) {
            GenPolynomial<ModInteger> g = facs.get( d );
            SortedMap<Long,GenPolynomial<ModInteger>> dfacs = baseDistinctDegreeFactors(g);
            for ( Long e : dfacs.keySet() ) {
                GenPolynomial<ModInteger> f = dfacs.get( e );
                factors.put( f, d );
            }
        }
        System.out.println("factors = " + factors);
        return factors;
    }

}
