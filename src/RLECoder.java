import java.io.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class RLECoder implements Executor {

    private DataInputStream input = null;
    private DataOutputStream output = null;
    private ArrayList<Executor> Consumers;
    public byte[] block = null;
    private int CODE_MODE;
    public byte[] out_array;//////////////////////need in 3d?
    APPROPRIATE_TYPES[] arr_types = {APPROPRIATE_TYPES.BYTE};
    Map<Executor,Pair> adapters_map;
    /**
     * create worker
     */
    public RLECoder() { }

    public int setConfig(String config)
    {
        Map<Enum, String> paramMap = new EnumMap(InterpretWorkerConfig.Params.class);
        int err = InterpretWorkerConfig.InterpretWorker(config, paramMap);
        if (err != 0)
            return -1;
        int code_mode = Integer.parseInt(paramMap.get(InterpretWorkerConfig.Params.CODE_MODE));
        this.CODE_MODE = code_mode;
        return 0;
    }

    public void SetInput (DataInputStream input)
    {
        this.input = input;
    }

    public void SetOutput (DataOutputStream output)
    {
        this.output = output;
    }

    public int setConsumer(Executor consumer)
    {
        int is_no_equal = 1;
        APPROPRIATE_TYPES[] cons_types = consumer.getConsumedTypes();
        APPROPRIATE_TYPES equal_type = APPROPRIATE_TYPES.BYTE;

        for (APPROPRIATE_TYPES prov_type: arr_types)
        {
            for (APPROPRIATE_TYPES cons_type:cons_types)
            {
                if (prov_type == cons_type)
                {
                    equal_type = prov_type;
                    is_no_equal = 0;
                }

            }
        }
        if (is_no_equal == 1)
        {
            Log.report("No equal appropriate types found");
            return -1;
        }
        setAdapter(this,geterByte,equal_type);
        this.Consumers.add(consumer);
        return 0;
    }

    public APPROPRIATE_TYPES[] getConsumedTypes()
    {
        return arr_types;
    }

    public void setAdapter(Executor provider, Object adapter, APPROPRIATE_TYPES type)
    {
        adapters_map.put(provider, new Pair(adapter, type))
    }

    class GeterByte implements InterfaceByteTransfer
    {
        public Byte getNextByte()
        {

        }
    }
    GeterByte geterByte = new GeterByte();//////////////////////////////

    public int run()
    {
        int err = 0;
        while (err == 0)
        {
            err = ReadToArray();
            if (err == -1) {
                return -1;
            }

            int err_put = this.Put(this.block);
            if (err_put != 0) {
                return err;
            }
            if (err_put != 0)
                return err_put;
        }
        return 0;
    }

    public int put(Executor provider)
    {
        int err;
        block = (byte [])obj;

        if (CODE_MODE == 0)
        {
            RunEncode();
        }
        else
        {
            RunDecode();
        }

        if (CheckOutput() == -1)
            return -1;

        block = null;
        if (output != null)
        {
            err = PrintArray(out_array);
        }
        else
        {
            err = Consumer.Put(out_array);
        }
        out_array = null;
        return err;
    }

    /**
     * run encode of data block
     */
    private void RunEncode()
    {
        int block_index = 0;
        int out_arr_index = 0;
        byte count = 1;
        byte i = block[block_index];
        int size = block.length;
        out_array = new byte[2 * size];

        while (i != -1 && block_index < size)
        {
            byte j = block[block_index + 1];
            block_index++;
            while (j == i)
            {
                count++;
                block_index++;
                if (block_index == size)
                {
                    break;
                }
                j = block[block_index];

            }

            out_array[out_arr_index] = count;
            out_arr_index++;
            out_array[out_arr_index] = i;
            out_arr_index++;
            i = j;
            count = 1;

        }

        if (out_arr_index < 2 * size)
        {
            out_array[out_arr_index] = -1;
        }
    }


    /**
     * run decode of data block
     */
    private void RunDecode()
    {
        int block_index = 0;
        int out_arr_index = 0;
        byte i = block[block_index];
        int size = GetNewArraySize();
        out_array = new byte[size];

        while(i != -1 && block_index < block.length)
        {
            i = block[block_index];
            byte j = block[block_index + 1];
            while( i > 0)
            {
                out_array[out_arr_index] = j;
                out_arr_index++;
                i--;
            }
            block_index += 2;
        }
    }

    /**
     * check output and consumer do not exist together
     * @return 0 if correct, -1 otherwise
     */
    private int CheckOutput()
    {
        if (output != null && Consumer != null)
        {
            Log.report("Incorrect worker");
            return -1;
        }

        if (output == null && Consumer == null)
        {
            Log.report("Incorrect worker");
            return -1;
        }
        return 0;
    }

    /**
     * read block from input file
     * @return 0 if correct, -2 if get EOF, -1 otherwise
     */
    private int ReadToArray()
    {
        int k = 0;
        byte[] array = new byte[4];
        try
        {
            byte i = 0;

            while (i != -1 && k < 4)
            {
                i = input.readByte();
                array[k] = i;
                k++;
            }
        }
        catch (EOFException e)
        {
            if (k < 4)
                array[k] = -1;
            block = array;
            return -2;
        }
        catch (IOException e)
        {
            Log.report("Can't encode file");
            return -1;
        }
        block = array;
        return 0;
    }

    /**
     * get size of new array, that will be created to decode
     * @return size of array
     */
    private int GetNewArraySize()
    {
        int i;
        int size = 0;
        for (i = 0; i < block.length && block[i] != -1; i += 2)
        {
            size += (int)block[i];
        }
        return size;
    }

    /**
     * print block to output file
     * @param out_arr block of data
     * @return 0 if correct, -1 otherwise
     */
    int PrintArray(byte[] out_arr)
    {
        try
        {

            int i = 0;
            while( i < out_arr.length && out_arr[i] != -1)
            {
                output.writeByte(out_arr[i]);
                output.flush();
                i++;
            }
        }

        catch (IOException e)
        {
            Log.report("Can't write to output file");
            return -1;
        }
        return 0;
    }
}
