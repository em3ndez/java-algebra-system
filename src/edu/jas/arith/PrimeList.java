/*
 * $Id$
 */

package edu.jas.arith;


// import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.BitSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.SortedMap;


/**
 * List of big primes. Provides an Iterator for generating prime numbers.
 * Similar to ALDES/SAC2 SACPOL.PRIME list. 
 * 
 * @author Heinz Kredel See Knuth vol 2,page 390, for list of known primes. See
 *         also ALDES/SAC2 SACPOL.PRIME
 */

public final class PrimeList implements Iterable<java.math.BigInteger> {


    /**
     * Range of probable primes.
     */
    public static enum Range {
        small, low, medium, large, mersenne
    };


    /**
     * Cache the val list for different size
     */
    private volatile static List<java.math.BigInteger> SMALL_LIST = null;


    private volatile static List<java.math.BigInteger> LOW_LIST = null;


    private volatile static List<java.math.BigInteger> MEDIUM_LIST = null;


    private volatile static List<java.math.BigInteger> LARGE_LIST = null;


    private volatile static List<java.math.BigInteger> MERSENNE_LIST = null;


    /**
     * The list of probable primes in requested range.
     */
    private List<java.math.BigInteger> val = null;


    /**
     * The last prime in the list.
     */
    private java.math.BigInteger last;


    /**
     * Constructor for PrimeList.
     */
    public PrimeList() {
        this(Range.medium);
    }


    /**
     * Constructor for PrimeList.
     * 
     * @param r size range for primes.
     */
    public PrimeList(Range r) {
        // initialize with some known primes, see knuth (2,390)
        switch (r) {
        case small:
            if (SMALL_LIST != null) {
                val = SMALL_LIST;
            } else {
                val = new ArrayList<java.math.BigInteger>(50);
                addSmall();
                SMALL_LIST = val;
            }
            break;
        case low:
            if (LOW_LIST != null) {
                val = LOW_LIST;
            } else {
                val = new ArrayList<java.math.BigInteger>(50);
                addLow();
                LOW_LIST = val;
            }
            break;
        default:
        case medium:
            if (MEDIUM_LIST != null) {
                val = MEDIUM_LIST;
            } else {
                val = new ArrayList<java.math.BigInteger>(50);
                addMedium();
                MEDIUM_LIST = val;
            }
            break;
        case large:
            if (LARGE_LIST != null) {
                val = LARGE_LIST;
            } else {
                val = new ArrayList<java.math.BigInteger>(50);
                addLarge();
                LARGE_LIST = val;
            }
            break;
        case mersenne:
            if (MERSENNE_LIST != null) {
                val = MERSENNE_LIST;
            } else {
                val = new ArrayList<java.math.BigInteger>(50);
                addMersenne();
                MERSENNE_LIST = val;
            }
            break;
        }
        last = get(size() - 1);
    }


    /**
     * Add small primes.
     */
    private void addSmall() {
        // really small
        val.add(java.math.BigInteger.valueOf(2L));
        val.add(java.math.BigInteger.valueOf(3L));
        val.add(java.math.BigInteger.valueOf(5L));
        val.add(java.math.BigInteger.valueOf(7L));
        val.add(java.math.BigInteger.valueOf(11L));
        val.add(java.math.BigInteger.valueOf(13L));
        val.add(java.math.BigInteger.valueOf(17L));
        val.add(java.math.BigInteger.valueOf(19L));
        val.add(java.math.BigInteger.valueOf(23L));
        val.add(java.math.BigInteger.valueOf(29L));
    }


    /**
     * Add low sized primes.
     */
    private void addLow() {
        // 2^15-x
        val.add(getLongPrime(15, 19));
        val.add(getLongPrime(15, 49));
        val.add(getLongPrime(15, 51));
        val.add(getLongPrime(15, 55));
        val.add(getLongPrime(15, 61));
        val.add(getLongPrime(15, 75));
        val.add(getLongPrime(15, 81));
        val.add(getLongPrime(15, 115));
        val.add(getLongPrime(15, 121));
        val.add(getLongPrime(15, 135));
        // 2^16-x
        val.add(getLongPrime(16, 15));
        val.add(getLongPrime(16, 17));
        val.add(getLongPrime(16, 39));
        val.add(getLongPrime(16, 57));
        val.add(getLongPrime(16, 87));
        val.add(getLongPrime(16, 89));
        val.add(getLongPrime(16, 99));
        val.add(getLongPrime(16, 113));
        val.add(getLongPrime(16, 117));
        val.add(getLongPrime(16, 123));
    }


