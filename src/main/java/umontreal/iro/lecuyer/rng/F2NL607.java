

/*
 * Class:        F2NL607
 * Description:  generator: combination of the WELL607 proposed by
                 Panneton with a nonlinear generator.
 * Environment:  Java
 * Software:     SSJ 
 * Copyright (C) 2001  Pierre L'Ecuyer and Université de Montréal
 * Organization: DIRO, Université de Montréal
 * @author       
 * @since

 * SSJ is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or
 * any later version.

 * SSJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * A copy of the GNU General Public License is available at
   <a href="http://www.gnu.org/licenses">GPL licence site</a>.
 */

package umontreal.iro.lecuyer.rng; 

import umontreal.iro.lecuyer.util.ArithmeticMod;
import umontreal.iro.lecuyer.util.PrintfFormat;

import java.util.Random;


/**
 * Implements the {@link RandomStream} interface by using as a backbone
 * generator the combination of the WELL607 proposed in
 * (and implemented in {@link WELL607}) with a nonlinear generator.
 * This nonlinear generator is made up of a small number of components (say <SPAN CLASS="MATH"><I>n</I></SPAN>)
 * combined via addition modulo 1. Each component contains an array already
 * filled with a ``random'' permutation of 
 * <SPAN CLASS="MATH">{0,..., <I>s</I> - 1}</SPAN> where <TT>s</TT> is the
 * size of the array.
 * These numbers and the lengths of the components can be changed by the user.
 * Each call to the generator uses the next number in each array
 * (or the first one if we are at the end of the array). By default,
 * there are 3 components of lengths 1019, 1021, and 1031, respectively.
 * The non-linear generator is combined with the WELL using a bitwise XOR operation.
 * This ensures that the new generator has at least as much equidistribution
 * as the WELL607, as shown in.
 * 
 * <P>
 * The combined generator has a period length of approximatively
 *  <SPAN CLASS="MATH">2<SUP>637</SUP></SPAN>. The values of <SPAN CLASS="MATH"><I>V</I></SPAN>, <SPAN CLASS="MATH"><I>W</I></SPAN> and <SPAN CLASS="MATH"><I>Z</I></SPAN>
 *  are <SPAN CLASS="MATH">2<SUP>250</SUP></SPAN>,
 * <SPAN CLASS="MATH">2<SUP>150</SUP></SPAN>, and <SPAN CLASS="MATH">2<SUP>400</SUP></SPAN>, respectively (see {@link RandomStream} for their
 * definition). The seed of the RNG has two part: the linear part is a
 * 19-dimensional vector of 32-bit integers, while the nonlinear part is
 * made up of a <SPAN CLASS="MATH"><I>n</I></SPAN>-dimensional vector of indices, representing the position
 * of the generator in each array of the nonlinear components.
 * 
 */
public class F2NL607  extends WELL607base {

   private static final long serialVersionUID = 70510L;
   //La date de modification a l'envers, lire 10/05/2007

   //stream variable for the WELL607 part
   private static int[] curr_stream = {0xD6AFB71C, 0x82ADB18E, 0x326E714E,
                                       0xB1EE42B6, 0xF1A834ED, 0x04AE5721,
                                       0xC5EA2843, 0xFA04116B, 0x6ACE14EF,
                                       0xCD5781A0, 0x6B1F731C, 0x7E3B8E3D,
                                       0x8B34DE2A, 0x74EC15F5, 0x84EBC216,
                                       0x83EA2C61, 0xE4A83B1E, 0xA5D82CB9,
                                       0x9E1A6C89};

   //data for the non-linear part
   private static int[][] nlData;
   private static int[] nlJumpW, nlJumpZ;
   private int[] nlState;

   //stream and substream variables for the non-linear part
   private int[] nlStream;
   private int[] nlSubstream;
   private static int[] curr_nlStream;

   //set to true when a instance of F2NL607 is constructed
   private static boolean constructed = false;

