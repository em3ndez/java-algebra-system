/*
 * $Id$
 */

package edu.jas.fd;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

import edu.jas.arith.BigComplex;
import edu.jas.arith.BigInteger;
import edu.jas.arith.BigRational;
import edu.jas.arith.ModInteger;
import edu.jas.arith.ModIntegerRing;
import edu.jas.arith.Product;
import edu.jas.arith.ProductRing;

import edu.jas.poly.PolyUtil;
import edu.jas.poly.PolynomialList;
import edu.jas.poly.TermOrder;
import edu.jas.poly.GenPolynomial;
import edu.jas.poly.GenPolynomialRing;
import edu.jas.poly.GenSolvablePolynomial;
import edu.jas.poly.GenSolvablePolynomialRing;
import edu.jas.poly.RecSolvablePolynomial;
import edu.jas.poly.RecSolvablePolynomialRing;
import edu.jas.poly.RelationGenerator;
import edu.jas.poly.WeylRelationsIterated;

import edu.jas.kern.ComputerThreads;
import edu.jas.ufd.PolyUfdUtil;
import edu.jas.gbmod.SolvableQuotient;
import edu.jas.gbmod.SolvableQuotientRing;
import edu.jas.gbmod.QuotSolvablePolynomial;
import edu.jas.gbmod.QuotSolvablePolynomialRing;


/**
 * FDUtil tests with JUnit.
 * @author Heinz Kredel.
 */

public class FDUtilTest extends TestCase {


    /**
     * main.
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();
        junit.textui.TestRunner.run(suite());
    }


    /**
     * Constructs a <CODE>FDUtilTest</CODE> object.
     * @param name String.
     */
    public FDUtilTest(String name) {
        super(name);
    }