    /**
     * Add medium sized primes.
     */
    private void addMedium() {
        // 2^28-x
        val.add(getLongPrime(28, 57));
        val.add(getLongPrime(28, 89));
        val.add(getLongPrime(28, 95));
        val.add(getLongPrime(28, 119));
        val.add(getLongPrime(28, 125));
        val.add(getLongPrime(28, 143));
        val.add(getLongPrime(28, 165));
        val.add(getLongPrime(28, 183));
        val.add(getLongPrime(28, 213));
        val.add(getLongPrime(28, 273));
        // 2^29-x
        val.add(getLongPrime(29, 3));
        val.add(getLongPrime(29, 33));
        val.add(getLongPrime(29, 43));
        val.add(getLongPrime(29, 63));
        val.add(getLongPrime(29, 73));
        val.add(getLongPrime(29, 75));
        val.add(getLongPrime(29, 93));
        val.add(getLongPrime(29, 99));
        val.add(getLongPrime(29, 121));
        val.add(getLongPrime(29, 133));
        // 2^32-x
        val.add(getLongPrime(32, 5));
        val.add(getLongPrime(32, 17));
        val.add(getLongPrime(32, 65));
        val.add(getLongPrime(32, 99));
        val.add(getLongPrime(32, 107));
        val.add(getLongPrime(32, 135));
        val.add(getLongPrime(32, 153));
        val.add(getLongPrime(32, 185));
        val.add(getLongPrime(32, 209));
        val.add(getLongPrime(32, 267));
    }


    /**
     * Add large sized primes.
     */
    private void addLarge() {
        // 2^59-x
        val.add(getLongPrime(59, 55));
        val.add(getLongPrime(59, 99));
        val.add(getLongPrime(59, 225));
        val.add(getLongPrime(59, 427));
        val.add(getLongPrime(59, 517));
        val.add(getLongPrime(59, 607));
        val.add(getLongPrime(59, 649));
        val.add(getLongPrime(59, 687));
        val.add(getLongPrime(59, 861));
        val.add(getLongPrime(59, 871));
        // 2^60-x
        val.add(getLongPrime(60, 93));
        val.add(getLongPrime(60, 107));
        val.add(getLongPrime(60, 173));
        val.add(getLongPrime(60, 179));
        val.add(getLongPrime(60, 257));
        val.add(getLongPrime(60, 279));
        val.add(getLongPrime(60, 369));
        val.add(getLongPrime(60, 395));
        val.add(getLongPrime(60, 399));
        val.add(getLongPrime(60, 453));
        // 2^63-x
        val.add(getLongPrime(63, 25));
        val.add(getLongPrime(63, 165));
        val.add(getLongPrime(63, 259));
        val.add(getLongPrime(63, 301));
        val.add(getLongPrime(63, 375));
        val.add(getLongPrime(63, 387));
        val.add(getLongPrime(63, 391));
        val.add(getLongPrime(63, 409));
        val.add(getLongPrime(63, 457));
        val.add(getLongPrime(63, 471));
        // 2^64-x not possible
    }


    /**
     * Add Mersenne sized primes.
     */
    private void addMersenne() {
        // 2^n-1
        val.add(getMersennePrime(2));
        val.add(getMersennePrime(3));
        val.add(getMersennePrime(5));
        val.add(getMersennePrime(7));
        val.add(getMersennePrime(13));
        val.add(getMersennePrime(17));
        val.add(getMersennePrime(19));
        val.add(getMersennePrime(31));
        val.add(getMersennePrime(61));
        val.add(getMersennePrime(89));
        val.add(getMersennePrime(107));
        val.add(getMersennePrime(127));
        val.add(getMersennePrime(521));
        val.add(getMersennePrime(607));
        val.add(getMersennePrime(1279));
        val.add(getMersennePrime(2203));
        val.add(getMersennePrime(2281));
        val.add(getMersennePrime(3217));
        val.add(getMersennePrime(4253));
        val.add(getMersennePrime(4423));
        val.add(getMersennePrime(9689));
        val.add(getMersennePrime(9941));
        val.add(getMersennePrime(11213));
        val.add(getMersennePrime(19937));
    }


