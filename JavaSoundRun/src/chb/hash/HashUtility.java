package chb.hash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HashUtility {

    //FNV-64a Hash, An enhanced version
    //long and Byte, their XOR just affects the lower
    //bits, and highter bits remain. So the hash value
    //will keep going large.
    public static long __HashFnv__(byte[] bytes)
    {
        long hash = 0L;
        long FNV_Prime = 1099511628211L;
        long octet = 0L;
        int remain = bytes.length;
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        while (remain >= 8)
        {
            octet = buffer.getLong(bytes.length-remain);
            hash ^= octet;
            hash *= FNV_Prime;
            remain -= 8;
        }
        if (remain > 0)
        {
            octet = __PackRemainByte_Hash_01(bytes, bytes.length - remain);
            hash ^= octet;
            hash *= FNV_Prime;
        }

        return hash;
    }
    private static long __PackRemainByte_Hash_01(byte[] bytes, int index)
    {
        long ub64 = 0;

        byte[] bt8 = new byte[8];
        int counter = 0;
        int divident = bytes.length - index;
        for (int i = 0; i < divident; ++i)
        {
            bt8[i] = bytes[index + counter % divident];
            ++counter;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(bt8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        ub64 = buffer.getLong();

        return ub64;
    }
    
    
    //Murmur2 hash
    //Simplified interface
    public static long __HashMurmur2__(byte[] data)
    {
        long m = 0x6a4a7935bd1e995L;
        long r = 47;
        long seed = 0;
        int length = data.length;

        long h = (seed & 0xffffffffL) ^ (length * m);

        long length8 = (long)(length / 8);

        ByteBuffer  buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < length8; i++)
        {
            int i8 = i * 8;
            long k =buffer.getLong(i8);

            k *= m;
            k ^= k >> (short)r;
            k *= m;

            h ^= k;
            h *= m;
        }

        switch (length % 8)
        {
            case 7:
                h ^= (long)(data[(length & ~7) + 6] & 0xff) << 48;
                h ^= (long)(data[(length & ~7) + 5] & 0xff) << 40;
                h ^= (long)(data[(length & ~7) + 4] & 0xff) << 32;
                h ^= (long)(data[(length & ~7) + 3] & 0xff) << 24;
                h ^= (long)(data[(length & ~7) + 2] & 0xff) << 16;
                h ^= (long)(data[(length & ~7) + 1] & 0xff) << 8;
                h ^= (long)(data[length & ~7] & 0xff);
                h *= m;
                break;
            case 6:
                h ^= (long)(data[(length & ~7) + 5] & 0xff) << 40;
                h ^= (long)(data[(length & ~7) + 4] & 0xff) << 32;
                h ^= (long)(data[(length & ~7) + 3] & 0xff) << 24;
                h ^= (long)(data[(length & ~7) + 2] & 0xff) << 16;
                h ^= (long)(data[(length & ~7) + 1] & 0xff) << 8;
                h ^= (long)(data[length & ~7] & 0xff);
                h *= m;
                break;
            case 5:
                h ^= (long)(data[(length & ~7) + 4] & 0xff) << 32;
                h ^= (long)(data[(length & ~7) + 3] & 0xff) << 24;
                h ^= (long)(data[(length & ~7) + 2] & 0xff) << 16;
                h ^= (long)(data[(length & ~7) + 1] & 0xff) << 8;
                h ^= (long)(data[length & ~7] & 0xff);
                h *= m;
                break;
            case 4:
                h ^= (long)(data[(length & ~7) + 3] & 0xff) << 24;
                h ^= (long)(data[(length & ~7) + 2] & 0xff) << 16;
                h ^= (long)(data[(length & ~7) + 1] & 0xff) << 8;
                h ^= (long)(data[length & ~7] & 0xff);
                h *= m;
                break;
            case 3:
                h ^= (long)(data[(length & ~7) + 2] & 0xff) << 16;
                h ^= (long)(data[(length & ~7) + 1] & 0xff) << 8;
                h ^= (long)(data[length & ~7] & 0xff);
                h *= m;
                break;
            case 2:
                h ^= (long)(data[(length & ~7) + 1] & 0xff) << 8;
                h ^= (long)(data[length & ~7] & 0xff);
                h *= m;
                break;
            case 1:
                h ^= (long)(data[length & ~7] & 0xff);
                h *= m;
                break;
        };

        h ^= h >> (short)r;
        h *= m;
        h ^= h >> (short)r;

        return h;
    }


}