    /**
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(FDUtilTest.class);
        return suite;
    }


    TermOrder to = new TermOrder(TermOrder.INVLEX);


    GenSolvablePolynomialRing<BigInteger> dfac;


    GenSolvablePolynomialRing<BigRational> rdfac;


    //GenSolvablePolynomialRing<BigRational> cfac;


    GenSolvablePolynomialRing<GenPolynomial<BigInteger>> rfac;


    GenSolvablePolynomialRing<GenPolynomial<BigRational>> rrfac;


    RecSolvablePolynomialRing<BigRational> rrfacTemp;


    //BigRational ai, bi, ci, di, ei;


    GenSolvablePolynomial<BigInteger> a, b, c, d, e, f;


    GenSolvablePolynomial<GenPolynomial<BigInteger>> ar, br, cr, dr, er, fr;


    GenSolvablePolynomial<GenPolynomial<BigRational>> arr, brr, crr, drr, err, frr;


    int rl = 4;


    int kl = 3;


    int ll = 4;


    int el = 3;


    float q = 0.35f;


    @Override
    protected void setUp() {
        a = b = c = d = e = null;
        //ai = bi = ci = di = ei = null;
        ar = br = cr = dr = er = null;
        String[] vars = new String[] { "a", "b", "c", "d" };
        dfac = new GenSolvablePolynomialRing<BigInteger>(new BigInteger(1), rl, to, vars);
        RelationGenerator<BigInteger> wl = new WeylRelationsIterated<BigInteger>();
        dfac.addRelations(wl);
        rfac = (RecSolvablePolynomialRing<BigInteger>) dfac.recursive(1);
        //cfac = (GenSolvablePolynomialRing<BigInteger>) rfac.coFac;
    }


    @Override
    protected void tearDown() {
        a = b = c = d = e = null;
        //ai = bi = ci = di = ei = null;
        ar = br = cr = dr = er = null;
        dfac = null;
        //cfac = null;
        rfac = null;
    }


    /**
     * Test base pseudo division.
     */
    public void testBasePseudoDivision() {
        String[] names = new String[] { "x" };
        dfac = new GenSolvablePolynomialRing<BigInteger>(new BigInteger(1),to,names);
        GenSolvablePolynomialRing<BigRational> rdfac;
        rdfac = new GenSolvablePolynomialRing<BigRational>(new BigRational(1),dfac);
        //System.out.println("\ndfac  = " + dfac);

        a = dfac.random(kl, 2*ll, el+17, q);
        //a = dfac.parse(" 3 x^5 + 44 ");
        //b = a;
        b = dfac.random(kl, 2*ll, el+3, q);
        //a = a.multiply(b);
        //a = a.sum(b);
        //b = dfac.parse(" 2 x^2 + 40 ");
        System.out.println("a   = " + a);
        System.out.println("b   = " + b);

        GenPolynomial<BigInteger>[] QR = PolyUtil.<BigInteger> basePseudoQuotientRemainder(a, b);
        c = (GenSolvablePolynomial<BigInteger>) QR[0];
        d = (GenSolvablePolynomial<BigInteger>) QR[1];
        System.out.println("q   = " + c);
        System.out.println("r   = " + d);

        GenSolvablePolynomial<BigInteger> n = (GenSolvablePolynomial<BigInteger>) c.multiply(b).sum(d);
        //System.out.println("n   = " + n); // + ", " + m.monic());
        //System.out.println("a   = " + a); // + ", " + a.monic());

        boolean t = PolyUtil.<BigInteger> isBasePseudoQuotientRemainder(a, b, c, d);
        assertTrue("lc^n a = q b + r: " + d, t);

        GenSolvablePolynomial<BigInteger>[] QRs = FDUtil.<BigInteger> basePseudoQuotientRemainder(a, b);
        e = QRs[0];
        f = QRs[1];
        System.out.println("");
        System.out.println("q   = " + e);
        System.out.println("r   = " + f);

        GenSolvablePolynomial<BigInteger> m = (GenSolvablePolynomial<BigInteger>) e.multiply(b).sum(f);
        System.out.println("n   = " + n); // + ", " + m.monic());
        System.out.println("m   = " + m); // + ", " + m.monic());
        System.out.println("a   = " + a); // + ", " + a.monic());

        t = PolyUtil.<BigInteger> isBasePseudoQuotientRemainder(a, b, e, f);
        assertTrue("ore(lc^n) a = q b + r: " + f, t);

        // compare with field coefficients:
        GenSolvablePolynomial<BigRational> ap, bp, cp, dp, ep, fp, qp, rp, rhs;
        ap = (GenSolvablePolynomial<BigRational>) PolyUtil.<BigRational> fromIntegerCoefficients(rdfac,a);
        bp = (GenSolvablePolynomial<BigRational>) PolyUtil.<BigRational> fromIntegerCoefficients(rdfac,b);
        cp = (GenSolvablePolynomial<BigRational>) PolyUtil.<BigRational> fromIntegerCoefficients(rdfac,c);
        dp = (GenSolvablePolynomial<BigRational>) PolyUtil.<BigRational> fromIntegerCoefficients(rdfac,d);
        ep = (GenSolvablePolynomial<BigRational>) PolyUtil.<BigRational> fromIntegerCoefficients(rdfac,e);
        fp = (GenSolvablePolynomial<BigRational>) PolyUtil.<BigRational> fromIntegerCoefficients(rdfac,f);
        //System.out.println("ap  = " + ap);
        //System.out.println("bp  = " + bp);
        //System.out.println("cp  = " + cp);
        //System.out.println("dp  = " + dp);
        //System.out.println("ep  = " + ep);
        //System.out.println("fp  = " + fp);

        qp = (GenSolvablePolynomial<BigRational>) ap.divide(bp);
        rp = (GenSolvablePolynomial<BigRational>) ap.remainder(bp);
        //System.out.println("qp  = " + qp);
        //System.out.println("rp  = " + rp);
        GenSolvablePolynomial<BigRational>[] QRr = (GenSolvablePolynomial<BigRational>[]) ap.quotientRemainder(bp);
        assertEquals("qp == QRr[0]: ", qp, QRr[0]);
        assertEquals("rp == QRr[1]: ", rp, QRr[1]);

        rhs = (GenSolvablePolynomial<BigRational>) qp.multiply(bp).sum(rp);
        //System.out.println("qp bp + rp  = " + rhs);
        assertEquals("ap == qp bp + rp: ", ap, rhs);

        assertEquals("cp == qp: ", qp.monic(), cp.monic() );
        assertEquals("dp == rp: ", rp.monic(), dp.monic() );
        // System.out.println("dp = qp: " + qp.monic().equals(dp.monic()) );
        assertEquals("ep == qp: ", ep.monic(), cp.monic() );
        assertEquals("fp == rp: ", fp.monic(), dp.monic() );
    }