    /**
     * Method to compute a prime as 2**n - m.
     * @param n power for 2.
     * @param m for 2**n - m.
     * @return 2**n - m
     */
    public static java.math.BigInteger getLongPrime(int n, int m) {
        if (n < 30) {
            return java.math.BigInteger.valueOf((1 << n) - m);
        }
        return java.math.BigInteger.ONE.shiftLeft(n).subtract(java.math.BigInteger.valueOf(m));
    }


    /**
     * Method to compute a Mersenne prime as 2**n - 1.
     * @param n power for 2.
     * @return 2**n - 1
     */
    public static java.math.BigInteger getMersennePrime(int n) {
        return java.math.BigInteger.ONE.shiftLeft(n).subtract(java.math.BigInteger.ONE);
    }


    /**
     * Check if the list contains really prime numbers.
     */
    protected boolean checkPrimes() {
        return checkPrimes(size());
    }


    /**
     * Check if the list contains really prime numbers.
     */
    protected boolean checkPrimes(int n) {
        boolean isPrime;
        int i = 0;
        for (java.math.BigInteger p : val) {
            if (i++ >= n) {
                break;
            }
            isPrime = p.isProbablePrime(63);
            if (!isPrime) {
                System.out.println("not prime = " + p);
                return false;
            }
        }
        return true;
    }


    /**
     * toString.
     */
    @Override
    public String toString() {
        return val.toString();
    }


    /**
     * size of current list.
     */
    public int size() {
        return val.size();
    }


    /**
     * get prime at index i.
     */
    public java.math.BigInteger get(int i) {
        java.math.BigInteger p;
        if (i < size()) {
            p = val.get(i);
        } else if (i == size()) {
            p = last.nextProbablePrime();
            val.add(p);
            last = p;
        } else {
            p = get(i-1);
            p = last.nextProbablePrime();
            val.add(p);
            last = p;
        }
        return p;
    }


    /** 
     * Digit prime generator. K and m are positive beta-integers.  
     * L is the list (p(1),...,p(r)) of all prime numbers p such that 
     * m le p lt m+2*K, with p(1) lt p(2) lt ... lt p(r).  
     * @param mp,kp Integers.
     */
    public static List<Integer> smallPrimes(int mp, int kp) {
        int m, k, ms;
        ms = mp;
        if (ms <= 1) { 
             ms = 1; 
        }
        m = ms;
        if (m % 2 == 0) { 
            m++; 
            kp--; 
        }
        //if (kp % 2 == 0) { 
        //    k = kp/2; 
        //} else { 
        //    k = (kp+1)/2; 
        //}
        k = kp;

        /* init */
        int h = 2*(k-1); 
        int m2 = m + h; // mp    
        BitSet p = new BitSet(k);
        p.set(0,k);
        //for (int i = 0; i < k; i++) { 
        //    p.set(i); 
        //}

        /* compute */
        int q, r, i;
        int c = 0, d = 0; 
        while (true) { 
            switch (c) {
                /* mark multiples of d for d=3 and d=6n-/+1 with d**2<=m2 */
            case 2: d += 2; c = 3; 
                    break;
            case 3: d += 4; c = 2; 
                    break;
            case 0: d = 3; c = 1; 
                    break;
            case 1: d = 5; c = 2; 
                    break;
            }
            if (d > (m2/d)) { 
                break; 
            }
            r = m % d; 
            if (r+h >= d || r == 0) {
                if (r == 0) { 
                    i = 0; 
                } else {
                    if (r % 2 == 0) { 
                        i = d-(r/2); 
                    } else { 
                        i = (d-r)/2; 
                    }
                }
                if (m <= d) { 
                    i += d; 
                }
                while ( i < k ) { 
                    p.set(i,false); 
                    i += d; 
                }
            }
        }
        /* output */
        int l = p.cardinality(); // l = 0
        //for (i=0; i<k; i++) { 
        //    if (p.get(i)) { 
        //         l++;
        //     } 
        //}
        if (ms <= 2) { 
            l++; 
        }
        //if (ms <= 1) { 
        //}
        List<Integer> po = new ArrayList<Integer>(l);
        if (l == 0) { 
            return po; 
        }
        //l = 0;
        if (ms == 1) { 
            //po.add(2); 
            //l++; 
            p.set(0,false); 
        }
        if (ms <= 2) { 
            po.add(2); 
            //l++; 
        }
        int pl = m; 
        //System.out.println("pl = " + pl + " p[0] = " + p[0]);
        //System.out.println("k-1 = " + (k-1) + " p[k-1] = " + p[k-1]);
        for (i = 0; i < k; i++) { 
            if (p.get(i)) { 
                po.add(pl); 
                //l++; 
            } 
            pl += 2;
        }
        return po;
    }