   //initialisation of the generator
   static
   {
      curr_nlStream = new int[]{ 0, 0, 0};

      nlData = new int[][]{ new int[1019],
                            new int[1021],
                            new int[1031]};

      Random rand = new Random(0);

      for(int i = 0; i < nlData.length; i++) {
         for(int j = 0; j < nlData[i].length; j++)
            nlData[i][j] = (int)((((long)j) << 32) / nlData[i].length);
         scramble(nlData[i], rand);

         /*
         for(int j = 0; j < nlData[i].length; j++)
            if(nlData[i][j] >= 0)
               System.out.print(nlData[i][j] + "U, ");
            else
               System.out.print(((long)nlData[i][j] + 0x100000000L) + "U, ");
         System.out.println(PrintfFormat.NEWLINE +
            PrintfFormat.NEWLINE + PrintfFormat.NEWLINE);
         */
      }

      //computing the jumps length
      //A FAIRE EN PRE-CALCUL
      nlJumpW = new int[3];
      nlJumpZ = new int[3];
      for(int i = 0; i < nlJumpW.length; i++) {
         int temp = 1;
         for(int j = 0; j < w; j++)
            temp = (2 * temp) % nlData[i].length;
         nlJumpW[i] = temp;
         for(int j = 0; j < v; j++)
            temp = (2 * temp) % nlData[i].length;
         nlJumpZ[i] = temp;
      }
   }

   private static void scramble(int[] data, Random rand) {
      int buffer;
      int j;

      for(int i = 0; i < data.length - 1; i++) {
         //choose a random number between i and data.length - 1
         j = (int)(rand.nextDouble() * (data.length - i)) + i;
         //exchange data[i] and data[j]
         buffer = data[i];
         data[i] = data[j];
         data[j] = buffer;
      }
   }

   private static void scramble(int[] data, RandomStream rand) {
      int buffer;
      int j;

      for(int i = 0; i < data.length - 1; i++) {
         //choose a random number between i and data.length - 1
         j = rand.nextInt(i, data.length - 1);
         //exchange data[i] and data[j]
         buffer = data[i];
         data[i] = data[j];
         data[j] = buffer;
      }
   }
 

   /**
    * Constructs a new stream, initializing it at its beginning.
    *   Also makes sure that the seed of the next constructed stream is
    *   <SPAN CLASS="MATH"><I>Z</I></SPAN> steps away. Sets its antithetic switch to <TT>false</TT> and sets
    *   the stream to normal precision mode (offers 32 bits of precision).
    * 
    */
   public F2NL607()  {
      //linear part
      initialisation();

      constructed = true;

      state = new int[BUFFER_SIZE];
      stream = new int[R];
      substream = new int[R];

      for(int i = 0; i < R; i++)
         stream[i] = curr_stream[i];

//    advanceSeed(curr_stream, Apz);
      advanceSeed(curr_stream, WELL607.pz);

      //non-linear part
      nlState = new int[nlData.length];
      nlStream = new int[nlData.length];
      nlSubstream = new int[nlData.length];

      for(int i = 0; i < nlData.length; i++) {
         nlStream[i] = curr_nlStream[i];
         curr_nlStream[i] += nlJumpZ[i];
      }

      resetStartStream();
   } 


   /**
    * Constructs a new stream with the identifier <TT>name</TT>
    *   (used in the <TT>toString</TT> method).
    * 
    * @param name name of the stream
    * 
    * 
    */
   public F2NL607 (String name)  {
      this();
      this.name = name;
   }

  
   public void resetStartStream() {
      for(int i = 0; i < R; i++)
         substream[i] = stream[i];
      for(int i = 0; i < nlSubstream.length; i++)
         nlSubstream[i] = nlStream[i];

      resetStartSubstream();
   }

   public void resetStartSubstream() {
      state_i = 0;
      for(int i = 0; i < R; i++)
         state[i] = substream[i];
      for(int i = 0; i < nlState.length; i++)
         nlState[i] = nlSubstream[i];
   }

