

public class Main {
    public static void main(String[] args){
        if (args.length == 1)
        {
            Manager manager = new Manager(args[0]);
            if (manager.isreadytorun)
                manager.Run();
        }
        else
        {
            Log.report("Wrong file format");
        }
        Log.close();
    }
}