    /**
     * Integer small prime divisors.  n is a positive integer.
     *  F is a list of primes (q(1),q(2),...,q(h)), h non-negative,
     *  q(1) le q(2) le ... lt q(h), such that n is equal to m times the
     *  product of the q(i) and m is not divisible by any prime in SMPRM.
     *  Either m=1 or m gt 1,000,000.
     */
    public static SortedMap<Integer, Integer> ISPD(int NL) {
        SortedMap<Integer, Integer> F = new TreeMap<Integer, Integer>();
        List<Integer> LP;
        int QL = 0;
        int PL;
        int q = 0;
        int RL = 0;
        boolean TL;

        int ML = NL;
        LP = smallPrimes(2,500); //SMPRM;
        TL = false;
        int i = 0;
        do {
            PL = LP.get(i);
            QL = ML / PL;
            RL = ML % PL;
            if (RL == 0) {
                Integer e = F.get(PL); 
                if (e == null) {
                    e = 1;
                } else {
                    e++;
                }
                F.put(PL,e);
                ML = QL;
            } else {
                i++; 
            }
            TL = ( QL <= PL );
        } while ( !(TL || ( i >= LP.size())) );
        //System.out.println("TL = " + TL + ", ML = " + ML + ", PL = " + PL + ", QL = " + QL);
        if (TL && ( ML != 1 )) {
            F.put(ML, 1);
            ML = 1; 
        }
        F.put(0, ML); // hack
        return F;
    }


    /**
     * Integer factorization.  n is a positive integer. F is a list 
     * (q(1), q(2),...,q(h)) of the prime factors of n, q(1) le q(2) le ...
     * le q(h), with n equal to the product of the q(i).
     */
    public static SortedMap<Integer, Integer> IFACT(int NL) {
        int AL, BL, CL, J1Y, ML, MLP, PL, RL;
        SortedMap<Integer, Integer> F = new TreeMap<Integer, Integer>();
        SortedMap<Integer, Integer> FP = null;
        int SL;
        int TL;
        // search small prime factors
        F = ISPD( NL ); // , F, ML
        ML = F.remove(0); // hack
        if (ML == 1) {
            return F;
        }
        //System.out.println("F = " + F);
        // search medium prime factors
        AL = 1000;
        TL = 0;
        do {
            MLP = ML - 1;
            RL = (int)(new ModLong(new ModLongRing(ML), 3 )).power(MLP).getVal(); //(3**MLP) mod ML; 
            //SACM.MIEXP( ML, MASSTOR.LFBETA( 3 ), MLP );
            if (RL == 1) {
                FP = IFACT( MLP );
                SL = ISPT( ML, MLP, FP );
                if (SL == 1) {
                    System.out.println("ISPT: FP = " + FP);
                    F.put(ML,1); // = MASSTOR.COMP( ML, F );
                    //F = MASSTOR.INV( F );
                    return F;
                }
            }
            CL = Roots.sqrt( new BigInteger(ML) ).getVal().intValue(); //SACI.ISQRT( ML, CL, TL );
            //System.out.println("CL = " + CL + ", ML = " + ML + ", CL^2 = " + (CL*CL));
            BL = Math.max( 5000, CL / 3 );
            if (AL > BL) {
                PL = 1;
            } else {
                //System.out.println("AL = " + AL + ", BL = " + BL);
                PL = IMPDS(ML, AL, BL); //, PL, ML );
                //System.out.println("PL = " + PL);
                if (PL != 1) {
                    AL = PL;
                    F.put(PL,1); //F = MASSTOR.COMP( PL, F );
                    ML = ML / PL;
                }
            }
        } while (PL != 1);
        AL = BL;
        BL = CL;
        //ILPDS( ML, AL, BL, PL, ML );
        PL = ILPDS( ML, AL, BL );
        if (PL != 1) {
            F.put(PL,1);
            ML = ML / PL;
        }
        System.out.println("PL = " + PL + ", ML = " + ML);
        if (ML != 1) {
            F.put(ML,1);
        }
        return F;
    }


