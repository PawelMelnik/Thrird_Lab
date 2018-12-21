import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user_phmf4 on 08.12.18.
 */
public class InterpretWorkerConfig {
    public enum Params {CODE_MODE, BlOCK_INDEX, BLOCK_LENGTH}

    protected static final String COLON = ":";

    public static final Map<String, Enum> worc_conf_lexemeMap;

    static {
        worc_conf_lexemeMap = new HashMap<>();
        worc_conf_lexemeMap.put("CODE_MODE", Params.CODE_MODE);
        worc_conf_lexemeMap.put("CODE_MODE", Params.BlOCK_INDEX);
        worc_conf_lexemeMap.put("CODE_MODE", Params.BLOCK_LENGTH);
    }

    /**
     * interpret current worker config file
     *
     * @param paramFile name of file
     * @param resultMap map where we will put lexeme
     * @throws IOException
     */
    public static int InterpretWorker(String paramFile, Map<Enum, String> resultMap) {
        try
        {
            Reader reader = new Reader(paramFile);
            String Paramstring;


            while ((Paramstring = reader.ReadLine()) != null) {
                if ((Paramstring = Paramstring.replaceAll("\\s", "")).isEmpty()) {
                    continue;
                }

                String[] ParamPair = Paramstring.split(COLON);

                if (!IsPairCorrect(ParamPair)) {
                    return -1;
                }

                resultMap.put(worc_conf_lexemeMap.get(ParamPair[0]), ParamPair[1]);
            }

            if (resultMap.putIfAbsent(Params.CODE_MODE, "0") == null) {
                Log.report("Missing CODE_MODE, using default: 0");
            }

            reader.CloseStream();
        }
        catch (IOException e)
        {
            return -1;
        }
    return 0;
}

    /**
     * is pair correct or not
     * @param ParamPair checking pair
     * @return true if correct, false otherwise
     */
    public static boolean IsPairCorrect(String[] ParamPair)
    {
        if (ParamPair.length != 2|| ParamPair[0].isEmpty() || ParamPair[1].isEmpty())
        {
            Log.report("Invalid syntax in config file");
            return false;
        }
        if (!isLexeme(ParamPair[0]))
        {
            Log.report("Unknown lexeme " + ParamPair[0]);
            return false;
        }
        return true;
    }

    /**
     *
     * @param lexeme current lexeme
     * @return true if have lexeme, false otherwise
     */
    public static boolean isLexeme(String lexeme)
    {
        for (String key : worc_conf_lexemeMap.keySet())
        {
            if (lexeme.equals(key))
            {
                return true;
            }
        }
        return false;
    }
}
