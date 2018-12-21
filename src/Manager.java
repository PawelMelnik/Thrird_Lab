import java.io.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Manager {
    private Map<Enum, String> configMap;
    private Map<String, Executor> conveyorMap;
    private Executor first_worker;
    boolean isreadytorun = false;

    /**
     * create manager with conveyor inside
     * @param config name of config file
     */
    Manager(String config)
    {
        configMap = new EnumMap(InterpreterConfig.Params.class);
        InterpreterConfig.Interpreted(config, configMap);

        Map<String, WorkerProperties> workersMap = new HashMap<>();
        Map<String, String[]> workers_relationsMap = new HashMap<>();
        int err1 = InterpreterWorkers.Interpreted(configMap.get(InterpreterConfig.Params.WORKERS_FILE), workersMap);
        int err2 = InterpreterRelations.Interpreted(configMap.get(InterpreterConfig.Params.RELATIONS_FILE), workers_relationsMap);

        conveyorMap = new HashMap<>();
        int err3 = CreateConveyorMap(workersMap, workers_relationsMap);

        if (err1 == 0 && err2 == 0 && err3 == 0)
            isreadytorun = true;
        else
        {
            Log.report("Can not create manager");
        }
    }

    /**
     * create conveyor map
     * @param workersMap map with interpreted workers file
     * @param workers_relationsMap map with interpreted relations file
     * @throws IOException
     */
    private int CreateConveyorMap(Map<String, WorkerProperties> workersMap, Map<String, String> workers_relationsMap)
    {
            for (String key : workersMap.keySet()) {

                RLECoder Var = new RLECoder(workersMap.get(key).config_name);
                Executor Variable = Var;
                if (!Var.isgoodcoder)
                {
                    return -1;
                }
                String input_file, output_file;
                InterpreterWorkers.WorkersTypes type = workersMap.get(key).type_of_worker;
                if (type == InterpreterWorkers.WorkersTypes.WORKER_FIRST) {
                    input_file = configMap.get(InterpreterConfig.Params.INPUT_FILE);
                    try {
                        DataInputStream input = new DataInputStream(new FileInputStream(input_file));
                        Variable.SetInput(input);
                        first_worker = Variable;
                    } catch (IOException ex) {

                        Log.report("Can't open input_file");
                        return -1;
                    }
                } else if (type == InterpreterWorkers.WorkersTypes.WORKER_LAST) {
                    output_file = configMap.get(InterpreterConfig.Params.OUTPUT_FILE);
                    try {
                        DataOutputStream output = new DataOutputStream(new FileOutputStream(output_file));
                        Variable.SetOutput(output);
                    } catch (IOException ex) {

                        Log.report("Can't open output_file");
                        return -1;
                    }
                }
                conveyorMap.put(key, Variable);
            }
        for (String key : workers_relationsMap.keySet())
        {
            Executor Worker = conveyorMap.get(key);
            Executor Consumer = conveyorMap.get(workers_relationsMap.get(key));
            Worker.SetConsumer(Consumer);
        }
        return 0;
    }

    /**
     * start conveyor work
     */
    public void Run()
    {
        int err = 0;
        err = first_worker.Run();

    }
}