    /**
     * Integer medium prime divisor search.  n, a and b are positive
     *  integers such that a le b le n and n has no
     *  positive divisors less than a.  If n has a prime
     *  divisor in the closed interval from a to b then p is the least
     *  such prime and q=n/p.  Otherwise p=1 and q=n.
     */
    public static int IMPDS(int NL, int AL, int BL) { 
        List<Integer> LP, UZ210;
        int R;
        int J1Y;
        int PLP;
        int RL1;
        int RL2;
        int RL;
        int PL, QL;

        RL = AL % 210;
        UZ210 = getUZ210();
        LP = UZ210;
        int ll = LP.size();
        int i = 0;
        while (RL > LP.get(i)) {
            i++; //LP = MASSTOR.RED( LP );
        }
        RL1 = LP.get(i); //MASSTOR.FIRSTi( LP );
        //J1Y = (RL1 - RL);
        PL = AL + (RL1 - RL); //SACI.ISUM( AL, MASSTOR.LFBETA( J1Y ) );
        //System.out.println("PL = " + PL + ", BL = " + BL);
        while (PL <= BL) {
            R = NL % PL; //SACI.IQR( NL, ((jas.maskern.MASSTOR_Cell)PL.val), QL, R );
            if (R == 0) {
                return PL;
            }
            i++; //LP = MASSTOR.RED( LP );
            if (i >= ll) { //LP == MASSTOR.SIL )
                LP = UZ210;
                RL2 = (RL1 - 210);
                i = 0;
            } else {
                RL2 = RL1;
            }
            RL1 = LP.get(i); //MASSTOR.FIRSTi( LP );
            J1Y = (RL1 - RL2);
            PL = PL + J1Y; //SACI.ISUM( ((jas.maskern.MASSTOR_Cell)PL.val), MASSTOR.LFBETA( J1Y ) );
        }
        PL = 1; //SACI.IONE;
        //QL = NL;
        return PL;
    }


    /**
     * Integer selfridge primality test.  m is an integer greater than or
     *  equal to 3.  mp=m-1.  F is a list (q(1),q(2),...,q(k)),
     *  q(1) le q(2) le ... le q(k), of the prime factors of mp, with
     *  mp equal to the product of the q(i). An attempt is made to find a 
     *  root of unity modulo m of order m-1.  If the existence of such a root 
     *  is discovered then m is prime and s=1.  If it is discovered that no such
     *  root exists then m is not a prime and s=-1.  Otherwise the primality
     *  of m remains uncertain and s=0.
     */
    public static int ISPT(int ML, int MLP, SortedMap<Integer, Integer> F) {
        int AL, BL, QL, QL1, MLPP;
        int PL1;
        int PL;
        int SL;
        List<Integer> SMPRM = smallPrimes(2,500); //SMPRM;
        List<Integer> PP;

        List<Map.Entry<Integer, Integer>> FP = new ArrayList<Map.Entry<Integer, Integer>>(F.entrySet());
        QL1 = 1; //SACI.IONE;
        PL1 = 1;
        int i = 0;
        while (true) {
                do {
                    if (i == FP.size()) { //FP == MASSTOR.SIL
                        System.out.println("SL=1: ML = " + ML + ", MLP = " + MLP);
                        SL = 1;
                        return SL;
                    }
                    QL = FP.get(i).getKey(); 
                    i++;    //FP = MASSTOR.RED( FP );
                } while ( !(QL > QL1) );
                QL1 = QL;
                PP = SMPRM;
                int j = 0;
                do {
                    if (j == PP.size()) {
                        System.out.println("SL=0: ML = " + ML + ", MLP = " + MLP);
                        SL = 0;
                        return SL;
                    }
                    PL = PP.get(j); // MASSTOR.FIRSTi( PP );
                    j++; //PP = MASSTOR.RED( PP );
                    if (PL > PL1) {
                        PL1 = PL;
                        //AL = SACM.MIEXP( ML, MASSTOR.LFBETA( PL ), MLP );
                        AL = (int)(new ModLong(new ModLongRing(ML), PL )).power(MLP).getVal(); //(PL**MLP) mod ML; 
                        if (AL != 1) {
                            System.out.println("SL=-1: ML = " + ML + ", PL = " + PL + ", MLP = " + MLP + ", AL = " + AL);
                            SL = (-1);
                            return SL;
                        }
                    }
                    MLPP = MLP / QL; //SACI.IQ( MLP, QL );
                    //BL = SACM.MIEXP( ML, MASSTOR.LFBETA( PL ), MLPP );
                    BL = (int)(new ModLong(new ModLongRing(ML), PL )).power(MLPP).getVal(); //(PL**MLPP) mod ML; 
                } while (BL == 1); // !(!SACLIST.EQUAL( BL, SACI.IONE )));
        }
    }


