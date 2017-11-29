public class myInt
{
  byte[] bytes;

  public myInt(int t)
  {
    this.bytes = new byte[31];

    setInt(t);
  }
  public myInt(byte[] t) throws IllegalArgumentException {
    this.bytes = new byte[31];
    if (t.length != 31) {
      throw new IllegalArgumentException();
    }
    setInt(t);
  }

  public int getInt()
  {
    int zahl = 0;
    for (int i = 0; i < this.bytes.length; i++) {
      if (this.bytes[i] == 1) {
        zahl = (int)(zahl + Math.pow(2.0D, i));
      }
    }
    return zahl;
  }

  public void setInt(int zahl)
  {
    for (int i = 30; i >= 0; i--) {
      int vergleich = (int)Math.pow(2.0D, i);
      if (zahl >= vergleich) {
        zahl -= vergleich;
        this.bytes[i] = 1;
      } else {
        this.bytes[i] = 0;
      }
    }
  }

  public void setInt(byte[] bits) throws IllegalArgumentException
  {
    if (bits.length != 31) {
      throw new IllegalArgumentException();
    }
    for (int i = 0; i < bits.length; i++)
      this.bytes[i] = bits[i];
  }

  public byte[] getBits()
  {
    return this.bytes;
  }
  public byte getspecificBit(int index) throws IndexOutOfBoundsException {
    if (index >= this.bytes.length) {
      throw new IndexOutOfBoundsException();
    }
    return this.bytes[index];
  }
}