   public void resetNextSubstream() {
//      advanceSeed(substream, WELL607.Apw);
      advanceSeed(substream, WELL607.pw);
      for(int i = 0; i < nlState.length; i++)
         nlSubstream[i] = (nlSubstream[i] + nlJumpW[i]) %
                          nlData[i].length;

      resetStartSubstream();
   }

   /**
    * Sets the initial seed of the linear part of the class
    *   <TT>F2NL607</TT> to the 19 integers of the vector <TT>seed[0..18]</TT>.
    *   This will be the initial seed (or seed) of the next created stream.
    *   At least one of the integers must be non-zero and if this integer is
    *   the last one, it must not be equal to <TT>0x7FFFFFFF</TT>.
    * 
    * @param seed array of 19 elements representing the seed
    * 
    * 
    */
   public static void setPackageLinearSeed (int seed[])  {
      verifySeed(seed);

      for(int i = 0; i < R; i++)
         curr_stream[i] = seed[i];
   } 


   /**
    * This method is discouraged for normal use.
    *   Initializes the stream at the beginning of a stream with the initial linear
    *   seed <TT>seed[0..18]</TT>. The seed must satisfy the same conditions as
    *   in <TT>setPackageSeed</TT>.
    *   The non-linear seed is not modified; thus the non-linear part of the random
    *   number generator is reset to the beginning of the old stream.
    *   This method only affects the specified stream; the others are not
    *   modified. Hence after calling this method, the beginning of the streams
    *   will no longer be spaced <SPAN CLASS="MATH"><I>Z</I></SPAN> values apart.
    *   For this reason, this method should only be used in very exceptional cases;
    *   proper use of the <TT>reset...</TT> methods and of the stream constructor is
    *   preferable.
    * 
    * @param seed array of 19 elements representing the seed
    * 
    * 
    */
   public void setLinearSeed (int seed[])  {
      verifySeed(seed);

      for(int i = 0; i < R; i++)
         stream[i] = seed[i];

      resetStartStream();
   } 


   /**
    * Returns the current state of the linear part of the stream,
    *   represented as an array of 19 integers.
    * 
    * @return the current state of the stream
    * 
    */
   public int[] getLinearState()  {
      return getState();
   } 


   /**
    * Sets the non-linear part of the initial seed of the class
    *   <TT>F2NL607</TT> to the <SPAN CLASS="MATH"><I>n</I></SPAN> integers of the vector <TT>seed[0..n-1]</TT>,
    *   where <SPAN CLASS="MATH"><I>n</I></SPAN> is the number of components of the non-linear part. The
    *   default is <SPAN CLASS="MATH"><I>n</I> = 3</SPAN>.
    *   Each of the integers must be between 0 and the length of the corresponding
    *   component minus one. By default, the lengths are 
    * <SPAN CLASS="MATH">(1019, 1021, 1031)</SPAN>.
    * 
    * @param seed array of <SPAN CLASS="MATH"><I>n</I></SPAN> elements representing the non-linear seed
    * 
    * 
    */
   public static void setPackageNonLinearSeed (int seed[])  {
      if (seed.length < nlData.length)
         throw new IllegalArgumentException("Seed must contain " +
                                            nlData.length + " values");
      for (int i = 0; i < nlData.length; i++)
         if (seed[i] < 0 || seed[i] >= nlData[i].length)
            throw new IllegalArgumentException("Seed number " + i +
                                               " must be between 0 and " +
                                               (nlData[i].length - 1));

      for(int i = 0; i < nlData.length; i++)
         curr_nlStream[i] = seed[i];
   } 