    /**
     * Integer large prime divisor search.  n is a positive integer with
     * no prime divisors less than 17.  1 le a le b le n.  A search is made
     * for a divisor p of the integer n, with a le p le b.  If such a p
     * is found then np=n/p, otherwise p=1 and np=n.  A modular version
     * of fermats method is used, and the search goes from a to b.
     */
    public static int ILPDS(int NL, int AL, int BL) { // return PL, NLP ignored
        int RL, J2Y, XL1, XL2, QL, XL, YL, YLP;
        List<ModLong> L = null;
        List<ModLong> LP;
        int RL1, RL2, J1Y, r, PL, TL;
        int ML = 0;
        int SL = 0;
        //SACI.IQR( NL, BL, QL, RL );
        QL = NL / BL;
        RL = NL % BL;
        XL1 = BL + QL;
        //SACI.IDQR( XL1, 2, XL1, _SL );
        SL  = XL1 % 2;
        XL1 = XL1 / 2; // after SL
        if ( (RL != 0) || ( SL != 0 )) {
            XL1 = XL1 + 1; 
        }
        QL  = NL / AL; 
        XL2 = AL + QL; 
        XL2 = XL2 / 2; 
        L = FRESL(NL); //FRESL( NL, ML, L ); // ML not returned
        if (L.isEmpty()) {
            return NL;
        }
        ML = L.get(0).ring.getModul().intValue(); // sic
        // sort: L = SACSET.LBIBMS( L ); revert: L = MASSTOR.INV( L );
        Collections.sort(L);
        Collections.reverse(L);
        System.out.println("FRESL: " + L);
        r = XL2 % ML; 
        LP = L;
        int i = 0;
        while ( i < LP.size() && r < LP.get(i).getSymmetricVal() ) {
	    i++; //LP = MASSTOR.RED( LP );
        }
        if (i == LP.size()) {
            i = 0; //LP = L;
            SL = ML;
        } else {
            SL = 0;
        }
        RL1 = (int)LP.get(i).getSymmetricVal(); //MASSTOR.FIRSTi( LP );
        i++;             //LP = MASSTOR.RED( LP );
        SL = ((SL + r) - RL1);
        XL = XL2 - SL;   
        TL = 0;
        while ( XL >= XL1 ) {
            J2Y = XL * XL;  
            YLP = J2Y - NL; 
            YL = Roots.sqrt( new BigInteger(YLP) ).getVal().intValue(); // SACI.ISQRT( YLP, YL, TL );
	    TL = YLP - YL*YL;
            if (TL == 0) {
                PL = XL - YL; 
                //NLP.val = SACI.ISUM( XL, YL );
                return PL;
            }
            if ( i < LP.size() ) {
		RL2 = (int)LP.get(i).getSymmetricVal(); //MASSTOR.FIRSTi( LP );
                i++; //LP = MASSTOR.RED( LP );
                SL = (RL1 - RL2);
            } else {
                i = 0;
                RL2 = (int)LP.get(i).getSymmetricVal(); //MASSTOR.FIRSTi( L );
                i++; //LP = MASSTOR.RED( L );
                J1Y = (ML + RL1);
                SL = (J1Y - RL2);
            }
            RL1 = RL2;
            XL = XL - SL;
        }
        PL = 1; //SACI.IONE;
        // unused NLP = NL;
        return PL;
    }


