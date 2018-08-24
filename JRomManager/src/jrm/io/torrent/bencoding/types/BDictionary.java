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
package jrm.io.torrent.bencoding.types;

import java.util.*;

//TODO we don't need this..?

/**
* Created by christophe on 15.01.15.
*/
public class BDictionary implements IBencodable
{
  private final Map<BByteString, IBencodable> dictionary;
  public byte[] blob;

  public BDictionary()
  {
      // LinkedHashMap to preserve order.
      this.dictionary = new LinkedHashMap<BByteString, IBencodable>();
  }

  ////////////////////////////////////////////////////////////////////////////
  //// LOGIC METHODS /////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public void add(BByteString key, IBencodable value)
  {
      this.dictionary.put(key, value);
  }

  public Object find(BByteString key)
  {
      return dictionary.get(key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //// BENCODING /////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  public String bencodedString()
  {
      StringBuilder sb = new StringBuilder();
      sb.append("d"); //$NON-NLS-1$
      for (Map.Entry<BByteString, IBencodable> entry : this.dictionary.entrySet())
      {
          sb.append(entry.getKey().bencodedString());
          sb.append(entry.getValue().bencodedString());
      }
      sb.append("e"); //$NON-NLS-1$
      return sb.toString();
  }

  public byte[] bencode()
  {
      // Get the total size of the keys and values.
      ArrayList<Byte> bytes = new ArrayList<Byte>();
      bytes.add((byte) 'd');

      for (Map.Entry<BByteString, IBencodable> entry : this.dictionary.entrySet())
      {
          byte[] keyBencoded = entry.getKey().bencode();
          byte[] valBencoded = entry.getValue().bencode();
          for (byte b : keyBencoded)
              bytes.add(b);
          for (byte b : valBencoded)
              bytes.add(b);
      }
      bytes.add((byte) 'e');
      byte[] bencoded = new byte[bytes.size()];

      for (int i = 0; i < bytes.size(); i++)
          bencoded[i] = bytes.get(i);

      return bencoded;
  }

  ////////////////////////////////////////////////////////////////////////////
  //// OVERRIDDEN METHODS ////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString()
  {
      StringBuilder sb = new StringBuilder();
      sb.append("\n[\n"); //$NON-NLS-1$
      for (Map.Entry<BByteString, IBencodable> entry : this.dictionary.entrySet())
      {
          sb.append(entry.getKey()).append(" :: ").append(entry.getValue()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      sb.append("]"); //$NON-NLS-1$

      return sb.toString();
  }
}