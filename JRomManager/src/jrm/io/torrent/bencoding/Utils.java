/* Copyright (C) 2015  Christophe De Troyer
 * Copyright (C) 2018  Optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.io.torrent.bencoding;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import jrm.misc.Log;

/**
 * @author Created by christophe on 15.01.15.
 * @author Fixed by optyfr on 2018-08-15
 */
public class Utils
{

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray(); //$NON-NLS-1$

    /**
     * Takes an array of bytes and converts them to a string of
     * hexadecimal characters.
     * Credit for this function goes to the author of this[1] StackOverflow
     * question.
     * [1] https://stackoverflow.com/a/9855338
     *
     * @param bytes byte array
     * @return String
     */
    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4]; // Get left part of byte
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Get right part of byte
        }
        return new String(hexChars);
    }

    /**
     * Takes a filepath and returns the nth byte of that file.
     *
     * @param path Path to file
     * @param nth  Nth position to get, starting from 0.
     * @return byte.
     */
    public static byte readNthByteFromFile(String path, long nth)
    {
        RandomAccessFile rf = null;
        try
        {
            rf = new RandomAccessFile(path, "r"); //$NON-NLS-1$

            if (rf.length() < nth)
                throw new EOFException("Reading outside of bounds of file"); //$NON-NLS-1$

            rf.seek(nth);
            byte curr = rf.readByte();
            rf.close();

            return curr;
        } catch (FileNotFoundException e)
        {
            Log.err(e.getMessage(),e);
        } catch (IOException e)
        {
            Log.err(e.getMessage(),e);
        } finally
        {
            assert rf != null;
            try
            {
                rf.close();
            } catch (IOException e)
            {
                Log.err(e.getMessage(),e);
            }
        }
        return 0;
    }

    /**
     * Given a byte and a position, returns if byte at
     * position is set. (Position starts at 0 from the left).
     *
     * @param b        byte
     * @param position position in byte
     * @return boolean indicating if bit is 1.
     */
    public static boolean isBitSet(byte b, int position)
    {
        return ((b >> position) & 1) == 1;
    }

    /**
     * Prints a byte.
     *
     * @param b byte
     */
    public static void printByte(byte b)
    {
        String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'); //$NON-NLS-1$
        System.out.println(s1); // 10000001
    }

    /**
     * Checks if a list of bytes are all printable ascii chars.
     * They are if the mostleft bit is never set (always 0).
     *
     * @param data array of bytes
     * @return bolean indicating wether this byte is a valid ascii char.
     */
    public static boolean allAscii(byte[] data)
    {
        for (byte b : data)
            if (isBitSet(b, 7))
                return false;
        return true;
    }


    /**
     * Creates the SHA1 hash for a given array of bytes.
     * @param input array of bytes.
     * @return String representation of SHA1 hash.
     */
    public static String SHAsum(byte[] input)
    {
        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
            return byteArray2Hex(md.digest(input));
        } catch (NoSuchAlgorithmException e)
        {
            Log.err(e.getMessage(),e);
        }
        return null;
    }

    /**
     * Takes an array of bytes that should represent a hexadecimal value.
     * Returns string representation of these values.
     * @param bytes bytes containing hex symbols.
     * @return String representation of the byte[]
     */
    private static String byteArray2Hex(final byte[] bytes)
    {
        try(Formatter formatter = new Formatter();)
        {
            for (byte b : bytes)
            {
                formatter.format("%02x", b); //$NON-NLS-1$
            }
            return formatter.toString();
        }
    }

    /**
     * Takes a String and returns a byte[] arrays. EAch byte contains the ascii
     * representation of the character in the string.
     * @param s String
     * @return byte array of ascii chars.
     */
    public static byte[] stringToAsciiBytes(String s)
    {
        byte[] ascii = new byte[s.length()];
        for(int charIdx = 0; charIdx < s.length(); charIdx++)
        {
            ascii[charIdx] = (byte) s.charAt(charIdx);
        }
        return ascii;
    }
}