    /**
     * Fermat residue list, single modulus.  m is a positive beta-integer.
     * a belongs to Z(m).  L is a list of the distinct b in Z(m) such
     * that b**2-a is a square in Z(m).
     */
    public static List<ModLong> FRLSM(int ML, int AL) {
        List<ModLong> Lp;
        SortedSet<ModLong> L;
        List<ModLong> S;
        List<ModLong> SP;
        int IL, JL, MLP;
        ModLong SL, SLP, SLPP;

        ModLongRing ring = new ModLongRing(ML);
        ModLong a = ring.fromInteger(AL); 
        MLP = ML / 2;
        S = new ArrayList<ModLong>();
        for (IL = 0; IL <= MLP; IL += 1) {
	    SL = ring.fromInteger(IL); 
            SL = SL.multiply(SL); //SACM.MDPROD( ML, IL, IL );
            S.add(SL); //S = MASSTOR.COMPi( SL, S );
        }
        L = new TreeSet<ModLong>();
        SP = S;
        for (IL = MLP; IL >= 0; IL -= 1) {
	    SL = SP.get(IL); //MASSTOR.FIRSTi( SP );
            // IL -= 1: SP = MASSTOR.RED( SP );
            SLP = SL.subtract(a); //SACM.MDDIF( ML, SL, AL );
            JL = S.indexOf(SLP);  //SACLIST.LSRCH( SLP, S );
            if ( JL >= 0 ) { // != 0
                SLP = ring.fromInteger(IL);
                L.add( SLP ); // = MASSTOR.COMPi( IL, L );
                SLPP = SLP.negate(); //SACM.MDNEG( ML, IL );
                if ( !SLPP.equals(SLP) ) {
                    L.add( SLPP ); // = MASSTOR.COMPi( ILP, L );
                }
             }
        }
        Lp = new ArrayList<ModLong>(L);
        return Lp;
    }


