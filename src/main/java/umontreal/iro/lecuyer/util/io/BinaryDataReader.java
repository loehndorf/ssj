

/*
 * Class:        BinaryDataReader
 * Description:  Binary data reader
 * Environment:  Java
 * Software:     SSJ 
 * Copyright (C) 2001  Pierre L'Ecuyer and Université de Montréal
 * Organization: DIRO, Université de Montréal
 * @author       David Munger 
 * @since        August 2009

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

package umontreal.iro.lecuyer.util.io;
import java.io.*;
import java.net.URL;



/**
 * Binary data reader. This class implements a module for importing data written
 *  with {@link BinaryDataWriter}.
 * 
 */
public class BinaryDataReader extends AbstractDataReader  {
   protected DataInputStream in;
   
   // only one of these will be used
   protected String filename           = null;
   protected URL url                   = null;
   protected File file                 = null;
   protected boolean canReset = false;

   /* *
    * Reads current field label.
    *
    */
   protected String readLabel() throws IOException {

      byte typechar = in.readByte();
      if (typechar != BinaryDataWriter.TYPECHAR_LABEL)
         throw new IOException("Expected a label");

      return readStringData();
   }


   /* *
    * Reads string data.
    *
    */    
   protected String readStringData() throws IOException {
      int length = in.readInt();
      if (length == 0)
         return null;
      byte[] s = new byte[length];
      for (int i = 0; i < length; i++)
         s[i] = in.readByte();
      return new String(s);
   }
      
   /* *
    * Reads string-array data.
    *
    */
   protected String[] readStringArrayData(int dim) throws IOException {
      String[] a = new String[dim];
      for (int i = 0; i < dim; i++)
         a[i] = readStringData();
      return a;
   }

   /* *
    * Reads 2D string-array data.
    *
    */
   protected String[][] readStringArray2DData(int dims[]) throws IOException {
      String[][] a = new String[dims[0]][dims[1]];
      for (int i = 0; i < dims[0]; i++)
         for (int j = 0; j < dims[1]; j++)
            a[i][j] = readStringData();
      return a;
   }


   /* *
    * Reads integer data.
    *
    */    
   protected int readIntData() throws IOException {
      return in.readInt();
   }
      
   /* *
    * Reads integer-array data.
    *
    */
   protected int[] readIntArrayData(int dim) throws IOException {
      int[] a = new int[dim];
      for (int i = 0; i < dim; i++)
         a[i] = readIntData();
      return a;
   }

   /* *
    * Reads 2D integer-array data.
    *
    */
   protected int[][] readIntArray2DData(int dims[]) throws IOException {
      int[][] a = new int[dims[0]][dims[1]];
      for (int i = 0; i < dims[0]; i++)
         for (int j = 0; j < dims[1]; j++)
            a[i][j] = readIntData();
      return a;
   }


   /* *
    * Reads float data.
    *
    */    
   protected float readFloatData() throws IOException {
      return in.readFloat();
   }
      
   /* *
    * Reads float-array data.
    *
    */
   protected float[] readFloatArrayData(int dim) throws IOException {
      float[] a = new float[dim];
      for (int i = 0; i < dim; i++)
         a[i] = readFloatData();
      return a;
   }

   /* *
    * Reads 2D float-array data.
    *
    */
   protected float[][] readFloatArray2DData(int dims[]) throws IOException {
      float[][] a = new float[dims[0]][dims[1]];
      for (int i = 0; i < dims[0]; i++)
         for (int j = 0; j < dims[1]; j++)
            a[i][j] = readFloatData();
      return a;
   }


   /* *
    * Reads double data.
    *
    */    
   protected double readDoubleData() throws IOException {
      return in.readDouble();
   }
      
   /* *
    * Reads double-array data.
    *
    */
   protected double[] readDoubleArrayData(int dim) throws IOException {
      double[] a = new double[dim];
      for (int i = 0; i < dim; i++)
         a[i] = readDoubleData();
      return a;
   }

   /* *
    * Reads 2D double-array data.
    *
    */
   protected double[][] readDoubleArray2DData(int dims[]) throws IOException {
      double[][] a = new double[dims[0]][dims[1]];
      for (int i = 0; i < dims[0]; i++)
         for (int j = 0; j < dims[1]; j++)
            a[i][j] = readDoubleData();
      return a;
   }