   /**
    * This method is discouraged for normal use.
    *   Initializes the stream at the beginning of a stream with the initial
    *   non-linear seed <TT>seed[0..n-1]</TT>, where <SPAN CLASS="MATH"><I>n</I></SPAN> is the number of components
    *   of the non-linear part of the generator.
    *   The linear seed is not modified so the linear part of the random number
    *   generator is reset to the beginning of the old stream.
    *   This method only affects the specified stream; the others are not
    *   modified. Hence after calling this method, the beginning of the streams
    *   will no longer be spaced <SPAN CLASS="MATH"><I>Z</I></SPAN> values apart.
    *   For this reason, this method should only be used in very exceptional cases;
    *   proper use of the <TT>reset...</TT> methods and of the stream constructor is
    *   preferable.
    * 
    * @param seed 
    * 
    * 
    */
   public void setNonLinearSeed (int seed[])  {
      if(seed.length != nlData.length)
         throw new IllegalArgumentException("Seed must contain " +
                                            nlData.length + " values");
      for(int i = 0; i < nlData.length; i++)
         if(seed[i] < 0 || seed[i] >= nlData[i].length)
            throw new IllegalArgumentException("Seed number " + i +
                                               " must be between 0 and " +
                                               (nlData[i].length - 1));

      for(int i = 0; i < nlData.length; i++)
         nlStream[i] = seed[i];

      resetStartStream();
   } 


   /**
    * Returns the current state of the non-linear part of the stream,
    *   represented as an array of <SPAN CLASS="MATH"><I>n</I></SPAN> integers, where <SPAN CLASS="MATH"><I>n</I></SPAN> is the number
    *   of components in the non-linear generator.
    * 
    * @return the current state of the stream
    * 
    */
   public int[] getNonLinearState()  {
      //we must prevent the user to change the state, so we give him a copy
      int[] state = new int[nlState.length];
      for(int i = 0; i < nlState.length; i++)
         state[i] = nlState[i];
      return state;
   } 


   /**
    * Return the data of all the components of the non-linear
    *   part of the random number generator. This data is explained in the
    *   introduction.
    * 
    * @return the data of the components
    * 
    */
   public static int[][] getNonLinearData()  {
      //we must prevent the user to change the data, so we give him a copy
      int[][] data = new int[nlData.length][];
      for(int i = 0; i < nlData.length; i++) {
         data[i] = new int[nlData[i].length];
         for(int j = 0; j < nlData[i].length; j++)
            data[i][j] = nlData[i][j];
      }
      return data;
   } 


   /**
    * Selects new data for the components of the non-linear generator.
    *   The number of arrays in {@code data} will decide the number of components.
    *   Each of the arrays will be assigned to one of the components. The period
    *   of the resulting non-linear generator will be equal to the lowest common
    *   multiple of the lengths of the arrays. It is thus recommended to choose
    *   only prime length for the best results.
    * 
    * <P>
    * NOTE : This method cannot be called if at least one instance of
    *   <TT>F2NL607</TT> has been constructed. In that case, it will throw
    *   an {@link IllegalStateException}.
    * 
    * @param data the new data of the components
    * 
    *   @exception IllegalStateException if an instance of the class was
    *     constructed before
    * 
    * 
    */
   public static void setNonLinearData (int[][] data)  {
      if(constructed)
         throw new IllegalStateException("setNonLinearData can only be " +
                                         "called before the creation of " +
                                         "any F2NL607");

      nlData = new int[data.length][];
      curr_nlStream = new int[data.length];
      for(int i = 0; i < data.length; i++) {
         nlData[i] = new int[data[i].length];
         for(int j = 0; j < data[i].length; j++)
            nlData[i][j] = data[i][j];
         curr_nlStream[i] = 0;
      }

      //recomputing the jumps length (an inefficient method is used)
      nlJumpW = new int[data.length];
      nlJumpZ = new int[data.length];
      for(int i = 0; i < nlJumpW.length; i++) {
         int temp = 1;
         for(int j = 0; j < w; j++)
            temp = (2 * temp) % nlData[i].length;
         nlJumpW[i] = temp;
         for(int j = 0; j < v; j++)
            temp = (2 * temp) % nlData[i].length;
         nlJumpZ[i] = temp;
      }
   } 