    /**
     * Fermat residue list.  n is a positive integer with no prime divisors
     * less than 17.  m is a positive beta-integer and L is an ordered list
     * of the elements of Z(m) such that if x**2-n is a square then x is
     * congruent to a (modulo m) for some a in L.
     */
    public static List<ModLong> FRESL(int NL) {
        List<ModLong> L, L1;
        List<Integer> H, M;
        int AL1, AL2, AL3, AL4, BL1, HL, J1Y, J2Y, KL, KL1, ML1;
        int ML;
        int BETA = (new BigInteger(2)).power(29).getVal().intValue() - 3;

        // modulus 2**5.
        BL1 = 0;
        AL1 = NL  % 32; //SACI.IDREM( NL, 32 );
        AL2 = AL1 % 16; //MASELEM.MASREM( AL1, 16 );
        AL3 = AL2 % 8;  //MASELEM.MASREM( AL2, 8 );
        AL4 = AL3 % 4;  //MASELEM.MASREM( AL3, 4 );
        if ( AL4 == 3 ) {
            ML = 4;
            if ( AL3 == 3 ) {
                BL1 = 2;
            } else {
                BL1 = 0;
            }
        } else {
            if ( AL3 == 1 ) {
                ML = 8;
                if ( AL2 == 1 ) {
                    BL1 = 1;
                } else {
                    BL1 = 3;
                }
            } else {
                ML = 16;
                switch ((short)(AL1 / 8)) {
                case (short)0: {
                    BL1 = 3;
                    break;
                }
                case (short)1: {
                    BL1 = 7;
                    break;
                }
                case (short)2: {
                    BL1 = 5;
                    break;
                }
                case (short)3: {
                    BL1 = 1;
                    break;
                }
                }
            }
        }
        L = new ArrayList<ModLong>();
        ModLongRing ring = new ModLongRing(ML);
        ModLongRing ring2;
        if ( ML == 4 ) {
            L.add( ring.fromInteger(BL1) ); //.val = MASSTOR.LFBETA( BL1 );
        } else {
            J1Y = ML - BL1;
            L.add( ring.fromInteger(BL1) );
            L.add( ring.fromInteger(J1Y) );
            // = MASSTOR.COMPi( BL1, MASSTOR.LFBETA( J1Y ) );
        }
        KL = L.size(); //MASSTOR.LENGTH( ((jas.maskern.MASSTOR_Cell)L.val) );

        // modulus 3**3.
        AL1 = NL % 27; //SACI.IDREM( NL, 27 );
        AL2 = AL1 % 3; //MASELEM.MASREM( AL1, 3 );
        if ( AL2 == 2 ) {
            ML1 = 3;
            ring2 = new ModLongRing(ML1);
            KL1 = 1;
            L1 = new ArrayList<ModLong>();
            L1.add( ring2.fromInteger(0) ); // = MASSTOR.LFBETA( 0 );
        } else {
            ML1 = 27;
            ring2 = new ModLongRing(ML1);
            KL1 = 4;
            L1 = FRLSM( ML1, AL1 );
            // ring2 == L1.get(0).ring
        }
        //L = SACM.MDLCRA( ML, ML1, L, L1 );
        L = ModLongRing.chineseRemainder(ring.getONE(), ring2.getONE(), L, L1);
        ML = (ML * ML1);
        ring = new ModLongRing(ML); // == L.get(0).ring
        KL = (KL * KL1);

        // modulus 5**2.
        AL1 = NL % 25; //SACI.IDREM( NL, 25 );
        AL2 = AL1 % 5; //MASELEM.MASREM( AL1, 5 );
        if (( AL2 == 2 ) || ( AL2 == 3 )) {
            ML1 = 5;
            ring2 = new ModLongRing(ML1);
            J1Y = (AL2 - 1);
            J2Y = (6 - AL2);
            L1 = new ArrayList<ModLong>();
            L1.add( ring2.fromInteger(J1Y) );
            L1.add( ring2.fromInteger(J2Y) );
            //L1 = MASSTOR.COMPi( J1Y, MASSTOR.LFBETA( J2Y ) );
            KL1 = 2;
        } else {
            ML1 = 25;
            ring2 = new ModLongRing(ML1);
            L1 = FRLSM( ML1, AL1 );
            KL1 = 7;
        }
        if ( ML1 >= BETA / ML ) {
            return L;
        }
        //L = SACM.MDLCRA( ML, ML1, L, L1 );
        L = ModLongRing.chineseRemainder(ring.getONE(), ring2.getONE(), L, L1);
        ML = (ML * ML1);
        ring = new ModLongRing(ML);
        KL = (KL * KL1);

        // moduli 7,11,13.
        L1 = new ArrayList<ModLong>();
        M = new ArrayList<Integer>(3);
        H = new ArrayList<Integer>(3);
        //M = MASSTOR.COMPi( 7, MASSTOR.COMPi( 11, MASSTOR.LFBETA( 13 ) ) );
        M.add(7); M.add(11); M.add(13);
        //H = MASSTOR.COMPi( 64, MASSTOR.COMPi( 48, MASSTOR.LFBETA( 0 ) ) );
        H.add(64); H.add(48); H.add(0);
        int i = 0;
        while (true) {
            ML1 = M.get(i); //MASSTOR.FIRSTi( M );
            // later: M = MASSTOR.RED( M );
            if ( ML1 >= BETA / ML ) {
                return L;
            }
            AL1 = NL % ML1; //SACI.IDREM( NL, ML1 );
            L1 = FRLSM( ML1, AL1 );
            KL1 = L1.size(); //MASSTOR.LENGTH( L1 );
            //L = SACM.MDLCRA( ML, ML1, L, L1 );
            L = ModLongRing.chineseRemainder(ring.getONE(), ring2.getONE(), L, L1);
            ML = (ML * ML1);
            ring = new ModLongRing(ML);
            KL = (KL * KL1);
            HL = H.get(i); //MASSTOR.FIRSTi( H );
            //H = MASSTOR.RED( H );
            i++;
            if ( KL > HL ) {
                return L;
            }
        }
        // return ?
    }


    /**
     * Compute units of Z sub 210.
     */
    public static List<Integer> getUZ210() {
        List<Integer> UZ = new ArrayList<Integer>();
        java.math.BigInteger z210 = java.math.BigInteger.valueOf(210); 
        //for (int i = 209; i >= 1; i -= 2) {
        for (int i = 1; i <= 209; i += 2) {
            if (z210.gcd(java.math.BigInteger.valueOf(i)).equals(java.math.BigInteger.ONE)) {
                UZ.add(i);
            }
        }
        return UZ;
    }


    /**
     * Iterator.
     */
    public Iterator<java.math.BigInteger> iterator() {
        return new Iterator<java.math.BigInteger>() {


            int index = -1;


            public boolean hasNext() {
                return true;
            }


            public void remove() {
                throw new UnsupportedOperationException("remove not implemented");
            }


            public java.math.BigInteger next() {
                index++;
                return get(index);
            }
        };
    }

}