    /**
     * Test recursive pseudo division.
     * @see edu.jas.ufd.PolyUfdUtilTest#testRecursivePseudoDivisionSparse
     */
    public void testRecursivePseudoDivision() {
        //String[] cnames = new String[] { "x" };
        //String[] mnames = new String[] { "t" };
        String[] names = new String[] { "t", "x", "y", "z" };
        rdfac = new GenSolvablePolynomialRing<BigRational>(new BigRational(1),to,names);
        RelationGenerator<BigRational> wl = new WeylRelationsIterated<BigRational>();
        rdfac.addRelations(wl);
        rrfacTemp = (RecSolvablePolynomialRing<BigRational>) rdfac.recursive(1);
        rrfac = rrfacTemp;
        GenSolvablePolynomialRing<BigRational> rcfac = (GenSolvablePolynomialRing<BigRational>) rrfac.coFac; 
        SolvableQuotientRing<BigRational> qfac = new SolvableQuotientRing<BigRational>(rcfac);
        //GenSolvablePolynomialRing<SolvableQuotient<BigRational>> rqfac 
        //   = new GenSolvablePolynomialRing<SolvableQuotient<BigRational>>(qfac,rrfac);
        QuotSolvablePolynomialRing<BigRational> rqfac 
           = new QuotSolvablePolynomialRing<BigRational>(qfac,rrfac);
        List<GenSolvablePolynomial<GenPolynomial<BigRational>>> rl = rrfacTemp.coeffTable.relationList();
        List<GenPolynomial<GenPolynomial<BigRational>>> rlc = PolynomialList.<GenPolynomial<BigRational>> castToList(rl);
        rqfac.polCoeff.coeffTable.addRelations(rlc);
        //System.out.println("\nrdfac  = " + rdfac.toScript());
        //System.out.println("rrfac  = " + rrfac.toScript());
        //System.out.println("rcfac  = " + rcfac.toScript());
        //System.out.println("qfac   = " + qfac.toScript());
        //System.out.println("rqfac  = " + rqfac.toScript());

        // q = q;
        kl = 2;
        ll = 2;

        arr = rrfac.random(kl, ll, el, q);
        //arr = rrfac.parse(" ( t + x + y ) z^2 + ( 2 x - 8 ) y^2 - ( 13 t^4 - 13 t^3 + t^2 + 2 t - 13 ) ");
        brr = rrfac.random(kl, ll, el, q);
        if (brr.isZERO()) { 
            brr = rrfac.parse(" ( x - 2 ) z - ( t - y^2 + y ) ");
        }
        System.out.println("FDQR: arr  = " + arr);
        System.out.println("FDQR: brr  = " + brr);

        drr = (GenSolvablePolynomial<GenPolynomial<BigRational>>) FDUtil.<BigRational> recursivePseudoQuotient(arr, brr);
        crr = (GenSolvablePolynomial<GenPolynomial<BigRational>>) FDUtil.<BigRational> recursiveSparsePseudoRemainder(arr, brr);
        System.out.println("FDQR: qr  = " + drr);
        System.out.println("FDQR: rr  = " + crr);

        GenSolvablePolynomial<GenPolynomial<BigRational>>[] QR; 
        QR = (GenSolvablePolynomial<GenPolynomial<BigRational>>[]) FDUtil.<BigRational> recursivePseudoQuotientRemainder(arr, brr);
        assertEquals("drr == QR[0]: ", drr, QR[0]);
        assertEquals("crr == QR[1]: ", crr, QR[1]);

        //boolean t = PolyUtil.<BigRational> isRecursivePseudoQuotientRemainder(arr, brr, drr, crr);
        boolean t = FDUtil.<BigRational> isRecursivePseudoQuotientRemainder(arr, brr, drr, crr);
        //System.out.println("FDQR: ore(lc^n) a == q b + r: " + t);
        assertTrue("ore(lc^n) a = q b + r: " + crr, t); // ?? 

        GenSolvablePolynomial<SolvableQuotient<BigRational>> ap, bp, cp, dp, qp, rp, rhs, 
                                                             apm, bpm, cpm, dpm, qpm, rpm, rhsm;
        ap = FDUtil.<BigRational> quotientFromIntegralCoefficients(rqfac,arr);
        bp = FDUtil.<BigRational> quotientFromIntegralCoefficients(rqfac,brr);
        cp = FDUtil.<BigRational> quotientFromIntegralCoefficients(rqfac,crr);
        dp = FDUtil.<BigRational> quotientFromIntegralCoefficients(rqfac,drr);
        apm = ap.monic();
        bpm = bp.monic();
        cpm = cp.monic();
        dpm = dp.monic();
        //System.out.println("FDQR: ap  = " + ap);
        System.out.println("FDQR: apm = " + apm);
        //System.out.println("FDQR: bp  = " + bp);
        System.out.println("FDQR: bpm = " + bpm);
        //System.out.println("FDQR: cp  = " + cp);
        System.out.println("FDQR: cpm = " + cpm);
        //System.out.println("FDQR: dp  = " + dp);
        System.out.println("FDQR: dpm = " + dpm);

        qp = (GenSolvablePolynomial<SolvableQuotient<BigRational>>) ap.divide(bp);
        rp = (GenSolvablePolynomial<SolvableQuotient<BigRational>>) ap.remainder(bp);
        qpm = qp.monic();
        rpm = rp.monic();
        //System.out.println("FDQR: qp  = " + qp);
        System.out.println("FDQR: qpm = " + qpm);
        //System.out.println("FDQR: rp  = " + rp);
        System.out.println("FDQR: rpm = " + rpm);
        rhs = (GenSolvablePolynomial<SolvableQuotient<BigRational>>) qp.multiply(bp).sum(rp);
        //for commutative divide: rhs = (GenSolvablePolynomial<SolvableQuotient<BigRational>>) bp.multiply(qp).sum(rp);
        rhsm = rhs.monic();
        //System.out.println("FDQR: qp bp + rp  = " + rhs);
        //System.out.println("FDQR: qp bp + rp  = " + rhsm);
        //System.out.println("FDQR: rpm  == cpm: " + rpm.equals(cpm) ); // to weak ??
        //System.out.println("FDQR: qpm  == dpm: " + qpm.equals(dpm) );
        //System.out.println("FDQR: rhs  == ap : " + rhs.equals(ap) );
        //System.out.println("FDQR: rhsm == apm: " + rhsm.equals(apm) );

        assertEquals("ap == qp bp + rp: ", ap, rhs);
        //assertEquals("apm == qp bp + rp,m: ", apm, rhsm);
        assertEquals("cpm == rpm: ", rpm, cpm );
        assertEquals("dpm == qpm: ", qpm, dpm ); // ??
    }

}