   /**
    * Selects new data for the components of the non-linear generator.
    *   The number of arrays in {@code data} will decide the number of components.
    *   Each of the arrays will be assigned to one of the components. The period
    *   of the resulting non-linear generator will be equal to the lowest common
    *   multiple of the lengths of the arrays. It is thus recommended to choose
    *   only prime length for the best results.
    * 
    * <P>
    * NOTE : This method cannot be called if at least one instance of
    *   <TT>F2NL607</TT> has been constructed. In that case, it will throw
    *   an {@link IllegalStateException}.
    * 
    * @param rand the random numbers source to do the scrambling
    * 
    *   @param steps number of time to do the scrambling
    * 
    *   @param size size of each components
    * 
    *   @exception IllegalStateException if an instance of the class was
    *     constructed before
    * 
    * 
    */
   public static void setScrambleData (RandomStream rand, int steps,
                                       int[] size)  {
      if (constructed)
         throw new IllegalStateException("setScrambleData can only be " +
                                         "called before the creation of " +
                                         "any F2NL607");

      curr_nlStream = new int[size.length];
      for(int i = 0; i < size.length; i++)
         curr_nlStream[i] = 0;

      nlData = new int[size.length][];
      for(int i = 0; i < size.length; i++)
         nlData[i] = new int[size[i]];

      for(int i = 0; i < nlData.length; i++) {
         for(int j = 0; j < nlData[i].length; j++)
            nlData[i][j] = (int)((((long)j) << 32) / nlData[i].length);
         for(int j = 0; j < steps; j++)
            scramble(nlData[i], rand);
      }

      //computing the jumps length
      //inefficient algorithm alert!
      nlJumpW = new int[size.length];
      nlJumpZ = new int[size.length];
      for(int i = 0; i < nlJumpW.length; i++) {
         int temp = 1;
         for(int j = 0; j < w; j++)
            temp = (2 * temp) % nlData[i].length;
         nlJumpW[i] = temp;
         for(int j = 0; j < v; j++)
            temp = (2 * temp) % nlData[i].length;
         nlJumpZ[i] = temp;
      }
   } 


   /**
    * Clones the current generator and return its copy.
    *  
    *  @return A deep copy of the current generator
    * 
    */
   public F2NL607 clone()  {
      F2NL607 retour = null;
      retour = (F2NL607)super.clone();
      retour.state = new int[BUFFER_SIZE];
      retour.substream = new int[R];
      retour.stream = new int[R];
      retour.nlState = new int[nlData.length];
      retour.nlStream = new int[nlData.length];
      retour.nlSubstream = new int[nlData.length];

      for (int i = 0; i<R; i++) {
         retour.substream[i] = substream[i];
         retour.stream[i] = stream[i];
      }
      for (int i = 0; i<BUFFER_SIZE; i++) {
         retour.state[i] = state[i];
      }
      for (int i = 0; i<nlData.length; i++) {
         retour.nlState[i] = nlState[i];
         retour.nlStream[i] = nlStream[i];
         retour.nlSubstream[i] = nlSubstream[i];
      }
      return retour;
   }


   public String toString() {
      StringBuffer sb = new StringBuffer();

      if(name == null)
         sb.append("The state of this F2NL607 is : " + PrintfFormat.NEWLINE);
      else
         sb.append("The state of " + name + " is : " + PrintfFormat.NEWLINE);
      sb.append(" Linear part : { ");
      sb.append(super.stringState());
      sb.append(PrintfFormat.NEWLINE + " Non-linear part : { ");
      for(int i = 0; i < nlState.length; i++)
         sb.append(nlState[i] + " ");
      sb.append("}");

      return sb.toString();
   }



   protected double nextValue() {
      for (int i = 0; i < nlData.length; i++)
         if (nlState[i] >= nlData[i].length - 1)
            nlState[i] = 0;
         else
            nlState[i]++;

      int nonLin = 0;
      for (int i = 0; i < nlData.length; i++)
         nonLin += nlData[i][nlState[i]];

      long result = (nextInt() ^ nonLin);
      if(result <= 0)
         result += 0x100000000L;
      return result * NORM;
   }
}