   /* *
    * Reads field data of arbitrary type.
    *
    * @param typechar   type code character (see {@link BinaryDataWriter} constants)
    * @param nDims   number of dimensions (0 for atomic data)
    * @param dims    length of each dimension
    *
    * @return an instance of the data or {@code null} if data type is unknown
    */
   protected Object readFieldData(byte typechar, int nDims, int dims[])
         throws IOException {

      if (nDims < 0 || nDims > 2)
         throw new IOException("unsupported number of dimensions: " + nDims);      
      
      Object data = null;
      
      switch (typechar) {
         
         case BinaryDataWriter.TYPECHAR_STRING:
            if (nDims == 0)
               data = readStringData();
            else if (nDims == 1)
               data = readStringArrayData(dims[0]);
            else if (nDims == 2)
               data = readStringArray2DData(dims);
            break;
            
         case BinaryDataWriter.TYPECHAR_INTEGER:
            if (nDims == 0)
               data = readIntData();
            else if (nDims == 1)
               data = readIntArrayData(dims[0]);
            else if (nDims == 2)
               data = readIntArray2DData(dims);
            break;
            
         case BinaryDataWriter.TYPECHAR_FLOAT:
            if (nDims == 0)
               data = readFloatData();
            else if (nDims == 1)
               data = readFloatArrayData(dims[0]);
            else if (nDims == 2)
               data = readFloatArray2DData(dims);
            break;
            
         case BinaryDataWriter.TYPECHAR_DOUBLE:
            if (nDims == 0)
               data = readDoubleData();
            else if (nDims == 1)
               data = readDoubleArrayData(dims[0]);
            else if (nDims == 2)
               data = readDoubleArray2DData(dims);
            break;
      }

      return data;      
   }



   /**
    * Opens the file with the specified name for reading.
    * 
    * @param filename name of the file to read the data from
    * 
    * 
    */
   public BinaryDataReader (String filename) throws IOException  {
      this.filename = filename;
      canReset = true;
      reset();
   }
   


   /**
    * Opens the file at the specified url for reading.
    * 
    * @param url url of the file to read the data from
    * 
    * 
    */
   public BinaryDataReader (URL url) throws IOException  {
      this.url = url;
      canReset = true;
      reset();
   }
   


   /**
    * Opens the specified file for reading.
    * 
    * @param file file to read the data from
    * 
    * 
    */
   public BinaryDataReader (File file) throws IOException  {
      this.file = file;
      canReset = true;
      reset();
   }
   

   
   /**
    * Opens the specified input stream for reading.
    * When using this constructor, the method {@link #readField readField} might will not be able to read
    * a field that is before the current reading position.
    * 
    * @param inputStream input stream to read the data from
    * 
    */
   public BinaryDataReader (InputStream inputStream) throws IOException  {
      this.in = new DataInputStream(in);
   }
   


   /**
    * Reads the next available field.
    * 
    * @return a newly created DataField instance or null if not found
    * 
    */
   public DataField readNextField() throws IOException  {

      String label = readLabel();

      byte typechar = in.readByte();

      int nDims = in.readByte();

      int[] dims = new int[nDims];
      for (int i = 0; i < nDims; i++)
         dims[i] = in.readInt();


      return new DataField(label, readFieldData(typechar, nDims, dims));
   }
   


   /**
    * Reads the first field labeled as <TT>label</TT>.
    * 
    * @return a newly created DataField instance or null if not found
    * 
    */
   public DataField readField (String label) throws IOException  {

      reset();
      
      while (in.available() > 0) {
         
         String fieldLabel = readLabel();

         byte typechar = in.readByte();

         int nDims = in.readByte();
         int[] dims = new int[nDims];
         for (int i = 0; i < nDims; i++)
            dims[i] = in.readInt();

         // if found, return field instance
         if (fieldLabel.compareTo(label) == 0)
            return new DataField(fieldLabel, readFieldData(typechar, nDims, dims));

         // otherwise, just skip the current field
         
         // strings don't have a fixed size, so just read them out
         if (typechar == BinaryDataWriter.TYPECHAR_STRING) {
            readFieldData(typechar, nDims, dims);
            continue;
         }
         
         // compute the number of bytes to be skipped
         int skipSize = 0;
         switch (typechar) {
            
            case BinaryDataWriter.TYPECHAR_INTEGER:
               skipSize = Integer.SIZE / 8;
               break;
               
            case BinaryDataWriter.TYPECHAR_FLOAT:
               skipSize = Float.SIZE / 8;
               break;
               
            case BinaryDataWriter.TYPECHAR_DOUBLE:
               skipSize = Double.SIZE / 8;
               break;
         }
         
         if (nDims > 0)
            skipSize *= dims[0];
         if (nDims > 1)
            skipSize *= dims[1];
         
         in.skipBytes(skipSize);
      }
      
      return null;
   }
   

   /**
    * Reopens the file (does not work with the constructor that takes an input stream).
    * 
    */
   public void reset() throws IOException  {
      if (!canReset)
         return;
         
      if (filename != null)
         this.in = new DataInputStream(new FileInputStream(filename));
      else if (file != null)
         this.in = new DataInputStream(new FileInputStream(file));
      else if (url != null)
         this.in = new DataInputStream(url.openStream());
   }
   


   /**
    * Returns <TT>true</TT> if there remains data to be read.
    * 
    */
   public boolean dataPending() throws IOException  {
      return (in.available() > 0);
   }
   


   /**
    * Closes the file.
    * 
    */
   public void close() throws IOException  {
      in.close();
   }
   

}
